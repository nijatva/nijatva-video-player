package com.nijatva.videoplayer.ui.more

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@Composable
fun VaultScreen(onBack: () -> Unit, onOpenVideo: (String, String) -> Unit) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    var all by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        all = app.mediaRepository.loadVideos()
        loading = false
    }

    val vaulted = remember(all, settings.vaultVideoIds) {
        all.filter { it.id in settings.vaultVideoIds }
    }

    ZScreen(title = "Vault", onBack = onBack) { modifier ->
        Box(modifier.fillMaxSize()) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vaulted.isEmpty() -> Text(
                    "Vault is empty. Select videos in the Videos tab and choose \"Move to vault\".",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    items(vaulted, key = { it.id }) { video ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                VideoRow(video = video) { onOpenVideo(video.uri.toString(), video.name) }
                            }
                            IconButton(onClick = {
                                app.settingsRepository.update { it.copy(vaultVideoIds = it.vaultVideoIds - video.id) }
                            }) {
                                Icon(Icons.Filled.LockOpen, contentDescription = "Restore")
                            }
                        }
                    }
                }
            }
        }
    }
}
