package com.textlexiq.llm.router

import com.textlexiq.llm.EngineTier
import com.textlexiq.llm.LLMEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SmartModelRouterTest {

    private lateinit var router: SmartModelRouter
    private lateinit var mockOnDevice: LLMEngine
    private lateinit var mockCloud: LLMEngine

    @Before
    fun setup() {
        mockOnDevice = object : LLMEngine {
            override val tier = EngineTier.ON_DEVICE
            override suspend fun generate(prompt: String) = "Local"
        }
        mockCloud = object : LLMEngine {
            override val tier = EngineTier.CLOUD_PREMIUM
            override suspend fun generate(prompt: String) = "Cloud"
        }
        
        router = SmartModelRouter(mockOnDevice, mockCloud)
    }

    @Test
    fun `routes short prompt to on-device`() {
        val shortPrompt = "Short text"
        val engine = router.route(shortPrompt, requiresHighIntelligence = false)
        assertEquals(EngineTier.ON_DEVICE, engine.tier)
    }

    @Test
    fun `routes long prompt to cloud`() {
        // Create > 500 tokens. 1 token ~= 4 chars -> 2000 chars
        val longPrompt = "A".repeat(2500)
        val engine = router.route(longPrompt, requiresHighIntelligence = false)
        assertEquals(EngineTier.CLOUD_PREMIUM, engine.tier)
    }
    
    @Test
    fun `routes complex prompt to cloud regardless of length`() {
        val shortComplex = "Write a haiku"
        val engine = router.route(shortComplex, requiresHighIntelligence = true)
        assertEquals(EngineTier.CLOUD_PREMIUM, engine.tier)
    }
}
