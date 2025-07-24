package com.quicktrim.ai.domain.transformer

import android.net.Uri
import com.quicktrim.ai.domain.model.QuickTrimResponse

interface QuickTrimTransformer {

    /**
     * Takes the uri of media file and extract the audio and returns response
     */
    suspend fun extractAudio(
        uri: Uri,
        onProgressUpdate: (Int) -> Unit
    ): QuickTrimResponse<QuickTrimTransformSuccess, QuickTrimTransformFailure>

    suspend fun trimVideo(
        uri: Uri,
        removedSegments: Set<Pair<Double, Double>>,
        onProgressUpdate: (Int) -> Unit
    ): QuickTrimResponse<QuickTrimTransformSuccess, QuickTrimTransformFailure>

}