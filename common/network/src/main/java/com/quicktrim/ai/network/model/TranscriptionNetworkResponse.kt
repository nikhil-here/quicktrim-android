package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

data class Transcription(
    @SerializedName("start") val start: Int,
    @SerializedName("end") val end: Int,
    @SerializedName("transcription") val transcription: String
)

data class TranscriptionNetworkResponse(
    @SerializedName("transcriptions") val transcriptions: List<Transcription>
)