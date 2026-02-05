package com.textlexiq.data.model

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

data class AiModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeMb: Int,
    val type: ModelType,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f, // 0.0 to 1.0
    val isDownloading: Boolean = false
)

enum class ModelType {
    LAYOUT_ANALYSIS,
    LLM_ON_DEVICE,
    IMAGE_ENHANCEMENT
}

class ModelManager(private val context: Context) {

    private val modelsDir = File(context.filesDir, "ai_models")

    private val _models = MutableStateFlow<List<AiModel>>(emptyList())
    val models: StateFlow<List<AiModel>> = _models.asStateFlow()

    init {
        if (!modelsDir.exists()) modelsDir.mkdirs()
        refreshModels()
    }

    private fun refreshModels() {
        // Define available models
        val availableModels = listOf(
            AiModel(
                id = "layout_yolo",
                name = "Layout Analysis (YOLO)",
                description = "Detects tables, headers, and images.",
                sizeMb = 45,
                type = ModelType.LAYOUT_ANALYSIS
            ),
            AiModel(
                id = "phi_3_mini_ocr",
                name = "Phi-3 Mini (LLM)",
                description = "High-quality on-device summarization & chat.",
                sizeMb = 1800, // 1.8GB
                type = ModelType.LLM_ON_DEVICE
            ),
            AiModel(
                id = "real_esrgan",
                name = "Neural Enhancer",
                description = "Upscales low-quality scans.",
                sizeMb = 30,
                type = ModelType.IMAGE_ENHANCEMENT
            )
        )

        // Check download status
        val updatedModels = availableModels.map { model ->
            val file = File(modelsDir, model.id + ".bin") // generic extension for now
            model.copy(
                isDownloaded = file.exists() && file.length() > 0
            )
        }
        _models.value = updatedModels
    }

    suspend fun downloadModel(modelId: String) {
        // Find model
        val currentList = _models.value
        val index = currentList.indexOfFirst { it.id == modelId }
        if (index == -1) return

        // Update state to downloading
        updateModel(index) { it.copy(isDownloading = true, downloadProgress = 0f) }

        // Simulate download
        try {
            val model = currentList[index]
            val targetFile = File(modelsDir, model.id + ".bin")
            
            // Mock download loop
            for (i in 1..10) {
                kotlinx.coroutines.delay(300)
                updateModel(index) { it.copy(downloadProgress = i / 10f) }
            }

            // Create "dummy" file to mark as downloaded
            targetFile.createNewFile()
            
            updateModel(index) { it.copy(isDownloading = false, isDownloaded = true, downloadProgress = 0f) }
        } catch (e: Exception) {
            updateModel(index) { it.copy(isDownloading = false, downloadProgress = 0f) }
        }
    }
    
    suspend fun deleteModel(modelId: String) {
        val currentList = _models.value
        val index = currentList.indexOfFirst { it.id == modelId }
        if (index == -1) return
        
        val targetFile = File(modelsDir, modelId + ".bin")
        if (targetFile.exists()) targetFile.delete()
        
        updateModel(index) { it.copy(isDownloaded = false) }
    }

    private fun updateModel(index: Int, update: (AiModel) -> AiModel) {
        _models.update { list ->
            val mutable = list.toMutableList()
            mutable[index] = update(mutable[index])
            mutable
        }
    }
    
    fun getModelPath(modelId: String): String? {
        val file = File(modelsDir, "$modelId.bin")
        return if (file.exists()) file.absolutePath else null
    }
}
