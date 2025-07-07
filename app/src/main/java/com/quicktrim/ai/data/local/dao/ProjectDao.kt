package com.quicktrim.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quicktrim.ai.data.local.entity.ProjectDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(projectDbEntity: ProjectDbEntity): Long

    @Update
    suspend fun updateProject(projectDbEntity: ProjectDbEntity)

    @Delete
    suspend fun deleteProject(project: ProjectDbEntity)

    @Query("SELECT * FROM `projects` ORDER BY `created_at` DESC")
    fun getAllProjects(): Flow<List<ProjectDbEntity>>

    @Query("SELECT * FROM `projects` WHERE `id` = :id")
    fun getProjectById(id: Long): Flow<ProjectDbEntity?>
}