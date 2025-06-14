package com.quicktrim.ai.di

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