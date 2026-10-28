package com.nijatva.videoplayer

import android.app.Application
import android.content.Context
import com.nijatva.videoplayer.aekant.AdBlocker
import com.nijatva.videoplayer.aekant.AdFilterEngine
import com.nijatva.videoplayer.data.ConnectionsRepository
import com.nijatva.videoplayer.data.HistoryRepository
import com.nijatva.videoplayer.data.MediaRepository
import com.nijatva.videoplayer.data.PlaylistRepository
import com.nijatva.videoplayer.data.SettingsRepository

/** Holds the app-wide repositories. */
class NijatvaApplication : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var connectionsRepository: ConnectionsRepository
        private set
    lateinit var mediaRepository: MediaRepository
        private set
    lateinit var playlistRepository: PlaylistRepository
        private set
    lateinit var adBlocker: AdBlocker
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        historyRepository = HistoryRepository(this)
        connectionsRepository = ConnectionsRepository(this)
        mediaRepository = MediaRepository(this)
        playlistRepository = PlaylistRepository(this)
        adBlocker = AdBlocker(this)
        AdFilterEngine.ensureLoaded(this)
    }
}

val Context.app: NijatvaApplication
    get() = applicationContext as NijatvaApplication
