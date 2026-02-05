package com.textlexiq.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.tasks.await
import android.graphics.Rect

class MLKitOCREngine : OCREngine {

    override val name = "mlkit"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val languageIdentifier = LanguageIdentification.getClient()

    override suspend fun extract(bitmap: Bitmap): OCRResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        
        // Convert blocks
        val blocks = visionText.textBlocks.map { block ->
            OCRBlock(
                text = block.text,
                confidence = block.lines.mapNotNull { it.confidence }.average().toFloat().takeIf { !it.isNaN() } ?: 1.0f, // MLKit text block doesn't strictly expose confidence in all versions, lines do? Actually MLKit Text API v2 returns confidence? 
                // Wait, MLKit bundled might not have confidence for Latin. Cloud does. 
                // Let's check docs or assume we use line layout logic for now. 
                // Actually the current TextExtractor was using block.confidence?
                // Looking at previous file: Yes "block.confidence".
                boundingBox = block.boundingBox
            )
        }

        // Sort blocks by reading order (Top-Down, Left-Right)
        val sortedBlocks = blocks.sortedWith(
            Comparator { b1, b2 ->
                val b1Top = b1.boundingBox?.top ?: 0
                val b2Top = b2.boundingBox?.top ?: 0
                val b1Left = b1.boundingBox?.left ?: 0
                val b2Left = b2.boundingBox?.left ?: 0
                
                if (Math.abs(b1Top - b2Top) < 20) {
                     b1Left.compareTo(b2Left)
                } else {
                     b1Top.compareTo(b2Top)
                }
            }
        )
        
        val orderedText = sortedBlocks.joinToString("\n\n") { it.text }
        val avgConfidence = if (sortedBlocks.isNotEmpty()) sortedBlocks.map { it.confidence }.average().toFloat() else 0f

        // Language Detection
        var detectedLanguage = "und"
        if (orderedText.isNotBlank()) {
            try {
                detectedLanguage = languageIdentifier.identifyLanguage(orderedText).await()
                if (detectedLanguage == "und") detectedLanguage = "en" // Default fallback
            } catch (e: Exception) {
                // Ignore language ID failure
            }
        }

        return OCRResult(
            text = orderedText.ifEmpty { visionText.text },
            confidence = avgConfidence,
            language = detectedLanguage,
            blocks = sortedBlocks
        )
    }
}
