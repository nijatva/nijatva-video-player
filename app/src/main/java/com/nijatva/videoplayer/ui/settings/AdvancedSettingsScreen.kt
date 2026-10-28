package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.aekant.AdFilterEngine
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun AdvancedSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    ZScreen(title = "Advanced settings", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Blocking") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Ad & tracker blocking",
                        settings.adBlockEnabled,
                        { v -> repo.update { it.copy(adBlockEnabled = v) } },
                        "Filter ad and tracker requests while streaming over the network",
                        Icons.Filled.Block,
                    )
                    SettingSwitch(
                        "Link cleaning",
                        settings.linkCleaningEnabled,
                        { v -> repo.update { it.copy(linkCleaningEnabled = v) } },
                        "Strip tracking parameters (utm_*, fbclid, gclid, …) from network links",
                        Icons.Filled.LinkOff,
                    )
                }
            }
            item {
                SettingsGroup {
                    androidx.compose.foundation.layout.Column(
                        Modifier.padding(16.dp),
                    ) {
                        Text("Filter rules loaded", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${AdFilterEngine.ruleCount()} network rules",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${AdFilterEngine.blockedCount} requests blocked so far",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { SectionHeader("Playback") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "Keep screen awake while playing",
                        settings.keepScreenOn,
                        { v -> repo.update { it.copy(keepScreenOn = v) } },
                        "Prevent the screen from sleeping during playback",
                        Icons.Filled.LightMode,
                    )
                    SettingSwitch(
                        "Show decoder badge",
                        settings.showDecoderBadge,
                        { v -> repo.update { it.copy(showDecoderBadge = v) } },
                        "Show the HW badge in the player top bar",
                        Icons.Filled.Memory,
                    )
                }
            }
        }
    }
}
