package com.textlexiq.data

import android.content.Context
import android.content.SharedPreferences
import com.textlexiq.llm.EngineTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory cache for simple reactive state
    private val _darkModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, true)) // Default to Dark!
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _ocrEngine = MutableStateFlow(prefs.getString(KEY_OCR_ENGINE, "mlkit") ?: "mlkit")
    val ocrEngine: StateFlow<String> = _ocrEngine.asStateFlow()

    private val _llmTier = MutableStateFlow(
        EngineTier.valueOf(prefs.getString(KEY_LLM_TIER, EngineTier.ON_DEVICE.name) ?: EngineTier.ON_DEVICE.name)
    )
    val llmTier: StateFlow<EngineTier> = _llmTier.asStateFlow()

    private val _isFirstRun = MutableStateFlow(prefs.getBoolean(KEY_FIRST_RUN, true))
    val isFirstRun: StateFlow<Boolean> = _isFirstRun.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _darkModeEnabled.update { enabled }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
        _isFirstRun.update { false }
    }

    fun setOcrEngine(engine: String) {
        prefs.edit().putString(KEY_OCR_ENGINE, engine).apply()
        _ocrEngine.update { engine }
    }

    fun setLlmTier(tier: EngineTier) {
        prefs.edit().putString(KEY_LLM_TIER, tier.name).apply()
        _llmTier.update { tier }
    }

    companion object {
        private const val PREFS_NAME = "textlexiq_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_OCR_ENGINE = "ocr_engine"
        private const val KEY_LLM_TIER = "llm_tier"
        private const val KEY_FIRST_RUN = "is_first_run"
    }
}
