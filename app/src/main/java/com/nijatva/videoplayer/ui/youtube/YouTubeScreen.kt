package com.nijatva.videoplayer.ui.youtube

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.nijatva.videoplayer.ui.components.ZScreen

/**
 * Plays a YouTube video with the official IFrame embed. This is the only
 * ToS-compliant way for a third-party app to play YouTube content — videos
 * that disallow embedding (or are age-restricted) will not play.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeScreen(videoId: String, title: String, onBack: () -> Unit) {
    ZScreen(title = title.ifBlank { "YouTube" }, onBack = onBack) { modifier ->
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadsImagesAutomatically = true
                    webViewClient = WebViewClient()
                    loadUrl(
                        "https://www.youtube.com/embed/$videoId" +
                            "?autoplay=1&playsinline=1&rel=0&modestbranding=1",
                    )
                }
            },
        )
    }
}
