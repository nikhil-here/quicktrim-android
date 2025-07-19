package com.quicktrim.ai.data.remote.model

import com.google.gson.annotations.SerializedName

data class SegmentRemoteResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("words")
    val words: List<WordRemoteResponse>,
)