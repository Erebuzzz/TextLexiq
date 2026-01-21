package com.textlexiq.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class TextRecognitionEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun runOcr(path: String): String {
        val bitmap = BitmapFactory.decodeFile(path)
            ?: error("Unable to decode processed image from $path")
        return runOcr(bitmap).also { bitmap.recycle() }
    }

    suspend fun runOcr(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        return visionText.text
    }
}
