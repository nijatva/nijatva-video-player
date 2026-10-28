package com.nijatva.videoplayer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.VideoThumbnail
import com.nijatva.videoplayer.util.formatDuration

@Composable
fun VideoGridItem(
    video: VideoItem,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val settings by context.app.settingsRepository.settings.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            VideoThumbnail(
                uri = video.uri.toString(),
                positionPercent = settings.thumbnailPosition,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(formatDuration(video.durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            if (selectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            video.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Renders videos as either a list or a grid, with optional multi-select. */
@Composable
fun VideoListContent(
    videos: List<VideoItem>,
    grid: Boolean,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onClick: (VideoItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(12.dp),
) {
    if (grid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(videos, key = { it.id }) { video ->
                VideoGridItem(
                    video = video,
                    selectionMode = selectionMode,
                    selected = video.id in selectedIds,
                    onClick = { if (selectionMode) onToggleSelect(video.id) else onClick(video) },
                )
            }
        }
    } else {
        LazyColumn(contentPadding = contentPadding) {
            items(videos, key = { it.id }) { video ->
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectionMode) {
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (video.id in selectedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (video.id in selectedIds) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        VideoRow(video = video) { if (selectionMode) onToggleSelect(video.id) else onClick(video) }
                    }
                }
            }
        }
    }
}
