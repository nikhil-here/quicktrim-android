package com.quicktrim.ai

fun Long.toTimeString(): String {
    val totalSeconds = this / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0)
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}


fun Double.toHMS(): String {
    val totalSeconds = this.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0)
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}

fun normalizeAndCompare(str1: String, str2: String): Boolean {
    fun normalize(input: String): String {
        return input
            .trim()
            .lowercase()
            .replace(Regex("[,]+$"), "")  // remove trailing commas
            .replace(Regex("^[,]+"), "")  // remove leading commas
            .replace(Regex("[,]{2,}"), ",") // collapse multiple commas if needed
    }
    return normalize(str1) == normalize(str2)
}