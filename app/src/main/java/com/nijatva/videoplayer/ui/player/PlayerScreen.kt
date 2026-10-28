package com.nijatva.videoplayer.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Rational
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.nijatva.videoplayer.aekant.UrlCleaner
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.VideoItem
import com.nijatva.videoplayer.playback.PlaybackService
import com.nijatva.videoplayer.util.formatDuration
import com.nijatva.videoplayer.util.formatSpeed
import com.nijatva.videoplayer.util.startVideoDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private const val SEEK_STEP_MS = 10_000L
private val SPEED_OPTIONS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f, 4f)

@Composable
private fun rememberMediaController(context: Context): MediaController? {
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var future by remember { mutableStateOf<ListenableFuture<MediaController>?>(null) }

    LaunchedEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val f = MediaController.Builder(context, token).buildAsync()
        future = f
        controller = withContext(Dispatchers.IO) { runCatching { f.get() }.getOrNull() }
    }

    DisposableEffect(Unit) {
        onDispose {
            future?.let { MediaController.releaseFuture(it) }
            controller = null
        }
    }
    return controller
}

@Composable
fun PlayerScreen(
    uri: String,
    title: String,
    folderId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val controller = rememberMediaController(context)

    if (controller == null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    PlayerContent(mediaPlayer = controller, uri = uri, title = title, folderId = folderId, onBack = onBack)
}

@androidx.annotation.OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerContent(
    mediaPlayer: MediaController,
    uri: String,
    title: String,
    folderId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val settings by app.settingsRepository.settings.collectAsState()
    val activity = context.findActivity()

    var playlist by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(-1) }
    var currentUri by remember { mutableStateOf(uri) }
    var currentTitle by remember { mutableStateOf(title) }

    var scale by remember { mutableFloatStateOf(1f) }
    var hud by remember { mutableStateOf<String?>(null) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(1f) }

    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var immersive by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var shuffleOn by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(false) }
    var showAudioTracks by remember { mutableStateOf(false) }
    var showSubtitles by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var sleepEndsAt by remember { mutableStateOf<Long?>(null) }
    var pointA by remember { mutableStateOf<Long?>(null) }
    var pointB by remember { mutableStateOf<Long?>(null) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    val prefs = remember { context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE) }

    LaunchedEffect(folderId) {
        if (folderId >= 0) {
            playlist = app.mediaRepository.loadVideos().filter { it.folderId == folderId }
            currentIndex = playlist.indexOfFirst { it.uri.toString() == uri }
        }
    }

    LaunchedEffect(currentUri) {
        mediaPlayer.setMediaItem(
            buildMediaItem(context, currentUri, settings.autoLoadSubtitles, settings.linkCleaningEnabled),
        )
        mediaPlayer.prepare()
        mediaPlayer.playWhenReady = true
        val remembered = prefs.getFloat("speed:$currentUri", settings.defaultSpeed)
        speed = remembered
        mediaPlayer.setPlaybackSpeed(remembered)
        mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !settings.autoLoadSubtitles)
            .build()
        if (settings.resume) {
            val saved = prefs.getLong(currentUri, 0L)
            if (saved > 0) mediaPlayer.seekTo(saved)
        }
        app.historyRepository.add(currentUri)
        controlsVisible = true
    }

    DisposableEffect(mediaPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    prefs.edit().remove(currentUri).apply()
                    if (settings.autoplay && currentIndex >= 0 && currentIndex + 1 < playlist.size) {
                        val next = playlist[currentIndex + 1]
                        currentIndex += 1
                        currentUri = next.uri.toString()
                        currentTitle = next.name
                    }
                }
            }
        }
        mediaPlayer.addListener(listener)
        onDispose {
            prefs.edit().putLong(currentUri, mediaPlayer.currentPosition).apply()
            mediaPlayer.removeListener(listener)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(mediaPlayer, lifecycleOwner, settings.backgroundPlay) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !settings.backgroundPlay) mediaPlayer.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(settings.orientation) {
        activity?.requestedOrientation = when (settings.orientation) {
            1 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(immersive) {
        activity?.let { act ->
            val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (immersive) controller.hide(WindowInsetsCompat.Type.systemBars())
            else controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(settings.keepScreenOn, isPlaying) {
        activity?.window?.let { win ->
            if (settings.keepScreenOn && isPlaying) {
                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.let { act ->
                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(mediaPlayer) {
        while (true) {
            positionMs = mediaPlayer.currentPosition
            durationMs = mediaPlayer.duration.coerceAtLeast(0L)
            isPlaying = mediaPlayer.isPlaying
            repeatMode = mediaPlayer.repeatMode
            shuffleOn = mediaPlayer.shuffleModeEnabled
            val a = pointA
            val b = pointB
            if (a != null && b != null && positionMs >= b) mediaPlayer.seekTo(a)
            delay(500)
        }
    }

    LaunchedEffect(sleepEndsAt) {
        val end = sleepEndsAt ?: return@LaunchedEffect
        while (true) {
            if (System.currentTimeMillis() >= end) {
                mediaPlayer.pause()
                sleepEndsAt = null
                hud = "Sleep timer finished"
                break
            }
            delay(1000)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, locked, settings.controllerTimeoutMs) {
        if (controlsVisible && isPlaying && !locked) {
            delay(settings.controllerTimeoutMs.toLong())
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(locked, settings.doubleTapGesture, settings.longPressGesture) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible; hud = null },
                    onDoubleTap = {
                        if (!locked && settings.doubleTapGesture) {
                            if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.play()
                        }
                    },
                    onLongPress = {
                        if (!locked && settings.longPressGesture) {
                            mediaPlayer.setPlaybackSpeed(2f)
                            hud = "2.0x"
                        }
                    },
                )
            }
            .pointerInput(locked, settings.seekSensitivity, settings.brightnessSensitivity, settings.volumeSensitivity) {
                if (locked) return@pointerInput
                var mode = 0
                var accumulated = 0f
                var startX = 0f
                var startVolume = 0
                var startBrightness = 0.5f
                detectDragGestures(
                    onDragStart = { offset ->
                        mode = 0
                        accumulated = 0f
                        startX = offset.x
                        startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        startBrightness = activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (mode == 0) mode = if (abs(dragAmount.x) > abs(dragAmount.y)) 1 else 2
                        when (mode) {
                            1 -> {
                                accumulated += dragAmount.x
                                val delta = (accumulated * 120f * settings.seekSensitivity).toLong()
                                hud = formatDuration((positionMs + delta).coerceAtLeast(0L))
                            }
                            2 -> {
                                if (startX < size.width / 2f) {
                                    val target = startBrightness - dragAmount.y / size.height
                                    val clamped = target.coerceIn(0.02f, 1f)
                                    activity?.window?.let { win ->
                                        win.attributes = win.attributes.apply { screenBrightness = clamped }
                                    }
                                    hud = "Brightness ${(clamped * 100).toInt()}%"
                                } else {
                                    val target = startVolume - (dragAmount.y / size.height) * maxVolume
                                    val clamped = target.toInt().coerceIn(0, maxVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
                                    hud = "Volume ${clamped * 100 / maxVolume}%"
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        if (mode == 1) {
                            val delta = (accumulated * 120f * settings.seekSensitivity).toLong()
                            mediaPlayer.seekTo(
                                (mediaPlayer.currentPosition + delta).coerceIn(0L, mediaPlayer.duration.coerceAtLeast(0L)),
                            )
                        }
                        mode = 0
                        accumulated = 0f
                        hud = null
                    },
                )
            }
            .pointerInput(locked, settings.zoomGesture) {
                if (!locked && settings.zoomGesture) {
                    detectTransformGestures { _, _, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 4f) }
                }
            },
    ) {
        AndroidView(
            factory = { ctx ->
                val initialResize = resizeMode
                PlayerView(ctx).apply {
                    player = mediaPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = initialResize
                }
            },
            update = {
                it.player = mediaPlayer
                it.resizeMode = resizeMode
                it.subtitleView?.setFractionalTextSize(0.0533f * (settings.subtitleTextSize / 16f))
            },
            modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale),
        )

        hud?.let { text ->
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        if (locked) {
            IconButton(
                onClick = { locked = false; controlsVisible = true },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Unlock", tint = Color.White)
            }
        } else if (controlsVisible) {
            PlayerControls(
                title = currentTitle,
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                scrubbing = scrubbing,
                scrubValue = scrubValue,
                speed = speed,
                backgroundPlay = settings.backgroundPlay,
                pip = settings.pip,
                hasPlaylist = playlist.isNotEmpty(),
                sleepActive = sleepEndsAt != null,
                abActive = pointA != null && pointB != null,
                repeatMode = repeatMode,
                shuffleOn = shuffleOn,
                showDecoderBadge = settings.showDecoderBadge,
                canDownload = currentUri.startsWith("http"),
                immersive = immersive,
                onBack = onBack,
                onTogglePlay = { if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.play() },
                onRewind = { mediaPlayer.seekTo((mediaPlayer.currentPosition - SEEK_STEP_MS).coerceAtLeast(0L)) },
                onForward = {
                    mediaPlayer.seekTo(
                        (mediaPlayer.currentPosition + SEEK_STEP_MS).coerceAtMost(mediaPlayer.duration.coerceAtLeast(0L)),
                    )
                },
                onScrub = { scrubbing = true; scrubValue = it },
                onScrubFinished = { mediaPlayer.seekTo(scrubValue.toLong()); scrubbing = false },
                onSubtitles = { showSubtitles = true },
                onAudioTrack = { showAudioTracks = true },
                onCast = {
                    runCatching { context.startActivity(Intent("android.settings.CAST_SETTINGS")) }
                        .onFailure {
                            runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)) }
                        }
                },
                onPlaylist = { showPlaylist = true },
                onRepeat = {
                    val next = (mediaPlayer.repeatMode + 1) % 3
                    mediaPlayer.repeatMode = next
                    hud = when (next) {
                        Player.REPEAT_MODE_ONE -> "Repeat one"
                        Player.REPEAT_MODE_ALL -> "Repeat all"
                        else -> "Repeat off"
                    }
                },
                onSleep = { showSleepTimer = true },
                onMarkA = { pointA = mediaPlayer.currentPosition; hud = "A set" },
                onMarkB = {
                    if (pointA != null) {
                        pointB = mediaPlayer.currentPosition
                        hud = "A-B repeat on"
                    } else {
                        hud = "Set A first"
                    }
                },
                onClearAb = { pointA = null; pointB = null; hud = "A-B cleared" },
                onBackground = { app.settingsRepository.update { it.copy(backgroundPlay = !it.backgroundPlay) } },
                onShuffle = {
                    mediaPlayer.shuffleModeEnabled = !mediaPlayer.shuffleModeEnabled
                    hud = if (mediaPlayer.shuffleModeEnabled) "Shuffle on" else "Shuffle off"
                },
                onLock = { locked = true; controlsVisible = false },
                onRotate = { app.settingsRepository.update { it.copy(orientation = (it.orientation + 1) % 3) } },
                onSpeed = { showSpeed = true },
                onResize = {
                    resizeMode = when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                onPip = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity?.let { act ->
                            act.enterPictureInPictureMode(
                                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build(),
                            )
                        }
                    }
                },
                onFullscreen = { immersive = !immersive },
                onDownload = {
                    startVideoDownload(context, currentUri, currentTitle)
                    hud = "Download started"
                },
                onClose = onBack,
            )
        }
    }

    if (showSpeed) {
        ModalBottomSheet(onDismissRequest = { showSpeed = false }) {
            Text(
                "Playback speed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
            SPEED_OPTIONS.forEach { option ->
                Text(
                    if (abs(option - speed) < 0.001f) "${formatSpeed(option)}x  ✓" else "${formatSpeed(option)}x",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            speed = option
                            mediaPlayer.setPlaybackSpeed(option)
                            prefs.edit().putFloat("speed:$currentUri", option).apply()
                            showSpeed = false
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPlaylist) {
        ModalBottomSheet(onDismissRequest = { showPlaylist = false }, sheetState = rememberModalBottomSheetState()) {
            Text(
                "Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(playlist) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentIndex = index
                                currentUri = item.uri.toString()
                                currentTitle = item.name
                                showPlaylist = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (index == currentIndex) Icons.Filled.PlayArrow else Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (index == currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                }
            }
        }
    }

    if (showAudioTracks) {
        val groups = mediaPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
        ModalBottomSheet(onDismissRequest = { showAudioTracks = false }) {
            Text(
                "Audio track",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
            if (groups.isEmpty()) {
                Text("No selectable audio tracks", modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                groups.forEachIndexed { index, group ->
                    Text(
                        "Track ${index + 1}${if (group.isSelected) "  ✓" else ""}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, 0))
                                    .build()
                                showAudioTracks = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showSubtitles) {
        val textGroups = mediaPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
        val subtitlesOff = mediaPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
        ModalBottomSheet(onDismissRequest = { showSubtitles = false }) {
            Text(
                "Subtitles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Subtitles", modifier = Modifier.weight(1f))
                Switch(
                    checked = !subtitlesOff,
                    onCheckedChange = { on ->
                        mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !on)
                            .build()
                    },
                )
            }
            if (textGroups.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                textGroups.forEachIndexed { index, group ->
                    Text(
                        "Track ${index + 1}${if (group.isSelected) "  ✓" else ""}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, 0))
                                    .build()
                                showSubtitles = false
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Text size: ${settings.subtitleTextSize} sp",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Slider(
                value = settings.subtitleTextSize.toFloat(),
                onValueChange = { v -> app.settingsRepository.update { it.copy(subtitleTextSize = v.toInt()) } },
                valueRange = 8f..32f,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showSleepTimer) {
        AlertDialog(
            onDismissRequest = { showSleepTimer = false },
            title = { Text("Sleep timer") },
            text = {
                Column {
                    listOf(0, 5, 10, 15, 30, 60).forEach { minutes ->
                        Text(
                            if (minutes == 0) "Off" else "$minutes minutes",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sleepEndsAt = if (minutes == 0) null
                                    else System.currentTimeMillis() + minutes * 60_000L
                                    showSleepTimer = false
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSleepTimer = false }) { Text("Cancel") } },
        )
    }
}

private fun buildMediaItem(
    context: Context,
    uriString: String,
    autoSubtitle: Boolean,
    cleanLinks: Boolean,
): MediaItem {
    val effective = if (cleanLinks && (uriString.startsWith("http://") || uriString.startsWith("https://"))) {
        UrlCleaner.clean(uriString)
    } else {
        uriString
    }
    val builder = MediaItem.Builder().setUri(effective)
    if (autoSubtitle) {
        val subtitle = findSidecarSubtitle(context, effective)
        if (subtitle != null) {
            builder.setSubtitleConfigurations(
                listOf(
                    MediaItem.SubtitleConfiguration.Builder(subtitle)
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build(),
                ),
            )
        }
    }
    return builder.build()
}

private fun findSidecarSubtitle(context: Context, uriString: String): Uri? {
    val path = when {
        uriString.startsWith("file://") -> uriString.removePrefix("file://")
        uriString.startsWith("content://") -> runCatching {
            context.contentResolver.query(
                Uri.parse(uriString),
                arrayOf(MediaStore.Video.Media.DATA),
                null, null, null,
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull()
        else -> null
    } ?: return null

    val base = path.substringBeforeLast('.')
    for (ext in listOf("srt", "vtt", "ass")) {
        val candidate = File("$base.$ext")
        if (candidate.exists()) return Uri.fromFile(candidate)
    }
    return null
}

@Composable
private fun PlayerControls(
    title: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    scrubbing: Boolean,
    scrubValue: Float,
    speed: Float,
    backgroundPlay: Boolean,
    pip: Boolean,
    hasPlaylist: Boolean,
    sleepActive: Boolean,
    abActive: Boolean,
    repeatMode: Int,
    shuffleOn: Boolean,
    showDecoderBadge: Boolean,
    canDownload: Boolean,
    immersive: Boolean,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
    onSubtitles: () -> Unit,
    onAudioTrack: () -> Unit,
    onCast: () -> Unit,
    onPlaylist: () -> Unit,
    onRepeat: () -> Unit,
    onSleep: () -> Unit,
    onMarkA: () -> Unit,
    onMarkB: () -> Unit,
    onClearAb: () -> Unit,
    onBackground: () -> Unit,
    onShuffle: () -> Unit,
    onLock: () -> Unit,
    onRotate: () -> Unit,
    onSpeed: () -> Unit,
    onResize: () -> Unit,
    onPip: () -> Unit,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    onClose: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        // Top bar: back, title, subtitles (CC), audio, more
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            if (showDecoderBadge) {
                Text(
                    "HW",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCast) {
                Icon(Icons.Filled.Cast, contentDescription = "Cast", tint = Color.White)
            }
            IconButton(onClick = onSubtitles) {
                Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = Color.White)
            }
            IconButton(onClick = onAudioTrack) {
                Icon(Icons.Filled.MusicNote, contentDescription = "Audio track", tint = Color.White)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Playlist") },
                        enabled = hasPlaylist,
                        onClick = { menuOpen = false; onPlaylist() },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (repeatMode) {
                                    Player.REPEAT_MODE_ONE -> "Repeat: one"
                                    Player.REPEAT_MODE_ALL -> "Repeat: all"
                                    else -> "Repeat: off"
                                },
                            )
                        },
                        onClick = { menuOpen = false; onRepeat() },
                    )
                    DropdownMenuItem(
                        text = { Text(if (sleepActive) "Sleep timer: on" else "Sleep timer") },
                        onClick = { menuOpen = false; onSleep() },
                    )
                    DropdownMenuItem(text = { Text("Set A") }, onClick = { menuOpen = false; onMarkA() })
                    DropdownMenuItem(text = { Text("Set B") }, onClick = { menuOpen = false; onMarkB() })
                    if (abActive) {
                        DropdownMenuItem(text = { Text("Clear A-B") }, onClick = { menuOpen = false; onClearAb() })
                    }
                    DropdownMenuItem(
                        text = { Text(if (backgroundPlay) "Background play: on" else "Background play: off") },
                        onClick = { menuOpen = false; onBackground() },
                    )
                    DropdownMenuItem(
                        text = { Text(if (shuffleOn) "Shuffle: on" else "Shuffle: off") },
                        onClick = { menuOpen = false; onShuffle() },
                    )
                    DropdownMenuItem(
                        text = { Text("Download") },
                        enabled = canDownload,
                        onClick = { menuOpen = false; onDownload() },
                    )
                    DropdownMenuItem(text = { Text("Close player") }, onClick = { menuOpen = false; onClose() })
                }
            }
        }

        // Centre transport: 10s back, play/pause, 10s forward
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRewind) {
                Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10 seconds", tint = Color.White, modifier = Modifier.size(34.dp))
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play or pause",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }
            IconButton(onClick = onForward) {
                Icon(Icons.Filled.Forward10, contentDescription = "Forward 10 seconds", tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }

        // Bottom: time, seek bar, control row
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDuration(if (scrubbing) scrubValue.toLong() else positionMs),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(formatDuration(durationMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            Slider(
                value = if (scrubbing) scrubValue else positionMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                onValueChange = onScrub,
                onValueChangeFinished = onScrubFinished,
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onLock) {
                    Icon(Icons.Filled.LockOpen, contentDescription = "Lock", tint = Color.White)
                }
                IconButton(onClick = onRotate) {
                    Icon(Icons.Filled.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
                }
                TextButton(onClick = onSpeed) {
                    Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${formatSpeed(speed)}x", color = Color.White)
                }
                IconButton(onClick = onResize) {
                    Icon(Icons.Filled.AspectRatio, contentDescription = "Aspect ratio", tint = Color.White)
                }
                if (pip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    IconButton(onClick = onPip) {
                        Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "Picture in picture", tint = Color.White)
                    }
                }
                IconButton(onClick = onFullscreen) {
                    Icon(
                        Icons.Filled.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = if (immersive) MaterialTheme.colorScheme.primary else Color.White,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
