package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.ui.Routes
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingClick
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun SettingsScreen(onBack: (() -> Unit)?, onOpen: (String) -> Unit) {
    ZScreen(title = "Settings", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SettingsGroup {
                    SettingClick("Appearance", { onOpen(Routes.APPEARANCE) }, "Adjust the app to your liking", Icons.Filled.Palette)
                    SettingClick("Media library", { onOpen(Routes.MEDIA_LIBRARY) }, "Scan and manage media library", Icons.Filled.VideoLibrary)
                    SettingClick("Player", { onOpen(Routes.PLAYER_SETTINGS) }, "Player appearance, playback controls", Icons.Filled.PlayArrow)
                    SettingClick("Gestures", { onOpen(Routes.GESTURES) }, "Configure player touch gestures", Icons.Filled.Gesture)
                    SettingClick("Audio", { onOpen(Routes.AUDIO) }, "Audio playback options", Icons.Filled.MusicNote)
                    SettingClick("Subtitle", { onOpen(Routes.SUBTITLE) }, "Subtitle appearance, playback options", Icons.Filled.Subtitles)
                    SettingClick("General", { onOpen(Routes.GENERAL) }, "User data options", Icons.Filled.Settings)
                    SettingClick("Advanced settings", { onOpen(Routes.ADVANCED) }, "Blocking, link cleaning and playback extras", Icons.Filled.Tune)
                    SettingClick("Network", { onOpen(Routes.NETWORK) }, "Network streams and saved connections", Icons.Filled.Wifi)
                    SettingClick("Privacy & security", { onOpen(Routes.PRIVACY) }, "App lock and the vault", Icons.Filled.Lock)
                    SettingClick("About", { onOpen(Routes.ABOUT) }, "Version, support, credits", Icons.Filled.Info)
                }
            }
        }
    }
}
