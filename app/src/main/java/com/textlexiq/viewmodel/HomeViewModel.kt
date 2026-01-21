package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.textlexiq.data.TextLexiqRepository
import com.textlexiq.data.model.DocumentSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentDocuments: List<DocumentSummary> = emptyList(),
    val emptyMessage: String = "Scan a document to get started."
)

sealed interface HomeAction {
    object StartScan : HomeAction
    data class OpenDocument(val id: String) : HomeAction
}

class HomeViewModel(
    private val repository: TextLexiqRepository = TextLexiqRepository.preview()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeRecentDocuments().collect { documents ->
                _uiState.update { current -> current.copy(recentDocuments = documents) }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.StartScan -> Unit
            is HomeAction.OpenDocument -> Unit
        }
    }
}
