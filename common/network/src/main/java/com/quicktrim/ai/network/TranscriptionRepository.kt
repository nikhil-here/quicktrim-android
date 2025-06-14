package com.quicktrim.ai.network

import com.google.gson.Gson
import com.quicktrim.ai.network.model.QuickTrimResponse
import com.quicktrim.ai.network.model.SegmentedJsonFormatResponse
import com.quicktrim.ai.network.model.SpeechToTextResponse
import com.quicktrim.ai.network.service.QuickTrimApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepository @Inject constructor(
    private val quickTrimApiService: QuickTrimApiService,
    private val gson: Gson
) {

    suspend fun speechToText(file: File): QuickTrimResponse<SegmentedJsonFormatResponse, Nothing> {
        try {
            //creating multipart file
            val audioPart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = file.asRequestBody()
            )

            //creating additional format for requesting data in segmented json format
            val additionalFormatsArray = JSONArray().apply {
                val format = JSONObject().apply {
                    put("format", "segmented_json")
                    put("max_segment_duration_s", 4.0)
                    put("max_segment_chars", 50)
                }
                put(format)
            }

            //fetching transcription from elven labs api
            val responseBody = quickTrimApiService.speechToText(
                file = audioPart,
                modelId = "scribe_v1",
                timestampGranularity = "word",
                additionalFormats = additionalFormatsArray.toString(),
                diarize = true
            )

            //parsing retrofit response body
            val speechToTextResponse = gson.fromJson(
                //Dummy.response,
                responseBody.string(),
                SpeechToTextResponse::class.java
            )

            //parsing segmented json format from speech to text response
            val segmentedJson = gson.fromJson(
                speechToTextResponse.additionalFormats?.firstOrNull()?.content.orEmpty(),
                SegmentedJsonFormatResponse::class.java
            )

            if (segmentedJson != null) {
                return QuickTrimResponse.Success(segmentedJson)
            } else {
                throw IllegalStateException("Segmented JSON format not found")
            }
        } catch (e: Exception) {
            return QuickTrimResponse.UnknownError(e)
        }
    }

}