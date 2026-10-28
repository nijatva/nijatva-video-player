package com.nijatva.videoplayer.util

import android.net.Uri

/** Returns the 11-character video id if [url] is a YouTube link, else null. */
fun youtubeVideoId(url: String): String? {
    if (url.isBlank()) return null
    val uri = runCatching { Uri.parse(url.trim()) }.getOrNull() ?: return null
    val host = uri.host?.lowercase() ?: return null

    val isYouTubeHost = host == "youtu.be" ||
        host == "youtube.com" ||
        host == "www.youtube.com" ||
        host == "m.youtube.com" ||
        host == "music.youtube.com" ||
        host == "youtube-nocookie.com" ||
        host == "www.youtube-nocookie.com"

    if (!isYouTubeHost) return null

    // youtu.be/<id>
    if (host == "youtu.be") {
        return uri.pathSegments.firstOrNull()?.takeIf { it.length == 11 }
    }

    val queryId = uri.getQueryParameter("v")
    if (!queryId.isNullOrBlank()) return queryId.takeIf { it.length == 11 }

    // /shorts/<id>, /embed/<id>, /live/<id>, /v/<id>
    val segments = uri.pathSegments
    if (segments.size >= 2) {
        val first = segments[0].lowercase()
        if (first == "shorts" || first == "embed" || first == "live" || first == "v") {
            return segments[1].takeIf { it.length == 11 }
        }
    }
    return null
}

fun isYouTubeUrl(url: String): Boolean = youtubeVideoId(url) != null
