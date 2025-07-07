package com.quicktrim.ai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectDbEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Long = 0,
    @ColumnInfo("project_name") val projectName: String,
    @ColumnInfo("original_media_url") val originalMediaUrl: String,
    @ColumnInfo("segmented_json_format") val segmentedJsonFormatDbEntity: SegmentedJsonFormatDbEntity? = null,
    @ColumnInfo("exported_media_url") val exportedMediaUrl: String?,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("updated_at") val updatedAt: Long = System.currentTimeMillis(),
)