package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
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
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Appearance", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Appearance") }
            item {
                SettingsGroup {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("System", "Light", "Dark").forEachIndexed { index, label ->
                            FilterChip(
                                selected = settings.themeMode == index,
                                onClick = { repo.update { it.copy(themeMode = index) } },
                                label = { Text(label) },
                            )
                        }
                    }
                    SettingSwitch(
                        "OLED black theme",
                        settings.highContrastDark,
                        { v -> repo.update { it.copy(highContrastDark = v) } },
                        "Use a pure black background for dark theme (saves battery on OLED)",
                        Icons.Filled.Contrast,
                    )
                    SettingSwitch(
                        "Dynamic color theme",
                        settings.dynamicColor,
                        { v -> repo.update { it.copy(dynamicColor = v) } },
                        "Apply colors from system wallpaper to the application theme",
                        Icons.Filled.ColorLens,
                    )
                }
            }
        }
    }
}
