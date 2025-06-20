package com.quicktrim.ai.ui.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.common.QuickTrimErrorDialog
import com.quicktrim.ai.ui.common.QuickTrimPlayer
import com.quicktrim.ai.ui.common.QuickTrimProcessIndicator
import com.quicktrim.ai.ui.common.QuickTrimProcessState
import com.quicktrim.ai.ui.common.QuickTrimSuccessDialog
import kotlin.system.exitProcess

private const val TAG = "ExportScreen"

@Composable
fun ExportScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val isMuted by mainViewModel.isMuted.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val aspectRatio by mainViewModel.aspectRatio.collectAsStateWithLifecycle()
    val expandedMode by mainViewModel.expandedMode.collectAsStateWithLifecycle()
    val progress by mainViewModel.progress.collectAsStateWithLifecycle()
    val totalDuration by mainViewModel.totalDuration.collectAsStateWithLifecycle()
    val playerView by mainViewModel.playerView.collectAsStateWithLifecycle()
    val processState by mainViewModel.processState.collectAsStateWithLifecycle()
    val error by mainViewModel.error.collectAsStateWithLifecycle()

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

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

        when(processState) {
            is QuickTrimProcessState.Success -> {
                QuickTrimSuccessDialog(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    title = processState.processTitle,
                    message = processState.processMessage,
                    ctaTitle = "COPY PATH",
                    onClick = {
                        (processState as QuickTrimProcessState.Success).outputPath?.let {
                            copyToClipboard(
                                context = context,
                                label = "Trimmed Video Path",
                                text = it
                            )
                        }
                    }
                ) { }
            }
            else -> {}
        }




    }
}