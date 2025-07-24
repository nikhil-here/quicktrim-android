package com.quicktrim.ai.ui.upload

import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.MainViewModel
import com.quicktrim.ai.R
import com.quicktrim.ai.domain.model.Project
import com.quicktrim.ai.toDate
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "UploadScreen"

@Composable
fun UploadScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel,
    uploadViewModel: UploadViewModel = hiltViewModel()
) {
    val projects by uploadViewModel.projects.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    val activity = LocalActivity.current ?: return
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadViewModel.onMediaUriSelected(uri)
        } else {
            Toast.makeText(context, "Unable to fetch uri", Toast.LENGTH_LONG).show()
        }
    }

    fun launchPickMedia() {
        pickMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.i(TAG, "UploadScreen: result $result")
        val isReadVideoPermissionGranted = result.getOrDefault(READ_MEDIA_VIDEO, false)
        val isReadMediaUserSelectedPermissionGranted =
            result.getOrDefault(READ_MEDIA_VISUAL_USER_SELECTED, false)
        if (isReadVideoPermissionGranted || isReadMediaUserSelectedPermissionGranted) {
            launchPickMedia()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    fun onUploadClick() {
        val isReadPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            context,
            READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        Log.i(TAG, "onUploadClick: isReadPermissionGranted $isReadPermissionGranted")

        when {
            isReadPermissionGranted -> {
                launchPickMedia()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, READ_MEDIA_VIDEO
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity, READ_MEDIA_VISUAL_USER_SELECTED
            ) -> {
                Toast.makeText(
                    context,
                    "Enable permissions from settings",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                permissionLauncher.launch(
                    arrayOf(
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_VISUAL_USER_SELECTED
                    )
                )
            }
        }
    }

    LaunchedEffect(true) {
        uploadViewModel.sideEffect.collectLatest {
            when (it) {
                is UploadScreenSideEffects.NavigateToEditScreen -> {
                    navController.navigate(Routes.Edit.withProjectId(id = it.projectId))
                }
            }
        }
    }


    UploadScreenUi(
        modifier = modifier,
        projects = projects,
        onUploadClick = { onUploadClick() },
        onProjectSelected = { uploadViewModel.onProjectSelected(it) }
    )
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UploadScreenUi(
    modifier: Modifier = Modifier,
    onUploadClick: () -> Unit,
    onProjectSelected: (Project) -> Unit,
    projects: List<Project>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (projects.isEmpty()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_description_one_liner),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(projects) {
                    ProjectRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        project = it,
                        onProjectSelected = onProjectSelected
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onUploadClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(100.dp))
                .size(56.dp)
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_upload_cta)
            )
        }
    }
}


@Composable
fun ProjectRow(
    modifier: Modifier = Modifier,
    project: Project,
    onProjectSelected: (Project) -> Unit
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = true
            ) { onProjectSelected(project) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(project.projectName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(project.updatedAt.toDate(), style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewUploadScreen() {
    QuicktrimandroidTheme {
        UploadScreenUi(
            onUploadClick = {},
            projects = emptyList(),
            onProjectSelected = {}
        )
    }
}

@Preview
@Composable
private fun PreviewProjectRow() {
    QuicktrimandroidTheme {
        UploadScreenUi(
            onUploadClick = {},
            projects = (1..4).map {
                Project(
                    originalMediaUrl = "",
                )
            },
            onProjectSelected = {}
        )
    }
}

