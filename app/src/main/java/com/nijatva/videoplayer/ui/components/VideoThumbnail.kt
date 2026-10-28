package com.nijatva.videoplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.nijatva.videoplayer.util.Thumbnails

/**
 * Draws a real frame from the video, falling back to a play glyph while the
 * thumbnail loads or when the file cannot be read.
 */
@Composable
fun VideoThumbnail(
    uri: String?,
    positionPercent: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, uri, positionPercent) {
        value = if (uri.isNullOrBlank()) null else Thumbnails.load(context, Uri.parse(uri), positionPercent)
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
