package com.nijatva.videoplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads and caches small video frame thumbnails. */
object Thumbnails {

    private val cache = LruCache<String, Bitmap>(80)

    /** Returns a thumbnail for [uri] taken at [positionPercent] (0..100), or null. */
    suspend fun load(context: Context, uri: Uri, positionPercent: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val key = "$uri#$positionPercent"
            cache.get(key)?.let { return@withContext it }

            val bitmap = loadViaResolver(context, uri)
                ?: loadViaRetriever(context, uri, positionPercent)

            if (bitmap != null) cache.put(key, bitmap)
            bitmap
        }

    private fun loadViaResolver(context: Context, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching {
            context.contentResolver.loadThumbnail(uri, Size(320, 180), null)
        }.getOrNull()
    }

    private fun loadViaRetriever(context: Context, uri: Uri, positionPercent: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val timeUs = (durationMs * positionPercent.coerceIn(0, 100) / 100L) * 1000L
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }
}
