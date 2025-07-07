package com.quicktrim.ai.domain.model

import com.quicktrim.ai.ui.Constants
import java.util.UUID

data class Project(
    val id: Long = System.currentTimeMillis(),
    val projectName: String = Constants.DEFAULT_PROJECT_NAME,
    val originalMediaUrl: String,
    val exportedMediaUrl: String? = null,
    val segmentedJsonFormat: SegmentedJsonFormat? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)