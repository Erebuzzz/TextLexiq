package com.textlexiq.ui.screens

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textlexiq.viewmodel.OcrAction
import com.textlexiq.viewmodel.OcrViewModel
import com.textlexiq.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    onDocumentSaved: (String) -> Unit,
    cleanedImagePath: String?,
    viewModel: OcrViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cleanedImagePath) {
        cleanedImagePath?.let { viewModel.onImagePathAvailable(it) }
    }

    LaunchedEffect(state.savedDocumentId) {
        val documentId = state.savedDocumentId ?: return@LaunchedEffect
        onDocumentSaved(documentId)
        viewModel.onNavigationHandled()
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
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
                }
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { padding ->
        com.textlexiq.ui.components.LoadingOverlay(
            isVisible = state.isProcessing || state.isSaving,
            message = if (state.isSaving) "Saving Document..." else "Extracting Text..."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Processing indicator removed from here

            Text(
                text = "Confidence: ${(state.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Confidence Overlay
            if (cleanedImagePath != null) {
                val bitmap = remember(cleanedImagePath) { androidx.compose.ui.graphics.asImageBitmap(android.graphics.BitmapFactory.decodeFile(cleanedImagePath)) }
                
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Give weight to image
                        .androidx.compose.foundation.background(androidx.compose.ui.graphics.Color.Black)
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = "Scanned Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    
                    if (state.ocrBlocks.isNotEmpty()) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val scaleX = size.width / bitmap.width
                            val scaleY = size.height / bitmap.height
                            
                            // To handle 'Fit' scale properly, we need to calculate the actual displayed image rect
                            val imageRatio = bitmap.width.toFloat() / bitmap.height
                            val canvasRatio = size.width / size.height
                            
                            var drawScale = 1f
                            var offsetX = 0f
                            var offsetY = 0f
                            
                            if (imageRatio > canvasRatio) {
                                // Fit width
                                drawScale = size.width / bitmap.width
                                val drawnHeight = bitmap.height * drawScale
                                offsetY = (size.height - drawnHeight) / 2
                            } else {
                                // Fit height
                                drawScale = size.height / bitmap.height
                                val drawnWidth = bitmap.width * drawScale
                                offsetX = (size.width - drawnWidth) / 2
                            }

                            state.ocrBlocks.forEach { block ->
                                block.boundingBox?.let { box ->
                                    val left = box.left * drawScale + offsetX
                                    val top = box.top * drawScale + offsetY
                                    val width = box.width() * drawScale
                                    val height = box.height() * drawScale
                                    
                                    val color = when {
                                        block.confidence > 0.8f -> androidx.compose.ui.graphics.Color.Green
                                        block.confidence > 0.5f -> androidx.compose.ui.graphics.Color.Yellow
                                        else -> androidx.compose.ui.graphics.Color.Red
                                    }
                                    
                                    drawRect(
                                        color = color.copy(alpha = 0.3f),
                                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                        size = androidx.compose.ui.geometry.Size(width, height)
                                    )
                                    drawRect(
                                        color = color,
                                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                        size = androidx.compose.ui.geometry.Size(width, height),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.editedText,
                onValueChange = { viewModel.onAction(OcrAction.UpdateText(it)) },
                modifier = Modifier.fillMaxWidth().weight(1f), // Share space
                label = { Text(text = "Extracted Text") },
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onAction(OcrAction.SaveDocument) },
                enabled = !state.isProcessing && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save Document")
            }

            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.onAction(OcrAction.RetryScan) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Retry")
            }
        }
    }
}
