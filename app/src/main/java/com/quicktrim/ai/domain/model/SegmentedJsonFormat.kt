package com.quicktrim.ai.domain.model


data class SegmentedJsonFormat(
    val segments: List<Segment> = emptyList()
)

data class Segment(
    val id: String,
    val text: String,
    val words: List<Word>,
    val isRemoved: Boolean = false
)

data class Word(
    val id: String,
    val end: Double,
    val logprob: Double,
    val speakerId: String,
    val start: Double,
    val text: String,
    val type: String,
    val isRemoved: Boolean = false
)