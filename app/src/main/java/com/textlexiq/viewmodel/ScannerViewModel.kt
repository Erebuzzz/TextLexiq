package com.textlexiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScannerUiState(
    val title: String = "Scanner",
    val cameraPermissionGranted: Boolean = false,
    val isCameraReady: Boolean = false,
    val isCapturing: Boolean = false,
    val capturedImagePath: String? = null,
    val errorMessage: String? = null,
    val detectedCorners: List<android.graphics.PointF> = emptyList(),
    val isAutoCaptureEnabled: Boolean = true
)

class ScannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    // Auto-capture logic variables
    private var stabilityCounter = 0
    private var lastCorners: List<android.graphics.PointF>? = null
    private val STABILITY_THRESHOLD = 0.02f // 2% movement allowed
    private val STABILITY_FRAMES_REQUIRED = 20 // Approx 1 second

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

    fun onEdgesDetected(corners: List<android.graphics.PointF>?) {
        // Update UI state for overlay
        _uiState.update { it.copy(detectedCorners = corners ?: emptyList()) }
        
        if (uiState.value.isAutoCaptureEnabled && !uiState.value.isCapturing && corners != null) {
            checkStability(corners)
        } else {
            stabilityCounter = 0
        }
    }
    
    private fun checkStability(current: List<android.graphics.PointF>) {
        val last = lastCorners
        if (last != null && last.size == current.size) {
            var isStable = true
            for (i in current.indices) {
                val dx = kotlin.math.abs(current[i].x - last[i].x)
                val dy = kotlin.math.abs(current[i].y - last[i].y)
                if (dx > STABILITY_THRESHOLD || dy > STABILITY_THRESHOLD) {
                    isStable = false
                    break
                }
            }
            
            if (isStable) {
                stabilityCounter++
                if (stabilityCounter >= STABILITY_FRAMES_REQUIRED) {
                    // Trigger Capture!
                    triggerAutoCapture()
                }
            } else {
                stabilityCounter = 0
            }
        } else {
            stabilityCounter = 0
        }
        lastCorners = current
    }
    
    val autoCaptureTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
    
    private fun triggerAutoCapture() {
        // Debounce
        if (uiState.value.isCapturing) return
        stabilityCounter = 0
        
        // Notify UI to take picture
        // Since we cannot take picture directly from VM, we emit an event
        // But for simplicity here, we can expose a flow or callback
        // This is a minimal implementation pattern:
        // Let's use a SharedFlow event or similar mechanism.
        // Actually, let's reset counter and let the UI observe a trigger state?
        // Better: suspend function or SharedFlow.
        // I'll add the flow property to the class.
        // (Note: need to launch inside scope)
        androidx.lifecycle.viewModelScope.launch {
            autoCaptureTrigger.emit(Unit)
        }
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
