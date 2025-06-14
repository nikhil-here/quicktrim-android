package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

class SpeechToTextResponse(
    @SerializedName("additional_formats")
    val additionalFormats: List<AdditionalFormat>?,
    @SerializedName("language_code")
    val languageCode: String?,
    @SerializedName("language_probability")
    val languageProbability: Double?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("words")
    val wordResponses: List<WordResponse>?
)

