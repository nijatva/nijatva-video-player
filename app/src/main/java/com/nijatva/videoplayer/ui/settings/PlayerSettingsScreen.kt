package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingSlider
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen
import com.nijatva.videoplayer.util.formatSpeed

@Composable
fun PlayerSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Player", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Interface") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Material You controls",
                        settings.materialYouControls,
                        { v -> repo.update { it.copy(materialYouControls = v) } },
                        "Use material you style controls",
                        Icons.Filled.Palette,
                    )
                    SettingSlider(
                        "Controller timeout",
                        "${settings.controllerTimeoutMs / 1000} seconds",
                        settings.controllerTimeoutMs / 1000f,
                        { v -> repo.update { it.copy(controllerTimeoutMs = (v * 1000).toInt()) } },
                        1f..15f,
                        Icons.Filled.Timer,
                    )
                }
            }
            item { SectionHeader("Playback") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Resume",
                        settings.resume,
                        { v -> repo.update { it.copy(resume = v) } },
                        "Resume played videos from the point where you stopped",
                        Icons.Filled.PlayArrow,
                    )
                    SettingSlider(
                        "Default playback speed",
                        formatSpeed(settings.defaultSpeed),
                        settings.defaultSpeed,
                        { v -> repo.update { it.copy(defaultSpeed = v) } },
                        0.25f..3f,
                        Icons.Filled.Speed,
                    )
                    SettingSwitch(
                        "Autoplay",
                        settings.autoplay,
                        { v -> repo.update { it.copy(autoplay = v) } },
                        "Automatically play the next video in the current folder",
                        Icons.Filled.PlayArrow,
                    )
                    SettingSwitch(
                        "Picture in Picture Mode",
                        settings.pip,
                        { v -> repo.update { it.copy(pip = v) } },
                        "Automatically switch to miniature player when tapping Home Button",
                        Icons.Filled.PictureInPictureAlt,
                    )
                    SettingSwitch(
                        "Background play",
                        settings.backgroundPlay,
                        { v -> repo.update { it.copy(backgroundPlay = v) } },
                        "Play in background when tapped on home or screen is locked",
                        Icons.Filled.Headphones,
                    )
                    SettingSwitch(
                        "Remember brightness level",
                        settings.rememberBrightness,
                        { v -> repo.update { it.copy(rememberBrightness = v) } },
                        "Remember brightness level between playback sessions",
                        Icons.Filled.Brightness6,
                    )
                    SettingSwitch(
                        "Remember selections",
                        settings.rememberSelections,
                        { v -> repo.update { it.copy(rememberSelections = v) } },
                        "Remember selections for each file like audio track, subtitle track, etc.",
                        Icons.Filled.Check,
                    )
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Video", "Portrait", "Landscape").forEachIndexed { index, label ->
                            FilterChip(
                                selected = settings.orientation == index,
                                onClick = { repo.update { it.copy(orientation = index) } },
                                label = { Text(label) },
                            )
                        }
                    }
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text("Player screen orientation", modifier = Modifier.weight(1f))
                        androidx.compose.material3.Icon(Icons.Filled.ScreenRotation, contentDescription = null)
                    }
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text("Video fit", modifier = Modifier.weight(1f))
                        androidx.compose.material3.Icon(Icons.Filled.AspectRatio, contentDescription = null)
                    }
                }
            }
        }
    }
}
