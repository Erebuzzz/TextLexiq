package com.textlexiq.data

import com.textlexiq.data.local.DocumentEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TextLexiqRepositoryTest {

    private lateinit var repository: TextLexiqRepository
    private lateinit var fakeDao: FakeDocumentDao
    private val fixedClock = Clock.fixed(Instant.parse("2023-01-01T10:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setup() {
        fakeDao = FakeDocumentDao()
        repository = TextLexiqRepository(fakeDao, fixedClock)
    }

    @Test
    fun saveDocument_insertsIntoDao() = runBlocking {
        val content = "Test Content"
        val id = repository.saveDocument(content, 0.9f)
        
        val saved = fakeDao.getDocumentById(id)
        assertNotNull(saved)
        assertEquals(content, saved?.content)
        assertEquals(0.9f, saved?.confidence)
    }

    @Test
    fun updateDocumentContent_updatesDao() = runBlocking {
        val id = repository.saveDocument("Initial", 1.0f)
        
        repository.updateDocumentContent(id, "Updated", "New Title")
        
        val updated = fakeDao.getDocumentById(id)
        assertEquals("Updated", updated?.content)
        assertEquals("New Title", updated?.title)
    }

    @Test
    fun deleteDocument_removesFromDao() = runBlocking {
        val id = repository.saveDocument("To Delete", 1.0f)
        repository.deleteDocument(id)
        
        val deleted = fakeDao.getDocumentById(id)
        assertEquals(null, deleted)
    }
}
