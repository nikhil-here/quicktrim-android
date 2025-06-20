package com.quicktrim.ai.ui.common

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
    var controlsVisible by remember { mutableStateOf(true) }
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
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.height(240.dp)
                            }
                        )
                        .background(MaterialTheme.colorScheme.scrim)
                        .aspectRatio(aspectRatio.coerceAtLeast(0f))
                        .align(Alignment.Center)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = controlsVisible, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    IconButton(
                        onClick = toggleMuteUnMute, modifier = Modifier.background(
                            MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "isMuted $isMuted",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
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
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(6.dp)
                )
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
                )
        )
    }
}

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
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onRewind, colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ), shape = RoundedCornerShape(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Replay5,
                contentDescription = stringResource(R.string.cd_rewind),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
        IconButton(
            modifier = Modifier.size(84.dp),
            onClick = onPlayPause,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(
                imageVector = playPauseIcon,
                contentDescription = stringResource(R.string.cd_play_pause),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        IconButton(
            onClick = onForward, colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ), shape = RoundedCornerShape(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Forward5,
                contentDescription = stringResource(R.string.cd_forward),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
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