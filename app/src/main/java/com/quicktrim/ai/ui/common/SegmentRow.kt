package com.quicktrim.ai.ui.common

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quicktrim.ai.network.model.SegmentResponse
import com.quicktrim.ai.network.model.SegmentedJsonFormatResponse
import com.quicktrim.ai.network.model.WordResponse
import com.quicktrim.ai.toHMS
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

@Composable
fun SegmentRow(
    modifier: Modifier = Modifier,
    segmentResponse: SegmentResponse,
    add: (SegmentResponse) -> Unit,
    remove: (SegmentResponse) -> Unit
) {
    val start = remember(segmentResponse) {
        segmentResponse.wordResponses.minBy { it.start }.start.toHMS()
    }

    val end = remember(segmentResponse) {
        segmentResponse.wordResponses.maxBy { it.end }.end.toHMS()
    }

    val annotatedText = buildAnnotatedString {
        segmentResponse.wordResponses.forEach {
            withStyle(
                style = if (it.isRemoved || segmentResponse.isRemoved) {
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    SpanStyle(textDecoration = TextDecoration.None)
                },
            ) {
                append(it.text)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(6.dp)
                .weight(2f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$start - $end",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Box(modifier = Modifier.fillMaxWidth(0.2f), contentAlignment = Alignment.Center) {
            if (segmentResponse.isRemoved) {
                IconButton(
                    onClick = {
                        add(segmentResponse)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Segment",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        remove(segmentResponse)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Remove Segment",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSegmentRow() {
    QuicktrimandroidTheme {
        SegmentRow(
            segmentResponse = SegmentResponse(
                text = "hi its nikhiel",
                wordResponses = listOf(
                    WordResponse(
                        start = 0.00,
                        end = 0.00,
                        logprob = 0.11,
                        speakerId = "",
                        text = "hi",
                        type = "word",
                        isRemoved = false
                    ),
                    WordResponse(
                        start = 0.00,
                        end = 0.00,
                        logprob = 0.11,
                        speakerId = "",
                        text = " ",
                        type = "word",
                        isRemoved = false
                    ),
                    WordResponse(
                        start = 0.00,
                        end = 0.00,
                        logprob = 0.11,
                        speakerId = "",
                        text = "its",
                        type = "",
                        isRemoved = false
                    ),
                    WordResponse(
                        start = 0.00,
                        end = 0.00,
                        logprob = 0.11,
                        speakerId = "",
                        text = " ",
                        type = "",
                        isRemoved = false
                    ),
                    WordResponse(
                        start = 0.00,
                        end = 0.00,
                        logprob = 0.11,
                        speakerId = "nikhiel",
                        text = "nikhiel",
                        type = "",
                        isRemoved = true
                    )
                ),
            ),
            add = {

            },
            remove = {

            }
        )
    }
}


@Preview
@Composable
private fun PreviewRemovedSegmentRow() {
    QuicktrimandroidTheme {
        SegmentRow(
            segmentResponse = Constants.PREVIEW_SEGMENT,
            add = {

            },
            remove = {

            }
        )
    }
}

@Preview
@Composable
private fun PreviewSegmentRowList() {
    val segment = Constants.PREVIEW_SEGMENT
    val segments = SegmentedJsonFormatResponse(
        segmentResponses = (0..5).map { segment }
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(bottom = 56.dp),
    ) {
        items(segments.segmentResponses) { segment ->
            SegmentRow(
                segmentResponse = segment,
                add = {

                },
                remove = {

                }
            )
            HorizontalDivider()
        }
    }
}