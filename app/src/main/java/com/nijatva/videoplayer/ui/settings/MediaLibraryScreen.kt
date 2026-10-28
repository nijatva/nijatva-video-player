package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingClick
import com.nijatva.videoplayer.ui.components.SettingSlider
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun MediaLibraryScreen(onBack: () -> Unit, onOpenFolders: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Media library", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Media library") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Mark last played media",
                        settings.markLastPlayed,
                        { v -> repo.update { it.copy(markLastPlayed = v) } },
                        "Mark last played media in home and in each folder",
                        Icons.Filled.Check,
                    )
                }
            }
            item { SectionHeader("Scan") }
            item {
                SettingsGroup {
                    SettingClick("Manage folders", onOpenFolders, "Select folders to be excluded in the media library", Icons.Filled.FolderOpen)
                }
            }
            item { SectionHeader("Thumbnail") }
            item {
                SettingsGroup {
                    SettingSlider(
                        "Thumbnail generation",
                        "Frame at position ${settings.thumbnailPosition}%",
                        settings.thumbnailPosition.toFloat(),
                        { v -> repo.update { it.copy(thumbnailPosition = v.toInt()) } },
                        0f..100f,
                        Icons.Filled.Image,
                    )
                }
            }
        }
    }
}
