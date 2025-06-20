package com.quicktrim.ai.ui.common


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quicktrim.ai.R
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

@Composable
fun QuickTrimSuccessDialog(
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().align (Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    painter = painterResource(R.drawable.quick_trim),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onClick
                ) {
                    Text(ctaTitle)
                }
                Spacer(Modifier.height(16.dp))
            }

        }
    }
}

@Preview
@Composable
private fun PreviewQuickTrimSuccessDialog() {
    QuicktrimandroidTheme {
        QuickTrimSuccessDialog(
            modifier = Modifier,
            title = "Video Saved",
            message = "trimmed video exported successfully",
            ctaTitle = "OPEN",
            onClick = {},
            onDismissRequest = {

            }
        )
    }
}