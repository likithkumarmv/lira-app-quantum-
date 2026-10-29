package com.lira.field.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// LIRA-Q Normative Sharp Industrial Corner Radius (4.dp controls, 8.dp cards)
val IndustrialShape = RoundedCornerShape(4.dp)
val IndustrialCardShape = RoundedCornerShape(8.dp)

data class LiraCustomColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val primary: Color,
    val secondary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val warning: Color,
    val warningSurface: Color,
    val warningBorder: Color,
    val error: Color,
    val errorSurface: Color,
    val errorBorder: Color,
    val success: Color,
    val successSurface: Color,
    val successBorder: Color
)

// =============================================================================
// LIRA-Q DARK THEME (Obsidian Sentinel Industrial)
// Background #0B1420, Surfaces #111F2E, Text #EAF3F8, Dividers #294355, Accent #39C9DF
// =============================================================================
val DarkLiraColors = LiraCustomColors(
    isDark = true,
    background = LiraDarkBg,
    surface = LiraDarkSurface,
    surfaceElevated = LiraDarkSurfaceElevated,
    border = LiraDarkBorder,
    primary = LiraDarkPrimary,
    secondary = LiraDarkSecondary,
    primaryContainer = Color(0xFF133246),
    onPrimary = Color(0xFF0B1420),
    textPrimary = LiraDarkTextPrimary,
    textSecondary = LiraDarkTextSecondary,
    textTertiary = LiraDarkTextTertiary,
    warning = LiraDarkWarning,
    warningSurface = LiraDarkWarningSurface,
    warningBorder = LiraDarkWarningBorder,
    error = LiraDarkError,
    errorSurface = LiraDarkErrorSurface,
    errorBorder = LiraDarkErrorBorder,
    success = LiraDarkSuccess,
    successSurface = LiraDarkSuccessSurface,
    successBorder = LiraDarkSuccessBorder
)

// =============================================================================
// LIRA-Q LIGHT THEME (Terminal Daylight Spec)
// Background #F3F6FA, Surfaces #FFFFFF, Text #102235, Dividers #D9E3EB, Primary #067A9B
// =============================================================================
val LightLiraColors = LiraCustomColors(
    isDark = false,
    background = LiraLightBg,
    surface = LiraLightSurface,
    surfaceElevated = LiraLightSurfaceElevated,
    border = LiraLightBorder,
    primary = LiraLightPrimary,
    secondary = LiraLightSecondary,
    primaryContainer = Color(0xFFD6EFF7),
    onPrimary = Color.White,
    textPrimary = LiraLightTextPrimary,
    textSecondary = LiraLightTextSecondary,
    textTertiary = LiraLightTextTertiary,
    warning = LiraLightWarning,
    warningSurface = LiraLightWarningSurface,
    warningBorder = LiraLightWarningBorder,
    error = LiraLightError,
    errorSurface = LiraLightErrorSurface,
    errorBorder = LiraLightErrorBorder,
    success = LiraLightSuccess,
    successSurface = LiraLightSuccessSurface,
    successBorder = LiraLightSuccessBorder
)

val LocalLiraColors = staticCompositionLocalOf { DarkLiraColors }

object LiraTheme {
    val colors: LiraCustomColors
        @Composable
        get() = LocalLiraColors.current
}

@Composable
fun LiraTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val customColors = if (darkTheme) DarkLiraColors else LightLiraColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = customColors.primary,
            onPrimary = customColors.onPrimary,
            primaryContainer = customColors.primaryContainer,
            surface = customColors.surface,
            onSurface = customColors.textPrimary,
            background = customColors.background,
            onBackground = customColors.textPrimary,
            outline = customColors.border,
            error = customColors.error,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = customColors.primary,
            onPrimary = customColors.onPrimary,
            primaryContainer = customColors.primaryContainer,
            surface = customColors.surface,
            onSurface = customColors.textPrimary,
            background = customColors.background,
            onBackground = customColors.textPrimary,
            outline = customColors.border,
            error = customColors.error,
            onError = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = if (darkTheme) 0xFF0B1420.toInt() else 0xFFF3F6FA.toInt()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLiraColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LiraTypography,
            content = content
        )
    }
}
