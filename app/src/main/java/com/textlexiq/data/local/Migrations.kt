package com.textlexiq.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to version 2.
 * Adds metadata columns: sourceImagePath, ocrEngine, language, tags, latexContent
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new nullable columns with defaults
        db.execSQL("ALTER TABLE documents ADD COLUMN sourceImagePath TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE documents ADD COLUMN ocrEngine TEXT NOT NULL DEFAULT 'mlkit'")
        db.execSQL("ALTER TABLE documents ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
        db.execSQL("ALTER TABLE documents ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE documents ADD COLUMN latexContent TEXT DEFAULT NULL")
    }
}
