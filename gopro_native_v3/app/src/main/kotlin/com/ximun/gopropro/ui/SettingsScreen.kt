@file:OptIn(ExperimentalMaterial3Api::class)

package com.ximun.gopropro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ximun.gopropro.GoProSettingsMappings
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.viewmodel.CameraUiState


@Composable
fun SettingsScreen(
    state: CameraUiState,
    onUpdateSetting: (Int, Int) -> Unit
) {
    val settings = state.settings
    val capabilities = state.capabilities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Paramètres Vidéo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val videoSettings = listOf(
            GoProConstants.SETTING_ID_RESOLUTION,
            GoProConstants.SETTING_ID_FPS,
            GoProConstants.SETTING_ID_ASPECT_RATIO,
            GoProConstants.SETTING_ID_LENS,
            GoProConstants.SETTING_ID_HYPERSMOOTH,
            GoProConstants.SETTING_ID_COLOR,
            GoProConstants.SETTING_ID_ISO_MAX,
            GoProConstants.SETTING_ID_WHITE_BALANCE
        )

        videoSettings.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Paramètres Système",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val systemSettings = listOf(
            GoProConstants.SETTING_ID_GPS,
            GoProConstants.SETTING_ID_AUTO_POWER_DOWN,
            GoProConstants.SETTING_ID_LED,
            GoProConstants.SETTING_ID_LCD_BRIGHTNESS
        )

        systemSettings.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }
    }
}

@Composable
fun RenderSetting(
    settingId: Int,
    capabilities: Map<Int, List<Int>>,
    settings: Map<Int, Int>,
    onUpdateSetting: (Int, Int) -> Unit
) {
    val caps = capabilities[settingId]
    if (!caps.isNullOrEmpty()) {
        SettingDropdown(
            label = GoProSettingsMappings.getSettingName(settingId),
            settingId = settingId,
            currentValue = settings[settingId],
            capabilities = caps,
            onValueChange = { value -> onUpdateSetting(settingId, value) }
        )
    }
}

@Composable
fun SettingDropdown(
    label: String,
    settingId: Int,
    currentValue: Int?,
    capabilities: List<Int>,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = GoProSettingsMappings.getAvailableOptions(settingId, capabilities)

    if (options.isEmpty()) return

    val currentLabel = currentValue?.let {
        GoProSettingsMappings.getLabel(settingId, it)
    } ?: "..."

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, optionLabel) ->
                    DropdownMenuItem(
                        text = { Text(optionLabel) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        },
                        leadingIcon = if (value == currentValue) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}
