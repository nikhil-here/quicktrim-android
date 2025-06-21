package com.quicktrim.ai.ui

import com.quicktrim.ai.network.model.SegmentResponse
import com.quicktrim.ai.network.model.WordResponse

object Constants {

    const val GENERIC_ERROR_MSG = "Something Went Wrong"
    const val GENERIC_WAIT_MSG = "this might take few mins"

    const val EXPORT_SUCCESS_TITLE = "Video Saved"
    const val EXPORT_SUCCESS_DESCRIPTION = "trimmed video exported successfully"

    const val AUTO_HIDE_DELAY = 5_000L


    val PREVIEW_SEGMENT = SegmentResponse(
        text = "hi its nikhiel",
        isRemoved = true,
        wordResponses = listOf(
            WordResponse(
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = "hi",
                type = "word",
                isRemoved = false
            ),
            WordResponse(
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = " ",
                type = "word",
                isRemoved = false
            ),
            WordResponse(
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = "its",
                type = "",
                isRemoved = false
            ),
            WordResponse(
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = " ",
                type = "",
                isRemoved = false
            ),
            WordResponse(
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "nikhiel",
                text = "nikhiel",
                type = "",
                isRemoved = true
            )
        ),
    )
}