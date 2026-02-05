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
            HomeViewModel(application.container.textLexiqRepository)
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            OcrViewModel(
                repository = application.container.textLexiqRepository
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            DocumentViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = application.container.textLexiqRepository,
                exporter = DocumentExporter.default(),
                smartModelRouter = application.container.smartModelRouter,
                application = application
            )
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            SettingsViewModel(application.container.userPreferencesRepository)
        }
        initializer {
            val application = (this[APPLICATION_KEY] as TextLexiqApp)
            ModelManagementViewModel(application.container.modelManager)
        }
    }
}
