@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.R
import com.ximun.gopropro.ui.theme.LocalAppColors

/** true si l'écran est en mode paysage (landscape) ou tablette */
val WindowSizeClass.isLandscape: Boolean
    get() = widthSizeClass != WindowWidthSizeClass.Compact

@Composable
fun HeaderSection(
    title: String,
    subtitle: String,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = appColors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = appColors.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        if (actions != null) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/** Indicateur visuel affiché quand la caméra connectée est de génération legacy (Hero 5/6/7/8). */
@Composable
fun LegacyBadge(modifier: Modifier = Modifier) {
    val accent = Color(0xFFF59E0B)
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = stringResource(R.string.dashboard_legacy_badge),
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/** Bloc d'avertissement plein-largeur, utilisé en mode legacy pour signaler qu'une fonction n'est pas disponible. */
@Composable
fun LegacyUnavailableNotice(message: String, modifier: Modifier = Modifier) {
    val accent = Color(0xFFF59E0B)
    val appColors = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = appColors.card,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = message,
                color = appColors.textPrimary,
                fontSize = 13.sp
            )
        }
    }
}
