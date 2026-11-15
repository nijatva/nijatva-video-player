package com.nijatva.videoplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nijatva.videoplayer.ui.AppNavigation
import com.nijatva.videoplayer.ui.theme.NijatvaTheme

/** Single-activity entry point. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialUri: String? = intent
            ?.takeIf { it.action == Intent.ACTION_VIEW }
            ?.data
            ?.toString()

        val app = application as NijatvaApplication

        setContent {
            val settings by app.settingsRepository.settings.collectAsState()
            NijatvaTheme(
                themeMode = settings.themeMode,
                accent = settings.accent,
                dynamicColor = settings.dynamicColor,
            ) {
                AppNavigation(initialUri = initialUri)
            }
        }
    }
}
