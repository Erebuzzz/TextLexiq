package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import com.textlexiq.exporter.DocumentExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DocumentViewUiState(
    val title: String = "Document",
    val content: String = "Your processed document will appear here.",
    val annotationCount: Int = 0
)

sealed interface DocumentAction {
    object Export : DocumentAction
    object OpenExporter : DocumentAction
}

class DocumentViewModel(
    private val exporter: DocumentExporter = DocumentExporter.default()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentViewUiState())
    val uiState: StateFlow<DocumentViewUiState> = _uiState.asStateFlow()

    fun onAction(action: DocumentAction) {
        when (action) {
            DocumentAction.Export -> exporter.queueExport()
            DocumentAction.OpenExporter -> exporter.openExportPicker()
        }
    }
}
