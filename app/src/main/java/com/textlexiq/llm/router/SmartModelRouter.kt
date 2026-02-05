package com.textlexiq.llm.router

import com.textlexiq.llm.EngineTier
import com.textlexiq.llm.LLMEngine
import com.textlexiq.llm.optimizer.TokenOptimizer
import com.textlexiq.llm.client.LlamaCppClient
import com.textlexiq.llm.client.CloudLLMClient

class SmartModelRouter(
    private val modelManager: com.textlexiq.data.model.ModelManager,
    private val onDeviceEngine: LLMEngine = LlamaCppClient(),
    private val cloudEngine: LLMEngine = CloudLLMClient(apiKey = "mock_key")
) {

    /**
     * Routes the prompt to the appropriate engine.
     * 
     * @param prompt The user input text.
     * @param requiresHighIntelligence Set to true if the task is complex (e.g. creative writing, complex reasoning).
     * @return The selected LLMEngine.
     */
    fun route(prompt: String, requiresHighIntelligence: Boolean = false): LLMEngine {
        // If high intelligence is required, always go to cloud for now
        if (requiresHighIntelligence) {
            return cloudEngine
        }

        // Check if On-Device Model is available (Downloaded)
        val onDeviceModelAvailable = modelManager.models.value.any { 
            it.type == com.textlexiq.data.model.ModelType.LLM_ON_DEVICE && it.isDownloaded 
        }

        if (onDeviceModelAvailable) {
            // Check token count suitability
            val estimatedTokens = TokenOptimizer.estimateTokens(prompt)
            if (estimatedTokens < 800) { 
                // Increased threshold since Phi-3 is decent
                return onDeviceEngine
            }
        }
        
        // Default request unavailable or too complex/long for local
        return cloudEngine
    }
    
    /**
     * Routes and Generates response, applying optimization if sending to Cloud (Paid).
     */
    suspend fun generateWithRouting(prompt: String, requiresHighIntelligence: Boolean = false): String {
        val engine = route(prompt, requiresHighIntelligence)
        
        var finalPrompt = prompt
        
        // If we routed to a Paid/Cloud tier, optimize the tokens to save money!
        if (engine.tier != EngineTier.ON_DEVICE) {
            // Use aggressive optimization for simple tasks, standard for complex
            val aggressiveStart = !requiresHighIntelligence 
            finalPrompt = TokenOptimizer.optimize(prompt, aggressive = aggressiveStart)
        }
        
        return engine.generate(finalPrompt)
    }
}
