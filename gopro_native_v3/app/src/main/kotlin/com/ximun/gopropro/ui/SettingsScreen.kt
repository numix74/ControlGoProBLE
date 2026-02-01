package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun SettingsScreen(
    state: CameraUiState,
    onUpdateSetting: (Int, Int) -> Unit
) {
    val displaySettings = listOf(
        SettingItem(GoProConstants.SETTING_ID_RESOLUTION, "Résolution", Icons.Default.VideoCameraBack),
        SettingItem(GoProConstants.SETTING_ID_FPS, "Images / Seconde", Icons.Default.Speed),
        SettingItem(GoProConstants.SETTING_ID_LENS, "Objectif", Icons.Default.FilterCenterFocus),
        SettingItem(GoProConstants.SETTING_ID_HYPERSMOOTH, "Hypersmooth", Icons.Default.StayCurrentPortrait),
        SettingItem(GoProConstants.SETTING_ID_COLOR, "Couleur", Icons.Default.Palette),
        SettingItem(GoProConstants.SETTING_ID_ISO_MAX, "ISO Max", Icons.Default.LightMode),
        SettingItem(GoProConstants.SETTING_ID_WHITE_BALANCE, "Balance Blanc", Icons.Default.WbSunny)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        headerSection("Réglages", "PROTUNE CONFIGURATION")

        Spacer(modifier = Modifier.height(32.dp))

        if (state.settings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement des réglages...", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(displaySettings) { item ->
                    val currentValue = state.settings[item.id]
                    val options = state.capabilities[item.id] ?: emptyList()
                    
                    SettingCard(
                        item = item,
                        currentValue = currentValue,
                        options = options,
                        onValueSelected = { newValue ->
                            onUpdateSetting(item.id, newValue)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun headerSection(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = subtitle,
            color = PrimaryTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

data class SettingItem(
    val id: Int,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SettingCard(
    item: SettingItem,
    currentValue: Int?,
    options: List<Int>,
    onValueSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasData = currentValue != null
    val finalOptions = if (options.isEmpty() && hasData) listOf(currentValue!!) else options

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = hasData && finalOptions.size > 1) { expanded = !expanded },
            color = AppCard,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.label.uppercase(),
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (hasData) getSettingValueName(item.id, currentValue!!) else "---",
                        color = if (hasData) PrimaryTeal else Color.DarkGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (finalOptions.size > 1) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (finalOptions.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(AppCard)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .fillMaxWidth(0.6f)
            ) {
                finalOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = getSettingValueName(item.id, opt),
                                color = if (opt == currentValue) PrimaryTeal else Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (opt == currentValue) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueSelected(opt)
                            expanded = false
                        },
                        trailingIcon = {
                            if (opt == currentValue) {
                                Icon(Icons.Default.Check, null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.background(if (opt == currentValue) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                    )
                }
            }
        }
    }
}

fun getSettingValueName(id: Int, value: Int): String {
    return when (id) {
        GoProConstants.SETTING_ID_RESOLUTION -> when (value) {
            1 -> "4K"
            4 -> "2.7K"
            9 -> "1080p"
            10 -> "4K 4:3"
            24 -> "5.3K"
            else -> "$value"
        }
        GoProConstants.SETTING_ID_FPS -> when (value) {
            0 -> "240"
            1 -> "120"
            2 -> "100"
            5 -> "60"
            8 -> "30"
            11 -> "24"
            else -> "$value"
        }
        GoProConstants.SETTING_ID_LENS -> when (value) {
            0 -> "Large"
            2 -> "SuperView"
            3 -> "Linéaire"
            4 -> "Linéaire+HL"
            else -> "$value"
        }
        GoProConstants.SETTING_ID_HYPERSMOOTH -> when (value) {
            0 -> "Off"
            1 -> "On"
            2 -> "High"
            3 -> "Boost"
            else -> "$value"
        }
        GoProConstants.SETTING_ID_ISO_MAX -> when (value) {
            0 -> "100"
            1 -> "200"
            2 -> "400"
            3 -> "800"
            4 -> "1600"
            5 -> "3200"
            6 -> "6400"
            else -> "$value"
        }
        GoProConstants.SETTING_ID_COLOR -> when (value) {
            0 -> "GoPro"
            1 -> "Flat"
            2 -> "Vivid"
            3 -> "Natural"
            else -> "C$value"
        }
        GoProConstants.SETTING_ID_WHITE_BALANCE -> when (value) {
            0 -> "Auto"
            1 -> "3200K"
            2 -> "4000K"
            3 -> "4800K"
            4 -> "5500K"
            5 -> "6000K"
            6 -> "6500K"
            7 -> "Native"
            else -> "WB $value"
        }
        else -> value.toString()
    }
}
