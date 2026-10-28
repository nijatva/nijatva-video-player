package com.nijatva.videoplayer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.ui.components.SectionHeader
import com.nijatva.videoplayer.ui.components.SettingsGroup
import com.nijatva.videoplayer.ui.components.SettingClick
import com.nijatva.videoplayer.ui.components.SettingSwitch
import com.nijatva.videoplayer.ui.components.ZScreen
import com.nijatva.videoplayer.util.hashPin

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = context.app.settingsRepository
    val settings by repo.settings.collectAsState()

    var showSetPin by remember { mutableStateOf(false) }
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }

    ZScreen(title = "Privacy & security", onBack = onBack) { modifier ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("App lock") }
            item {
                SettingsGroup {
                    SettingSwitch(
                        "App lock",
                        settings.appLockEnabled,
                        { v ->
                            if (v && settings.appLockPinHash.isEmpty()) {
                                showSetPin = true
                            } else {
                                repo.update { it.copy(appLockEnabled = v) }
                            }
                        },
                        "Require a PIN to open the app and the vault",
                        Icons.Filled.Lock,
                    )
                    SettingClick(
                        if (settings.appLockPinHash.isEmpty()) "Set PIN" else "Change PIN",
                        { pin1 = ""; pin2 = ""; showSetPin = true },
                        "Choose a numeric PIN",
                        Icons.Filled.Password,
                    )
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "The PIN is stored only as a SHA-256 hash on this device.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }

    if (showSetPin) {
        AlertDialog(
            onDismissRequest = { showSetPin = false },
            title = { Text("Set PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pin1,
                        onValueChange = { pin1 = it.filter { c -> c.isDigit() } },
                        label = { Text("New PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pin2,
                        onValueChange = { pin2 = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = pin1.length >= 4 && pin1 == pin2,
                    onClick = {
                        repo.update { it.copy(appLockPinHash = hashPin(pin1), appLockEnabled = true) }
                        showSetPin = false
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSetPin = false }) { Text("Cancel") } },
        )
    }
}
