package com.textlexiq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class],
    version = 2,
    exportSchema = true
)
abstract class TextLexiqDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}

