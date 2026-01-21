package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScannerUiState(
    val title: String = "Scanner",
    val cameraPermissionGranted: Boolean = false,
    val isCameraReady: Boolean = false,
    val isCapturing: Boolean = false,
    val capturedImagePath: String? = null,
    val errorMessage: String? = null
)

class ScannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                cameraPermissionGranted = granted,
                errorMessage = if (granted) null else "Camera permission is required to scan documents."
            )
        }
    }

    fun onCameraReady() {
        _uiState.update { it.copy(isCameraReady = true) }
    }

    fun onCaptureStarted() {
        _uiState.update { it.copy(isCapturing = true, errorMessage = null) }
    }

    fun onCaptureFailed(message: String) {
        _uiState.update {
            it.copy(
                isCapturing = false,
                errorMessage = message
            )
        }
    }

    fun onCaptureSuccess(filePath: String) {
        _uiState.update {
            it.copy(
                isCapturing = false,
                capturedImagePath = filePath,
                errorMessage = null
            )
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(capturedImagePath = null) }
    }
}
