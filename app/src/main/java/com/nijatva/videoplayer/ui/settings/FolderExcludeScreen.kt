package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import com.nijatva.videoplayer.data.model.MediaFolder
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun FolderExcludeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    var folders by remember { mutableStateOf<List<MediaFolder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        folders = app.mediaRepository.loadFolders()
        loading = false
    }

    ZScreen(title = "Manage folders", onBack = onBack) { modifier ->
        Box(modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    item {
                        Text(
                            "Untick a folder to hide it from the media library.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    items(folders, key = { it.id }) { folder ->
                        val excluded = folder.id in settings.excludedFolderIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    app.settingsRepository.update {
                                        val ids = if (excluded) it.excludedFolderIds - folder.id
                                        else it.excludedFolderIds + folder.id
                                        it.copy(excludedFolderIds = ids)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = !excluded, onCheckedChange = null)
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(folder.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${folder.videoCount} videos",
                                    style = MaterialTheme.typography.bodySmall,
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
