package com.quicktrim.ai.ui.common

import androidx.media3.common.C
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MultiMediaSeekbarController(
    private val player: ExoPlayer,
    private val onProgressUpdate: (progressMs: Long, totalDuration: Long) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    fun start() {
        progressJob?.cancel()
        progressJob = scope.launch {
             while (isActive) {
                val totalDuration = getTotalDuration()
                val currentProgress = getCurrentCumulativePosition()
                onProgressUpdate(currentProgress, totalDuration)
                delay(50)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
    }

    private fun getTotalDuration(): Long {
        var total = 0L
        for (i in 0 until player.mediaItemCount) {
            val duration = player.getDurationForMediaItemIndex(i)
            if (duration != C.TIME_UNSET) {
                total += duration
            }
        }
        return total
    }

    private fun getCurrentCumulativePosition(): Long {
        val currentIndex = player.currentMediaItemIndex
        var cumulative = 0L

        for (i in 0 until currentIndex) {
            val duration = player.getDurationForMediaItemIndex(i)
            if (duration != C.TIME_UNSET) {
                cumulative += duration
            }
        }

        return cumulative + player.currentPosition
    }

    private fun ExoPlayer.getDurationForMediaItemIndex(index: Int): Long {
        val timeline = this.currentTimeline
        return if (!timeline.isEmpty && index < timeline.windowCount) {
            val window = Timeline.Window()
            timeline.getWindow(index, window)
            window.durationMs
        } else {
            C.TIME_UNSET
        }
    }
}