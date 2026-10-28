package com.nijatva.videoplayer.ui.more

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onOpenVideo: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenVault: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val recentUris by app.historyRepository.recent.collectAsState()

    var allVideos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { onOpenVideo(it.toString(), "Video") } }

    LaunchedEffect(recentUris) {
        if (recentUris.isNotEmpty()) allVideos = app.mediaRepository.loadVideos()
    }

    val recent = remember(recentUris, allVideos) {
        recentUris.mapNotNull { u -> allVideos.firstOrNull { it.uri.toString() == u } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Vault", Icons.Filled.Lock, Modifier.weight(1f)) { onOpenVault() }
                    ActionCard("Pick file", Icons.AutoMirrored.Filled.List, Modifier.weight(1f)) {
                        pickFile.launch(arrayOf("video/*"))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("Playlists", Icons.AutoMirrored.Filled.PlaylistPlay, Modifier.weight(1f)) {
                        onOpenPlaylists()
                    }
                    ActionCard("Clear history", Icons.Filled.Delete, Modifier.weight(1f)) {
                        app.historyRepository.clear()
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (recent.isEmpty()) {
                item {
                    Text(
                        "Videos you play will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(recent, key = { it.id }) { video ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                video.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(label)
    }
}
