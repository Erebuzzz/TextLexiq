package com.textlexiq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val confidence: Float,
    val createdAt: Long,
    val updatedAt: Long,
    // Metadata fields
    val sourceImagePath: String? = null,
    val ocrEngine: String = "mlkit",
    val language: String = "en",
    val tags: String = "",  // Comma-separated tags
    val latexContent: String? = null  // Generated LaTeX source
)
