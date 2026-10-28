package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.ZScreen

private data class Library(val name: String, val license: String)

private val libraries = listOf(
    Library("Kotlin", "Apache License 2.0"),
    Library("AndroidX Core", "Apache License 2.0"),
    Library("Jetpack Compose (UI, Material 3)", "Apache License 2.0"),
    Library("AndroidX Activity Compose", "Apache License 2.0"),
    Library("AndroidX Lifecycle", "Apache License 2.0"),
    Library("AndroidX Navigation Compose", "Apache License 2.0"),
    Library("AndroidX DataStore", "Apache License 2.0"),
    Library("AndroidX Media3 (ExoPlayer)", "Apache License 2.0"),
    Library("kotlinx.coroutines", "Apache License 2.0"),
    Library("kotlinx.serialization", "Apache License 2.0"),
    Library("Material Icons Extended", "Apache License 2.0"),
)

@Composable
fun LibrariesScreen(onBack: () -> Unit) {
    ZScreen(title = "Libraries", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Open-source libraries") }
            item {
                SettingsGroup {
                    libraries.forEach { library ->
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(library.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                library.license,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Video Player is licensed under the Apache License 2.0. Full license texts are available from each project.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
