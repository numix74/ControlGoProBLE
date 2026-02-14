package com.ximun.gopropro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Couleurs sombres
val AppBackground = Color(0xFF0F172A)
val AppCard = Color(0xFF1E293B)
val AppPrimary = Color(0xFF3B82F6)
val PrimaryTeal = Color(0xFF4CC4C4)
val HilightYellow = Color(0xFFCA8A04)

// Couleurs claires
val LightBackground = Color(0xFFF1F5F9)
val LightCard = Color(0xFFFFFFFF)

/**
 * Couleurs de l'app accessibles globalement via LocalAppColors.current
 */
data class AppColors(
    val background: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color
)

val DarkAppColors = AppColors(
    background = AppBackground,
    card = AppCard,
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    border = Color.White.copy(alpha = 0.05f)
)

val LightAppColors = AppColors(
    background = LightBackground,
    card = LightCard,
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF64748B),
    border = Color(0xFFE2E8F0)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimary,
    secondary = PrimaryTeal,
    tertiary = HilightYellow,
    background = AppBackground,
    surface = AppCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    secondary = PrimaryTeal,
    tertiary = HilightYellow,
    background = LightBackground,
    surface = LightCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
)

// Placeholder Typography
val Typography = Typography()

@Composable
fun GoProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}