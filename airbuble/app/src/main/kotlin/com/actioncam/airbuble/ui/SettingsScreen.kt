package com.actioncam.airbuble.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actioncam.airbuble.camera.CaptureMode
import com.actioncam.airbuble.camera.SettingDescriptor
import com.actioncam.airbuble.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    availableModes: List<CaptureMode>,
    availableSettings: List<SettingDescriptor>,
    currentModeId: String,
    isDarkMode: Boolean = true,
    isBubbleEnabled: Boolean = true,
    isCameraReady: Boolean = false,
    diagnosticLineCount: Int = 0,
    onSwitchMode: (String) -> Unit,
    onChangeSetting: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onToggleDarkMode: () -> Unit = {},
    onToggleBubble: () -> Unit = {},
    onSyncTime: () -> Unit = {},
    onExportLogs: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modes & Réglages", color = LocalAppColors.current.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onRefresh) {
                Text("Actualiser", color = LocalAppColors.current.accent, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Section App (toujours visible)
        SectionLabel("APP")
        Spacer(Modifier.height(10.dp))
        ToggleRow(
            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            label = "Mode sombre",
            checked = isDarkMode,
            onToggle = onToggleDarkMode
        )
        Spacer(Modifier.height(8.dp))
        ToggleRow(
            icon = Icons.Default.BubbleChart,
            label = "Bulle flottante",
            checked = isBubbleEnabled,
            onToggle = onToggleBubble
        )

        // Section Caméra (visible seulement si connecté)
        if (isCameraReady) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("CAMÉRA")
            Spacer(Modifier.height(10.dp))
            SyncTimeRow(onSyncTime = onSyncTime)
        }

        // Section Diagnostic (visible seulement si des logs existent — debug only)
        if (diagnosticLineCount > 0) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("DIAGNOSTIC")
            Spacer(Modifier.height(10.dp))
            ExportLogsRow(lineCount = diagnosticLineCount, onExport = onExportLogs)
        }

        if (availableModes.isEmpty() && availableSettings.isEmpty()) {
            if (!isCameraReady) { Spacer(Modifier.height(24.dp)); EmptyState() }
            return@Column
        }

        Spacer(Modifier.height(24.dp))

        // Modes section
        if (availableModes.isNotEmpty()) {
            SectionLabel("MODE DE CAPTURE")
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableModes) { mode ->
                    ModeChip(
                        label = mode.name,
                        isSelected = mode.id == currentModeId,
                        onClick = { onSwitchMode(mode.id) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Settings section
        if (availableSettings.isNotEmpty()) {
            SectionLabel("RÉGLAGES")
            Spacer(Modifier.height(10.dp))
            availableSettings.forEach { setting ->
                SettingRow(setting = setting, onValueSelected = { onChangeSetting(setting.id, it) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LocalAppColors.current.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Aucune donnée disponible\n\nConnecte-toi à une caméra\npour voir les modes et réglages.",
            color = LocalAppColors.current.textDim,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun ModeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) LocalAppColors.current.accent.copy(alpha = 0.15f) else LocalAppColors.current.card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) LocalAppColors.current.accent else Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = label,
            color = if (isSelected) LocalAppColors.current.accent else LocalAppColors.current.textDim,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, label: String, checked: Boolean, onToggle: () -> Unit) {
    Surface(color = LocalAppColors.current.card, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = LocalAppColors.current.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(label, color = LocalAppColors.current.textPrimary, fontSize = 14.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = LocalAppColors.current.accent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = LocalAppColors.current.textDim.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun SyncTimeRow(onSyncTime: () -> Unit) {
    val scope = rememberCoroutineScope()
    var syncDone by remember { mutableStateOf(false) }

    Surface(color = LocalAppColors.current.card, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = LocalAppColors.current.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sync horloge", color = LocalAppColors.current.textPrimary, fontSize = 14.sp)
            }
            if (syncDone) {
                Text("✓ Synchronisé", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            } else {
                TextButton(onClick = {
                    onSyncTime()
                    scope.launch {
                        syncDone = true
                        delay(3000)
                        syncDone = false
                    }
                }) {
                    Text("Synchroniser", color = LocalAppColors.current.accent, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ExportLogsRow(lineCount: Int, onExport: () -> Unit) {
    Surface(color = LocalAppColors.current.card, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, null, tint = LocalAppColors.current.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Exporter les logs", color = LocalAppColors.current.textPrimary, fontSize = 14.sp)
                    Text("$lineCount lignes capturées", color = LocalAppColors.current.textDim, fontSize = 11.sp)
                }
            }
            TextButton(onClick = onExport) {
                Text("Partager", color = LocalAppColors.current.accent, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingRow(setting: SettingDescriptor, onValueSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Surface(color = LocalAppColors.current.card, shape = RoundedCornerShape(12.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(setting.name, color = LocalAppColors.current.textPrimary, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        setting.currentValue.ifEmpty { "—" },
                        color = LocalAppColors.current.accent,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(LocalAppColors.current.card)
            ) {
                setting.availableValues.forEach { value ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                value.displayName,
                                color = if (value.displayName == setting.currentValue) LocalAppColors.current.accent else Color.White,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onValueSelected(value.id)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(textColor = Color.White)
                    )
                }
            }
        }
    }
}
