package com.nijatva.videoplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nijatva_prefs")

internal val appJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private val KEY_SETTINGS = stringPreferencesKey("settings_json")
private val KEY_HISTORY = stringPreferencesKey("history_json")
private val KEY_CONNECTIONS = stringPreferencesKey("connections_json")
private val KEY_PLAYLISTS = stringPreferencesKey("playlists_json")

/** Persists the whole [Settings] object as a JSON blob. */
class SettingsRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        scope.launch {
            context.dataStore.data.collect { prefs ->
                val raw = prefs[KEY_SETTINGS]
                _settings.value = if (raw.isNullOrBlank()) {
                    Settings()
                } else {
                    runCatching { appJson.decodeFromString(Settings.serializer(), raw) }
                        .getOrDefault(Settings())
                }
            }
        }
    }

    /** Applies a change to the current settings and persists the result. */
    fun update(transform: (Settings) -> Settings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        scope.launch {
            context.dataStore.edit { it[KEY_SETTINGS] = appJson.encodeToString(Settings.serializer(), updated) }
        }
    }

    fun exportJson(): String = appJson.encodeToString(Settings.serializer(), _settings.value)

    fun importJson(text: String): Boolean = runCatching {
        val parsed = appJson.decodeFromString(Settings.serializer(), text)
        _settings.value = parsed
        scope.launch {
            context.dataStore.edit { it[KEY_SETTINGS] = appJson.encodeToString(Settings.serializer(), parsed) }
        }
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
            context.dataStore.data.collect { prefs ->
                val raw = prefs[KEY_HISTORY]
                _recent.value = if (raw.isNullOrBlank()) emptyList()
                else runCatching { appJson.decodeFromString(ListSerializer(String.serializer()), raw) }
                    .getOrDefault(emptyList())
            }
        }
    }

    fun add(uri: String) {
        val updated = (listOf(uri) + _recent.value.filterNot { it == uri }).take(50)
        _recent.value = updated
        scope.launch {
            context.dataStore.edit {
                it[KEY_HISTORY] = appJson.encodeToString(
                    ListSerializer(String.serializer()), updated
                )
            }
        }
    }

    fun clear() {
        _recent.value = emptyList()
        scope.launch { context.dataStore.edit { it.remove(KEY_HISTORY) } }
    }
}

/** Stores the user's saved network connections. */
class ConnectionsRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connections = MutableStateFlow<List<NetworkConnection>>(emptyList())
    val connections: StateFlow<List<NetworkConnection>> = _connections.asStateFlow()

    init {
        scope.launch {
            context.dataStore.data.collect { prefs ->
                val raw = prefs[KEY_CONNECTIONS]
                _connections.value = if (raw.isNullOrBlank()) emptyList()
                else runCatching {
                    appJson.decodeFromString(
                        ListSerializer(NetworkConnection.serializer()), raw
                    )
                }.getOrDefault(emptyList())
            }
        }
    }

    fun add(connection: NetworkConnection) {
        val updated = _connections.value.filterNot { it.id == connection.id } + connection
        _connections.value = updated
        persist(updated)
    }

    fun remove(id: String) {
        val updated = _connections.value.filterNot { it.id == id }
        _connections.value = updated
        persist(updated)
    }

    private fun persist(list: List<NetworkConnection>) {
        scope.launch {
            context.dataStore.edit {
                it[KEY_CONNECTIONS] = appJson.encodeToString(
                    ListSerializer(NetworkConnection.serializer()), list
                )
            }
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
            context.dataStore.data.collect { prefs ->
                val raw = prefs[KEY_PLAYLISTS]
                _playlists.value = if (raw.isNullOrBlank()) emptyList()
                else runCatching {
                    appJson.decodeFromString(ListSerializer(Playlist.serializer()), raw)
                }.getOrDefault(emptyList())
            }
        }
    }

    fun create(name: String) {
        val updated = _playlists.value + Playlist(id = java.util.UUID.randomUUID().toString(), name = name)
        persist(updated)
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
            context.dataStore.edit {
                it[KEY_PLAYLISTS] = appJson.encodeToString(ListSerializer(Playlist.serializer()), list)
            }
        }
    }
}
