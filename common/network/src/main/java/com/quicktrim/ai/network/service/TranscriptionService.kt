package com.quicktrim.ai.network.service

import com.quicktrim.ai.network.model.TranscriptionNetworkResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TranscriptionService {

    companion object {
        const val FORM_DATA_AUDIO = "audio"
        const val FORM_DATA_MODEL = "model"
        const val FORM_DATA_LANGUAGE = "language"
    }

    @Multipart
    @POST("transcribe")
    suspend fun transcribe(
        @Part audio: MultipartBody.Part,
        @Part(FORM_DATA_MODEL) model: String? = null,
        @Part(FORM_DATA_LANGUAGE) language: String? = null
    ): TranscriptionNetworkResponse

}