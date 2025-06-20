package com.quicktrim.ai.ui

sealed class Routes(val path: String) {

    data object Upload : Routes(path = "upload")
    data object Edit : Routes(path = "edit")
    data object Export : Routes(path = "export")
    data object UpdateFillerWords : Routes(path = "updateFillerWords")


    companion object {
         const val ARG_URI = "arg_uri"
    }
}

fun String?.toAppBarTitle() : String {
    return when(this) {
        Routes.Upload.path -> "Home"
        Routes.Edit.path -> "Trim Media"
        Routes.Export.path -> "Export Media"
        else -> "QuickTrim"
    }
}