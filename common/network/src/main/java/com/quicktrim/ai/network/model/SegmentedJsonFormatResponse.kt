package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

data class SegmentedJsonFormatResponse(
    @SerializedName("segments")
    val segmentResponses: List<SegmentResponse>
)