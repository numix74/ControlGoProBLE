package com.actioncam.airbuble.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val card: Color,
    val accent: Color,
    val textPrimary: Color,
    val textDim: Color,
    val border: Color,
    val error: Color,
    val recordRed: Color = Color(0xFFEF4444),
    val timerYellow: Color = Color(0xFFEAB308),
    val warning: Color = Color(0xFFF59E0B),
    val success: Color = Color(0xFF22C55E)
)

val DarkAppColors = AppColors(
    background  = Color(0xFF0F172A),
    card        = Color(0xFF1E293B),
    accent      = Color(0xFF4CC4C4),
    textPrimary = Color.White,
    textDim     = Color(0xFF94A3B8),
    border      = Color(0xFF334155),
    error       = Color(0xFFDC2626)
)

val LightAppColors = AppColors(
    background  = Color(0xFFF1F5F9),
    card        = Color(0xFFFFFFFF),
    accent      = Color(0xFF0D9488),
    textPrimary = Color(0xFF0F172A),
    textDim     = Color(0xFF64748B),
    border      = Color(0xFFCBD5E1),
    error       = Color(0xFFDC2626)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
