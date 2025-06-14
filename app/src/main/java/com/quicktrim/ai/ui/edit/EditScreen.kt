package com.quicktrim.ai.ui.edit

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
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
fun EditScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val isMuted by mainViewModel.isMuted.collectAsStateWithLifecycle()
    val progress by mainViewModel.progress.collectAsStateWithLifecycle()
    val totalDuration by mainViewModel.totalDuration.collectAsStateWithLifecycle()
    val playerView by mainViewModel.playerView.collectAsStateWithLifecycle()
    val processState by mainViewModel.processState.collectAsStateWithLifecycle()
    val error by mainViewModel.error.collectAsStateWithLifecycle()
    val segmentJsonFormatResponse by mainViewModel.segmentedJsonFormatResponse.collectAsStateWithLifecycle()

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
            /**
             * Player
             */
            QuickTrimPlayer(
                modifier = Modifier.fillMaxWidth(),
                isMuted = isMuted,
                progress = progress,
                totalDuration = totalDuration,
                playerView = { playerView },
                toggleMuteUnMute = {
                    mainViewModel.toggleMuteUnMute()
                }
            )
            Spacer(Modifier.height(12.dp))

            /**
             * Process Indicator
             */
            QuickTrimProcessIndicator(
                modifier = Modifier,
                state = processState
            )
            Spacer(Modifier.height(12.dp))


            /**
             * Filler Words CTA
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate(Routes.UpdateFillerWords.path)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "add filler words"
                    )
                    Text("Filler Words")
                }
            }
            Spacer(Modifier.height(12.dp))

            /**
             * Segments
             */
            if (!segmentJsonFormatResponse.segmentResponses.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
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
                        Spacer(Modifier.height(6.dp))
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


        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 6.dp)
        ) {
            IconButton(
                onClick = {
                    mainViewModel.export()
                    navController.navigate(Routes.Export.path)
                },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    ), shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Export"
                )
            }
        }

    }
}