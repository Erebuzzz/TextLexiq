package com.textlexiq.llm.optimizer

object TokenOptimizer {

    // Common English stopwords (subset) to remove in aggressive mode
    private val STOP_WORDS = setOf(
        "a", "an", "the", "and", "or", "but", "if", "then", "else", "when", 
        "at", "by", "for", "from", "in", "into", "of", "off", "on", "onto", 
        "out", "over", "to", "up", "with", "is", "am", "are", "was", "were", 
        "be", "been", "being", "have", "has", "had", "do", "does", "did",
        "that", "this", "these", "those", "it", "he", "she", "they", "we", "i", "you"
    )

    /**
     * Optimizes the input text to reduce token count.
     * @param text The input prompt text.
     * @param aggressive If true, removes stopwords. Use with caution for creative tasks.
     * @return The optimized string.
     */
    fun optimize(text: String, aggressive: Boolean = false): String {
        // 1. Normalize whitespace: Replace multiple spaces/tabs/newlines with single space
        // We preserve single newlines for paragraph structure if strictly needed, 
        // but for pure token optimization, flattening is often better. 
        // Let's replace runs of whitespace with a single space.
        var optimized = text.trim().replace(Regex("\\s+"), " ")

        // 2. Aggressive Optimization: Remove Stopwords
        if (aggressive) {
            val words = optimized.split(" ")
            optimized = words.filter { word ->
                !STOP_WORDS.contains(word.lowercase())
            }.joinToString(" ")
        }

        return optimized
    }
    
    /**
     * Estimates token count (approximated as chars / 4 for English).
     */
    fun estimateTokens(text: String): Int {
        return text.length / 4
    }
}
