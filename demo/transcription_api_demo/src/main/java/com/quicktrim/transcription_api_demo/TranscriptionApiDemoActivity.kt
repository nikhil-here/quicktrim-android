package com.quicktrim.transcription_api_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.quicktrim.transcription_api_demo.ui.DemoScreen
import com.quicktrim.transcription_api_demo.ui.theme.QuicktrimandroidTheme
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class TranscriptionApiDemoActivity : ComponentActivity() {

    companion object {
        private const val TAG = "TranscriptionApiDemoAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuicktrimandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                    )
                }
            }
        }
    }
}