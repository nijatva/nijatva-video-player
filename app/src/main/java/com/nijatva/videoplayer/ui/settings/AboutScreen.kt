package com.nijatva.videoplayer.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.BuildConfig
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.ZScreen

private const val REPO_URL = "https://github.com/vasudev-work/nijatva-video-player"

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenLibraries: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    ZScreen(title = "About", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF10B981), Color(0xFF1D4ED8)),
                            ),
                        )
                        .padding(16.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF121212)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Video Player",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) by Vasudev Dangaya",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onOpenLibraries,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFA7F3D0),
                                    contentColor = Color(0xFF052E16),
                                ),
                            ) {
                                Icon(Icons.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Libraries", fontWeight = FontWeight.SemiBold)
                            }
                            FilledTonalButton(
                                onClick = { openUrl(REPO_URL) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFBFDBFE),
                                    contentColor = Color(0xFF0B2A5B),
                                ),
                            ) {
                                Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Github", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("About") }
            item {
                SettingsGroup {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "A lightweight, open-source video player for Android, built with Kotlin, Jetpack Compose and AndroidX Media3 (ExoPlayer).",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Package", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(BuildConfig.APPLICATION_ID, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("License", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Apache License 2.0", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Copyright", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("© 2026 Vasudev Dangaya", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
