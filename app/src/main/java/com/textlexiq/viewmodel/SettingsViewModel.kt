package com.textlexiq.viewmodel

import com.textlexiq.llm.EngineTier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class SettingsUiState(
    val title: String = "Settings",
    val darkModeEnabled: Boolean = true,
    val ocrEngine: String = "mlkit",
    val llmTier: EngineTier = EngineTier.ON_DEVICE
)

sealed interface SettingsAction {
    data class SetDarkMode(val enabled: Boolean) : SettingsAction
    data class SetOcrEngine(val engine: String) : SettingsAction
    data class SetLlmTier(val tier: EngineTier) : SettingsAction
}

class SettingsViewModel(
    private val userPreferencesRepository: com.textlexiq.data.UserPreferencesRepository
) : ViewModel() {

    // Combine flows for UI State
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.darkModeEnabled,
        userPreferencesRepository.ocrEngine,
        userPreferencesRepository.llmTier
    ) { darkMode, ocr, llm ->
        SettingsUiState(
            darkModeEnabled = darkMode,
            ocrEngine = ocr,
            llmTier = llm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetDarkMode -> userPreferencesRepository.setDarkMode(action.enabled)
            is SettingsAction.SetOcrEngine -> userPreferencesRepository.setOcrEngine(action.engine)
            is SettingsAction.SetLlmTier -> userPreferencesRepository.setLlmTier(action.tier)
        }
    }
}
