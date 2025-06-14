package com.quicktrim.transcription_api_demo.domain

import com.quicktrim.ai.network.model.SegmentResponse
import com.quicktrim.ai.network.model.SegmentedJsonFormatResponse

data class TranscriptionEntity(
    val segments: List<SegmentEntity>
)

data class SegmentEntity(
    val segmentResponse: SegmentResponse,
    val isRemoved: Boolean = false
)

fun SegmentedJsonFormatResponse.toTranscriptionEntity() : TranscriptionEntity {
    return TranscriptionEntity(
        segments = segmentResponses.map { segment ->
            SegmentEntity(
                segmentResponse = segment,
                isRemoved = false
            )
        }
    )
}
