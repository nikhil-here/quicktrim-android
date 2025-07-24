package com.quicktrim.ai.data.remote.model

import com.google.gson.annotations.SerializedName

class SpeechToTextRemoteResponse(
    @SerializedName("additional_formats")
    val additionalFormats: List<AdditionalFormat>?,
    @SerializedName("language_code")
    val languageCode: String?,
    @SerializedName("language_probability")
    val languageProbability: Double?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("words")
    val wordRemoteRespons: List<WordRemoteResponse>?
)

