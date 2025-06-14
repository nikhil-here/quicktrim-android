package com.quicktrim.transcription_api_demo.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.quicktrim.ai.network.model.SegmentResponse
import com.quicktrim.transcription_api_demo.DemoViewModel
import com.quicktrim.transcription_api_demo.domain.SegmentEntity

private const val TAG = "DemoScreen"

@OptIn(UnstableApi::class)
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    demoViewModel: DemoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mediaPicked = demoViewModel.mediaPicked.collectAsStateWithLifecycle()
    var playerView = demoViewModel.playerView.collectAsStateWithLifecycle()
    var transcription = demoViewModel.transcription.collectAsStateWithLifecycle()

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            demoViewModel.onMediaItemSelected(uri = uri)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        playerView.value?.let { view ->
            AndroidView(
                factory = {
                    view
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.4f)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (!mediaPicked.value) {
            Button(
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                }
            ) {
                Text("Upload File")
            }
        } else {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        demoViewModel.extract()
                    }
                ) {
                    Text("Transform")
                }

                Button(
                    onClick = {
                        demoViewModel.onCancel()
                    }
                ) {
                    Text("Cancel")
                }
            }

        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            items(transcription.value?.segments ?: emptyList()) { segment ->
                Segment(
                    segment = segment,
                    onRemove = {
                        demoViewModel.onRemoveSegment(it)
                    },
                    onAdd = {
                        demoViewModel.onAddSegment(it)
                    }
                )
            }
        }
    }
}

@Composable
fun Segment(
    modifier: Modifier = Modifier,
    segment: SegmentEntity,
    onRemove: (SegmentResponse) -> Unit,
    onAdd: (SegmentResponse) -> Unit
) {
    val segmentResponse = segment.segmentResponse
    val startTime = segmentResponse.wordResponses.minBy({ word -> word.start }).start
    val endTime = segmentResponse.wordResponses.maxBy({ word -> word.end }).end

    Row {
        Column(
            modifier = modifier
                .weight(1f)
                .padding(all = 6.dp),
        ) {
            Text(segmentResponse.text, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text("$startTime - $endTime", style = MaterialTheme.typography.bodySmall)
        }

        if (segment.isRemoved) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onAdd(segmentResponse)
                }
            ) {
                Text("ADD")
            }
        } else {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onRemove(segmentResponse)
                }
            ) {
                Text("REMOVE")
            }
        }

    }
}