package com.textlexiq.dl.layout

import android.graphics.RectF

enum class RegionType {
    TEXT,
    TITLE,
    LIST,
    TABLE,
    FIGURE
}

data class LayoutRegion(
    val type: RegionType,
    val boundingBox: RectF,
    val confidence: Float
)

interface LayoutAnalyzer {
    suspend fun analyze(bitmap: android.graphics.Bitmap): List<LayoutRegion>
}
