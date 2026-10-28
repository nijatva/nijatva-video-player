package com.nijatva.videoplayer.util

import com.nijatva.videoplayer.data.model.MediaFolder
import com.nijatva.videoplayer.data.model.VideoItem

/** Removes excluded folders and vaulted videos from the library. */
fun List<VideoItem>.filterLibrary(excludedFolderIds: List<Long>, vaultIds: List<Long>): List<VideoItem> =
    filter { it.folderId !in excludedFolderIds && it.id !in vaultIds }

fun List<MediaFolder>.filterLibrary(excludedFolderIds: List<Long>): List<MediaFolder> =
    filter { it.id !in excludedFolderIds }

/** Applies the user's sort choice: 0 = name, 1 = date, 2 = size, 3 = duration. */
fun List<VideoItem>.sortedForLibrary(sortBy: Int, ascending: Boolean): List<VideoItem> {
    val comparator: Comparator<VideoItem> = when (sortBy) {
        1 -> compareBy { it.dateAddedSeconds }
        2 -> compareBy { it.sizeBytes }
        3 -> compareBy { it.durationMs }
        else -> compareBy { it.name.lowercase() }
    }
    return sortedWith(if (ascending) comparator else comparator.reversed())
}

fun List<MediaFolder>.sortedFoldersForLibrary(sortBy: Int, ascending: Boolean): List<MediaFolder> {
    val comparator: Comparator<MediaFolder> = when (sortBy) {
        1 -> compareBy { it.lastModified }
        2 -> compareBy { it.videoCount }
        3 -> compareBy { it.totalDurationMs }
        else -> compareBy { it.name.lowercase() }
    }
    return sortedWith(if (ascending) comparator else comparator.reversed())
}
