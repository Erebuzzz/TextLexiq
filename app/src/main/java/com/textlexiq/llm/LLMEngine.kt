package com.textlexiq.llm

enum class EngineTier {
    ON_DEVICE, // Free, offline, lower intelligence
    CLOUD_BASIC, // Cheap/Fast (e.g. Gemini Flash)
    CLOUD_PREMIUM // Expensive/Smart (e.g. Gemini Pro / GPT-4)
}

interface LLMEngine {
    val tier: EngineTier
    suspend fun generate(prompt: String): String
}
