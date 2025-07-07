package com.quicktrim.ai.domain.repository

import com.quicktrim.ai.data.local.dao.ProjectDao
import com.quicktrim.ai.domain.mapper.toProject
import com.quicktrim.ai.domain.mapper.toProjectDbEntity
import com.quicktrim.ai.domain.model.Project
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val dao: ProjectDao
) {
    suspend fun saveProject(project: Project) = dao.insertProject(project.toProjectDbEntity())

    suspend fun updateProject(project: Project) = dao.updateProject(project.toProjectDbEntity())

    suspend fun deleteProject(project: Project) = dao.deleteProject(project.toProjectDbEntity())

    fun getProjects() = dao.getAllProjects().map { it.map { it.toProject() } }

    fun getProjectById(id: Long) = dao.getProjectById(id).map { it?.toProject() }
}