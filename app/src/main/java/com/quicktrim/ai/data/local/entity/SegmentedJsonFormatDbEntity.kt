package com.quicktrim.ai.data.local.entity

import com.google.gson.annotations.SerializedName

data class SegmentedJsonFormatDbEntity(
    @SerializedName("segments")
    val segments: List<SegmentDbEntity>
)

data class SegmentDbEntity(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("words")
    val words: List<WordDbEntity>,
    @SerializedName("isRemoved")
    val isRemoved: Boolean = false
)

data class WordDbEntity(
    @SerializedName("id")
    val id: String,
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
    @SerializedName("isRemoved")
    val isRemoved: Boolean = false
)
