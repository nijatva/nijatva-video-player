package com.nijatva.videoplayer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Soft hand-offs to the other Nijatva apps.
 *
 * Every call is a SOFT dependency: try the matching Nijatva app, else let the system
 * resolve it (chooser), else show a short message. Nothing here throws, so a missing
 * sibling app can never crash this app and no feature is blocked.
 *
 * Family: Photos com.nijatva.photos · Gallery com.nijatva.gallery · Camera com.nijatva.camera ·
 * Photo Editor com.nijatva.photo.editor · Video Player com.nijatva.videoplayer ·
 * Music Player com.nijatva.music.player · File Manager com.nijatva.file · Browser in.nijatva.browser
 */
object NijatvaMediaIntents {

    const val PHOTOS = "com.nijatva.photos"
    const val GALLERY = "com.nijatva.gallery"
    const val CAMERA = "com.nijatva.camera"
    const val PHOTO_EDITOR = "com.nijatva.photo.editor"
    const val VIDEO_PLAYER = "com.nijatva.videoplayer"
    const val MUSIC_PLAYER = "com.nijatva.music.player"
    const val FILE_MANAGER = "com.nijatva.file"
    const val BROWSER = "in.nijatva.browser"

    fun isInstalled(context: Context, pkg: String): Boolean = try {
        context.packageManager.getLaunchIntentForPackage(pkg) != null
    } catch (e: Exception) {
        false
    }

    private fun startOn(context: Context, intent: Intent, pkg: String): Boolean = try {
        context.startActivity(Intent(intent).setPackage(pkg))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }

    private fun startAny(context: Context, intent: Intent, title: String): Boolean = try {
        context.startActivity(Intent.createChooser(intent, title))
        true
    } catch (e: Exception) {
        false
    }

    private fun viewOf(uri: String, mime: String) = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(uri), mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun nothingFound(context: Context, what: String): Boolean {
        Toast.makeText(context, "No app found to $what", Toast.LENGTH_SHORT).show()
        return false
    }

    /** Open an image/video in Nijatva Gallery, else Photos, else any viewer. */
    fun openImage(context: Context, uri: String, mime: String? = "image/*"): Boolean {
        val intent = viewOf(uri, mime ?: "image/*")
        if (startOn(context, intent, GALLERY)) return true
        if (startOn(context, intent, PHOTOS)) return true
        return startAny(context, intent, "Open in") || nothingFound(context, "open this item")
    }

    /** ACTION_EDIT in Nijatva Photo Editor, else any editor. */
    fun editImage(context: Context, uri: String, mime: String? = "image/*"): Boolean {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(Uri.parse(uri), mime ?: "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        if (startOn(context, intent, PHOTO_EDITOR)) return true
        return startAny(context, intent, "Edit with") || nothingFound(context, "edit this image")
    }

    /** Play a video in Nijatva Video Player, else any player. */
    fun playVideo(context: Context, uri: String, mime: String? = "video/*"): Boolean {
        val intent = viewOf(uri, mime ?: "video/*")
        if (startOn(context, intent, VIDEO_PLAYER)) return true
        return startAny(context, intent, "Play with") || nothingFound(context, "play this video")
    }

    /** Play audio in Nijatva Music Player, else any player. */
    fun playAudio(context: Context, uri: String, mime: String? = "audio/*"): Boolean {
        val intent = viewOf(uri, mime ?: "audio/*")
        if (startOn(context, intent, MUSIC_PLAYER)) return true
        return startAny(context, intent, "Play with") || nothingFound(context, "play this audio")
    }

    /** Open Nijatva Camera (falls back to the system still-image camera). */
    fun openCamera(context: Context): Boolean {
        context.packageManager.getLaunchIntentForPackage(CAMERA)?.let {
            if (startOn(context, it, CAMERA)) return true
        }
        return try {
            context.startActivity(Intent("android.media.action.STILL_IMAGE_CAMERA"))
            true
        } catch (e: Exception) {
            nothingFound(context, "open the camera")
        }
    }

    /** Open Nijatva File Manager. */
    fun openFileManager(context: Context): Boolean {
        context.packageManager.getLaunchIntentForPackage(FILE_MANAGER)?.let {
            if (startOn(context, it, FILE_MANAGER)) return true
        }
        return nothingFound(context, "open the file manager")
    }

    /** Open a URL in Nijatva Browser, else any browser. */
    fun openInBrowser(context: Context, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (startOn(context, intent, BROWSER)) return true
        return startAny(context, intent, "Open with") || nothingFound(context, "open this link")
    }

    /** Share through the system sheet (always content://, never file://). */
    fun shareMedia(context: Context, uri: String, mime: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startAny(context, intent, "Share")
    }
}
