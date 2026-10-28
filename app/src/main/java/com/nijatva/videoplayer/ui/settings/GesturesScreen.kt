package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
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

@Composable
fun GesturesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Gestures", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Gestures") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Seek gesture",
                        settings.seekGesture,
                        { v -> repo.update { it.copy(seekGesture = v) } },
                        "Swipe horizontally to seek forwards or backwards",
                        Icons.Filled.Tune,
                    )
                    SettingSlider(
                        "Seek gesture sensitivity",
                        settings.seekSensitivity.toString(),
                        settings.seekSensitivity,
                        { v -> repo.update { it.copy(seekSensitivity = v) } },
                        0.1f..1f,
                        Icons.Filled.Speed,
                    )
                    SettingSwitch(
                        "Brightness gesture",
                        settings.brightnessGesture,
                        { v -> repo.update { it.copy(brightnessGesture = v) } },
                        "Adjust brightness by swiping vertically",
                        Icons.Filled.Brightness6,
                    )
                    SettingSlider(
                        "Brightness gesture sensitivity",
                        settings.brightnessSensitivity.toString(),
                        settings.brightnessSensitivity,
                        { v -> repo.update { it.copy(brightnessSensitivity = v) } },
                        0.1f..1f,
                        Icons.Filled.Speed,
                    )
                    SettingSwitch(
                        "Volume gesture",
                        settings.volumeGesture,
                        { v -> repo.update { it.copy(volumeGesture = v) } },
                        "Adjust volume by swiping vertically",
                        Icons.Filled.VolumeUp,
                    )
                    SettingSlider(
                        "Volume gesture sensitivity",
                        settings.volumeSensitivity.toString(),
                        settings.volumeSensitivity,
                        { v -> repo.update { it.copy(volumeSensitivity = v) } },
                        0.1f..1f,
                        Icons.Filled.Speed,
                    )
                    SettingSwitch(
                        "Zoom gesture",
                        settings.zoomGesture,
                        { v -> repo.update { it.copy(zoomGesture = v) } },
                        "Pinch to zoom the video",
                        Icons.Filled.ZoomIn,
                    )
                    SettingSwitch(
                        "Pan gesture",
                        settings.panGesture,
                        { v -> repo.update { it.copy(panGesture = v) } },
                        "Zoom and pan the video",
                        Icons.Filled.PanTool,
                    )
                    SettingSwitch(
                        "Double tap gesture",
                        settings.doubleTapGesture,
                        { v -> repo.update { it.copy(doubleTapGesture = v) } },
                        "Select double tap gesture action",
                        Icons.Filled.TouchApp,
                    )
                    SettingSwitch(
                        "Long press gesture",
                        settings.longPressGesture,
                        { v -> repo.update { it.copy(longPressGesture = v) } },
                        "Long press to play at 2.0x speed",
                        Icons.Filled.Gesture,
                    )
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(5, 10, 30, 60).forEach { seconds ->
                            FilterChip(
                                selected = settings.seekIncrementMs == seconds * 1000,
                                onClick = { repo.update { it.copy(seekIncrementMs = seconds * 1000) } },
                                label = { Text("${seconds}s") },
                            )
                        }
                    }
                    Text(
                        "Seek increment",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
