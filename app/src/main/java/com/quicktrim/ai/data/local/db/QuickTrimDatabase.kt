package com.quicktrim.ai.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quicktrim.ai.data.local.dao.ProjectDao
import com.quicktrim.ai.data.local.entity.ProjectDbEntity

@Database(entities = [ProjectDbEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class QuickTrimDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}