package com.quicktrim.ai.ui.common

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quicktrim.ai.network.model.SegmentedJsonFormatResponse
import com.quicktrim.ai.network.model.WordResponse

private const val TAG = "SegmentParagraph"

@Composable
fun SegmentParagraph(
    modifier: Modifier = Modifier,
    segmentedJsonFormatResponse: SegmentedJsonFormatResponse,
    progressMs: Long,
    onRemoveOrAddWord: (WordResponse) -> Unit
) {
    Log.i(TAG, "SegmentParagraph: progress $progressMs")
    val segments = segmentedJsonFormatResponse.segmentResponses

    val allWords = remember(segments) {
        segments.flatMap { segment -> segment.wordResponses.map { it.copy(isRemoved = it.isRemoved || segment.isRemoved) } }
    }

    val annotatedText = buildAnnotatedString {
        allWords.forEachIndexed { index, word ->
            val startMs = (word.start * 1000).toLong()
            val endMs = (word.end * 1000).toLong()

            val isHighlighted = progressMs in (startMs until endMs)

            pushStringAnnotation(tag = "WORD", annotation = index.toString())
            withStyle(
                style = if (word.isRemoved) {
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    SpanStyle(
                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        background = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                }

            ) {
                append(word.text)
            }
            pop()
        }
    }

    ClickableText(
        text = annotatedText,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        style = MaterialTheme.typography.headlineLarge,
        onClick = { offset ->
            Log.i(TAG, "SegmentParagraph: word offset $offset")
            annotatedText.getStringAnnotations("WORD", offset, offset)
                .firstOrNull()
                ?.let { annotation ->
                    val wordIndex = annotation.item.toInt()
                    val word = allWords[wordIndex]
                    Log.i(TAG, "SegmentParagraph: word $word")
                    onRemoveOrAddWord(word)
                }
        },
    )
}