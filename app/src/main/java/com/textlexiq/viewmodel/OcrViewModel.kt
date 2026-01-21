data class OcrUiState(
package com.textlexiq.viewmodel

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.textlexiq.data.TextLexiqRepository
import com.textlexiq.ocr.OCRResult
import com.textlexiq.ocr.TextExtractor
import com.textlexiq.utils.DefaultDispatcherProvider
import com.textlexiq.utils.DispatcherProvider
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OcrUiState(
    val title: String = "OCR",
    val extractedText: String = "",
    val editedText: String = "",
    val confidence: Float = 0f,
    val isProcessing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedDocumentId: String? = null
)

sealed interface OcrAction {
    object RetryScan : OcrAction
    object SaveDocument : OcrAction
    data class UpdateText(val text: String) : OcrAction
}

class OcrViewModel(
    private val repository: TextLexiqRepository = TextLexiqRepository.preview(),
    private val extractor: TextExtractor = TextExtractor.preview(),
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState(extractedText = "Recognized text will appear here."))
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()
    private var lastImagePath: String? = null

    fun onImagePathAvailable(path: String) {
        if (!File(path).exists()) {
            _uiState.update { it.copy(errorMessage = "Processed image unavailable.") }
            return
        }
        lastImagePath = path
        runOcr(path)
    }

    fun onAction(action: OcrAction) {
        when (action) {
            OcrAction.RetryScan -> retryOcr()
            is OcrAction.UpdateText -> _uiState.update { it.copy(editedText = action.text) }
            OcrAction.SaveDocument -> saveDocument()
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(savedDocumentId = null) }
    }

    private fun retryOcr() {
        val path = lastImagePath
        if (path != null) {
            runOcr(path)
        } else {
            _uiState.update { it.copy(errorMessage = "No image available to retry.") }
        }
    }

    private fun runOcr(path: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            runCatching {
                val bitmap = BitmapFactory.decodeFile(path)
                    ?: error("Unable to decode processed image.")
                val result = extractor.extract(bitmap)
                bitmap.recycle()
                result
            }.onSuccess { result ->
                handleOcrSuccess(result)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = throwable.message ?: "Failed to run OCR."
                    )
                }
            }
        }
    }

    private fun handleOcrSuccess(result: OCRResult) {
        val sanitizedText = result.text.ifBlank { "No text detected." }
        _uiState.update {
            it.copy(
                isProcessing = false,
                extractedText = sanitizedText,
                editedText = sanitizedText,
                confidence = result.confidence,
                errorMessage = null
            )
        }
    }

    private fun saveDocument() {
        val textToSave = uiState.value.editedText
        if (textToSave.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Cannot save an empty document.") }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                repository.saveDocument(textToSave, uiState.value.confidence)
            }.onSuccess { id ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedDocumentId = id.toString()
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Failed to save document."
                    )
                }
            }
        }
    }
}
