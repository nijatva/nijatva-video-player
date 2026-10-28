package com.nijatva.videoplayer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** The runtime permission needed to read the device video library. */
fun requiredVideoPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun hasVideoPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, requiredVideoPermission()) ==
        PackageManager.PERMISSION_GRANTED
