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
import androidx.compose.material3.icons.filled.AutoAwesome
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.CropCorners
import com.textlexiq.viewmodel.CropViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAndPreviewScreen(
    imagePath: String,
    initialCornersString: String? = null,
    onBack: () -> Unit,
    onNavigateToOcr: (String) -> Unit,
    viewModel: CropViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val originalBitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }
    
    // Normalized coordinates 0..1
    // Parse helper
    val initialPoints = remember(initialCornersString) {
        if (!initialCornersString.isNullOrEmpty()) {
            try {
                val parts = initialCornersString.split(",").map { it.toFloat() }
                if (parts.size == 8) {
                    listOf(
                        androidx.compose.ui.geometry.Offset(parts[0], parts[1]),
                        androidx.compose.ui.geometry.Offset(parts[2], parts[3]),
                        androidx.compose.ui.geometry.Offset(parts[4], parts[5]),
                        androidx.compose.ui.geometry.Offset(parts[6], parts[7])
                    )
                } else null
            } catch (e: Exception) { null }
        } else null
    }

    var tl by remember { mutableStateOf(initialPoints?.get(0) ?: androidx.compose.ui.geometry.Offset(0.1f, 0.1f)) }
    var tr by remember { mutableStateOf(initialPoints?.get(1) ?: androidx.compose.ui.geometry.Offset(0.9f, 0.1f)) }
    var br by remember { mutableStateOf(initialPoints?.get(2) ?: androidx.compose.ui.geometry.Offset(0.9f, 0.9f)) }
    var bl by remember { mutableStateOf(initialPoints?.get(3) ?: androidx.compose.ui.geometry.Offset(0.1f, 0.9f)) }

    LaunchedEffect(state.processedImagePath) {
        val processedPath = state.processedImagePath ?: return@LaunchedEffect
        onNavigateToOcr(processedPath)
        viewModel.consumeNavigation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Adjust Corners") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tl = androidx.compose.ui.geometry.Offset(0.1f, 0.1f)
                        tr = androidx.compose.ui.geometry.Offset(0.9f, 0.1f)
                        br = androidx.compose.ui.geometry.Offset(0.9f, 0.9f)
                        bl = androidx.compose.ui.geometry.Offset(0.1f, 0.9f)
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
                        .weight(1f) // Let it fill space properly? aspect ratio makes it tricky.
                        // Actually, simpler to just use aspectRatio and let parent wrap.
                ) {
                    Image(
                        bitmap = originalBitmap.asImageBitmap(),
                        contentDescription = "Captured image",
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    DraggableCornerOverlay(
                        modifier = Modifier.fillMaxSize(),
                        tl = tl, tr = tr, br = br, bl = bl,
                        onCornerChange = { index, newPos ->
                            when(index) {
                                0 -> tl = newPos
                                1 -> tr = newPos
                                2 -> br = newPos
                                3 -> bl = newPos
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Drag corners to align with document edges.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.processImage(
                                context = context,
                                sourcePath = imagePath,
                                corners = com.textlexiq.viewmodel.CropCorners(
                                    tl = android.graphics.PointF(tl.x, tl.y),
                                    tr = android.graphics.PointF(tr.x, tr.y),
                                    br = android.graphics.PointF(br.x, br.y),
                                    bl = android.graphics.PointF(bl.x, bl.y)
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isProcessing
                    ) {
                        Text(text = "Correct Perspective")
                    }

                    // Neural Enhance Button (Visible only if logic permits, currently always shown but disabled if not downloaded could be better UX)
                    // Checking if 'real_esrgan' is downloaded requires observing ModelManager.
                    // For MVP simplicity, we add the UI and let ViewModel handle the "is model available" check or we just simulate it.
                    // Let's add the button.
                    val isEnhanceAvailable = true // In real app, observe modelManager.models flow
                    
                    if (isEnhanceAvailable) {
                         androidx.compose.material3.FilledTonalButton(
                             onClick = { 
                                 // Trigger enhancement 
                                 // Ideally calls viewModel.enhanceImage(...)
                                 // For now, toast
                                 android.widget.Toast.makeText(context, "Neural Enhancement (Super-Res) requires 30MB model download.", android.widget.Toast.LENGTH_SHORT).show()
                             },
                             enabled = !state.isProcessing
                         ) {
                             Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Enhance")
                         }
                    }
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
            }
        }
    }
}

@Composable
private fun DraggableCornerOverlay(
    modifier: Modifier = Modifier,
    tl: Offset, tr: Offset, br: Offset, bl: Offset,
    onCornerChange: (Int, Offset) -> Unit
) {
    var draggingIndex by remember { mutableStateOf(-1) }
    
    Canvas(
        modifier = modifier.androidx.compose.ui.input.pointer.pointerInput(Unit) {
            androidx.compose.foundation.gestures.detectDragGestures(
                onDragStart = { offset ->
                    val w = size.width
                    val h = size.height
                    val corners = listOf(
                        Offset(tl.x * w, tl.y * h),
                        Offset(tr.x * w, tr.y * h),
                        Offset(br.x * w, br.y * h),
                        Offset(bl.x * w, bl.y * h)
                    )
                    // Find closest corner within radius
                    val radius = 48.dp.toPx()
                    val closest = corners.withIndex().minByOrNull { (it.value - offset).getDistance() }
                    if (closest != null && (closest.value - offset).getDistance() < radius) {
                        draggingIndex = closest.index
                    }
                },
                onDrag = { change, dragAmount ->
                    if (draggingIndex != -1) {
                         change.consume()
                         val w = size.width
                         val h = size.height
                         
                         // Calculate new normalized position
                         var currentPos = when(draggingIndex) {
                             0 -> tl
                             1 -> tr
                             2 -> br
                             3 -> bl
                             else -> tl
                         }
                         
                         val currentPx = Offset(currentPos.x * w, currentPos.y * h)
                         val newPx = currentPx + dragAmount
                         
                         // Clamp to 0..1
                         val newNorm = Offset(
                             (newPx.x / w).coerceIn(0f, 1f),
                             (newPx.y / h).coerceIn(0f, 1f)
                         )
                         
                         onCornerChange(draggingIndex, newNorm)
                    }
                },
                onDragEnd = { draggingIndex = -1 },
                onDragCancel = { draggingIndex = -1 }
            )
        }
    ) {
        val w = size.width
        val h = size.height
        
        val cornerPoints = listOf(
            Offset(tl.x * w, tl.y * h),
            Offset(tr.x * w, tr.y * h),
            Offset(br.x * w, br.y * h),
            Offset(bl.x * w, bl.y * h)
        )
        
        // Draw Path (Polygon)
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(cornerPoints[0].x, cornerPoints[0].y)
        path.lineTo(cornerPoints[1].x, cornerPoints[1].y)
        path.lineTo(cornerPoints[2].x, cornerPoints[2].y)
        path.lineTo(cornerPoints[3].x, cornerPoints[3].y)
        path.close()
        
        drawPath(
            path = path,
            color = Color.Cyan.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        drawPath(
            path = path,
            color = Color.Cyan,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw Corners
        cornerPoints.forEachIndexed { index, point ->
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (index == draggingIndex) Color.Magenta else Color.Cyan,
                radius = 10.dp.toPx(),
                center = point
            )
        }
    }
}
