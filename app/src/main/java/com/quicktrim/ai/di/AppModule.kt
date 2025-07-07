package com.quicktrim.ai.di

import android.content.Context
import androidx.room.Room
import com.quicktrim.ai.data.local.dao.ProjectDao
import com.quicktrim.ai.data.local.db.QuickTrimDatabase
import com.quicktrim.ai.transformer.MediaTransformer
import com.quicktrim.ai.transformer.QuickTrimTransformer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideQuickTrimTransformer(
        mediaTransformer: MediaTransformer
    ): QuickTrimTransformer {
        return mediaTransformer
    }

    @Provides
    @Singleton
    fun provideQuickTrimDb(@ApplicationContext context: Context): QuickTrimDatabase {
        return Room.databaseBuilder(
            context,
            QuickTrimDatabase::class.java,
            "quicktrim_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProjectDao(quickTrimDatabase: QuickTrimDatabase): ProjectDao {
        return quickTrimDatabase.projectDao()
    }

}