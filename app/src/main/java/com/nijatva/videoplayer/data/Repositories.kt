package com.nijatva.videoplayer.data

import android.content.Context
import android.content.SharedPreferences
import com.nijatva.videoplayer.data.model.NetworkConnection
import com.nijatva.videoplayer.data.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal val appJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

/* Plain SharedPreferences — no DataStore dependency. */
private const val PREFS_NAME = "nijatva_prefs"
private const val KEY_SETTINGS = "settings_json"
private const val KEY_HISTORY = "history_json"
private const val KEY_CONNECTIONS = "connections_json"
private const val KEY_PLAYLISTS = "playlists_json"

private fun Context.prefs(): SharedPreferences =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

/** Persists the whole [Settings] object as a JSON blob. */
class SettingsRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        scope.launch {
            val raw = context.prefs().getString(KEY_SETTINGS, null)
            if (!raw.isNullOrBlank()) {
                _settings.value = runCatching {
                    appJson.decodeFromString(Settings.serializer(), raw)
                }.getOrDefault(Settings())
            }
        }
    }

    /** Applies a change to the current settings and persists the result. */
    fun update(transform: (Settings) -> Settings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        persist(updated)
    }

    private fun persist(value: Settings) {
        scope.launch {
            context.prefs().edit()
                .putString(KEY_SETTINGS, appJson.encodeToString(Settings.serializer(), value))
                .apply()
        }
    }

    fun exportJson(): String = appJson.encodeToString(Settings.serializer(), _settings.value)

    fun importJson(text: String): Boolean = runCatching {
        val parsed = appJson.decodeFromString(Settings.serializer(), text)
        _settings.value = parsed
        persist(parsed)
        true
    }.getOrDefault(false)
}

/** Stores the list of recently played videos. */
class HistoryRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recent: StateFlow<List<String>> = _recent.asStateFlow()

    init {
        scope.launch {
            val raw = context.prefs().getString(KEY_HISTORY, null)
            if (!raw.isNullOrBlank()) {
                _recent.value = runCatching {
                    appJson.decodeFromString(ListSerializer(String.serializer()), raw)
                }.getOrDefault(emptyList())
            }
        }
    }

    fun add(uri: String) {
        val updated = (listOf(uri) + _recent.value.filterNot { it == uri }).take(50)
        _recent.value = updated
        scope.launch {
            context.prefs().edit()
                .putString(KEY_HISTORY, appJson.encodeToString(ListSerializer(String.serializer()), updated))
                .apply()
        }
    }

    fun clear() {
        _recent.value = emptyList()
        scope.launch { context.prefs().edit().remove(KEY_HISTORY).apply() }
    }
}

/** Stores the user's saved network connections. */
class ConnectionsRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connections = MutableStateFlow<List<NetworkConnection>>(emptyList())
    val connections: StateFlow<List<NetworkConnection>> = _connections.asStateFlow()

    init {
        scope.launch {
            val raw = context.prefs().getString(KEY_CONNECTIONS, null)
            if (!raw.isNullOrBlank()) {
                _connections.value = runCatching {
                    appJson.decodeFromString(ListSerializer(NetworkConnection.serializer()), raw)
                }.getOrDefault(emptyList())
            }
        }
    }

    fun add(connection: NetworkConnection) {
        val updated = _connections.value.filterNot { it.id == connection.id } + connection
        persist(updated)
    }

    fun remove(id: String) {
        persist(_connections.value.filterNot { it.id == id })
    }

    private fun persist(list: List<NetworkConnection>) {
        _connections.value = list
        scope.launch {
            context.prefs().edit()
                .putString(KEY_CONNECTIONS, appJson.encodeToString(ListSerializer(NetworkConnection.serializer()), list))
                .apply()
        }
    }
}

/** Stores the user's playlists. */
class PlaylistRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        scope.launch {
            val raw = context.prefs().getString(KEY_PLAYLISTS, null)
            if (!raw.isNullOrBlank()) {
                _playlists.value = runCatching {
                    appJson.decodeFromString(ListSerializer(Playlist.serializer()), raw)
                }.getOrDefault(emptyList())
            }
        }
    }

    fun create(name: String) {
        persist(_playlists.value + Playlist(id = java.util.UUID.randomUUID().toString(), name = name))
    }

    fun delete(id: String) {
        persist(_playlists.value.filterNot { it.id == id })
    }

    fun rename(id: String, name: String) {
        persist(_playlists.value.map { if (it.id == id) it.copy(name = name) else it })
    }

    fun addVideo(id: String, videoId: Long) {
        persist(_playlists.value.map { if (it.id == id) it.copy(videoIds = it.videoIds + videoId) else it })
    }

    fun removeVideo(id: String, videoId: Long) {
        persist(_playlists.value.map { if (it.id == id) it.copy(videoIds = it.videoIds - videoId) else it })
    }

    private fun persist(list: List<Playlist>) {
        _playlists.value = list
        scope.launch {
            context.prefs().edit()
                .putString(KEY_PLAYLISTS, appJson.encodeToString(ListSerializer(Playlist.serializer()), list))
                .apply()
        }
    }
}
