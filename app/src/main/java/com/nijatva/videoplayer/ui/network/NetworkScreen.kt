package com.nijatva.videoplayer.ui.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.nijatva.videoplayer.aekant.UrlCleaner
import com.nijatva.videoplayer.util.startVideoDownload
import com.nijatva.videoplayer.app
import com.nijatva.videoplayer.data.model.NetworkConnection
import java.util.UUID

private val protocols = listOf("SMB", "FTP", "SFTP", "WebDAV")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    onOpenStream: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val connections by app.connectionsRepository.connections.collectAsState()
    val settings by app.settingsRepository.settings.collectAsState()

    var url by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add connection")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Network Stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Enter a network URL:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("http://example.com/example.mp4") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        val trimmed = url.trim()
                                        val clean = if (settings.linkCleaningEnabled) UrlCleaner.clean(trimmed) else trimmed
                                        startVideoDownload(context, clean, clean.substringBefore('?').substringAfterLast('/'))
                                    }
                                },
                                enabled = url.isNotBlank(),
                            ) {
                                Text("Download")
                            }
                            Button(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        val trimmed = url.trim()
                                        onOpenStream(
                                            if (settings.linkCleaningEnabled) UrlCleaner.clean(trimmed) else trimmed,
                                        )
                                    }
                                },
                                enabled = url.isNotBlank(),
                            ) {
                                Text("Open network stream")
                            }
                        }
                    }
                }
            }

            if (connections.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No connections yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add an SMB, FTP, SFTP or WebDAV connection to browse and play videos from your network.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
            } else {
                items(connections, key = { it.id }) { connection ->
                    ConnectionRow(
                        connection = connection,
                        onDelete = { app.connectionsRepository.remove(connection.id) },
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddConnectionDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, protocol, host ->
                app.connectionsRepository.add(
                    NetworkConnection(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        protocol = protocol,
                        host = host,
                    ),
                )
                showDialog = false
            },
        )
    }
}

@Composable
private fun ConnectionRow(connection: NetworkConnection, onDelete: () -> Unit) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(connection.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${connection.protocol} · ${connection.host}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun AddConnectionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf(protocols.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add connection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    protocols.forEach { p ->
                        FilterChip(
                            selected = protocol == p,
                            onClick = { protocol = p },
                            label = { Text(p) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name.ifBlank { host }, protocol, host) },
                enabled = host.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
