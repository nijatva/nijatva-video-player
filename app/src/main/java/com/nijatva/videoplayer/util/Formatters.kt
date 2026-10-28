package com.nijatva.videoplayer.util

import java.util.Locale

/** Formats a duration in milliseconds as H:MM:SS or M:SS. */
fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/** Formats a byte count as a short human-readable string. */
fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0) "${bytes} B" else String.format(Locale.US, "%.1f %s", value, units[unit])
}

/** Formats a playback speed such as 1.0 or 1.5. */
fun formatSpeed(speed: Float): String = String.format(Locale.US, "%.1f", speed)
