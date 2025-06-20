package com.quicktrim.ai.ui.common

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Forward5
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.quicktrim.ai.R
import com.quicktrim.ai.toTimeString
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "QuickTrimPlayer"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickTrimPlayer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    expandedMode: Boolean,
    isMuted: Boolean,
    progress: () -> Long,
    totalDuration: Long,
    playerView: () -> View?,
    toggleMuteUnMute: () -> Unit,
    onForward: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    toggleExpandMode: () -> Unit,
    aspectRatio: Float = 9 / 16f
) {
    var controlsVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var autoHideJob: Job? = null

    fun scheduleAutoHideJob() {
        autoHideJob?.cancel()
        autoHideJob = scope.launch {
            delay(Constants.AUTO_HIDE_DELAY)
            controlsVisible = false
        }
    }

    Column(
        modifier = modifier.combinedClickable(
            enabled = true,
            onClick = {
                controlsVisible = !controlsVisible
                scheduleAutoHideJob()
            },
            onDoubleClick = toggleExpandMode,
            interactionSource = null, indication = null
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(6.dp))
        ) {
            playerView()?.let { view ->
                AndroidView(
                    factory = {
                        //remove view from previous parent
                        (view.parent as ViewGroup?)?.removeView(view)
                        view
                    }, modifier = Modifier
                        .then(
                            if (expandedMode) {
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(aspectRatio.coerceAtLeast(0f))
                            } else {
                                Log.i(TAG, "QuickTrimPlayer: aspectRatio $aspectRatio")
                                Modifier
                                    .height(240.dp)
                                    .aspectRatio(aspectRatio.coerceAtLeast(0.1f))
                            }
                        )
                        .background(MaterialTheme.colorScheme.scrim)

                        .align(Alignment.Center)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                QuickTrimPrimaryControl(
                    modifier = Modifier.fillMaxWidth(),
                    isPlaying = isPlaying,
                    onForward = onForward,
                    onRewind = onRewind,
                    onPlayPause = onPlayPause
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = if (expandedMode) 32.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearWavyProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = {
                            progress().toFloat() / totalDuration.toFloat()
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = progress().toTimeString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )

                        Text(
                            text = totalDuration.toTimeString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            modifier = Modifier
                .width(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .pointerInput(true) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            toggleExpandMode()
                        },
                        onVerticalDrag = { _, _ ->

                        }
                    )
                }
                .combinedClickable(
                    enabled = true,
                    onClick = toggleExpandMode
                ),
            thickness = 6.dp
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickTrimPrimaryControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onForward: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
) {
    val playPauseIcon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
            onClick = onRewind,
            colors =  IconButtonDefaults.filledIconButtonColors(),
            shape = IconButtonDefaults.mediumRoundShape
        ) {
            Icon(
                modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                imageVector = Icons.Filled.Replay5,
                contentDescription = stringResource(R.string.cd_rewind),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        FilledIconButton(
            modifier = Modifier.size(IconButtonDefaults.largeContainerSize()),
            onClick = onPlayPause,
            shape = IconButtonDefaults.extraLargeSquareShape,
            colors = IconButtonDefaults.filledIconButtonColors()
        ) {
            Icon(
                modifier = Modifier.size(IconButtonDefaults.extraLargeIconSize),
                imageVector = playPauseIcon,
                contentDescription = stringResource(R.string.cd_play_pause),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        FilledIconButton(
            modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
            onClick = onForward,
            colors =  IconButtonDefaults.filledIconButtonColors(),
            shape = IconButtonDefaults.mediumRoundShape
        ) {
            Icon(
                modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                imageVector = Icons.Outlined.Forward5,
                contentDescription = stringResource(R.string.cd_rewind),
            )
        }
    }
}


@Preview
@Composable
private fun PreviewPrimaryControls() {
    val context = LocalContext.current
    QuicktrimandroidTheme {
        QuickTrimPrimaryControl(
            modifier = Modifier
                .fillMaxWidth(),
            onForward = {},
            onRewind = {},
            onPlayPause = {},
            isPlaying = true
        )
    }
}

@Preview
@Composable
private fun PreviewVideoPlayer() {
    val context = LocalContext.current
    QuicktrimandroidTheme {
        QuickTrimPlayer(
            modifier = Modifier
                .fillMaxWidth(),
            expandedMode = false,
            isMuted = false,
            isPlaying = true,
            progress = { 6000 },
            totalDuration = 60000,
            playerView = { View(context) },
            toggleMuteUnMute = {},
            onForward = {},
            onRewind = {},
            onPlayPause = {},
            toggleExpandMode = {}
        )
    }
}