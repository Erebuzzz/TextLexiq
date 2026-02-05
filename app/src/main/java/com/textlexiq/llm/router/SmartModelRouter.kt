package com.textlexiq.llm.router

import com.textlexiq.llm.EngineTier
import com.textlexiq.llm.LLMEngine
import com.textlexiq.llm.optimizer.TokenOptimizer
import com.textlexiq.llm.client.LlamaCppClient
import com.textlexiq.llm.client.CloudLLMClient

class SmartModelRouter(
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
        val estimatedTokens = TokenOptimizer.estimateTokens(prompt)
        
        // Strategy: "Floating Access"
        // 1. If task is explicitly marked as complex, use Cloud (Premium/Basic).
        // 2. If task is simple/short, prefer On-Device to save cost and latency.
        // 3. Threshold: 500 tokens (~2000 chars) is roughly the limit for efficient small on-device models on mobile.
        
        if (requiresHighIntelligence) {
            return cloudEngine
        }

        if (estimatedTokens < 500) {
            // Task is small enough for on-device (e.g. summarizing a receipt or short letter)
            // Provided the on-device engine is actually available/capable.
            // Assuming onDeviceEngine represents a verified working local model.
            return onDeviceEngine
        }
        
        // Default to cloud for larger contexts
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
