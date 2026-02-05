package com.textlexiq.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import cz.adaptech.tesseract4android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TesseractOCREngine(private val context: Context) : OCREngine {

    override val name = "tesseract"
    private val tessApi = TessBaseAPI()
    private var isInitialized = false

    // Initialize on first use (suspend)
    private suspend fun ensureInitialized() {
        if (isInitialized) return
        
        // Set up tessdata path
        val tessDataPath = File(context.filesDir, "tessdata")
        if (!tessDataPath.exists()) {
             tessDataPath.mkdirs()
        }
        
        // Check for eng.traineddata
        val engData = File(tessDataPath, "eng.traineddata")
        if (!engData.exists()) {
             // In a real app, we would copy from assets.
             // For now, we assume user/provisioning puts it there, or we fail gracefully.
             try {
                 context.assets.open("tessdata/eng.traineddata").use { input ->
                     FileOutputStream(engData).use { output ->
                         input.copyTo(output)
                     }
                 }
             } catch (e: IOException) {
                 // Asset might not exist in this dev phase
                 throw IllegalStateException("Tesseract data missing. Please add 'eng.traineddata' to assets/tessdata/")
             }
        }

        // Init API
        // Parent dir of tessdata
        val dataPath = context.filesDir.absolutePath 
        isInitialized = tessApi.init(dataPath, "eng")
    }

    override suspend fun extract(bitmap: Bitmap): OCRResult {
        try {
            ensureInitialized()
        } catch (e: Exception) {
            return OCRResult("Tesseract Init Failed: ${e.message}", 0f, "en", emptyList())
        }
        
        tessApi.setImage(bitmap)
        val fullText = tessApi.utF8Text ?: ""
        val confidence = tessApi.meanConfidence() / 100f // 0-100 to 0-1
        
        // Block iterator for bounding boxes would go here. 
        // TessBaseAPI access to iterators is complex. 
        // For MVP we might skip blocks or try to get basics.
        // Actually, we can assume a single block for now or implement iterator later.
        
        // Let's return at least one block for the whole text
        val blocks = listOf(
            OCRBlock(fullText, confidence, null) // No rect for now
        )

        return OCRResult(fullText, confidence, "en", blocks)
    }
    
    fun close() {
        tessApi.recycle()
    }
}
