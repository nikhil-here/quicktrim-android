package com.quicktrim.ai.domain.transformer

data class QuickTrimTransformSuccess(
    val outputPath: String,
    val fileName: String
)

data class QuickTrimTransformFailure(
    val exception: Exception,
    val message: String
)
