package com.quicktrim.ai.ui.edit

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.quicktrim.ai.domain.model.Project
import com.quicktrim.ai.domain.model.QuickTrimResponse
import com.quicktrim.ai.domain.model.Segment
import com.quicktrim.ai.domain.model.SegmentedJsonFormat
import com.quicktrim.ai.domain.model.Word
import com.quicktrim.ai.domain.repository.ProjectRepository
import com.quicktrim.ai.domain.repository.TranscriptionRepository
import com.quicktrim.ai.domain.transformer.QuickTrimTransformSuccess
import com.quicktrim.ai.domain.transformer.QuickTrimTransformer
import com.quicktrim.ai.domain.transformer.toMs
import com.quicktrim.ai.normalizeAndCompare
import com.quicktrim.ai.ui.Constants
import com.quicktrim.ai.ui.common.MultiMediaSeekbarController
import com.quicktrim.ai.ui.common.QuickTrimProcessState
import com.quicktrim.ai.ui.common.TranscriptionViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class EditScreenSideEffects {

}

private const val TAG = "EditViewModel"

@HiltViewModel
class EditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProjectRepository,
    private val transformer: QuickTrimTransformer,
    private val transcriptionRepository: TranscriptionRepository
) : ViewModel() {

    var _projectId: Long = -1

    private val _processState = MutableStateFlow<QuickTrimProcessState>(QuickTrimProcessState.None)
    val processState = _processState.asStateFlow()

    private val _error = MutableStateFlow<QuickTrimResponse.UnknownError?>(null)
    val error = _error.asStateFlow()

    private val _expandedMode = MutableStateFlow(true)
    val expandedMode = _expandedMode.asStateFlow()

    private val _project = MutableStateFlow<Project?>(null)
    val project = _project.asStateFlow()

    /**
     * Player States
     */
    private var exoPlayer: ExoPlayer? = null

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration = _totalDuration.asStateFlow()

    private val _playerView = MutableStateFlow<PlayerView?>(null)
    val playerView = _playerView.asStateFlow()

    private val _aspectRatio = MutableStateFlow<Float>(9 / 16f)
    val aspectRatio = _aspectRatio.asStateFlow()

    private val _transcriptionViewMode = MutableStateFlow(TranscriptionViewMode.PARAGRAPH)
    val transcriptionViewMode = _transcriptionViewMode.asStateFlow()

    private val _fillerWords = MutableStateFlow(setOf<String>())
    val fillerWords = _fillerWords.asStateFlow()

    private lateinit var originalMediaUri: Uri
    var originalMediaDuration: Long? = null
    private var controller: MultiMediaSeekbarController? = null

    private val onProgressUpdate = { progress: Long, duration: Long ->
        _progress.update { progress }
        _totalDuration.update { duration }
    }

    private val playerListener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            Log.i(TAG, "onVideoSizeChanged: videoSize $videoSize")
            if (videoSize.width == 0 || videoSize.height == 0) return
            val ratio = (videoSize.width / videoSize.height.toFloat()).coerceAtLeast(0.1f)
            Log.i(TAG, "onVideoSizeChanged aspectRatio: $ratio")
            _aspectRatio.update { ratio }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (originalMediaDuration == null) {
                originalMediaDuration = exoPlayer?.duration ?: 0L
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.update { isPlaying }
        }
    }

    fun initialize(projectId: Long) {
        viewModelScope.launch {
            _projectId = projectId
            val project = repository.getProjectById(projectId).firstOrNull() ?: return@launch
            _project.update { project }
            originalMediaUri = project.originalMediaUrl.toUri()
            initializePlayer(project)
            initObservers()
            val isTranscriptionGenerated = project.segmentedJsonFormat != null
            if (!isTranscriptionGenerated) {
                generateTranscription(project)
            } else {
                _expandedMode.update { false }
            }
        }
    }

    fun initObservers() {
        viewModelScope.launch {
            launch {
                Log.i(TAG, "initObservers: called with $_projectId")
                repository.getProjectById(_projectId).collectLatest { projectUpdate ->
                    Log.i(TAG, "initObservers: projectUpdate $projectUpdate")
                    _project.update { projectUpdate }
                    playTrimVideoPreview()
                }
            }
        }
    }

    suspend fun initializePlayer(project: Project) = withContext(Dispatchers.Main) {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
            addListener(playerListener)
        }
        controller = exoPlayer?.let {
            MultiMediaSeekbarController(
                player = it,
                onProgressUpdate = onProgressUpdate
            )
        }
        _isMuted.update { exoPlayer?.volume == 0f }
        _playerView.update {
            PlayerView(context).apply {
                this.player = exoPlayer
                useController = false
            }
        }
    }

    private suspend fun generateTranscription(project: Project) {
        val audioExtracted = project.extractedAudioUrl != null
        if (!audioExtracted) {
            extractAudio(uri = project.originalMediaUrl.toUri())
        } else {
            fetchTranscription(audioFilePath = project.extractedAudioUrl)
        }
    }

    private suspend fun extractAudio(uri: Uri) {
        Log.i(TAG, "extractAudio: $uri")
        _processState.update {
            QuickTrimProcessState.Indefinite(
                title = "Extracting Audio",
                message = Constants.GENERIC_WAIT_MSG
            )
        }
        val extractionResponse = transformer.extractAudio(
            uri = uri,
            onProgressUpdate = { progress ->
                _processState.update {
                    Log.i(TAG, "extractAudio: progress $progress")
                    QuickTrimProcessState.Definite(
                        progress = progress.toFloat(),
                        title = "Extracting Audio",
                        message = Constants.GENERIC_WAIT_MSG
                    )
                }
            }
        )
        Log.i(TAG, "extractAudio: response $extractionResponse")
        _processState.update { QuickTrimProcessState.None }

        when (extractionResponse) {
            is QuickTrimResponse.Success<QuickTrimTransformSuccess> -> {
                val extractedAudioUrl = extractionResponse.body.outputPath
                repository.updateExtractedAudioUrl(
                    projectId = _projectId,
                    extractedAudioUrl = extractedAudioUrl
                )
                fetchTranscription(extractionResponse.body.outputPath)
            }

            else -> {
                _error.update {
                    QuickTrimResponse.UnknownError(
                        IllegalStateException(
                            "unable to extract the audio from given media file, please restart the application"
                        )
                    )
                }
            }
        }
    }

    private fun fetchTranscription(audioFilePath: String) {
        viewModelScope.launch {
            Log.i(TAG, "fetchTranscription: audioFilePath $audioFilePath")

            _processState.update {
                QuickTrimProcessState.Indefinite(
                    title = "Generating Transcription",
                    message = null
                )
            }

            try {
                val file = File(audioFilePath)
                val response = transcriptionRepository.speechToText(file)
                _processState.update { QuickTrimProcessState.None }
                Log.i(TAG, "fetchTranscription: response $response")
                when (response) {
                    is QuickTrimResponse.Success<SegmentedJsonFormat> -> {
                        _expandedMode.update { false }
                        repository.updateSegmentedJsonFormat(
                            projectId = _projectId,
                            segmentedJsonFormat = response.body
                        )
                    }

                    is QuickTrimResponse.UnknownError -> {
                        _error.update {
                            QuickTrimResponse.UnknownError(
                                response.error
                            )
                        }
                    }

                    else -> {
                        _error.update {
                            QuickTrimResponse.UnknownError(
                                IllegalStateException(
                                    "unable to generate transcription, please restart the application"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchTranscription: ", e)
                _error.update {
                    QuickTrimResponse.UnknownError(
                        IllegalStateException(
                            "unable to generate transcription, please restart the application ${e.message}"
                        )
                    )
                }
            }
        }
    }

    fun setMediaItem(mediaItem: List<MediaItem>) {
        exoPlayer?.setMediaItems(mediaItem)
        exoPlayer?.prepare()
        controller?.start()
    }

    fun toggleMuteUnMute() {
        Log.i(TAG, "toggleMuteUnMute: current volume ${exoPlayer?.volume}")
        if (exoPlayer?.volume == 0f) {
            exoPlayer?.volume = 1f
            _isMuted.update { false }
        } else {
            exoPlayer?.volume = 0f
            _isMuted.update { true }
        }
    }

    fun onForward() {
        val currentPosition = exoPlayer?.currentPosition ?: 0L
        val duration = exoPlayer?.duration ?: C.TIME_UNSET
        val target = minOf(currentPosition + 5000L, duration)
        exoPlayer?.seekTo(target)
    }

    fun onRewind() {
        val currentPosition = exoPlayer?.currentPosition ?: 0L
        val target = maxOf(currentPosition - 5000L, 0L)
        exoPlayer?.seekTo(target)
    }

    fun onTogglePlayPause() {
        Log.i(TAG, "toggleMuteUnMute: current volume ${exoPlayer?.volume}")
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        } else {
            exoPlayer?.play()
        }
    }

    fun toggleExpandMode() {
        _expandedMode.update { !it }
    }

    fun add(addSegment: Segment) = viewModelScope.launch {
        _project.update { updatedProject ->
            updatedProject?.copy(
                segmentedJsonFormat = updatedProject.segmentedJsonFormat?.copy(
                    segments = updatedProject.segmentedJsonFormat.segments.map { segment ->
                        if (segment == addSegment) {
                            segment.copy(isRemoved = false)
                        } else {
                            segment
                        }
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun remove(addSegment: Segment) {
        _project.update { updatedProject ->
            updatedProject?.copy(
                segmentedJsonFormat = updatedProject.segmentedJsonFormat?.copy(
                    segments = updatedProject.segmentedJsonFormat.segments.map { segment ->
                        if (segment == addSegment) {
                            segment.copy(isRemoved = true)
                        } else {
                            segment
                        }
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun export() {
        viewModelScope.launch {
            _processState.update {
                QuickTrimProcessState.Indefinite(
                    title = "Trimming Video",
                    message = Constants.GENERIC_WAIT_MSG
                )
            }

            _expandedMode.update { false }

            val removedSegmentsAndWords = getRemovedSegments()

            val result = transformer.trimVideo(
                uri = originalMediaUri ?: return@launch,
                removedSegments = removedSegmentsAndWords,
                onProgressUpdate = { progress ->
                    Log.i(TAG, "extract: progress $progress")
                    _processState.update {
                        QuickTrimProcessState.Definite(
                            progress = progress.toFloat(),
                            title = "Trimming Video",
                            message = Constants.GENERIC_WAIT_MSG
                        )
                    }
                }
            )

            _processState.update { QuickTrimProcessState.None }

            when (result) {
                is QuickTrimResponse.Success<QuickTrimTransformSuccess> -> {
                    setMediaItem(listOf(MediaItem.fromUri(result.body.outputPath)))
                    _processState.update {
                        QuickTrimProcessState.Success(
                            title = Constants.EXPORT_SUCCESS_TITLE,
                            message = "${Constants.EXPORT_SUCCESS_DESCRIPTION} \n${result.body.outputPath}",
                            outputPath = result.body.outputPath
                        )
                    }
                }

                else -> {
                    _error.update {
                        QuickTrimResponse.UnknownError(
                            IllegalStateException(
                                "unable to trim or export the video"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getRemovedSegments(): Set<Pair<Double, Double>> {
        val removedSegmentsAndWords = mutableSetOf<Pair<Double, Double>>()
        (_project.value?.segmentedJsonFormat?.segments ?: emptyList())
            .forEach { segment ->
                if (segment.isRemoved) {
                    removedSegmentsAndWords.add(
                        Pair(
                            segment.words.minBy { word -> word.start }.start,
                            segment.words.maxBy { word -> word.end }.end,
                        )
                    )
                } else {
                    segment.words.forEach { word ->
                        if (word.isRemoved) {
                            Log.i(TAG, "getRemovedSegments: found removed word $word")
                            removedSegmentsAndWords.add(Pair(word.start, word.end))
                        }
                    }
                }
            }
        return removedSegmentsAndWords
    }

    suspend fun playTrimVideoPreview() = withContext(Dispatchers.Main) {
        //extract the duration of original video
        Log.i(TAG, "playTrimVideoPreview: called $originalMediaUri")
        val retriever = MediaMetadataRetriever().apply { setDataSource(context, originalMediaUri) }
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toDouble()
                ?: 0.00

        //compute keep intervals
        val removedSegments = getRemovedSegments()
        val mediaItemList = if (removedSegments.isEmpty()) {
            listOf(MediaItem.fromUri(originalMediaUri))
        } else {
            val sortedRemovals = removedSegments.sortedBy { it.first }
            val keepIntervals = mutableListOf<Pair<Double, Double>>()
            var lastEnd = 0.00
            for ((start, end) in sortedRemovals) {
                if (lastEnd < start) keepIntervals.add(Pair(lastEnd, start))
                lastEnd = end
            }
            if (lastEnd < _totalDuration.value) keepIntervals.add(Pair(lastEnd, duration))
            //create edited media item sequence
            keepIntervals.map { (start, end) ->
                val mediaItem = MediaItem.Builder()
                    .setUri(originalMediaUri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(start.toMs())
                            .setEndPositionMs(end.toMs())
                            .build()
                    )
                    .build()
                mediaItem
            }
        }
        setMediaItem(mediaItemList)
    }

    fun onRemoveFillerWord(fillerWord: String) {
        _fillerWords.update {
            val updatedList = it.toMutableSet().apply {
                remove(fillerWord)
            }
            updatedList
        }
        _project.update { project ->
            project?.copy(
                segmentedJsonFormat = project.segmentedJsonFormat?.copy(
                    segments = project.segmentedJsonFormat.segments.map { segment ->
                        segment.copy(
                            words = segment.words.map { word ->
                                if (normalizeAndCompare(word.text, fillerWord)) {
                                    word.copy(isRemoved = false)
                                } else {
                                    word
                                }
                            }
                        )
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun onRemoveOrAddWordResponse(removedWord: Word) {
        _project.update { updatedProject ->
            updatedProject?.copy(
                segmentedJsonFormat = updatedProject.segmentedJsonFormat?.copy(
                    segments = updatedProject.segmentedJsonFormat.segments.map { segment ->
                        segment.copy(
                            words = segment.words.map { word ->
                                if (word == removedWord) {
                                    word.copy(isRemoved = !word.isRemoved)
                                } else {
                                    word
                                }
                            }
                        )
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun onAddWordResponse(removedWord: Word) {
        _project.update { updatedProject ->
            updatedProject?.copy(
                segmentedJsonFormat = updatedProject.segmentedJsonFormat?.copy(
                    segments = updatedProject.segmentedJsonFormat.segments.map { segment ->
                        segment.copy(
                            words = segment.words.map { word ->
                                if (word == removedWord) {
                                    word.copy(isRemoved = false)
                                } else {
                                    word
                                }
                            }
                        )
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun onAddFillerWord(fillerWord: String) {
        Log.i(TAG, "onAddFillerWord: $fillerWord")
        _fillerWords.update {
            val updatedList = it.toMutableSet().apply {
                add(fillerWord)
            }
            updatedList
        }
        _project.update { updatedProject ->
            updatedProject?.copy(
                segmentedJsonFormat = updatedProject.segmentedJsonFormat?.copy(
                    segments = updatedProject.segmentedJsonFormat.segments.map { segment ->
                        segment.copy(
                            words = segment.words.map { word ->
                                if (normalizeAndCompare(word.text, fillerWord)) {
                                    Log.i(TAG, "onAddFillerWord: match found")
                                    word.copy(isRemoved = true)
                                } else {
                                    word
                                }
                            }
                        )
                    }
                )
            )
        }
        syncSegmentedJsonFormatDb()
    }

    fun syncSegmentedJsonFormatDb() {
        viewModelScope.launch {
            repository.updateSegmentedJsonFormat(
                projectId = _projectId,
                segmentedJsonFormat = _project.value?.segmentedJsonFormat
            )
        }
    }

    fun onExportSuccessDialogDismissRequest() {
        _processState.update { QuickTrimProcessState.None }
    }

    fun onTranscriptionViewModelChange(mode: TranscriptionViewMode) {
        _transcriptionViewMode.update { mode }
    }

    fun onExportScreenBackClick() {
        _processState.update { QuickTrimProcessState.None }
    }

    override fun onCleared() {
        exoPlayer?.release()
        _playerView.update { null }
    }


}