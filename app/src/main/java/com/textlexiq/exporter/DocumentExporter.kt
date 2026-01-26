package com.textlexiq.exporter

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

class DocumentExporter {

    private val ioDispatcher = Dispatchers.IO

    suspend fun exportToPdf(content: String, title: String, outputFile: File): Result<File> = withContext(ioDispatcher) {
        try {
            val writer = PdfWriter(outputFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            val titlePara = Paragraph(Text(title).setFontSize(18f).setBold())
            document.add(titlePara)
            
            // Basic text addition - could be enhanced with Markdown parsing later
            val contentPara = Paragraph(content)
            document.add(contentPara)
            
            document.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun exportToWord(content: String, title: String, outputFile: File): Result<File> = withContext(ioDispatcher) {
        try {
            val document = XWPFDocument()
            
            val titlePara = document.createParagraph()
            val titleRun = titlePara.createRun()
            titleRun.isBold = true
            titleRun.fontSize = 18
            titleRun.setText(title)
            
            val contentPara = document.createParagraph()
            val contentRun = contentPara.createRun()
            if (content.contains("\n")) {
                val lines = content.split("\n")
                for ((index, line) in lines.withIndex()) {
                    contentRun.setText(line)
                    if (index < lines.size - 1) {
                        contentRun.addBreak()
                    }
                }
            } else {
                contentRun.setText(content)
            }
            
            FileOutputStream(outputFile).use { out ->
                document.write(out)
            }
            document.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun exportToLatex(content: String, title: String, latexSource: String?, outputFile: File): Result<File> = withContext(ioDispatcher) {
         try {
            val finalContent = if (!latexSource.isNullOrBlank()) {
                 latexSource
            } else {
                 """
                 \documentclass{article}
                 \usepackage[utf8]{inputenc}
                 \title{$title}
                 \begin{document}
                 \maketitle
                 
                 ${content.replace("\n", "\n\n")}
                 
                 \end{document}
                 """.trimIndent()
            }
            
            outputFile.writeText(finalContent)
            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    companion object {
        fun default(): DocumentExporter = DocumentExporter()
    }
}
