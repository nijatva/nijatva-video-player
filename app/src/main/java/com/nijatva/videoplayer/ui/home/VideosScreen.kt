package com.nijatva.videoplayer.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.OverFlowButton
import com.nijatva.videoplayer.ui.more.AddToPlaylistDialog
import com.nijatva.videoplayer.ui.components.SortMenu
import com.nijatva.videoplayer.ui.components.rememberVideoDeleter
import com.nijatva.videoplayer.util.filterLibrary
import com.nijatva.videoplayer.util.sortedForLibrary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onOpenVideo: (String, String, Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showAddToPlaylist by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            videos = app.mediaRepository.loadVideos()
            loading = false
        }
    }

    val deleter = rememberVideoDeleter { selectedIds = emptySet(); selectionMode = false; refresh() }

    LaunchedEffect(Unit) { refresh() }

    val shown = remember(videos, query, settings.excludedFolderIds, settings.vaultVideoIds, settings.sortBy, settings.sortAscending) {
        videos
            .filterLibrary(settings.excludedFolderIds, settings.vaultVideoIds)
            .let { list -> if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) } }
            .sortedForLibrary(settings.sortBy, settings.sortAscending)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionMode) "${selectedIds.size} selected" else "Videos",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (selectionMode) {
                        IconButton(
                            onClick = {
                                deleter(shown.filter { it.id in selectedIds }.map { it.uri })
                            },
                            enabled = selectedIds.isNotEmpty(),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                        OverFlowButton { dismiss ->
                            DropdownMenuItem(
                                text = { Text("Move to vault") },
                                onClick = {
                                    dismiss()
                                    app.settingsRepository.update {
                                        it.copy(vaultVideoIds = (it.vaultVideoIds + selectedIds).distinct())
                                    }
                                    selectionMode = false
                                    selectedIds = emptySet()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Add to playlist") },
                                onClick = { dismiss(); showAddToPlaylist = true },
                            )
                        }
                        IconButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        }
                    } else {
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { app.settingsRepository.update { it.copy(gridView = !it.gridView) } }) {
                            Icon(
                                if (settings.gridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = "Toggle grid",
                            )
                        }
                        SortMenu(
                            sortBy = settings.sortBy,
                            ascending = settings.sortAscending,
                            onSortBy = { app.settingsRepository.update { s -> s.copy(sortBy = it) } },
                            onAscending = { app.settingsRepository.update { s -> s.copy(sortAscending = it) } },
                        )
                        OverFlowButton { dismiss ->
                            DropdownMenuItem(
                                text = { Text("Select") },
                                onClick = { dismiss(); selectionMode = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Vault") },
                                onClick = { dismiss(); onOpenVault() },
                            )
                            DropdownMenuItem(
                                text = { Text("History") },
                                onClick = { dismiss(); onOpenHistory() },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                placeholder = { Text("Search videos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )
            Box(Modifier.fillMaxSize()) {
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
                        onClick = { video -> onOpenVideo(video.uri.toString(), video.name, video.folderId) },
                        contentPadding = PaddingValues(12.dp),
                    )
                }
            }
        }
    }

    if (showAddToPlaylist) {
        AddToPlaylistDialog(
            onDismiss = { showAddToPlaylist = false },
            onAdd = { playlistId ->
                selectedIds.forEach { app.playlistRepository.addVideo(playlistId, it) }
                showAddToPlaylist = false
                selectionMode = false
                selectedIds = emptySet()
            },
        )
    }
}
