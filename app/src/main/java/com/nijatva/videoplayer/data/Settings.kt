package com.nijatva.videoplayer.data

import kotlinx.serialization.Serializable

/** All user-configurable options, persisted as a single JSON document. */
@Serializable
data class Settings(
    // Appearance
    val themeMode: Int = 0,              // 0 = system, 1 = light, 2 = dark
    val highContrastDark: Boolean = false,
    val dynamicColor: Boolean = false,

    // Media library
    val markLastPlayed: Boolean = true,
    val thumbnailPosition: Int = 0,      // percent 0..100

    // Player
    val materialYouControls: Boolean = false,
    val controllerTimeoutMs: Int = 4000,
    val resume: Boolean = true,
    val defaultSpeed: Float = 1.0f,
    val autoplay: Boolean = true,
    val pip: Boolean = true,
    val backgroundPlay: Boolean = false,
    val rememberBrightness: Boolean = false,
    val rememberSelections: Boolean = true,
    val orientation: Int = 0,            // 0 = video orientation, 1 = portrait, 2 = landscape

    // Gestures
    val seekGesture: Boolean = true,
    val seekSensitivity: Float = 0.5f,
    val brightnessGesture: Boolean = true,
    val brightnessSensitivity: Float = 0.5f,
    val volumeGesture: Boolean = true,
    val volumeSensitivity: Float = 0.5f,
    val zoomGesture: Boolean = true,
    val panGesture: Boolean = false,
    val doubleTapGesture: Boolean = true,
    val longPressGesture: Boolean = false,
    val seekIncrementMs: Int = 10_000,

    // Audio
    val requireAudioFocus: Boolean = true,
    val pauseOnDisconnect: Boolean = true,
    val systemVolumePanel: Boolean = true,
    val volumeBoost: Boolean = false,

    // Subtitles
    val autoLoadSubtitles: Boolean = true,
    val subtitleTextSize: Int = 16,

    // General
    val sortBy: Int = 0,                 // 0 = name, 1 = date, 2 = size, 3 = duration
    val sortAscending: Boolean = true,
    val gridView: Boolean = false,

    // Library
    val excludedFolderIds: List<Long> = emptyList(),
    val vaultVideoIds: List<Long> = emptyList(),
    val preferredAudioLanguage: String = "",
    val adBlockEnabled: Boolean = true,
    val linkCleaningEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showDecoderBadge: Boolean = false,

    // Privacy
    val appLockEnabled: Boolean = false,
    val appLockPinHash: String = "",
    val sleepTimerMinutes: Int = 0,
)
