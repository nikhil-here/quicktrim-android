package com.quicktrim.ai.network.di

import com.quicktrim.ai.network.BuildConfig
import com.quicktrim.ai.network.service.TranscriptionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTranscriptionService(retrofit: Retrofit): TranscriptionService {
        return retrofit.create(TranscriptionService::class.java)
    }
}