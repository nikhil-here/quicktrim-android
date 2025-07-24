package com.quicktrim.ai.ui.edit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.ai.R
import com.quicktrim.ai.domain.model.SegmentedJsonFormat
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
import kotlinx.coroutines.flow.map
import kotlin.system.exitProcess

private const val TAG = "EditScreen"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditScreen(
    projectId: Long,
    modifier: Modifier = Modifier,
    navController: NavController,
    editViewModel: EditViewModel
) {
    val context = LocalContext.current
    val expandedMode by editViewModel.expandedMode.collectAsStateWithLifecycle()
    val isMuted by editViewModel.isMuted.collectAsStateWithLifecycle()
    val isPlaying by editViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by editViewModel.progress.collectAsStateWithLifecycle()
    val aspectRatio by editViewModel.aspectRatio.collectAsStateWithLifecycle()
    val totalDuration by editViewModel.totalDuration.collectAsStateWithLifecycle()
    val playerView by editViewModel.playerView.collectAsStateWithLifecycle()
    val processState by editViewModel.processState.collectAsStateWithLifecycle()
    val error by editViewModel.error.collectAsStateWithLifecycle()
    val transcriptionViewMode by editViewModel.transcriptionViewMode.collectAsStateWithLifecycle()
    val project by editViewModel.project.collectAsStateWithLifecycle()
    val segmentJsonFormatResponse by remember {
        derivedStateOf { project?.segmentedJsonFormat ?: SegmentedJsonFormat() }
    }
    val transcriptionLoaded by remember {
        derivedStateOf { segmentJsonFormatResponse.segments.isNotEmpty() }
    }
    val lazyListState = rememberLazyListState()

    Log.i(TAG, "EditScreen: segmentJsonFormatResponse $segmentJsonFormatResponse")

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(true) {
        editViewModel.initialize(projectId = projectId)
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
                    editViewModel.toggleMuteUnMute()
                },
                aspectRatio = aspectRatio,
                onRewind = {
                    editViewModel.onRewind()
                },
                onForward = {
                    editViewModel.onForward()
                },
                onPlayPause = {
                    editViewModel.onTogglePlayPause()
                },
                toggleExpandMode = {
                    editViewModel.toggleExpandMode()
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
            if (!segmentJsonFormatResponse.segments.isEmpty()) {
                when (transcriptionViewMode) {
                    TranscriptionViewMode.PARAGRAPH -> {
                        SegmentParagraph(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 56.dp, start = 8.dp, end = 8.dp),
                            segmentedJsonFormat = segmentJsonFormatResponse,
                            progressMs = progress,
                            onRemoveOrAddWord = {
                                editViewModel.onRemoveOrAddWordResponse(it)
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
                            items(segmentJsonFormatResponse.segments) { segment ->
                                SegmentRow(
                                    segment = segment,
                                    add = {
                                        editViewModel.add(it)
                                    },
                                    remove = {
                                        editViewModel.remove(it)
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
                    navController.popBackStack()
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
                                editViewModel.onTranscriptionViewModelChange(
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
                                editViewModel.onTranscriptionViewModelChange(
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
                        editViewModel.onExportSuccessDialogDismissRequest()
                    }
                )
            }

            else -> {}
        }

    }
}