package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
private fun Pill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

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
                    Column(Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Pill("System", settings.themeMode == 0) { repo.update { it.copy(themeMode = 0) } }
                            Pill("Dark", settings.themeMode == 2) { repo.update { it.copy(themeMode = 2) } }
                            Pill("Light", settings.themeMode == 1) { repo.update { it.copy(themeMode = 1) } }
                            Pill("Black", settings.themeMode == 3) { repo.update { it.copy(themeMode = 3) } }
                        }

                        Spacer(Modifier.height(22.dp))

                        Text("Accent colour", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Pill("Green", settings.accent == 0) { repo.update { it.copy(accent = 0) } }
                            Pill("Saffron", settings.accent == 1) { repo.update { it.copy(accent = 1) } }
                            Pill("Indigo", settings.accent == 2) { repo.update { it.copy(accent = 2) } }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Saved on this device only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    SettingSwitch(
                        "Dynamic color theme",
                        settings.dynamicColor,
                        { v -> repo.update { it.copy(dynamicColor = v) } },
                        "Use wallpaper colours instead of the accent (Android 12+)",
                        Icons.Filled.ColorLens,
                    )
                }
            }
        }
    }
}
