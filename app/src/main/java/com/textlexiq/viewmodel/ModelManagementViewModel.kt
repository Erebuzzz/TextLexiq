package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.textlexiq.data.model.ModelManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModelManagementViewModel(private val modelManager: ModelManager) : ViewModel() {

    val models = modelManager.models

    fun downloadModel(id: String) {
        viewModelScope.launch {
            modelManager.downloadModel(id)
        }
    }

    fun deleteModel(id: String) {
        viewModelScope.launch {
            modelManager.deleteModel(id)
        }
    }
}
