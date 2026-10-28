package com.nijatva.videoplayer.data.model

import android.net.Uri
import kotlinx.serialization.Serializable

/** A single video file discovered in the device media store. */
data class VideoItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val folderId: Long,
    val folderName: String,
    val path: String,
    val dateAddedSeconds: Long,
)

/** A folder (media bucket) containing one or more videos. */
data class MediaFolder(
    val id: Long,
    val name: String,
    val path: String,
    val videoCount: Int,
    val totalDurationMs: Long,
    val lastModified: Long,
    val firstVideoUri: String? = null,
)

/** A saved network connection shown on the Network screen. */
@Serializable
data class NetworkConnection(
    val id: String,
    val name: String,
    val protocol: String,
    val host: String,
)
