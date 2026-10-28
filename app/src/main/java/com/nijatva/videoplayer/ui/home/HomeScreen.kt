package com.nijatva.videoplayer.ui.home

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.MediaFolder
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.VideoThumbnail
import com.nijatva.videoplayer.util.filterLibrary
import com.nijatva.videoplayer.util.formatDuration
import com.nijatva.videoplayer.util.hasVideoPermission
import com.nijatva.videoplayer.util.requiredVideoPermission
import com.nijatva.videoplayer.util.sortedFoldersForLibrary
import com.nijatva.videoplayer.util.sortedForLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenFolder: (Long, String) -> Unit,
    onOpenVideo: (String, String, Long) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var folders by remember { mutableStateOf<List<MediaFolder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var granted by remember { mutableStateOf(hasVideoPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    LaunchedEffect(granted) {
        if (granted) {
            loading = true
            videos = app.mediaRepository.loadVideos()
            folders = app.mediaRepository.loadFolders()
            loading = false
        }
    }

    if (!granted) {
        LaunchedEffect(Unit) { permissionLauncher.launch(requiredVideoPermission()) }
    }

    val visible = remember(videos, settings) {
        videos.filterLibrary(settings.excludedFolderIds, settings.vaultVideoIds)
    }
    val continueWatching = remember(visible, loading) {
        val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
        prefs.all.entries
            .mapNotNull { (uri, value) ->
                val position = (value as? Long) ?: return@mapNotNull null
                val video = visible.firstOrNull { it.uri.toString() == uri } ?: return@mapNotNull null
                if (position <= 5_000L || (video.durationMs > 0 && position >= video.durationMs - 5_000L)) {
                    null
                } else {
                    video to position
                }
            }
            .sortedByDescending { it.second }
            .take(10)
    }
    val recentlyAdded = remember(visible) {
        visible.sortedForLibrary(1, false).take(6)
    }
    val topFolders = remember(folders, settings) {
        folders.filterLibrary(settings.excludedFolderIds)
            .sortedFoldersForLibrary(settings.sortBy, settings.sortAscending)
            .take(10)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Player", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (continueWatching.isNotEmpty()) {
                        item {
                            SectionTitle("Continue watching")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(continueWatching, key = { it.first.id }) { (video, position) ->
                                    ContinueCard(video, position, settings.thumbnailPosition) {
                                        onOpenVideo(video.uri.toString(), video.name, video.folderId)
                                    }
                                }
                            }
                        }
                    }

                    if (recentlyAdded.isNotEmpty()) {
                        item {
                            SectionTitle("Recently added")
                            Column(Modifier.padding(horizontal = 12.dp)) {
                                recentlyAdded.forEach { video ->
                                    VideoRow(video = video) {
                                        onOpenVideo(video.uri.toString(), video.name, video.folderId)
                                    }
                                }
                            }
                        }
                    }

                    if (topFolders.isNotEmpty()) {
                        item {
                            SectionTitle("Folders")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(topFolders, key = { it.id }) { folder ->
                                    FolderCard(folder, settings.thumbnailPosition) {
                                        onOpenFolder(folder.id, folder.name)
                                    }
                                }
                            }
                        }
                    }

                    if (continueWatching.isEmpty() && recentlyAdded.isEmpty() && topFolders.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "No videos found. Videos on your device will appear here.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun ContinueCard(
    video: VideoItem,
    position: Long,
    thumbnailPercent: Int,
    onClick: () -> Unit,
) {
    Column(Modifier.width(200.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)),
        ) {
            VideoThumbnail(video.uri.toString(), thumbnailPercent, Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(formatDuration(video.durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        LinearProgressIndicator(
            progress = {
                val total = video.durationMs.coerceAtLeast(1L)
                (position.toFloat() / total).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth().height(3.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(video.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FolderCard(folder: MediaFolder, thumbnailPercent: Int, onClick: () -> Unit) {
    Column(Modifier.width(150.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)),
        ) {
            VideoThumbnail(folder.firstVideoUri, thumbnailPercent, Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text("${folder.videoCount}", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(folder.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

