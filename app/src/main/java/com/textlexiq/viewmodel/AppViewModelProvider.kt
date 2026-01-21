package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.textlexiq.TextLexiqApp
import com.textlexiq.exporter.DocumentExporter

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            HomeViewModel(application.container.repository)
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            OcrViewModel(
                repository = application.container.repository
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            DocumentViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = application.container.repository,
                exporter = DocumentExporter.default()
            )
        }
    }
}
