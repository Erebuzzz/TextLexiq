package com.textlexiq.data

import android.content.Context
import androidx.room.Room
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
        ).fallbackToDestructiveMigration()
            .build()
    }

    override val repository: TextLexiqRepository by lazy {
        TextLexiqRepository.create(database.documentDao())
    }
}
