package com.textlexiq.exporter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentExporter private constructor() {

    suspend fun exportToPdf(content: String) = withContext(Dispatchers.IO) {
        // TODO: Hook up iText implementation for PDF generation.
    }

    suspend fun exportToWord(content: String) = withContext(Dispatchers.IO) {
        // TODO: Hook up Apache POI implementation for DOCX generation.
    }

    fun queueExport() {
        // Placeholder for export queue integration.
    }

    fun openExportPicker() {
        // Placeholder for showing export destination picker UI.
    }

    companion object {
        fun default(): DocumentExporter = DocumentExporter()
    }
}
