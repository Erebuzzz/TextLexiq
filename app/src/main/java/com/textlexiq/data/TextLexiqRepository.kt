package com.textlexiq.data

import com.textlexiq.data.local.DocumentDao
import com.textlexiq.data.local.DocumentEntity
import com.textlexiq.data.model.DocumentSummary
import com.textlexiq.utils.formatRelativeTime
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TextLexiqRepository internal constructor(
    private val documentDao: DocumentDao,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    fun observeRecentDocuments(): Flow<List<DocumentSummary>> =
        documentDao.observeDocuments().map { documents ->
            documents.map { it.toSummary() }
        }

    suspend fun saveDocument(
        content: String,
        confidence: Float,
        sourceImagePath: String? = null,
        ocrEngine: String = "mlkit",
        language: String = "en"
    ): Long {
        val now = clock.millis()
        val entity = DocumentEntity(
            title = generateTitle(content, now),
            content = content,
            confidence = confidence,
            createdAt = now,
            updatedAt = now,
            sourceImagePath = sourceImagePath,
            ocrEngine = ocrEngine,
            language = language
        )
        return documentDao.insert(entity)
    }

    suspend fun getDocumentById(id: Long): DocumentEntity? = documentDao.getDocumentById(id)

    suspend fun updateDocument(document: DocumentEntity): DocumentEntity {
        val updated = document.copy(updatedAt = clock.millis())
        documentDao.update(updated)
        return updated
    }

    suspend fun deleteDocument(id: Long) {
        documentDao.deleteById(id)
    }

    suspend fun updateDocumentContent(id: Long, newContent: String, newTitle: String? = null): Boolean {
        val existing = documentDao.getDocumentById(id) ?: return false
        val updated = existing.copy(
            content = newContent,
            title = newTitle ?: existing.title,
            updatedAt = clock.millis()
        )
        documentDao.update(updated)
        return true
    }

    suspend fun updateDocumentTags(id: Long, tags: List<String>): Boolean {
        val existing = documentDao.getDocumentById(id) ?: return false
        val updated = existing.copy(
            tags = tags.joinToString(","),
            updatedAt = clock.millis()
        )
        documentDao.update(updated)
        return true
    }

    suspend fun updateLatexContent(id: Long, latexContent: String): Boolean {
        val existing = documentDao.getDocumentById(id) ?: return false
        val updated = existing.copy(
            latexContent = latexContent,
            updatedAt = clock.millis()
        )
        documentDao.update(updated)
        return true
    }

    private fun DocumentEntity.toSummary(): DocumentSummary {
        val summaryText = content.lines().firstOrNull()?.take(SUMMARY_PREVIEW_LENGTH)
            ?: content.take(SUMMARY_PREVIEW_LENGTH)
        return DocumentSummary(
            id = id.toString(),
            name = title,
            summary = summaryText,
            lastUpdatedLabel = formatRelativeTime(updatedAt, clock)
        )
    }

    private fun generateTitle(content: String, timestamp: Long): String {
        val firstLine = content.lines()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.take(TITLE_MAX_LENGTH)

        if (!firstLine.isNullOrBlank()) return firstLine

        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.getDefault())
        val dateTime = Instant.ofEpochMilli(timestamp).atZone(clock.zone)
        return "Document ${formatter.format(dateTime)}"
    }

    companion object {
        private const val SUMMARY_PREVIEW_LENGTH = 160
        private const val TITLE_MAX_LENGTH = 60

        fun create(documentDao: DocumentDao): TextLexiqRepository = TextLexiqRepository(documentDao)

        fun preview(): TextLexiqRepository {
            val inMemoryDao = InMemoryDocumentDao()
            return TextLexiqRepository(inMemoryDao)
        }
    }
}

private class InMemoryDocumentDao : DocumentDao {
    private val documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    private var nextId = 1L

    override fun observeDocuments(): Flow<List<DocumentEntity>> = documents

    override suspend fun getDocumentById(id: Long): DocumentEntity? =
        documents.value.firstOrNull { it.id == id }

    override suspend fun insert(document: DocumentEntity): Long {
        val assignedId = if (document.id == 0L) nextId++ else document.id
        val entity = document.copy(id = assignedId)
        documents.update { current ->
            listOf(entity) + current.filterNot { it.id == assignedId }
        }
        return assignedId
    }

    override suspend fun update(document: DocumentEntity) {
        documents.update { current ->
            current.map { if (it.id == document.id) document else it }
        }
    }

    override suspend fun deleteById(id: Long) {
        documents.update { current ->
            current.filterNot { it.id == id }
        }
    }

    override fun observeDocumentsByTag(tag: String): Flow<List<DocumentEntity>> =
        documents.map { list ->
            list.filter { it.tags.contains(tag, ignoreCase = true) }
        }
}

