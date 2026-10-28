package com.nijatva.videoplayer.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.ZScreen
import com.nijatva.videoplayer.ui.components.rememberVideoDeleter
import com.nijatva.videoplayer.util.sortedForLibrary
import kotlinx.coroutines.launch

@Composable
fun FolderScreen(
    folderId: Long,
    onBack: () -> Unit,
    onOpenVideo: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    fun refresh() {
        scope.launch {
            videos = app.mediaRepository.loadVideos().filter { it.folderId == folderId }
            loading = false
        }
    }

    val deleter = rememberVideoDeleter { selectedIds = emptySet(); selectionMode = false; refresh() }

    LaunchedEffect(folderId) { refresh() }

    val title = if (selectionMode) "${selectedIds.size} selected" else (videos.firstOrNull()?.folderName ?: "Folder")
    val shown = remember(videos, settings.sortBy, settings.sortAscending) {
        videos.sortedForLibrary(settings.sortBy, settings.sortAscending)
    }

    ZScreen(
        title = title,
        onBack = { if (selectionMode) { selectionMode = false; selectedIds = emptySet() } else onBack() },
        actions = {
            if (selectionMode) {
                IconButton(
                    onClick = { deleter(shown.filter { it.id in selectedIds }.map { it.uri }) },
                    enabled = selectedIds.isNotEmpty(),
                ) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                IconButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel")
                }
            } else {
                IconButton(onClick = { app.settingsRepository.update { it.copy(gridView = !it.gridView) } }) {
                    Icon(
                        if (settings.gridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                        contentDescription = "Toggle grid",
                    )
                }
                IconButton(onClick = { selectionMode = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Select")
                }
            }
        },
    ) { modifier ->
        Box(modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                VideoListContent(
                    videos = shown,
                    grid = settings.gridView,
                    selectionMode = selectionMode,
                    selectedIds = selectedIds,
                    onToggleSelect = { id ->
                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                    },
                    onClick = { video -> onOpenVideo(video.uri.toString(), video.name) },
                )
            }
        }
    }
}
