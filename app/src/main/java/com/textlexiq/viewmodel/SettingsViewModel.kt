package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val title: String = "Settings",
    val darkModeEnabled: Boolean = false,
    val onDeviceLlm: Boolean = true
)

sealed interface SettingsAction {
    data class SetDarkMode(val enabled: Boolean) : SettingsAction
    data class SetOnDeviceLlm(val enabled: Boolean) : SettingsAction
}

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetDarkMode -> _uiState.update { it.copy(darkModeEnabled = action.enabled) }
            is SettingsAction.SetOnDeviceLlm -> _uiState.update { it.copy(onDeviceLlm = action.enabled) }
        }
    }
}
