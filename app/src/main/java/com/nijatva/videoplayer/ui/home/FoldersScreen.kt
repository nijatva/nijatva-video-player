package com.nijatva.videoplayer.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.nijatva.videoplayer.ui.components.SortMenu
import com.nijatva.videoplayer.ui.components.VideoThumbnail
import com.nijatva.videoplayer.util.filterLibrary
import com.nijatva.videoplayer.util.formatDuration
import com.nijatva.videoplayer.util.hasVideoPermission
import com.nijatva.videoplayer.util.requiredVideoPermission
import com.nijatva.videoplayer.util.sortedFoldersForLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(onOpenFolder: (Long, String) -> Unit) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    var folders by remember { mutableStateOf<List<MediaFolder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var granted by remember { mutableStateOf(hasVideoPermission(context)) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    LaunchedEffect(granted) {
        if (granted) {
            loading = true
            folders = app.mediaRepository.loadFolders()
            loading = false
        }
    }
    if (!granted) {
        LaunchedEffect(Unit) { permissionLauncher.launch(requiredVideoPermission()) }
    }

    val shown = remember(folders, settings) {
        folders.filterLibrary(settings.excludedFolderIds)
            .sortedFoldersForLibrary(settings.sortBy, settings.sortAscending)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folders", fontWeight = FontWeight.Bold) },
                actions = {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                shown.isEmpty() -> Text(
                    "No folders found.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                settings.gridView -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(shown, key = { it.id }) { folder ->
                        FolderGridCell(folder, settings.thumbnailPosition) { onOpenFolder(folder.id, folder.name) }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(shown, key = { it.id }) { folder ->
                        FolderListRow(folder, settings.thumbnailPosition) { onOpenFolder(folder.id, folder.name) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderGridCell(folder: MediaFolder, thumbnailPercent: Int, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))) {
            VideoThumbnail(folder.firstVideoUri, thumbnailPercent, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(6.dp))
        Text(folder.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            if (folder.videoCount == 1) "1 Video" else "${folder.videoCount} Videos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FolderListRow(folder: MediaFolder, thumbnailPercent: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(112.dp).height(64.dp).clip(RoundedCornerShape(12.dp))) {
            VideoThumbnail(folder.firstVideoUri, thumbnailPercent, Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(formatDuration(folder.totalDurationMs), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(folder.name, style = MaterialTheme.typography.titleMedium)
            Text(
                folder.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    if (folder.videoCount == 1) "1 Video" else "${folder.videoCount} Videos",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
