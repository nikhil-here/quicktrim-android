package com.quicktrim.ai.ui.common

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.quicktrim.ai.toTimeString

@Composable
fun QuickTrimPlayer(
    modifier: Modifier = Modifier,
    isMuted: Boolean,
    progress: Long,
    totalDuration: Long,
    playerView: () -> View?,
    toggleMuteUnMute: () -> Unit
) {

    Column(
        modifier = modifier,
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
                    },
                    modifier = Modifier
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.scrim)
                        .aspectRatio(9 / 16f)
                )
            }

            IconButton(
                onClick = toggleMuteUnMute,
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "isMuted $isMuted",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                progress.toFloat() / totalDuration.toFloat()
            },
            modifier = Modifier.fillMaxWidth(0.6f),
            strokeCap = StrokeCap.Square,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progress.toTimeString(),
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = totalDuration.toTimeString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


@Preview
@Composable
private fun PreviewVideoPlayer() {
    val context = LocalContext.current
    QuickTrimPlayer(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        isMuted = false,
        progress = 6000,
        totalDuration = 60000,
        playerView = { View(context) },
        toggleMuteUnMute = {

        }
    )
}