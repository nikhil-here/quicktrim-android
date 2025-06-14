package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

data class WordResponse(
    @SerializedName("end")
    val end: Double,
    @SerializedName("logprob")
    val logprob: Double,
    @SerializedName("speaker_id")
    val speakerId: String,
    @SerializedName("start")
    val start: Double,
    @SerializedName("text")
    val text: String,
    @SerializedName("type")
    val type: String,
    val isRemoved: Boolean = false
)