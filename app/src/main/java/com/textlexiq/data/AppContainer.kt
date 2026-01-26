package com.textlexiq.data

import android.content.Context
import androidx.room.Room
import com.textlexiq.data.local.MIGRATION_1_2
import com.textlexiq.data.local.TextLexiqDatabase

interface AppContainer {
    val repository: TextLexiqRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val database: TextLexiqDatabase by lazy {
        Room.databaseBuilder(
            context,
            TextLexiqDatabase::class.java,
            "textlexiq-database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    override val repository: TextLexiqRepository by lazy {
        TextLexiqRepository.create(database.documentDao())
    }
}

