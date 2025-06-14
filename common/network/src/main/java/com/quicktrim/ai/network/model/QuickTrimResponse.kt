package com.quicktrim.ai.network.model

import okio.IOException

sealed class QuickTrimResponse<out SUCCESS : Any, out ERROR : Any> {

    data class Success<SUCCESS : Any>(val body: SUCCESS) : QuickTrimResponse<SUCCESS, Nothing>()

    data object SuccessNoBody
        : QuickTrimResponse<Nothing, Nothing>()

    data class ApiError<ERROR : Any>(val body: ERROR, val code: Int) :
        QuickTrimResponse<Nothing, ERROR>()

    data class QuickTrimError(val error: IOException) : QuickTrimResponse<Nothing, Nothing>()

    data class UnknownError(val error: Throwable) : QuickTrimResponse<Nothing, Nothing>()

}