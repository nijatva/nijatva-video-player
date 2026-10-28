package com.nijatva.videoplayer.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.ui.components.ZScreen
import com.nijatva.videoplayer.ui.home.VideoRow
import com.nijatva.videoplayer.util.filterLibrary
import com.nijatva.videoplayer.util.sortedForLibrary

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenVideo: (String, String, Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    var all by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { all = app.mediaRepository.loadVideos() }

    val results = remember(all, query, settings) {
        all.filterLibrary(settings.excludedFolderIds, settings.vaultVideoIds)
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedForLibrary(settings.sortBy, settings.sortAscending)
    }

    ZScreen(title = "Search", onBack = onBack) { modifier ->
        Column(modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                placeholder = { Text("Search videos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )
            Box(Modifier.fillMaxSize()) {
                if (results.isEmpty()) {
                    Text(
                        if (query.isBlank()) "Type to search your library" else "No matches",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(results, key = { it.id }) { video ->
                            VideoRow(video = video) {
                                onOpenVideo(video.uri.toString(), video.name, video.folderId)
                            }
                        }
                    }
                }
            }
        }
    }
}
