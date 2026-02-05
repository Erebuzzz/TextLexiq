package com.textlexiq.ocr

import android.graphics.Rect

data class OCRResult(
    val text: String,
    val confidence: Float,
    val language: String = "und", // ISO 639-1
    val blocks: List<OCRBlock> = emptyList()
)

data class OCRBlock(
    val text: String,
    val confidence: Float,
    val boundingBox: Rect?
)
