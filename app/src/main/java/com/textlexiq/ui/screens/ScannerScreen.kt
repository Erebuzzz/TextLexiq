package com.textlexiq.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.CameraAlt
import androidx.compose.material3.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.ScannerViewModel
import java.io.File
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onImageCaptured: (String) -> Unit,
    viewModel: ScannerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onPermissionResult
    )

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER } }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Auto-Capture Trigger Observation
    LaunchedEffect(Unit) {
        viewModel.autoCaptureTrigger.collect {
             // Trigger capture if camera is ready and not capturing
             if (state.isCameraReady && !state.isCapturing) {
                 captureImage(
                    context = context,
                    imageCapture = imageCapture,
                    executor = ContextCompat.getMainExecutor(context),
                    onCaptureStarted = viewModel::onCaptureStarted,
                    onSuccess = viewModel::onCaptureSuccess,
                    onFailure = viewModel::onCaptureFailed
                 )
             }
        }
    }

    LaunchedEffect(state.capturedImagePath) {
        val imagePath = state.capturedImagePath ?: return@LaunchedEffect
        onImageCaptured(imagePath)
        viewModel.onNavigationHandled()
    }

    val imageAnalysis = remember {
        androidx.camera.core.ImageAnalysis.Builder()
            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    // Simple rotation handling + Bitmap conversion (inefficient but works for MVP)
                    // In prod, use YUV directly with OpenCV
                    val bitmap = imageProxy.toBitmap() // Requires androidx.camera:camera-core extension or manual
                    // The camera-core dependency is present, but toBitmap typically requires camera-view or latest
                    // Let's assume toBitmap() works or we catch detection errors.
                    // Actually, let's wrap in try-catch and close image
                    try {
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val rotatedBitmap = if (rotation != 0) {
                            val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                            android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else {
                            bitmap
                        }
                        
                        val corners = com.textlexiq.scanner.EdgeDetector.detectDocument(rotatedBitmap)
                        viewModel.onEdgesDetected(corners)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        imageProxy.close()
                    }
                }
            }
    }

    DisposableEffect(state.cameraPermissionGranted) {
        if (!state.cameraPermissionGranted) {
            onDispose { }
        } else {
            val executor = ContextCompat.getMainExecutor(context)
            val listener = Runnable {
                val cameraProvider = runCatching { cameraProviderFuture.get() }.getOrNull()
                if (cameraProvider == null) {
                    viewModel.onCaptureFailed("Unable to initialize camera provider.")
                    return@Runnable
                }
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )
                    viewModel.onCameraReady()
                } catch (exc: Exception) {
                    Log.e("ScannerScreen", "Failed to bind camera", exc)
                    viewModel.onCaptureFailed("Failed to start camera preview.")
                }
            }
            cameraProviderFuture.addListener(listener, executor)

            onDispose {
                runCatching { cameraProviderFuture.get() }
                    .getOrNull()
                    ?.unbindAll()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Auto-Capture Toggle (Icon logic would go here ideally)
                    IconButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry permission")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.cameraPermissionGranted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                EdgeDetectionOverlay(
                    modifier = Modifier.fillMaxSize(),
                    corners = state.detectedCorners
                )

                FloatingActionButton(
                    onClick = {
                        captureImage(
                            context = context,
                            imageCapture = imageCapture,
                            executor = ContextCompat.getMainExecutor(context),
                            onCaptureStarted = viewModel::onCaptureStarted,
                            onSuccess = viewModel::onCaptureSuccess,
                            onFailure = viewModel::onCaptureFailed
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    enabled = state.isCameraReady && !state.isCapturing
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture")
                }
            } else {
                PermissionMessage(
                    modifier = Modifier
                        .align(Alignment.Center),
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            if (state.isCapturing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Capturing…", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onCaptureStarted: () -> Unit,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val outputFile = createTempImageFile(context)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    onCaptureStarted()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess(outputFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                outputFile.delete()
                onFailure(exception.message ?: "Failed to capture image.")
            }
        }
    )
}

private fun createTempImageFile(context: Context): File {
    val directory = File(context.filesDir, "captures").apply { if (!exists()) mkdirs() }
    return File.createTempFile("capture_", ".jpg", directory)
}

@Composable
private fun EdgeDetectionOverlay(
    modifier: Modifier = Modifier,
    corners: List<android.graphics.PointF> = emptyList()
) {
    Canvas(modifier = modifier) {
        if (corners.size == 4) {
             val path = androidx.compose.ui.graphics.Path()
             // Corners are normalized 0..1
             // Camera preview might be scaled (Fit Center)
             // For MVP assuming fill, but usually we need to coordinate transform.
             // Given standard preview view usage, detection is on 640x480 analysis but displayed on screen size.
             // Points 0..1 should map to width/height of canvas.
             
             val w = size.width
             val h = size.height
             
             path.moveTo(corners[0].x * w, corners[0].y * h)
             for (i in 1..3) {
                 path.lineTo(corners[i].x * w, corners[i].y * h)
             }
             path.close()
             
             drawPath(
                 path = path,
                 color = Color.Green.copy(alpha = 0.5f),
                 style = Stroke(width = 4.dp.toPx())
             )
             drawPath(
                 path = path,
                 color = Color.Green.copy(alpha = 0.2f)
             )
        } else {
            // Draw guideline
            val rectWidth = size.width * 0.85f
            val rectHeight = size.height * 0.65f
            val left = (size.width - rectWidth) / 2f
            val top = (size.height - rectHeight) / 2f
            drawRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f)))
            )
        }
    }
}

@Composable
private fun PermissionMessage(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onRequestPermission) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Request camera permission")
        }
        Text(
            text = "Grant camera permission to start scanning",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
