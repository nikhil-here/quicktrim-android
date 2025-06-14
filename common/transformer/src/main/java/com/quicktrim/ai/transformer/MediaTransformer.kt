package com.quicktrim.ai.transformer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.quicktrim.ai.network.model.QuickTrimResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG = "MediaTransformer"

@OptIn(UnstableApi::class)
class MediaTransformer @Inject constructor(
    @ApplicationContext private val context: Context
) : QuickTrimTransformer {

    override suspend fun extractAudio(
        uri: Uri,
        onProgressUpdate: (Int) -> Unit
    ): QuickTrimResponse<QuickTrimTransformSuccess, QuickTrimTransformFailure> =
        suspendCancellableCoroutine { continuation ->
            Log.i(TAG, "extractAudio: uri $uri")
            //editing mediaItem extracting audio
            val inputMediaItem = MediaItem.fromUri(uri)
            val editedMediaItem = EditedMediaItem
                .Builder(inputMediaItem)
                .setRemoveVideo(true)
                .build()

            //creating output path
            val fileExtension = ".m4a"
            val fileName = SystemClock.elapsedRealtime().toString() + fileExtension
            val outputPath = context.cacheDir.path + "/" +  fileName
            createFile(fileName)

            Log.i(TAG, "extractAudio: outputPath $outputPath")

            //initializing transformer
            val transformer = Transformer.Builder(context)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(
                    object : Transformer.Listener {

                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult
                        ) {
                            Log.i(TAG, "extractAudio onCompleted: $exportResult")
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.Success(
                                        QuickTrimTransformSuccess(
                                            outputPath = outputPath,
                                            fileName = fileName
                                        )
                                    ),
                                    onCancellation = null
                                )
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            Log.i(TAG, "extractAudio onError: $exportException")
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.UnknownError(
                                        error = exportException
                                    ),
                                    onCancellation = null
                                )
                            }
                        }

                        override fun onFallbackApplied(
                            composition: Composition,
                            originalTransformationRequest: TransformationRequest,
                            fallbackTransformationRequest: TransformationRequest
                        ) {
                            Log.i(TAG, "extractAudio onFallbackApplied: ")
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.UnknownError(
                                        error = IllegalArgumentException("fallback required , switch to $fallbackTransformationRequest")
                                    ),
                                    onCancellation = null
                                )
                            }
                        }
                    }
                )
                .build()

            transformer.start(editedMediaItem, outputPath)

            //add progress listener
            val progressHolder = ProgressHolder()
            val handler = Handler(Looper.getMainLooper())
            setupProgressRunnable(
                handler = handler,
                progressHolder = progressHolder,
                transformer = transformer,
                delay = 500,
                onProgressUpdate = {
                    onProgressUpdate(it)
                }
            )

            //cancel transformer if coroutine is cancelled
            continuation.invokeOnCancellation { transformer.cancel() }
        }

    override suspend fun trimVideo(
        uri: Uri,
        removedSegments: Set<Pair<Double, Double>>,
        onProgressUpdate: (Int) -> Unit
    ): QuickTrimResponse<QuickTrimTransformSuccess, QuickTrimTransformFailure> =
        suspendCancellableCoroutine { continuation ->

            //extract the duration of original video
            val retriever = MediaMetadataRetriever().apply { setDataSource(context, uri) }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toDouble() ?: 0.00

            //compute keep intervals
            val sortedRemovals = removedSegments.sortedBy { it.first }
            val keepIntervals = mutableListOf<Pair<Double, Double>>()
            var lastEnd = 0.00
            for ((start, end) in sortedRemovals) {
                if (lastEnd < start) keepIntervals.add(Pair(lastEnd, start))
                lastEnd = end
            }
            if (lastEnd < duration) keepIntervals.add(Pair(lastEnd, duration))

            //create edited media item sequence
            val editedMediaItems = keepIntervals.map { (start, end) ->
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(start.toMs())
                            .setEndPositionMs(end.toMs())
                            .build()
                    )
                    .build()
                EditedMediaItem.Builder(mediaItem).build()
            }
            val editedMediaItemSequence = EditedMediaItemSequence(editedMediaItems)
            val composition = Composition.Builder(editedMediaItemSequence)
                .build()

            //creating output file for muxing
            val fileExtension = ".mp4"
            val fileName = SystemClock.elapsedRealtime().toString() + fileExtension
            val outputPath = context.cacheDir.path + "/" + fileName
            createFile(fileName)

            //initializing transformer
            val transformer = Transformer.Builder(context)
                .experimentalSetTrimOptimizationEnabled(true)
                .addListener(
                    object : Transformer.Listener {

                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.Success(
                                        QuickTrimTransformSuccess(
                                            outputPath = outputPath,
                                            fileName = fileName
                                        )
                                    ),
                                    onCancellation = null
                                )
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.UnknownError(
                                        error = exportException
                                    ),
                                    onCancellation = null
                                )
                            }
                        }

                        override fun onFallbackApplied(
                            composition: Composition,
                            originalTransformationRequest: TransformationRequest,
                            fallbackTransformationRequest: TransformationRequest
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    value = QuickTrimResponse.UnknownError(
                                        error = IllegalArgumentException("fallback required , switch to $fallbackTransformationRequest")
                                    ),
                                    onCancellation = null
                                )
                            }
                        }
                    }
                )
                .build()

            transformer.start(composition, outputPath)

            //add progress listener
            val progressHolder = ProgressHolder()
            val handler = Handler(Looper.getMainLooper())
            setupProgressRunnable(
                handler = handler,
                progressHolder = progressHolder,
                transformer = transformer,
                delay = 500,
                onProgressUpdate = {
                    onProgressUpdate(it)
                }
            )

            //cancel transformer if coroutine is cancelled
            continuation.invokeOnCancellation { transformer.cancel() }
        }

    private fun setupProgressRunnable(
        handler: Handler,
        progressHolder: ProgressHolder,
        transformer: Transformer,
        delay: Long,
        onProgressUpdate: (Int) -> Unit
    ) = object : Runnable {
        override fun run() {
            val state = transformer.getProgress(progressHolder)
            when (state) {
                Transformer.PROGRESS_STATE_AVAILABLE -> {
                    onProgressUpdate(progressHolder.progress * 100)
                    handler.postDelayed(this, delay)
                }

                Transformer.PROGRESS_STATE_NOT_STARTED -> {
                    handler.postDelayed(this, delay)
                }

                else -> {
                    //do nothing
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createFile(fileName: String): File {
        val file = File(context.cacheDir, fileName)
        if (file.exists() && !file.delete()) {
            throw IllegalStateException("Could not delete the previous export output file")
        }
        if (!file.createNewFile()) {
            throw IllegalStateException("Could not create the export output file")
        }
        return file
    }
}