package com.quicktrim.ai.ui.common


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

@Composable
fun QuickTrimErrorDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String?,
    ctaTitle: String,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "error",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = onClick
                ) {
                    Text(ctaTitle)
                }
            }

        }
    }
}

@Preview
@Composable
private fun PreviewQuickTrimErrorDialog() {
    QuicktrimandroidTheme {
        QuickTrimErrorDialog(
            modifier = Modifier.fillMaxSize(0.5f),
            title = "Unable to extract audio from given media file",
            message = "Restart the application and try again",
            ctaTitle = "Restart",
            onClick = {

            },
            onDismissRequest = {

            }
        )
    }
}