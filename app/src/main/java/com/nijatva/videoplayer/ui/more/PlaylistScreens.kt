package com.nijatva.videoplayer.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.ZScreen
import com.nijatva.videoplayer.ui.home.VideoRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistListScreen(onBack: (() -> Unit)?, onOpenPlaylist: (String, String) -> Unit) {
    val context = LocalContext.current
    val app = context.app
    val playlists by app.playlistRepository.playlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New playlist")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (playlists.isEmpty()) {
                Text(
                    "No playlists yet. Tap + to create one.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPlaylist(playlist.id, playlist.name) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${playlist.videoIds.size} videos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { app.playlistRepository.delete(playlist.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { app.playlistRepository.create(name.ifBlank { "Playlist" }); showCreate = false },
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBack: () -> Unit,
    onOpenVideo: (String, String, Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val playlists by app.playlistRepository.playlists.collectAsState()
    val playlist = playlists.firstOrNull { it.id == playlistId }

    var all by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        all = app.mediaRepository.loadVideos()
        loading = false
    }

    val videos = remember(playlist, all) {
        val ids = playlist?.videoIds ?: emptyList()
        ids.mapNotNull { id -> all.firstOrNull { it.id == id } }
    }

    ZScreen(title = playlist?.name ?: "Playlist", onBack = onBack) { modifier ->
        Box(modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (videos.isEmpty()) {
                Text(
                    "This playlist is empty.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    items(videos, key = { it.id }) { video ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                VideoRow(video = video) { onOpenVideo(video.uri.toString(), video.name, -1L) }
                            }
                            IconButton(onClick = { app.playlistRepository.removeVideo(playlistId, video.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Dialog to add a video to one of the user's playlists. */
@Composable
fun AddToPlaylistDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    val context = LocalContext.current
    val playlists by context.app.playlistRepository.playlists.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist") },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text("No playlists yet — create one from More → Playlists.")
                } else {
                    playlists.forEach { playlist ->
                        Text(
                            playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdd(playlist.id) }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
