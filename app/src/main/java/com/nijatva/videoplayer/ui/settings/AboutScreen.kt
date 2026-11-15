package com.nijatva.videoplayer.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nijatva.videoplayer.BuildConfig
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.ZScreen

private const val REPO_URL = "https://github.com/vasudev-work/nijatva-video-player"
private const val PROFILE_URL = "https://github.com/vasudev-work"
private const val WEBSITE = "github.com/vasudev-work"
private const val EMAIL = "vasudev.work@rediffmail.com"

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenLibraries: () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    fun openUrl(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    ZScreen(title = "About", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Developer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "About the developer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            /* ---------- developer card ---------- */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(scheme.surface)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(scheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "VD",
                            color = scheme.onPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Vasudev Dangaya",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "@vasudev-work",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "I build Android apps with clean UI and real, working features.\nKotlin · Android SDK · no unnecessary dependencies.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(18.dp))

                    /* stats */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(scheme.surfaceVariant)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatColumn("28", "Repositories")
                        StatColumn("24", "Apps planned")
                        StatColumn("2", "With code")
                    }

                    Spacer(Modifier.height(18.dp))

                    /* maintains */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Maintains",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        MaintainChip("N")
                        MaintainChip("A")
                        MaintainChip("Q")
                        MaintainChip("+22", muted = true)
                    }

                    Spacer(Modifier.height(16.dp))

                    InfoRow("Location", "India")
                    InfoRow("Website", WEBSITE)
                    InfoRow("Email", EMAIL)

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { openUrl(PROFILE_URL) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF111111),
                        ),
                    ) {
                        Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View GitHub Profile", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            /* ---------- app info ---------- */
            item { SectionHeader("About the app") }
            item {
                SettingsGroup {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "A lightweight, open-source video player for Android, built with Kotlin, Jetpack Compose and AndroidX Media3 (ExoPlayer).",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Version", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Package", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text(BuildConfig.APPLICATION_ID, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("License", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text("Apache License 2.0", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenLibraries)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp), tint = scheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Open-source libraries", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("›", style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }

            /* ---------- footer ---------- */
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Designed & developed by Vasudev Dangaya",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF9933), Color(0xFFFFFFFF), Color(0xFF138808)),
                                ),
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "MAKE IN INDIA",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111),
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MaintainChip(text: String, muted: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (muted) scheme.surfaceVariant else scheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (muted) scheme.onSurfaceVariant else scheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}
