package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Subtitles
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
fun SubtitleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Subtitle", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Subtitle") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Auto-load subtitles",
                        settings.autoLoadSubtitles,
                        { v -> repo.update { it.copy(autoLoadSubtitles = v) } },
                        "Automatically load a subtitle file with the same name as the video",
                        Icons.Filled.Subtitles,
                    )
                    SettingSlider(
                        "Subtitle text size",
                        "${settings.subtitleTextSize} sp",
                        settings.subtitleTextSize.toFloat(),
                        { v -> repo.update { it.copy(subtitleTextSize = v.toInt()) } },
                        8f..32f,
                        Icons.Filled.FormatSize,
                    )
                }
            }
        }
    }
}
