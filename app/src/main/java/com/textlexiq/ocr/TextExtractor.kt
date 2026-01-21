package com.textlexiq.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OCRResult(val text: String, val confidence: Float)

class TextExtractor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(bitmap: Bitmap): OCRResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        val confidences = visionText.textBlocks
            .mapNotNull { block ->
                block.confidence.takeIf { it in 0f..1f }
            }

        val averageConfidence = if (confidences.isNotEmpty()) {
            confidences.average().toFloat()
        } else {
            0f
        }

        return OCRResult(
            text = visionText.text,
            confidence = averageConfidence
        )
    }

    companion object {
        fun preview(): TextExtractor = TextExtractor()
    }
}
