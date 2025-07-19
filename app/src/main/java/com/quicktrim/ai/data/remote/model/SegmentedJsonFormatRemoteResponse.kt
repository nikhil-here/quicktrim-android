package com.quicktrim.ai.data.remote.model

import com.google.gson.annotations.SerializedName

data class SegmentedJsonFormatRemoteResponse(
    @SerializedName("segments")
    val segments: List<SegmentRemoteResponse>
)