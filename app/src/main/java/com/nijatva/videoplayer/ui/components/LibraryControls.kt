package com.nijatva.videoplayer.ui.components

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/** A sort menu matching the General settings (name / date / size / duration). */
@Composable
fun SortMenu(
    sortBy: Int,
    ascending: Boolean,
    onSortBy: (Int) -> Unit,
    onAscending: (Boolean) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.Sort, contentDescription = "Sort")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf("Name", "Date", "Size", "Duration").forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(if (sortBy == index) "$label ✓" else label) },
                    onClick = { onSortBy(index); open = false },
                )
            }
            DropdownMenuItem(
                text = { Text(if (ascending) "Descending" else "Ascending") },
                onClick = { onAscending(!ascending) },
            )
        }
    }
}

/** Confirms and deletes videos, using the system dialog on Android 11+. */
@Composable
fun rememberVideoDeleter(onDeleted: () -> Unit): (List<Uri>) -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { onDeleted() }

    return remember(context) {
        { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    runCatching {
                        val pending = MediaStore.createDeleteRequest(context.contentResolver, uris)
                        launcher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                    }.onFailure { onDeleted() }
                } else {
                    uris.forEach { runCatching { context.contentResolver.delete(it, null, null) } }
                    onDeleted()
                }
            }
        }
    }
}

/** Small helper to show a menu button. */
@Composable
fun OverFlowButton(content: @Composable (dismiss: () -> Unit) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            content { open = false }
        }
    }
}
