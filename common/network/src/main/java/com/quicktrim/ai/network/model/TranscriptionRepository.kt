package com.quicktrim.ai.network.model

import com.quicktrim.ai.network.service.TranscriptionService
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepository @Inject constructor(
    private val transcriptionService: TranscriptionService
) {

    suspend fun transcribe(
        audioFile: File
    ): TranscriptionNetworkResponse {
        val audioPart = MultipartBody.Part.createFormData(
            name = TranscriptionService.FORM_DATA_AUDIO,
            filename = audioFile.name,
            body = audioFile.asRequestBody()
        )
        return transcriptionService.transcribe(audio = audioPart)
    }
}