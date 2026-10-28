# Video Player

A lightweight, open-source video player for Android, written in Kotlin with
Jetpack Compose and AndroidX Media3 (ExoPlayer). It browses the videos on your
device as a folder library, plays them with gesture controls, and includes a
full settings tree.

## Features

- **Bottom navigation** — Home · Library · Folders · Playlists · Settings.
- **Home** — Continue watching (with progress), Recently added, and a Folders row.
- **Library** — every video, searchable, grid or list, sortable, multi-select delete,
  plus Vault and History. **Folders** groups videos by folder with a real frame thumbnail.
- **Videos** — a flat, searchable list of everything on the device.
- **Player** — ExoPlayer with a custom control overlay (play/pause, seek bar, rewind/forward 10s,
  playlist, repeat, audio track, subtitle toggle, lock, aspect ratio, rotate, background play,
  shuffle, picture-in-picture, close) and gesture controls:
  - swipe horizontally to seek,
  - swipe vertically to change brightness (left) or volume (right),
  - double-tap to play/pause, long-press for 2x speed,
  - pinch to zoom.
- **Network** — play a network stream by URL, save SMB/FTP/SFTP/WebDAV connections, and
  **download** a stream (four-part ranged download with pause/resume, falling back to the
  system DownloadManager when the server cannot be split).
- **YouTube links** — open a `youtube.com/watch`, `youtu.be` or `/shorts` link (for example one
  shared on WhatsApp) and it plays in-app using the official YouTube IFrame embed. Links from
  other apps are handled too. Only videos that allow embedding will play.
- **More** — pick a file, plus a history of recently played videos.
- **Player sheets** — speed, audio track and subtitle pickers open as bottom sheets
  (subtitles include an on/off switch, track list and text size).
- **Privacy & security** — an app lock PIN (stored as a SHA-256 hash) gates the app and the vault.
- **Working volume boost** — a LoudnessEnhancer attached to the playback session.
- **Advanced settings** — a single screen of on/off toggles: ad & tracker blocking for
  network streams (bundled EasyList rules), link cleaning (strips `utm_*`, `fbclid`, `gclid`, …),
  keep-screen-awake during playback, and the decoder badge. (Blocking/cleaning engine and the
  bundled lists are ported from the AekantKit drop-in.)
- **Theme** — a green + blue + white + black palette across light, dark and OLED-black
  (emerald green accent, azure/royal blue secondary).
- **Settings** — Appearance (theme, high-contrast, dynamic colour), Media library,
  Player (controls, resume, autoplay, picture-in-picture, background play, orientation),
  Gestures, Audio, Subtitle, General and About. All options are persisted.
- **Background playback** — a media session service keeps playback alive off-screen,
  with a notification and lock-screen controls (volume keys work too).
- **Subtitles** — text tracks render with an adjustable size, and a sidecar
  `.srt`/`.vtt`/`.ass` next to the video is loaded automatically.
- **Audio tracks** — pick a track from the player; sleep timer and A-B repeat included.
- **Library tools** — search, grid/list view, sorting, multi-select delete,
  folder exclusion, playlists, a vault for hidden videos, and settings backup/restore.
- Picture-in-picture, autoplay next, resume playback, and a choice of playback speed.

## Tech stack

| Layer     | Choice                                     |
|-----------|--------------------------------------------|
| Language  | Kotlin                                     |
| UI        | Jetpack Compose + Material 3               |
| Playback  | AndroidX Media3 (ExoPlayer) + PlayerView   |
| Navigation| Navigation Compose                         |
| Storage   | DataStore (Preferences) + kotlinx.serialization |
| Build     | Gradle (Kotlin DSL) + version catalog      |
| minSdk    | 24                                         |
| targetSdk | 35                                         |

## Project structure

```
app/src/main/java/com/nijatva/videoplayer/
  MainActivity.kt              # entry point
  NijatvaApplication.kt         # repositories
  data/                        # settings, history, connections, MediaStore library
  ui/
    AppNavigation.kt           # bottom-bar navigation + routes
    home/                      # Home (folders), Videos, folder detail
    player/                    # ExoPlayer screen with gestures
    network/                   # network streams + connections
    more/                      # pick file, history
    settings/                  # the full settings tree
    components/                # shared settings UI
    theme/                     # Material 3 colour scheme
  util/                        # formatters, permissions
```

## Download a prebuilt APK

Every push to `main` is built automatically by GitHub Actions. To get an
installable debug APK without setting up a local toolchain:

1. Open the **Actions** tab of this repository.
2. Select the most recent **Build APK** run.
3. Download the **nijatva-video-player-debug** artifact (a zip containing `app-debug.apk`).
4. Copy the `.apk` to your Android device and install it. You may need to allow
   "Install unknown apps" for your browser or file manager.

You can also start a build on demand from the Actions tab
(**Build APK → Run workflow**).

## Getting started

### Requirements

- Android Studio (Ladybug or newer) **or** a command-line Android SDK.
- JDK 17.
- An Android device or emulator running API 24+.

### Build and run

Clone the repository and open it in Android Studio, then press **Run**.

From the command line:

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build and install on a connected device
```

The debug APK is written to `app/build/outputs/apk/debug/`.

> A `local.properties` file with your `sdk.dir` is generated automatically by
> Android Studio. For command-line builds, create it yourself (it is
> git-ignored).

## Notes

- SMB/FTP/SFTP/WebDAV connections can be added and saved; browsing files over
  those protocols is not wired up yet — network playback currently works via
  direct stream URLs.
- "Background play" keeps the player from pausing when the app leaves the
  foreground; a full media session / foreground service is not implemented yet.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the
full text.

Copyright 2026 Vasudev Dangaya.
