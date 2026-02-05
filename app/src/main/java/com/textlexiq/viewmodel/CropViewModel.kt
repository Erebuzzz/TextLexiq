package com.textlexiq.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.textlexiq.ocr.ImagePreprocessor
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Retaining CropBounds for simpler layouts if needed, but adding Corners support
data class CropCorners(
    val tl: android.graphics.PointF,
    val tr: android.graphics.PointF,
    val br: android.graphics.PointF,
    val bl: android.graphics.PointF
)

data class CropUiState(
    val isProcessing: Boolean = false,
    val processedImagePath: String? = null,
    val errorMessage: String? = null
)

class CropViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CropUiState())
    val uiState: StateFlow<CropUiState> = _uiState.asStateFlow()

    fun processImage(
        context: Context,
        sourcePath: String,
        corners: CropCorners
    ) {
        viewModelScope.launch(Dispatchers.Default) {
             _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            runCatching {
                val original = BitmapFactory.decodeFile(sourcePath)
                    ?: error("Unable to decode captured image.")
                
                // Use OpenCV Perspective Transform
                val cornerList = listOf(corners.tl, corners.tr, corners.br, corners.bl)
                val cropped = com.textlexiq.scanner.ImageTransformer.correctPerspective(original, cornerList)
                
                val processed = ImagePreprocessor.finalizeForOcr(cropped)

                val outputFile = createProcessedFile(context)
                FileOutputStream(outputFile).use { stream ->
                    processed.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                outputFile.absolutePath
            }.onSuccess { processedPath ->
                _uiState.update { it.copy(isProcessing = false, processedImagePath = processedPath) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = throwable.message ?: "Failed to prepare image."
                    )
                }
            }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(processedImagePath = null) }
    }

    private fun createProcessedFile(context: Context): File {
        val directory = File(context.filesDir, "processed").apply { if (!exists()) mkdirs() }
        return File.createTempFile("processed_", ".jpg", directory)
    }
}
