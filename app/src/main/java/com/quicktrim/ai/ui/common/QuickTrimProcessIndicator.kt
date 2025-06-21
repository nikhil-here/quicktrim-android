package com.quicktrim.ai.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

sealed class QuickTrimProcessState(
    val processTitle: String,
    val processMessage: String? = null,
) {
    data class Indefinite(
        val title: String,
        val message: String?
    ) : QuickTrimProcessState(title, message)

    data class Definite(
        val progress: Float,
        val title: String,
        val message: String?
    ) : QuickTrimProcessState(title, message)

    data class Success(
        val title: String,
        val message: String?,
        val outputPath: String? = null
    ) : QuickTrimProcessState(title, message)

    data object None : QuickTrimProcessState("", "")
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickTrimProcessIndicator(
    modifier: Modifier = Modifier,
    state: QuickTrimProcessState
) {
    if (state != QuickTrimProcessState.None) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LoadingIndicator(
                polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = state.processTitle.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (!state.processMessage.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.processMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Preview
@Composable
fun PreviewQuickTrimProcessState(modifier: Modifier = Modifier) {
    QuickTrimProcessIndicator(
        state = QuickTrimProcessState.Definite(
            0.3f,
            "Extracting Audio",
            "this might take some time"
        )
    )
}

@Preview
@Composable
fun PreviewQuickTrimProcessStateIndefinite(modifier: Modifier = Modifier) {
    QuickTrimProcessIndicator(
        state = QuickTrimProcessState.Indefinite(
            "Generating Transcription",
            "this might take some time"
        )
    )
}


@Preview
@Composable
fun PreviewQuickTrimProcessStateSuccess(modifier: Modifier = Modifier) {
    QuicktrimandroidTheme {
        QuickTrimProcessIndicator(
            state = QuickTrimProcessState.Success(
                "Trimming completed",
                "trim video is saved on your device"
            )
        )
    }

}