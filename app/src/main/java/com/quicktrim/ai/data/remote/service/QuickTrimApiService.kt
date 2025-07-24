package com.quicktrim.ai.data.remote.service

import com.quicktrim.ai.BuildConfig
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface QuickTrimApiService {

    @Multipart
    @POST("speech-to-text")
    suspend fun speechToText(
        @Part file: MultipartBody.Part,
        @Part("model_id") modelId: String? = null,
        @Part("timestamps_granularity") timestampGranularity: String? = null,
        @Part("additional_formats") additionalFormats: String? = null,
        @Part("diarize") diarize: Boolean = false,
        @Header("xi-api-key") apiKey: String = BuildConfig.API_KEY
    ): ResponseBody

}