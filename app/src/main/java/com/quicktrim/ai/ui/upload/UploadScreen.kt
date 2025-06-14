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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.Routes

private const val TAG = "UploadScreen"

@Composable
fun UploadScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val activity = LocalActivity.current ?: return

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            mainViewModel.onUriSelected(uri)
            navController.navigate(Routes.Edit.path)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quick Trim",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "transcription based video trimmer",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PackageManager.PERMISSION_GRANTED -> {
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
        ) {
            Text("Upload")
        }
    }
}