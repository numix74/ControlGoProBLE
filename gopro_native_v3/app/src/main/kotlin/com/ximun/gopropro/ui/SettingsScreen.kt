package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProSettingsMappings
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.ui.theme.AppCard
import com.ximun.gopropro.ui.theme.PrimaryTeal
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
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(title = "RÉGLAGES", subtitle = "PARAMÈTRES DE LA CAMÉRA")
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Videocam, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PARAMÈTRES VIDÉO",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))


        val videoSettings = listOf(
            GoProConstants.SETTING_ID_RESOLUTION,
            GoProConstants.SETTING_ID_FPS,
            GoProConstants.SETTING_ID_ASPECT_RATIO,
            GoProConstants.SETTING_ID_LENS,
            GoProConstants.SETTING_ID_HYPERSMOOTH,
            GoProConstants.SETTING_ID_ANTI_FLICKER,
            GoProConstants.SETTING_ID_BIT_RATE,
            GoProConstants.SETTING_ID_BIT_DEPTH,
            GoProConstants.SETTING_ID_VIDEO_PROFILE
        )

        videoSettings.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PARAMÈTRES SYSTÈME",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

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
    val currentVal = settings[settingId]
    // Si on a la valeur actuelle, on affiche le réglage, même si on n'a pas encore les toutes les capacités
    if (currentVal != null) {
        val caps = capabilities[settingId] ?: listOf(currentVal)
        SettingDropdown(
            label = GoProSettingsMappings.getSettingName(settingId),
            settingId = settingId,
            currentValue = currentVal,
            capabilities = caps,
            onValueChange = { value -> onUpdateSetting(settingId, value) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                color = AppCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentLabel, color = Color.Gray)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            DropdownMenu(
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
