package com.textlexiq.ocr

import android.graphics.Bitmap

interface OCREngine {
    suspend fun extract(bitmap: Bitmap): OCRResult
    val name: String
}
