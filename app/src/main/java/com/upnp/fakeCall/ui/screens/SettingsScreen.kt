package com.upnp.fakeCall.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upnp.fakeCall.FakeCallViewModel
import com.upnp.fakeCall.ivr.IvrNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FakeCallViewModel,
    onBack: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val ivrConfig = state.ivrConfig
    val ivrNodes = ivrConfig?.nodes?.values?.sortedBy { it.title } ?: emptyList()

    var showAddNodeDialog by rememberSaveable { mutableStateOf(false) }
    var mappingNodeId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingAudioNodeId by rememberSaveable { mutableStateOf<String?>(null) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.onAudioFileSelected(uri)
    }
    val recordingsFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        viewModel.onRecordingFolderSelected(uri)
    }

    val ivrAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val nodeId = pendingAudioNodeId
        if (nodeId != null) {
            viewModel.onIvrNodeAudioSelected(nodeId, uri)
        }
        pendingAudioNodeId = null
    }

    val ivrExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri ->
        viewModel.exportIvrConfig(uri)
    }

    val ivrImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.importIvrConfig(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                LargeTopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                SectionHeader("Provider Options")
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!state.hasRequiredPermissions) {
                            SettingsRow(
                                icon = Icons.Filled.Phone,
                                title = "Phone permissions required",
                                subtitle = "Grant access to register the call provider.",
                                onClick = onRequestPermissions
                            )
                        }

                        ListItem(
                            headlineContent = { Text("Provider name") },
                            supportingContent = {
                                OutlinedTextField(
                                    value = state.providerName,
                                    onValueChange = viewModel::onProviderNameChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Filled.Phone, contentDescription = null)
                            }
                        )

                        SettingsRow(
                            icon = Icons.Filled.CheckCircle,
                            title = "Save & register provider",
                            subtitle = "Make this account available for incoming calls.",
                            onClick = viewModel::saveProvider
                        )

                        SettingsRow(
                            icon = Icons.Filled.Settings,
                            title = "Enable provider in system",
                            subtitle = if (state.isProviderEnabled) {
                                "Provider is enabled."
                            } else {
                                "Open Calling Accounts to enable it."
                            },
                            onClick = { openCallingAccounts(context, viewModel) }
                        )
                    }
                }

                SectionHeader("Audio & Media")
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingsRow(
                            icon = Icons.Filled.MusicNote,
                            title = "Select audio file",
                            subtitle = "Current: ${state.selectedAudioName.ifBlank { "Default" }}",
                            onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) }
                        )

                        SettingsRow(
                            icon = Icons.AutoMirrored.Filled.VolumeOff,
                            title = "Use default audio",
                            subtitle = "Disable custom audio playback.",
                            onClick = viewModel::clearAudioSelection
                        )

                        SettingsToggleRow(
                            icon = Icons.Filled.Mic,
                            title = "Microphone recording",
                            subtitle = if (state.isRecordingEnabled) "Enabled" else "Disabled",
                            checked = state.isRecordingEnabled,
                            onCheckedChange = viewModel::onRecordingEnabledChange
                        )
                    }
                }

                SectionHeader("Storage")
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingsRow(
                            icon = Icons.Filled.Folder,
                            title = "Recording folder",
                            subtitle = "Save to: ${state.recordingsFolderName}",
                            onClick = { recordingsFolderLauncher.launch(null) }
                        )

                        SettingsRow(
                            icon = Icons.Filled.Refresh,
                            title = "Reset recording folder",
                            subtitle = "Use Downloads/FakeCall",
                            onClick = viewModel::clearRecordingFolderSelection
                        )
                    }
                }

                SectionHeader("Mailbox (IVR)")
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsRow(
                            icon = Icons.Filled.Folder,
                            title = "Import mailbox XML",
                            subtitle = "Load a saved IVR tree.",
                            onClick = { ivrImportLauncher.launch(arrayOf("text/xml", "application/xml")) }
                        )

                        SettingsRow(
                            icon = Icons.Filled.Refresh,
                            title = "Export mailbox XML",
                            subtitle = "Share your current IVR tree.",
                            onClick = { ivrExportLauncher.launch("fakecall_mailbox.xml") }
                        )

                        SettingsRow(
                            icon = Icons.Filled.Add,
                            title = "Add menu node",
                            subtitle = "Create a new IVR menu.",
                            onClick = { showAddNodeDialog = true }
                        )

                        if (ivrNodes.isEmpty()) {
                            Text(
                                text = "No mailbox nodes yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ivrNodes.forEach { node ->
                                    MailboxNodeCard(
                                        node = node,
                                        nodes = ivrNodes,
                                        isRoot = ivrConfig?.rootId == node.id,
                                        onSetRoot = { viewModel.setIvrRoot(node.id) },
                                        onSelectAudio = {
                                            pendingAudioNodeId = node.id
                                            ivrAudioPicker.launch(arrayOf("audio/*"))
                                        },
                                        onClearAudio = { viewModel.clearIvrNodeAudio(node.id) },
                                        onAddMapping = { mappingNodeId = node.id },
                                        onRemoveMapping = { digit -> viewModel.removeIvrRoute(node.id, digit) },
                                        onDelete = { viewModel.removeIvrNode(node.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.statusMessage.isNotBlank()) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showAddNodeDialog) {
        AddNodeDialog(
            onDismiss = { showAddNodeDialog = false },
            onConfirm = { name ->
                viewModel.addIvrNode(name)
                showAddNodeDialog = false
            }
        )
    }

    val mappingNode = ivrNodes.firstOrNull { it.id == mappingNodeId }
    if (mappingNode != null) {
        MappingDialog(
            node = mappingNode,
            nodes = ivrNodes,
            onDismiss = { mappingNodeId = null },
            onConfirm = { digit, targetId ->
                viewModel.addIvrRoute(mappingNode.id, digit, targetId)
                mappingNodeId = null
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
private fun MailboxNodeCard(
    node: IvrNode,
    nodes: List<IvrNode>,
    isRoot: Boolean,
    onSetRoot: () -> Unit,
    onSelectAudio: () -> Unit,
    onClearAudio: () -> Unit,
    onAddMapping: () -> Unit,
    onRemoveMapping: (Char) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isRoot) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Root menu") },
                            leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) }
                        )
                    } else {
                        TextButton(onClick = onSetRoot) {
                            Icon(Icons.Filled.StarBorder, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set as root")
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete node")
                }
            }

            ListItem(
                headlineContent = { Text("Node audio") },
                supportingContent = {
                    Text(node.audioLabel.ifBlank { "No audio selected" })
                },
                leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSelectAudio) {
                            Icon(Icons.Filled.Folder, contentDescription = "Select audio")
                        }
                        IconButton(onClick = onClearAudio) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear audio")
                        }
                    }
                }
            )

            Text(
                text = "Digit mappings (0 = back)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (node.routes.isEmpty()) {
                Text(
                    text = "No mappings yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    node.routes.toSortedMap().forEach { (digit, target) ->
                        val title = nodes.firstOrNull { it.id == target }?.title ?: "Unknown"
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text("$digit → $title") },
                            trailingIcon = {
                                IconButton(onClick = { onRemoveMapping(digit) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove mapping")
                                }
                            },
                            shape = RoundedCornerShape(999.dp)
                        )
                    }
                }
            }

            FilledTonalButton(onClick = onAddMapping) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add mapping")
            }
        }
    }
}

@Composable
private fun AddNodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New mailbox node") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Node title") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingDialog(
    node: IvrNode,
    nodes: List<IvrNode>,
    onDismiss: () -> Unit,
    onConfirm: (Char, String) -> Unit
) {
    val availableTargets = nodes.filter { it.id != node.id }
    var selectedDigit by rememberSaveable { mutableStateOf('1') }
    var selectedTargetId by rememberSaveable { mutableStateOf(availableTargets.firstOrNull()?.id.orEmpty()) }
    var digitExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add mapping") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = digitExpanded,
                    onExpandedChange = { digitExpanded = !digitExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDigit.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Digit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = digitExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = digitExpanded,
                        onDismissRequest = { digitExpanded = false }
                    ) {
                        listOf('1','2','3','4','5','6','7','8','9','*','#').forEach { digit ->
                            DropdownMenuItem(
                                text = { Text(digit.toString()) },
                                onClick = {
                                    selectedDigit = digit
                                    digitExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { targetExpanded = !targetExpanded }
                ) {
                    OutlinedTextField(
                        value = availableTargets.firstOrNull { it.id == selectedTargetId }?.title.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target node") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        availableTargets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text(target.title) },
                                onClick = {
                                    selectedTargetId = target.id
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }

                if (availableTargets.isEmpty()) {
                    Text(
                        text = "Add another node to create mappings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDigit, selectedTargetId) },
                enabled = availableTargets.isNotEmpty() && selectedTargetId.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openCallingAccounts(context: Context, viewModel: FakeCallViewModel) {
    val intent = viewModel.openCallingAccountsIntent()
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        if (it is ActivityNotFoundException) {
            // Ignore silently on unsupported devices.
        }
    }
}
