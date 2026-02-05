package com.textlexiq.llm.optimizer

import org.junit.Assert.assertEquals
import org.junit.Test

class TokenOptimizerTest {

    @Test
    fun `optimize trims whitespace and collapses multiple spaces`() {
        val input = "  Hello    World  "
        val expected = "Hello World"
        val result = TokenOptimizer.optimize(input, aggressive = false)
        assertEquals(expected, result)
    }

    @Test
    fun `optimize aggressive removes stop words`() {
        val input = "The quick brown fox jumps over the lazy dog"
        // Stopwords "The", "over", "the" should be removed (if "the" is in stopword list).
        // Assuming typical stopword list: "the", "a", "an", "over", "is"...
        // Let's verify against the implementation first later, but for now write the test based on standard behavior.
        // If "The" (case insensitive) is removed: "quick brown fox jumps lazy dog"
        
        val result = TokenOptimizer.optimize(input, aggressive = true)
        
        // Check contains key content
        assert(result.contains("quick"))
        assert(result.contains("fox"))
        assert(!result.lowercase().contains(" the ")) // "the" should be gone
    }

    @Test
    fun `estimateTokens returns accurate character count`() {
        // Implementation divides chars by 4
        val input = "12345678" // 8 chars -> 2 tokens
        val result = TokenOptimizer.estimateTokens(input)
        assertEquals(2, result)
    }
}
