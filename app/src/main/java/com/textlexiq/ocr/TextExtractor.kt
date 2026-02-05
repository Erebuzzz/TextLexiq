package com.textlexiq.ocr

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TextExtractor(private val engine: OCREngine) {

    suspend fun extract(bitmap: Bitmap): OCRResult {
        // Preprocess image using OpenCV (Deskew, Binarize)
        val enhancedBitmap = withContext(Dispatchers.Default) {
             ImagePreprocessor.enhanceForOcr(bitmap)
        }
        
        return engine.extract(enhancedBitmap)
    }

    companion object {
        fun create(engineType: String = "mlkit", context: android.content.Context? = null): TextExtractor {
            val engine = when (engineType) {
                "tesseract" -> if (context != null) TesseractOCREngine(context) else MLKitOCREngine()
                else -> MLKitOCREngine()
            }
            return TextExtractor(engine)
        }
        
        // Helper for previews/tests
        fun preview(): TextExtractor = TextExtractor(MLKitOCREngine())
    }
}
