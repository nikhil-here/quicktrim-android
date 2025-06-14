package com.quicktrim.transcription_api_demo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.quicktrim.ai.network.TranscriptionRepository
import com.quicktrim.ai.network.model.QuickTrimResponse
import com.quicktrim.ai.network.model.SegmentResponse
import com.quicktrim.ai.network.model.SegmentedJsonFormatResponse
import com.quicktrim.ai.transformer.QuickTrimTransformSuccess
import com.quicktrim.ai.transformer.QuickTrimTransformer
import com.quicktrim.transcription_api_demo.domain.TranscriptionEntity
import com.quicktrim.transcription_api_demo.domain.toTranscriptionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ProcessStep(val text: String) {
    data class ExtractAudio(
        val progress: Int,
        val message: String
    ) : ProcessStep(message)

    data class GeneratingTranscription(
        val message: String
    ) : ProcessStep(message)

    data class Export(
        val progress: Int,
        val message: String
    ) : ProcessStep(message)
}

data class QuickTrimError(
    val title: String,
    val message: String
)

private const val TAG = "DemoViewModel"

@HiltViewModel
class DemoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transformer: QuickTrimTransformer,
    private val transcriptionRepository: TranscriptionRepository
) : ViewModel() {

    private val _mediaPicked = MutableStateFlow(false)
    val mediaPicked = _mediaPicked.asStateFlow()

    private val _playerView = MutableStateFlow<PlayerView?>(null)
    val playerView = _playerView.asStateFlow()

    private val _processSteps = MutableStateFlow<ProcessStep?>(null)
    val processSteps = _processSteps.asStateFlow()

    private val _error = MutableStateFlow<QuickTrimError?>(null)
    val error = _error.asStateFlow()

    private val _transcription = MutableStateFlow<TranscriptionEntity?>(null)
    val transcription = _transcription.asStateFlow()

    private var originalMediaUri: Uri? = null
    private var exoPlayer: ExoPlayer? = null

    fun onMediaItemSelected(uri: Uri) {
        Log.i(TAG, "onMediaItemSelected: $uri")
        _mediaPicked.update { true }

        //start playback of selected media item
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            originalMediaUri = uri
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = true
            prepare()
        }

        _playerView.update {
            PlayerView(context).apply {
                this.player = exoPlayer
            }
        }

        extractAudio(uri = uri)
    }

    private fun extractAudio(uri: Uri) {
        Log.i(TAG, "extractAudio: $uri")
        viewModelScope.launch {
            val extractionResponse = transformer.extractAudio(
                uri = uri,
                onProgressUpdate = { progress ->
                    _processSteps.update {
                        Log.i(TAG, "extractAudio: progress $progress")
                        ProcessStep.ExtractAudio(
                            progress = progress,
                            message = "Extracting Audio"
                        )
                    }
                }
            )
            Log.i(TAG, "extractAudio: response $extractionResponse")
            _processSteps.update { null }
            when (extractionResponse) {
                is QuickTrimResponse.Success<QuickTrimTransformSuccess> -> {
                    fetchTranscription(
                        audioFilePath = extractionResponse.body.outputPath
                    )
                }

                else -> {
                    _error.update {
                        QuickTrimError(
                            title = "Audio Extraction Failed",
                            message = Constants.GENERIC_ERROR_BODY
                        )
                    }
                }
            }
        }
    }

    private fun fetchTranscription(audioFilePath: String) {
        viewModelScope.launch {
            Log.i(TAG, "fetchTranscription: audioFilePath $audioFilePath")
            
            _processSteps.update {
                ProcessStep.GeneratingTranscription(
                    message = "Generating Transcription"
                )
            }

            try {
                val file = File(audioFilePath)
                val response = transcriptionRepository.speechToText(file)
                _processSteps.update { null }
                Log.i(TAG, "fetchTranscription: response $response")
                when (response) {
                    is QuickTrimResponse.Success<SegmentedJsonFormatResponse> -> {
                        _transcription.update {
                            response.body.toTranscriptionEntity()
                        }
                    }

                    else -> {
                        QuickTrimError(
                            title = "Unable to Load Transcription",
                            message = Constants.GENERIC_ERROR_BODY
                        )
                    }
                }
            } catch (e: Exception) {
                _error.update {
                    QuickTrimError(
                        title = "Unable to Load Extracted Audio",
                        message = Constants.GENERIC_ERROR_BODY
                    )
                }
            }
        }
    }

    fun onRemoveSegment(segmentResponse: SegmentResponse) {
        Log.i(TAG, "onRemoveSegment: $segmentResponse")
        _transcription.update { transcription ->
            transcription?.copy(
                segments = transcription.segments.map {
                    if (it.segmentResponse == segmentResponse) {
                        Log.i(TAG, "onRemoveSegment: found match")
                        it.copy(isRemoved = true)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun onAddSegment(segmentResponse: SegmentResponse) {
        Log.i(TAG, "onAddSegment: $segmentResponse")
        _transcription.update { transcription ->
            transcription?.copy(
                segments = transcription.segments.map {
                    if (it == segmentResponse) {
                        it.copy(isRemoved = false)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun extract() {
        Log.i(TAG, "extract: ")
        viewModelScope.launch {
            val removedSegments = _transcription.value?.segments
                ?.filter { it.isRemoved }
                ?.map {
                    Pair(
                        it.segmentResponse.wordResponses.minBy { word -> word.start }.start,
                        it.segmentResponse.wordResponses.maxBy { word -> word.end }.end,
                    )
                }?.toSet()

            Log.i(TAG, "extract: removedSegments $removedSegments")

            val result = transformer.trimVideo(
                uri = originalMediaUri ?: return@launch,
                removedSegments = removedSegments ?: emptySet(),
                onProgressUpdate = { progress ->
                    Log.i(TAG, "extract: progress $progress")
                    _processSteps.update {
                        ProcessStep.Export(
                            progress = progress,
                            message = "Exporting the composition"
                        )
                    }
                }
            )

            Log.i(TAG, "extract: result $result")

            _processSteps.update { null }

            when (result) {
                is QuickTrimResponse.Success<QuickTrimTransformSuccess> -> {
                    playMedia(result.body.outputPath)
                }

                else -> {
                    _error.update {
                        QuickTrimError(
                            title = "Error Exporting Media",
                            message = Constants.GENERIC_ERROR_BODY
                        )
                    }
                }
            }
        }
    }
    
    fun onCancel() {
        Log.i(TAG, "onCancel: ")
        _mediaPicked.update { false }
        _playerView.update { null }
        exoPlayer?.release()
        originalMediaUri = null
        _transcription.update { null }
    }


    private fun playMedia(path: String) {
        Log.i(TAG, "playMedia: path $path")
        val uri = path.toUri()
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.play()
    }


}