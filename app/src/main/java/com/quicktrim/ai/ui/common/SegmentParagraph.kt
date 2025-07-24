package com.quicktrim.ai.ui.common

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.quicktrim.ai.data.remote.model.WordRemoteResponse
import com.quicktrim.ai.domain.model.SegmentedJsonFormat
import com.quicktrim.ai.domain.model.Word

private const val TAG = "SegmentParagraph"

@Composable
fun SegmentParagraph(
    modifier: Modifier = Modifier,
    segmentedJsonFormat: SegmentedJsonFormat,
    progressMs: Long,
    onRemoveOrAddWord: (Word) -> Unit
) {
    Log.i(TAG, "SegmentParagraph: progress $progressMs")
    val segments = segmentedJsonFormat.segments

    val allWords = remember(segments) {
        segments.flatMap { segment -> segment.words.map { it.copy(isRemoved = it.isRemoved || segment.isRemoved) } }
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
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        style = MaterialTheme.typography.displaySmall,
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