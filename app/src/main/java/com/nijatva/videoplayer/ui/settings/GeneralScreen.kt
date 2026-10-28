package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingClick
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen

@Composable
fun GeneralScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { u ->
            val text = runCatching {
                context.contentResolver.openInputStream(u)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (!text.isNullOrBlank()) repo.importJson(text)
        }
    }

    ZScreen(title = "General", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("User data options") }
            item {
                SettingsGroup {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Name", "Date", "Size", "Duration").forEachIndexed { index, label ->
                            FilterChip(
                                selected = settings.sortBy == index,
                                onClick = { repo.update { it.copy(sortBy = index) } },
                                label = { Text(label) },
                            )
                        }
                    }
                    SettingSwitch(
                        "Sort ascending",
                        settings.sortAscending,
                        { v -> repo.update { it.copy(sortAscending = v) } },
                        "Sort media in ascending order",
                        Icons.Filled.Sort,
                    )
                    SettingSwitch(
                        "Default grid view",
                        settings.gridView,
                        { v -> repo.update { it.copy(gridView = v) } },
                        "Show the media library as a grid",
                        Icons.Filled.GridView,
                    )
                    SettingClick(
                        "Back up settings",
                        {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TEXT, repo.exportJson())
                            }
                            context.startActivity(Intent.createChooser(intent, "Export settings"))
                        },
                        "Share your settings as JSON",
                        Icons.Filled.Upload,
                    )
                    SettingClick(
                        "Restore settings",
                        { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        "Import settings from a JSON file",
                        Icons.Filled.Download,
                    )
                }
            }
        }
    }
}
