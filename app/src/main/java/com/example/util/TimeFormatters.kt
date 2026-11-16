package com.example.util

import java.util.Locale

fun formatTimecodeMillis(ms: Long): String {
    val safeMs = ms.coerceAtLeast(0L)
    val totalSeconds = safeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = safeMs % 1000
    return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
}

fun formatTimecodeCentis(ms: Long): String {
    val safeMs = ms.coerceAtLeast(0L)
    val totalSeconds = safeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val centis = (safeMs % 1000) / 10
    return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
}

fun formatSecondsLabel(ms: Long): String {
    val sec = ms / 1000.0
    return String.format(Locale.US, "%.2fs", sec)
}
