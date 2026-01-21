package com.textlexiq.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.CropBounds
import com.textlexiq.viewmodel.CropViewModel
import kotlin.ranges.ClosedFloatingPointRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAndPreviewScreen(
    imagePath: String,
    onBack: () -> Unit,
    onNavigateToOcr: (String) -> Unit,
    viewModel: CropViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val originalBitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }
    var horizontalCrop by remember { mutableStateOf(0.05f..0.95f) }
    var verticalCrop by remember { mutableStateOf(0.05f..0.95f) }

    LaunchedEffect(state.processedImagePath) {
        val processedPath = state.processedImagePath ?: return@LaunchedEffect
        onNavigateToOcr(processedPath)
        viewModel.consumeNavigation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Adjust Capture") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        horizontalCrop = 0.05f..0.95f
                        verticalCrop = 0.05f..0.95f
                    }) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset crop")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (originalBitmap == null) {
                Text(
                    text = "Unable to load captured image.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val imageAspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(imageAspectRatio)
                ) {
                    Image(
                        bitmap = originalBitmap.asImageBitmap(),
                        contentDescription = "Captured image",
                        modifier = Modifier.fillMaxSize()
                    )
                    CropBoundsOverlay(
                        modifier = Modifier.fillMaxSize(),
                        horizontalCrop = horizontalCrop,
                        verticalCrop = verticalCrop
                    )
                }

                Text(text = "Horizontal crop")
                RangeSlider(
                    value = horizontalCrop,
                    onValueChange = { range ->
                        horizontalCrop = range.start.coerceIn(0f, range.endInclusive) .. range.endInclusive.coerceIn(range.start, 1f)
                    },
                    valueRange = 0f..1f
                )

                Text(text = "Vertical crop")
                RangeSlider(
                    value = verticalCrop,
                    onValueChange = { range ->
                        verticalCrop = range.start.coerceIn(0f, range.endInclusive) .. range.endInclusive.coerceIn(range.start, 1f)
                    },
                    valueRange = 0f..1f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.processImage(
                            context = context,
                            sourcePath = imagePath,
                            cropBounds = CropBounds(
                                left = horizontalCrop.start,
                                top = verticalCrop.start,
                                right = horizontalCrop.endInclusive,
                                bottom = verticalCrop.endInclusive
                            )
                        )
                    },
                    enabled = !state.isProcessing
                ) {
                    Text(text = "Enhance for OCR")
                }

                if (state.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CropBoundsOverlay(
    modifier: Modifier = Modifier,
    horizontalCrop: ClosedFloatingPointRange<Float>,
    verticalCrop: ClosedFloatingPointRange<Float>
) {
    Canvas(modifier = modifier) {
        val left = size.width * horizontalCrop.start
        val right = size.width * horizontalCrop.endInclusive
        val top = size.height * verticalCrop.start
        val bottom = size.height * verticalCrop.endInclusive
        val width = right - left
        val height = bottom - top
         
        drawRect(
            color = Color.White.copy(alpha = 0.45f),
            topLeft = Offset(left, top),
            size = Size(width, height),
            style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f)))
        )
    }
}
