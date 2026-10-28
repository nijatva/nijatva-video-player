package com.nijatva.videoplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.nijatva.videoplayer.data.model.MediaFolder
import com.nijatva.videoplayer.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads the device video library from MediaStore. */
class MediaRepository(private val context: Context) {

    private val collection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    private val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DATE_ADDED,
    )

    suspend fun loadVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<VideoItem>()
        runCatching {
            context.contentResolver.query(
                collection, projection, null, null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC",
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val path = cursor.getString(dataCol) ?: ""
                    items += VideoItem(
                        id = id,
                        uri = ContentUris.withAppendedId(collection, id),
                        name = cursor.getString(nameCol) ?: "Unknown",
                        durationMs = cursor.getLong(durCol),
                        sizeBytes = cursor.getLong(sizeCol),
                        folderId = cursor.getLong(bucketCol),
                        folderName = cursor.getString(bucketNameCol) ?: "Unknown",
                        path = path,
                        dateAddedSeconds = cursor.getLong(dateCol),
                    )
                }
            }
        }
        items
    }

    suspend fun loadFolders(): List<MediaFolder> = withContext(Dispatchers.IO) {
        loadVideos()
            .groupBy { it.folderId }
            .map { (id, videos) ->
                MediaFolder(
                    id = id,
                    name = videos.first().folderName,
                    path = videos.first().path.substringBeforeLast('/', ""),
                    videoCount = videos.size,
                    totalDurationMs = videos.sumOf { it.durationMs },
                    lastModified = videos.maxOf { it.dateAddedSeconds },
                    firstVideoUri = videos.first().uri.toString(),
                )
            }
            .sortedByDescending { it.lastModified }
    }
}
