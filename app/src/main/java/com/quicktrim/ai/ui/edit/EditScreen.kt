package com.quicktrim.ai.ui.edit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.MainViewModel
import com.quicktrim.ai.R
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.common.QuickTrimErrorDialog
import com.quicktrim.ai.ui.common.QuickTrimPlayer
import com.quicktrim.ai.ui.common.QuickTrimProcessIndicator
import com.quicktrim.ai.ui.common.QuickTrimProcessState
import com.quicktrim.ai.ui.common.QuickTrimSuccessDialog
import com.quicktrim.ai.ui.common.SegmentParagraph
import com.quicktrim.ai.ui.common.SegmentRow
import com.quicktrim.ai.ui.common.TranscriptionViewMode
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val expandedMode by mainViewModel.expandedMode.collectAsStateWithLifecycle()
    val isMuted by mainViewModel.isMuted.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by mainViewModel.progress.collectAsStateWithLifecycle()
    val aspectRatio by mainViewModel.aspectRatio.collectAsStateWithLifecycle()
    val totalDuration by mainViewModel.totalDuration.collectAsStateWithLifecycle()
    val playerView by mainViewModel.playerView.collectAsStateWithLifecycle()
    val processState by mainViewModel.processState.collectAsStateWithLifecycle()
    val error by mainViewModel.error.collectAsStateWithLifecycle()
    val transcriptionViewMode by mainViewModel.transcriptionViewMode.collectAsStateWithLifecycle()
    val segmentJsonFormatResponse by mainViewModel.segmentedJsonFormatResponse.collectAsStateWithLifecycle()
    val transcriptionLoaded by remember {
        derivedStateOf { segmentJsonFormatResponse.segmentResponses.isNotEmpty() }
    }
    val lazyListState = rememberLazyListState()

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            /**
             * Player
             */
            QuickTrimPlayer(
                modifier = Modifier.fillMaxWidth(),
                isMuted = isMuted,
                expandedMode = expandedMode,
                isPlaying = isPlaying,
                progress = { progress },
                totalDuration = totalDuration,
                playerView = { playerView },
                toggleMuteUnMute = {
                    mainViewModel.toggleMuteUnMute()
                },
                aspectRatio = aspectRatio,
                onRewind = {
                    mainViewModel.onRewind()
                },
                onForward = {
                    mainViewModel.onForward()
                },
                onPlayPause = {
                    mainViewModel.onTogglePlayPause()
                },
                toggleExpandMode = {
                    mainViewModel.toggleExpandMode()
                }
            )
            /**
             * Process Indicator
             */
            QuickTrimProcessIndicator(
                modifier = Modifier.fillMaxWidth(),
                state = processState
            )
            /**
             * Segments
             */
            if (!segmentJsonFormatResponse.segmentResponses.isEmpty()) {
                when (transcriptionViewMode) {
                    TranscriptionViewMode.PARAGRAPH -> {
                        SegmentParagraph(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 56.dp, start = 8.dp, end = 8.dp),
                            segmentedJsonFormatResponse = segmentJsonFormatResponse,
                            progressMs = progress,
                            onRemoveOrAddWord = {
                                mainViewModel.onRemoveOrAddWordResponse(it)
                            },
                        )
                    }

                    TranscriptionViewMode.SEGMENT -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(bottom = 56.dp),
                            state = lazyListState
                        ) {
                            items(segmentJsonFormatResponse.segmentResponses) { segment ->
                                SegmentRow(
                                    segmentResponse = segment,
                                    add = {
                                        mainViewModel.add(it)
                                    },
                                    remove = {
                                        mainViewModel.remove(it)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
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

        if (transcriptionLoaded && !expandedMode && !lazyListState.isScrollInProgress) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalFloatingToolbar(
                    modifier = Modifier
                        .height(48.dp),
                    expanded = true,
                ) {
                    ToggleButton(
                        modifier = Modifier,
                        checked = transcriptionViewMode == TranscriptionViewMode.PARAGRAPH,
                        onCheckedChange = {},
                        shapes = ToggleButtonDefaults.shapes(
                            CircleShape,
                            CircleShape,
                            CircleShape
                        )
                    ) {
                        IconButton(
                            onClick = {
                                mainViewModel.onTranscriptionViewModelChange(
                                    TranscriptionViewMode.PARAGRAPH
                                )
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinearScale,
                                contentDescription = stringResource(R.string.cd_filler_words),
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    ToggleButton(
                        modifier = Modifier,
                        checked = transcriptionViewMode == TranscriptionViewMode.SEGMENT,
                        onCheckedChange = {},
                        shapes = ToggleButtonDefaults.shapes(
                            CircleShape,
                            CircleShape,
                            CircleShape
                        )
                    ) {
                        IconButton(
                            onClick = {
                                mainViewModel.onTranscriptionViewModelChange(
                                    TranscriptionViewMode.SEGMENT
                                )
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LineWeight,
                                contentDescription = stringResource(R.string.cd_filler_words),
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.UpdateFillerWords.path)
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_filler_words),
                    )
                }
            }
        }

        when (processState) {
            is QuickTrimProcessState.Success -> {
                QuickTrimSuccessDialog(
                    modifier = Modifier.fillMaxWidth(),
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
                    },
                    onDismissRequest = {
                        mainViewModel.onExportSuccessDialogDismissRequest()
                    }
                )
            }

            else -> {}
        }

    }
}