package com.quicktrim.ai.network.model

import com.google.gson.annotations.SerializedName

data class AdditionalFormat(
    @SerializedName("content")
    val content: String,
    @SerializedName("content_type")
    val contentType: String,
    @SerializedName("file_extension")
    val fileExtension: String,
    @SerializedName("is_base64_encoded")
    val isBase64Encoded: Boolean,
    @SerializedName("requested_format")
    val requestedFormat: String
)