package com.textlexiq.data

import android.content.Context
import androidx.room.Room
import com.textlexiq.data.local.MIGRATION_1_2
import com.textlexiq.data.local.TextLexiqDatabase
import com.textlexiq.data.UserPreferencesRepository

interface AppContainer {
    val textLexiqRepository: TextLexiqRepository
    val userPreferencesRepository: UserPreferencesRepository
    val smartModelRouter: com.textlexiq.llm.router.SmartModelRouter
    val modelManager: com.textlexiq.data.model.ModelManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: TextLexiqDatabase by lazy {
        Room.databaseBuilder(
            context,
            TextLexiqDatabase::class.java,
            "textlexiq-database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    override val textLexiqRepository: TextLexiqRepository by lazy {
        TextLexiqRepository.create(database.documentDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val smartModelRouter: SmartModelRouter by lazy {
        SmartModelRouter(modelManager)
    }

    override val modelManager: com.textlexiq.data.model.ModelManager by lazy {
        com.textlexiq.data.model.ModelManager(context)
    }
}

