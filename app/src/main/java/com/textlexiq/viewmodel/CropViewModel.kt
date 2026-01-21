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

data class CropBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
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
        cropBounds: CropBounds
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            runCatching {
                val original = BitmapFactory.decodeFile(sourcePath)
                    ?: error("Unable to decode captured image.")
                val cropped = cropBitmap(original, cropBounds)
                val processed = ImagePreprocessor.preprocess(cropped)

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

    private fun cropBitmap(source: Bitmap, bounds: CropBounds): Bitmap {
        val leftPx = (bounds.left * source.width).roundToInt().coerceIn(0, source.width - 2)
        val rightPx = (bounds.right * source.width).roundToInt().coerceIn(leftPx + 1, source.width - 1)
        val topPx = (bounds.top * source.height).roundToInt().coerceIn(0, source.height - 2)
        val bottomPx = (bounds.bottom * source.height).roundToInt().coerceIn(topPx + 1, source.height - 1)

        val width = (rightPx - leftPx).coerceAtLeast(1)
        val height = (bottomPx - topPx).coerceAtLeast(1)
        return Bitmap.createBitmap(source, leftPx, topPx, width, height)
    }
}
