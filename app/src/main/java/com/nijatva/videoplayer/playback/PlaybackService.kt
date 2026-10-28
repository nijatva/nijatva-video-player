package com.nijatva.videoplayer.playback

import android.content.Intent
import android.media.audiofx.LoudnessEnhancer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nijatva.videoplayer.NijatvaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * Hosts the player so playback keeps going in the background. Media3 publishes
 * the media notification and holds the foreground service for us.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var boostEnabled = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        val app = application as NijatvaApplication
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AdBlockInterceptor(app.adBlocker) { app.settingsRepository.settings.value.adBlockEnabled })
            .build()
        val dataSourceFactory = DefaultDataSource.Factory(this, OkHttpDataSource.Factory(okHttpClient))
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                attachEnhancer(audioSessionId)
            }
        })

        // Keep the volume-boost effect in step with the setting.
        scope.launch {
            app.settingsRepository.settings.collect { settings ->
                boostEnabled = settings.volumeBoost
                applyBoost()
            }
        }

        mediaSession = MediaSession.Builder(this, player).build()
    }

    private fun attachEnhancer(sessionId: Int) {
        runCatching { loudnessEnhancer?.release() }
        loudnessEnhancer = if (sessionId != C.AUDIO_SESSION_ID_UNSET) {
            runCatching { LoudnessEnhancer(sessionId) }.getOrNull()
        } else {
            null
        }
        applyBoost()
    }

    private fun applyBoost() {
        val enhancer = loudnessEnhancer ?: return
        runCatching {
            if (boostEnabled) {
                enhancer.setTargetGain(1000) // +10 dB, roughly "up to 200%" perceived
                enhancer.enabled = true
            } else {
                enhancer.enabled = false
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        runCatching { loudnessEnhancer?.release() }
        loudnessEnhancer = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
