package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

data class SegmentResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("words")
    val wordResponses: List<WordResponse>,
    val isRemoved: Boolean = false
)