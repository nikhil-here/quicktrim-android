package com.quicktrim.ai.ui

sealed class Routes(val path: String) {
    data object Upload : Routes(path = "upload")
    data object Edit : Routes(path = "edit")
    data object UpdateFillerWords : Routes(path = "updateFillerWords")
}

fun String?.toAppBarTitle() : String {
    return when(this) {
        Routes.Upload.path -> "Home"
        Routes.Edit.path -> "Trim Media"
        else -> "QuickTrim"
    }
}