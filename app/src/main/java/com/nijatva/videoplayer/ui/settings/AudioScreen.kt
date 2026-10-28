package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun AudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Audio", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Playback") }
            item {
                SettingsGroup {
                    SettingClick("Preferred audio language", {}, "Language of the audio track you want to use by default", Icons.Filled.Language)
                    SettingSwitch(
                        "Require audio focus",
                        settings.requireAudioFocus,
                        { v -> repo.update { it.copy(requireAudioFocus = v) } },
                        "Pause other players while this player is playing videos, and vice versa",
                        Icons.Filled.Speaker,
                    )
                    SettingSwitch(
                        "Pause on disconnect",
                        settings.pauseOnDisconnect,
                        { v -> repo.update { it.copy(pauseOnDisconnect = v) } },
                        "Pause playback when headset disconnected from the device",
                        Icons.Filled.Headphones,
                    )
                    SettingSwitch(
                        "System volume panel",
                        settings.systemVolumePanel,
                        { v -> repo.update { it.copy(systemVolumePanel = v) } },
                        "Show system volume panel while adjusting volume with headset on",
                        Icons.Filled.VolumeUp,
                    )
                    SettingSwitch(
                        "Volume boost",
                        settings.volumeBoost,
                        { v -> repo.update { it.copy(volumeBoost = v) } },
                        "Boost audio volume up to 200%",
                        Icons.Filled.VolumeOff,
                    )
                }
            }
        }
    }
}
