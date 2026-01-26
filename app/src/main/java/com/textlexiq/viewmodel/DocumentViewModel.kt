package com.textlexiq.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.textlexiq.data.TextLexiqRepository
import com.textlexiq.data.local.DocumentEntity
import com.textlexiq.exporter.DocumentExporter
import com.textlexiq.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentViewUiState(
    val isLoading: Boolean = true,
    val documentId: Long? = null,
    val title: String = "Document",
    val content: String = "",
    val isEditing: Boolean = false,
    val editedContent: String = "",
    val editedTitle: String = "",
    val confidence: Float = 0f,
    val ocrEngine: String = "mlkit",
    val language: String = "en",
    val tags: List<String> = emptyList(),
    val latexContent: String? = null,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val error: String? = null
)

enum class ExportFormat {
    PDF, DOCX, LATEX
}

sealed interface DocumentAction {
    data class Export(val format: ExportFormat) : DocumentAction
    object OpenExporter : DocumentAction
    object ToggleEdit : DocumentAction
    object SaveChanges : DocumentAction
    object DiscardChanges : DocumentAction
    object Delete : DocumentAction
    data class UpdateContent(val content: String) : DocumentAction
    data class UpdateTitle(val title: String) : DocumentAction
    data class UpdateTags(val tags: List<String>) : DocumentAction
}

class DocumentViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TextLexiqRepository,
    private val exporter: DocumentExporter = DocumentExporter.default(),
    private val application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {

    private val documentId: Long = savedStateHandle.get<Long>(Screen.Document.documentIdArg) ?: -1L

    private val _uiState = MutableStateFlow(DocumentViewUiState())
    val uiState: StateFlow<DocumentViewUiState> = _uiState.asStateFlow()

    private var currentDocument: DocumentEntity? = null

    init {
        if (documentId >= 0) {
            loadDocument(documentId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadDocument(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val document = repository.getDocumentById(id)
                if (document != null) {
                    currentDocument = document
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            documentId = document.id,
                            title = document.title,
                            content = document.content,
                            editedContent = document.content,
                            editedTitle = document.title,
                            confidence = document.confidence,
                            ocrEngine = document.ocrEngine,
                            language = document.language,
                            tags = document.tags.split(",").filter { tag -> tag.isNotBlank() },
                            latexContent = document.latexContent
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Document not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load document")
                }
            }
        }
    }

    fun onAction(action: DocumentAction) {
        when (action) {
            is DocumentAction.Export -> exportDocument(action.format)
            DocumentAction.OpenExporter -> exporter.openExportPicker() // Can be removed if unused
            DocumentAction.ToggleEdit -> toggleEditMode()
            DocumentAction.SaveChanges -> saveChanges()
            DocumentAction.DiscardChanges -> discardChanges()
            DocumentAction.Delete -> deleteDocument()
            is DocumentAction.UpdateContent -> updateContent(action.content)
            is DocumentAction.UpdateTitle -> updateTitle(action.title)
            is DocumentAction.UpdateTags -> updateTags(action.tags)
        }
    }

    private fun exportDocument(format: ExportFormat) {
        val doc = currentDocument ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                // Use external cache dir for sharing
                val cacheDir = application.externalCacheDir ?: application.cacheDir
                val timestamp = System.currentTimeMillis()
                val fileName = "${doc.title.replace("\\s+".toRegex(), "_")}_$timestamp"
                
                val result = when (format) {
                    ExportFormat.PDF -> exporter.exportToPdf(
                        doc.content, 
                        doc.title, 
                        java.io.File(cacheDir, "$fileName.pdf")
                    )
                    ExportFormat.DOCX -> exporter.exportToWord(
                        doc.content, 
                        doc.title, 
                        java.io.File(cacheDir, "$fileName.docx")
                    )
                    ExportFormat.LATEX -> exporter.exportToLatex(
                        doc.content, 
                        doc.title, 
                        doc.latexContent, 
                        java.io.File(cacheDir, "$fileName.tex")
                    )
                }

                if (result.isSuccess) {
                    val file = result.getOrThrow()
                    // TODO: Trigger share intent
                    _uiState.update { 
                        it.copy(
                            isExporting = false, 
                            exportMessage = "Exported to ${file.name}. (Sharing implementation pending)" 
                        ) 
                    }
                } else {
                     _uiState.update { 
                        it.copy(
                            isExporting = false, 
                            error = "Export failed: ${result.exceptionOrNull()?.message}" 
                        ) 
                    }
                }
            } catch (e: Exception) {
                 _uiState.update { 
                    it.copy(
                        isExporting = false, 
                        error = "Export failed: ${e.message}" 
                    ) 
                }
            }
        }
    }

    private fun toggleEditMode() {
        _uiState.update { state ->
            if (state.isEditing) {
                state.copy(
                    isEditing = false,
                    editedContent = state.content,
                    editedTitle = state.title,
                    hasChanges = false
                )
            } else {
                state.copy(
                    isEditing = true,
                    editedContent = state.content,
                    editedTitle = state.title
                )
            }
        }
    }

    private fun updateContent(content: String) {
        _uiState.update { state ->
            state.copy(
                editedContent = content,
                hasChanges = content != state.content || state.editedTitle != state.title
            )
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { state ->
            state.copy(
                editedTitle = title,
                hasChanges = title != state.title || state.editedContent != state.content
            )
        }
    }

    private fun updateTags(tags: List<String>) {
        viewModelScope.launch {
            val docId = _uiState.value.documentId ?: return@launch
            repository.updateDocumentTags(docId, tags)
            _uiState.update { it.copy(tags = tags) }
        }
    }

    private fun saveChanges() {
        val state = _uiState.value
        val docId = state.documentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val success = repository.updateDocumentContent(
                    id = docId,
                    newContent = state.editedContent,
                    newTitle = state.editedTitle.takeIf { it != state.title }
                )
                if (success) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            content = state.editedContent,
                            title = state.editedTitle,
                            hasChanges = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isSaving = false, error = "Failed to save changes")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "Failed to save")
                }
            }
        }
    }

    private fun discardChanges() {
        _uiState.update { state ->
            state.copy(
                isEditing = false,
                editedContent = state.content,
                editedTitle = state.title,
                hasChanges = false
            )
        }
    }

    private fun deleteDocument() {
        val docId = _uiState.value.documentId ?: return
        viewModelScope.launch {
            try {
                repository.deleteDocument(docId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete document")
                }
            }
        }
    }
}
