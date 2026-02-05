package com.textlexiq.data

import com.textlexiq.data.local.DocumentDao
import com.textlexiq.data.local.DocumentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDocumentDao : DocumentDao {

    private val documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    private var nextId = 1L

    override fun observeDocuments(): Flow<List<DocumentEntity>> = documents

    override suspend fun getDocumentById(id: Long): DocumentEntity? {
        return documents.value.find { it.id == id }
    }

    override suspend fun insert(document: DocumentEntity): Long {
        val id = if (document.id == 0L) nextId++ else document.id
        val newDoc = document.copy(id = id)
        documents.update { it + newDoc }
        return id
    }

    override suspend fun update(document: DocumentEntity) {
        documents.update { list ->
            list.map { if (it.id == document.id) document else it }
        }
    }

    override suspend fun deleteById(id: Long) {
        documents.update { list ->
            list.filter { it.id != id }
        }
    }

    override fun observeDocumentsByTag(tag: String): Flow<List<DocumentEntity>> {
        return documents.map { list ->
            list.filter { it.tags.contains(tag) }
        }
    }
}
