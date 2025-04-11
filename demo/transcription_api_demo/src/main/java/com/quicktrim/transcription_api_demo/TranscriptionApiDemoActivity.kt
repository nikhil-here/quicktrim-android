package com.quicktrim.transcription_api_demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.quicktrim.ai.network.model.TranscriptionNetworkResponse
import com.quicktrim.ai.network.model.TranscriptionRepository
import com.quicktrim.ai.utility.FileUtility
import com.quicktrim.transcription_api_demo.ui.theme.QuicktrimandroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class TranscriptionApiDemoActivity : ComponentActivity() {

    @Inject
    lateinit var transcriptionRepository: TranscriptionRepository

    companion object {
        private const val TAG = "TranscriptionApiDemoAct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuicktrimandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var response by remember { mutableStateOf<String>("Trigger Transcribe Request to see result") }

                        var isLoading by remember { mutableStateOf(false) }

                        response.let {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .fillMaxHeight(0.5f)
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()),
                                text = it.toString(),
                                textAlign = TextAlign.Left
                            )
                        }

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            modifier = Modifier,
                            onClick = {
                                isLoading = true
                                transcribe(
                                    onSuccess = {
                                        response = it.transcriptions.joinToString(separator = "\n\n") {
                                            "${it.start} --> ${it.end}\n${it.transcription}"
                                        }
                                        isLoading = false
                                    },
                                    onError = {
                                        response = it
                                        isLoading = false
                                    }
                                )
                            }
                        ) {
                            Text("Transcribe")
                        }
                    }
                }
            }
        }
    }

    private fun transcribe(
        onSuccess: (TranscriptionNetworkResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fileFromRaw: File? = FileUtility.getFileFromRaw(
                    context = this@TranscriptionApiDemoActivity,
                    resId = R.raw.input,
                    fileName = "input.m4a"
                )
                if (fileFromRaw == null) {
                    onError("Unable to generate res file")
                    return@launch
                }
                val response = transcriptionRepository.transcribe(audioFile = fileFromRaw)
                onSuccess(response)
            } catch (e: Exception) {
                onError(e.message ?: "Unexpected Error")
            }
        }
    }
}