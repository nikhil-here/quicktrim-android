package com.quicktrim.ai.data.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.quicktrim.ai.data.local.entity.SegmentedJsonFormatDbEntity

class Converters {

    private val gson by lazy { Gson() }

    @TypeConverter
    fun fromSegmentedJsonFormat(result: SegmentedJsonFormatDbEntity?): String? {
        return runCatching { gson.toJson(result) }.getOrNull()
    }


    @TypeConverter
    fun toSegmentedJsonFormat(data: String?): SegmentedJsonFormatDbEntity? {
        return runCatching {
            gson.fromJson<SegmentedJsonFormatDbEntity>(
                data,
                SegmentedJsonFormatDbEntity::class.java
            )
        }.getOrNull()
    }

}