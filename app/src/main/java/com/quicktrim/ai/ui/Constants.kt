package com.quicktrim.ai.ui

import com.quicktrim.ai.data.remote.model.SegmentRemoteResponse
import com.quicktrim.ai.data.remote.model.WordRemoteResponse
import com.quicktrim.ai.domain.model.Segment
import com.quicktrim.ai.domain.model.Word

object Constants {

    const val GENERIC_ERROR_MSG = "Something Went Wrong"
    const val GENERIC_WAIT_MSG = "this might take few mins"


    const val DEFAULT_PROJECT_NAME = "untitled"

    const val EXPORT_SUCCESS_TITLE = "Video Saved"
    const val EXPORT_SUCCESS_DESCRIPTION = "trimmed video exported successfully"

    const val AUTO_HIDE_DELAY = 5_000L


    val PREVIEW_SEGMENT = Segment(
        id = "",
        text = "hi its nikhiel",
        words = listOf(
            Word(
                id = "",
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = "hi",
                type = "word",
                isRemoved = false
            ),
            Word(
                id = "",
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = " ",
                type = "word",
                isRemoved = false
            ),
            Word(
                id = "",
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = "its",
                type = "",
                isRemoved = false
            ),
            Word(
                id = "",
                start = 0.00,
                end = 0.00,
                logprob = 0.11,
                speakerId = "",
                text = " ",
                type = "",
                isRemoved = false
            ),
            Word(
                id = "",
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