package com.nijatva.videoplayer.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.nijatva.videoplayer.aekant.FastDownloader

/**
 * Starts a download for [url]. Uses the four-part ranged downloader when the
 * server supports byte ranges, otherwise falls back to the system DownloadManager.
 */
fun startVideoDownload(context: Context, url: String, title: String) {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return
    val name = title.ifBlank {
        url.substringBefore('?').substringAfterLast('/').ifBlank { "video.mp4" }
    }
    Thread {
        val total = runCatching { FastDownloader.probe(url, null) }.getOrDefault(-1L)
        if (total > 0) {
            runCatching { FastDownloader.start(context.applicationContext, url, name, null, null, total) }
        } else {
            runCatching {
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(name)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            }
        }
    }.start()
}
