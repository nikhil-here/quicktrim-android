package com.quicktrim.ai.ui.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.common.QuickTrimErrorDialog
import com.quicktrim.ai.ui.common.QuickTrimPlayer
import com.quicktrim.ai.ui.common.QuickTrimProcessIndicator
import com.quicktrim.ai.ui.common.SegmentRow
import kotlin.system.exitProcess

@Composable
fun ExportScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val isMuted by mainViewModel.isMuted.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val aspectRatio by mainViewModel.aspectRatio.collectAsStateWithLifecycle()
    val expandedMode by mainViewModel.expandedMode.collectAsStateWithLifecycle()
    val progress by mainViewModel.progress.collectAsStateWithLifecycle()
    val totalDuration by mainViewModel.totalDuration.collectAsStateWithLifecycle()
    val playerView by mainViewModel.playerView.collectAsStateWithLifecycle()
    val processState by mainViewModel.processState.collectAsStateWithLifecycle()
    val error by mainViewModel.error.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            QuickTrimPlayer(
                modifier = Modifier.fillMaxWidth(),
                isPlaying = isPlaying,
                isMuted = isMuted,
                progress = { progress },
                totalDuration = totalDuration,
                playerView = { playerView },
                toggleMuteUnMute = {
                    mainViewModel.toggleMuteUnMute()
                },
                onRewind = {
                    mainViewModel.onRewind()
                },
                onForward = {
                    mainViewModel.onForward()
                },
                onPlayPause = {
                    mainViewModel.onTogglePlayPause()
                },
                aspectRatio = aspectRatio,
                expandedMode = expandedMode,
                toggleExpandMode = {
                    mainViewModel.toggleExpandMode()
                }
            )
            Spacer(Modifier.height(12.dp))
            QuickTrimProcessIndicator(
                modifier = Modifier,
                state = processState
            )
        }

        error?.let {
            QuickTrimErrorDialog(
                title = Constants.GENERIC_ERROR_MSG,
                message = it.error.message.orEmpty(),
                ctaTitle = "RESTART",
                onClick = {
                    exitProcess(0)
                },
                onDismissRequest = {}
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(end = 6.dp, top = 6.dp)
        ) {
            IconButton(
                onClick = {
                    mainViewModel.onExportScreenBackClick()
                    navController.popBackStack()
                },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    ), shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    }
}