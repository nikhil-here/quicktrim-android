package com.quicktrim.ai.ui.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicktrim.ai.domain.model.Project
import com.quicktrim.ai.domain.repository.ProjectRepository
import com.quicktrim.ai.ui.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val TAG = "UploadViewModel"

sealed class UploadScreenSideEffects {
    data class NavigateToEditScreen(val projectId: Long, val uri: String) :
        UploadScreenSideEffects()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProjectRepository
) : ViewModel() {

    private val _sideEffect = Channel<UploadScreenSideEffects>()
    val sideEffect = _sideEffect.receiveAsFlow()

    val projects = repository.getProjects()

    fun onMediaUriSelected(uri: Uri) {
        viewModelScope.launch {

            val internalUri = moveMediaToInternalStorage(uri)

            val project = Project(
                projectName = getCleanFileName(uri),
                originalMediaUrl = internalUri
            )
            val projectId = repository.saveProject(project)
            Log.i(TAG, "onMediaUriSelected: project created with id $projectId")
            _sideEffect.send(
                UploadScreenSideEffects.NavigateToEditScreen(
                    projectId = projectId,
                    uri = uri.toString()
                )
            )
        }
    }

    fun onProjectSelected(project: Project) {
        viewModelScope.launch {
            _sideEffect.send(
                UploadScreenSideEffects.NavigateToEditScreen(
                    projectId = project.id,
                    uri = project.originalMediaUrl
                )
            )
        }
    }

    fun getCleanFileName(uri: Uri): String {
        return runCatching {
            var fileName = ""

            // Try to extract display name from content resolver
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }

            // Fallback: extract from URI path
            if (fileName.isBlank()) {
                fileName = uri.path?.substringAfterLast('/') ?: "untitled"
            }

            // Clean the file name: remove spaces, replace with underscore
            fileName.trim().replace("\\s+".toRegex(), "_")
        }.getOrDefault(Constants.DEFAULT_PROJECT_NAME)
    }

    fun moveMediaToInternalStorage(uri: Uri) : String {
        val contentResolver = context.contentResolver
        val fileName = getCleanFileName(uri)
        val inputStream = contentResolver.openInputStream(uri)
        val outputFile = File(context.filesDir, "fileName.mp4")
        val outputStream = FileOutputStream(outputFile)
        inputStream?.copyTo(outputStream)
        return outputFile.path.toString()
    }
}