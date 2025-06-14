package com.quicktrim.transcription_api_demo.di

import com.quicktrim.ai.transformer.MediaTransformer
import com.quicktrim.ai.transformer.QuickTrimTransformer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideQuickTrimTransformer(
        mediaTransformer: MediaTransformer
    ) : QuickTrimTransformer {
        return mediaTransformer
    }

}