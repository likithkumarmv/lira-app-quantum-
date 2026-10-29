package com.lira.field.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// LIRA-Q SENTINEL DUAL-THEME DESIGN SYSTEM
// Strictly aligned with LIRA Master PRD v10.0 & LIRA-Q Web Operations Companion
// =============================================================================

// --- Light Theme Palette (Daylight High-Glare Terminal Spec) ---
val LiraLightBg = Color(0xFFF3F6FA)              // Clean Industrial Canvas
val LiraLightSurface = Color(0xFFFFFFFF)         // Pure White Container Surface
val LiraLightSurfaceElevated = Color(0xFFE8EFF5) // Subtle Raised Element
val LiraLightBorder = Color(0xFFD9E3EB)          // Crisp Structural Divider
val LiraLightPrimary = Color(0xFF067A9B)         // LIRA-Q Deep Industrial Cyan Primary Action
val LiraLightSecondary = Color(0xFF0284C7)       // Maritime Blue Supporting
val LiraLightTextPrimary = Color(0xFF102235)     // High-Contrast Deep Navy Text
val LiraLightTextSecondary = Color(0xFF516678)   // Legible Muted Label Text
val LiraLightTextTertiary = Color(0xFF8295A5)    // Monospace / Timestamps

// Light Semantic Feedback Tokens
val LiraLightWarning = Color(0xFFD97706)         // Amber Warning
val LiraLightWarningSurface = Color(0xFFFEF3C7)  // Soft Amber Container
val LiraLightWarningBorder = Color(0xFFFCD34D)
val LiraLightError = Color(0xFFDC2626)           // Red Error / Blocker
val LiraLightErrorSurface = Color(0xFFFEE2E2)    // Soft Red Container
val LiraLightErrorBorder = Color(0xFFFCA5A5)
val LiraLightSuccess = Color(0xFF16A34A)         // Green Confirmed / Passed
val LiraLightSuccessSurface = Color(0xFFDCFCE7)  // Soft Green Container
val LiraLightSuccessBorder = Color(0xFF86EFAC)

// --- Dark Theme Palette (Obsidian Sentinel Industrial Spec) ---
val LiraDarkBg = Color(0xFF0B1420)               // Deep Navy-Carbon Canvas
val LiraDarkSurface = Color(0xFF111F2E)          // Sentinel Slate Panel Surface
val LiraDarkSurfaceElevated = Color(0xFF182A3E)  // Elevated Control Surface
val LiraDarkBorder = Color(0xFF294355)           // 1dp Structural Divider Border
val LiraDarkPrimary = Color(0xFF39C9DF)          // Electric Cyan High-Vis Primary Action
val LiraDarkSecondary = Color(0xFF067A9B)        // Deep Maritime Contrast
val LiraDarkTextPrimary = Color(0xFFEAF3F8)      // Crisp Moonlight Primary Text
val LiraDarkTextSecondary = Color(0xFF9FB5C5)    // Muted Secondary Slate
val LiraDarkTextTertiary = Color(0xFF647D92)     // Subdued Monospace Telemetry

// Dark Semantic Feedback Tokens
val LiraDarkWarning = Color(0xFFF59E0B)          // Electric Amber
val LiraDarkWarningSurface = Color(0xFF261D12)   // Matte Dark Amber Container
val LiraDarkWarningBorder = Color(0xFFB45309)
val LiraDarkError = Color(0xFFEF4444)            // Signal Red
val LiraDarkErrorSurface = Color(0xFF2E1417)     // Matte Dark Red Container
val LiraDarkErrorBorder = Color(0xFF991B1B)
val LiraDarkSuccess = Color(0xFF10B981)          // Terminal Emerald Green
val LiraDarkSuccessSurface = Color(0xFF0F2419)   // Matte Dark Green Container
val LiraDarkSuccessBorder = Color(0xFF059669)

// Backward-compatible aliases mapped to canonical LIRA-Q tokens
val IndustrialCarbonBg = LiraDarkBg
val IndustrialSteelSurface = LiraDarkSurface
val IndustrialElevatedSurface = LiraDarkSurfaceElevated
val IndustrialBorder = LiraDarkBorder
val IndustrialPrimaryAccent = LiraDarkPrimary
val IndustrialSafetyAmber = LiraDarkWarning
val IndustrialTextPrimary = LiraDarkTextPrimary
val IndustrialTextSecondary = LiraDarkTextSecondary
val IndustrialTextTertiary = LiraDarkTextTertiary
val IndustrialAlertSurface = LiraDarkErrorSurface
val IndustrialAlertBorder = LiraDarkErrorBorder
val IndustrialAlertText = LiraDarkError
val IndustrialVerifiedSurface = LiraDarkSuccessSurface
val IndustrialVerifiedBorder = LiraDarkSuccessBorder
val IndustrialVerifiedText = LiraDarkSuccess

val TerminalPaperBg = LiraLightBg
val TerminalPaperSurface = LiraLightSurface
val TerminalPaperElevated = LiraLightSurfaceElevated
val TerminalPaperBorder = LiraLightBorder
val TerminalPaperPrimary = LiraLightPrimary
val TerminalPaperSafetyAmber = LiraLightWarning
val TerminalPaperTextPrimary = LiraLightTextPrimary
val TerminalPaperTextSecondary = LiraLightTextSecondary
val TerminalPaperTextTertiary = LiraLightTextTertiary
val TerminalPaperAlertSurface = LiraLightErrorSurface
val TerminalPaperAlertBorder = LiraLightErrorBorder
val TerminalPaperAlertText = LiraLightError
val TerminalPaperVerifiedSurface = LiraLightSuccessSurface
val TerminalPaperVerifiedBorder = LiraLightSuccessBorder
val TerminalPaperVerifiedText = LiraLightSuccess

val DarkBg = LiraDarkBg
val DarkSurface = LiraDarkSurface
val DarkSurfaceElevated = LiraDarkSurfaceElevated
val DarkBorder = LiraDarkBorder
val DarkPrimary = LiraDarkPrimary
val DarkSecondary = LiraDarkSecondary
val DarkTextPrimary = LiraDarkTextPrimary
val DarkTextSecondary = LiraDarkTextSecondary
val DarkTextTertiary = LiraDarkTextTertiary

val LightBg = LiraLightBg
val LightSurface = LiraLightSurface
val LightSurfaceElevated = LiraLightSurfaceElevated
val LightBorder = LiraLightBorder
val LightPrimary = LiraLightPrimary
val LightSecondary = LiraLightSecondary
val LightTextPrimary = LiraLightTextPrimary
val LightTextSecondary = LiraLightTextSecondary
val LightTextTertiary = LiraLightTextTertiary
