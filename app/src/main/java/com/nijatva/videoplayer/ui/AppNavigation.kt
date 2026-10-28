package com.nijatva.videoplayer.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.home.FolderScreen
import com.nijatva.videoplayer.ui.home.FoldersScreen
import com.nijatva.videoplayer.ui.home.HomeScreen
import com.nijatva.videoplayer.ui.home.VideosScreen
import com.nijatva.videoplayer.ui.lock.LockScreen
import com.nijatva.videoplayer.ui.more.HistoryScreen
import com.nijatva.videoplayer.ui.more.PlaylistDetailScreen
import com.nijatva.videoplayer.ui.more.PlaylistListScreen
import com.nijatva.videoplayer.ui.more.VaultScreen
import com.nijatva.videoplayer.ui.network.NetworkScreen
import com.nijatva.videoplayer.ui.player.PlayerScreen
import com.nijatva.videoplayer.ui.search.SearchScreen
import com.nijatva.videoplayer.ui.youtube.YouTubeScreen
import com.nijatva.videoplayer.ui.settings.AboutScreen
import com.nijatva.videoplayer.ui.settings.AdvancedSettingsScreen
import com.nijatva.videoplayer.ui.settings.AppearanceScreen
import com.nijatva.videoplayer.ui.settings.AudioScreen
import com.nijatva.videoplayer.ui.settings.FolderExcludeScreen
import com.nijatva.videoplayer.ui.settings.GeneralScreen
import com.nijatva.videoplayer.ui.settings.GesturesScreen
import com.nijatva.videoplayer.ui.settings.LibrariesScreen
import com.nijatva.videoplayer.ui.settings.MediaLibraryScreen
import com.nijatva.videoplayer.ui.settings.PlayerSettingsScreen
import com.nijatva.videoplayer.ui.settings.PrivacyScreen
import com.nijatva.videoplayer.ui.settings.SettingsScreen
import com.nijatva.videoplayer.ui.settings.SubtitleScreen
import com.nijatva.videoplayer.util.youtubeVideoId

object Routes {
    // Bottom-navigation tabs
    const val HOME = "home"
    const val LIBRARY = "library"
    const val FOLDERS = "folders"
    const val PLAYLISTS = "playlists"
    const val SETTINGS = "settings"

    // Other destinations
    const val SEARCH = "search"
    const val VAULT = "vault"
    const val HISTORY = "history"
    const val NETWORK = "network"

    const val APPEARANCE = "settings/appearance"
    const val MEDIA_LIBRARY = "settings/media_library"
    const val PLAYER_SETTINGS = "settings/player"
    const val GESTURES = "settings/gestures"
    const val AUDIO = "settings/audio"
    const val SUBTITLE = "settings/subtitle"
    const val GENERAL = "settings/general"
    const val ADVANCED = "settings/advanced"
    const val ABOUT = "settings/about"
    const val LIBRARIES = "settings/libraries"
    const val FOLDER_EXCLUDE = "settings/folders"
    const val PRIVACY = "settings/privacy"

    const val FOLDER = "folder/{folderId}?name={name}"
    fun folder(folderId: Long, name: String) = "folder/$folderId?name=${Uri.encode(name)}"

    const val PLAYLIST_DETAIL = "playlist/{playlistId}?name={name}"
    fun playlistDetail(id: String, name: String) = "playlist/$id?name=${Uri.encode(name)}"

    const val YOUTUBE = "youtube/{videoId}?title={title}"
    fun youtube(videoId: String, title: String) = "youtube/$videoId?title=${Uri.encode(title)}"

    const val PLAYER = "player?uri={uri}&title={title}&folderId={folderId}"
    fun player(uri: String, title: String, folderId: Long) =
        "player?uri=${Uri.encode(uri)}&title=${Uri.encode(title)}&folderId=$folderId"
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomItem(Routes.LIBRARY, "Library", Icons.Filled.VideoLibrary),
    BottomItem(Routes.FOLDERS, "Folders", Icons.Filled.Folder),
    BottomItem(Routes.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.PlaylistPlay),
    BottomItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun AppNavigation(initialUri: String? = null) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()

    // App lock: gate everything behind the PIN when it is enabled.
    var unlocked by remember { mutableStateOf(!settings.appLockEnabled) }
    LaunchedEffect(settings.appLockEnabled) {
        if (!settings.appLockEnabled) unlocked = true
    }
    if (!unlocked) {
        LockScreen(onUnlocked = { unlocked = true })
        return
    }

    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(initialUri) {
        if (!initialUri.isNullOrBlank()) {
            val id = youtubeVideoId(initialUri)
            if (id != null) {
                navController.navigate(Routes.youtube(id, "YouTube"))
            } else {
                navController.navigate(Routes.player(initialUri, "Video", -1L))
            }
        }
    }
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenFolder = { id, name -> navController.navigate(Routes.folder(id, name)) },
                    onOpenVideo = { uri, title, folderId -> navController.navigate(Routes.player(uri, title, folderId)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                )
            }
            composable(Routes.LIBRARY) {
                VideosScreen(
                    onOpenVideo = { uri, title, folderId -> navController.navigate(Routes.player(uri, title, folderId)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenVault = { navController.navigate(Routes.VAULT) },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) },
                )
            }
            composable(Routes.FOLDERS) {
                FoldersScreen(onOpenFolder = { id, name -> navController.navigate(Routes.folder(id, name)) })
            }
            composable(Routes.PLAYLISTS) {
                PlaylistListScreen(
                    onBack = null,
                    onOpenPlaylist = { id, name -> navController.navigate(Routes.playlistDetail(id, name)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = null,
                    onOpen = { navController.navigate(it) },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { uri, title, folderId -> navController.navigate(Routes.player(uri, title, folderId)) },
                )
            }
            composable(Routes.VAULT) {
                VaultScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { uri, title -> navController.navigate(Routes.player(uri, title, -1L)) },
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { uri, title -> navController.navigate(Routes.player(uri, title, -1L)) },
                )
            }
            composable(Routes.NETWORK) {
                NetworkScreen(
                    onOpenStream = { uri ->
                        val id = youtubeVideoId(uri)
                        if (id != null) {
                            navController.navigate(Routes.youtube(id, "YouTube"))
                        } else {
                            navController.navigate(Routes.player(uri, "Network stream", -1L))
                        }
                    },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }

            composable(
                route = Routes.FOLDER,
                arguments = listOf(
                    navArgument("folderId") { type = NavType.LongType },
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                val folderId = entry.arguments?.getLong("folderId") ?: -1L
                FolderScreen(
                    folderId = folderId,
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { uri, title -> navController.navigate(Routes.player(uri, title, folderId)) },
                )
            }

            composable(
                route = Routes.PLAYLIST_DETAIL,
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                PlaylistDetailScreen(
                    playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { uri, title, folderId -> navController.navigate(Routes.player(uri, title, folderId)) },
                )
            }

            composable(
                route = Routes.YOUTUBE,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                YouTubeScreen(
                    videoId = entry.arguments?.getString("videoId").orEmpty(),
                    title = entry.arguments?.getString("title").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.PLAYER,
                arguments = listOf(
                    navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    navArgument("folderId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { entry ->
                PlayerScreen(
                    uri = entry.arguments?.getString("uri").orEmpty(),
                    title = entry.arguments?.getString("title").orEmpty(),
                    folderId = entry.arguments?.getLong("folderId") ?: -1L,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.APPEARANCE) { AppearanceScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MEDIA_LIBRARY) {
                MediaLibraryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFolders = { navController.navigate(Routes.FOLDER_EXCLUDE) },
                )
            }
            composable(Routes.FOLDER_EXCLUDE) { FolderExcludeScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PLAYER_SETTINGS) { PlayerSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.GESTURES) { GesturesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.AUDIO) { AudioScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SUBTITLE) { SubtitleScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.GENERAL) { GeneralScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ADVANCED) { AdvancedSettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLibraries = { navController.navigate(Routes.LIBRARIES) },
                )
            }
            composable(Routes.LIBRARIES) { LibrariesScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
