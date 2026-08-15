package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class TerminalThemeType(
  val displayName: String,
  val description: String
) {
  DARK_MINIMAL("Dark Minimal", "High-contrast slate dark theme with vibrant terminal green and cyan accents"),
  SOLARIZED("Solarized Dark", "Classic precision palette using deep teal-cyan (#002B36) with solarized yellow and magenta")
}

data class TerminalColorScheme(
  val type: TerminalThemeType,
  val background: Color,
  val surfaceBg: Color,
  val cardBg: Color,
  val cardBorder: Color,
  val headerBg: Color,
  val promptGreen: Color,
  val promptPrefix: Color,
  val cyanAccent: Color,
  val amberAccent: Color,
  val roseAccent: Color,
  val purpleAccent: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val textDim: Color,
  val dotRed: Color,
  val dotYellow: Color,
  val dotGreen: Color
)

object TerminalThemes {
  val DarkMinimal = TerminalColorScheme(
    type = TerminalThemeType.DARK_MINIMAL,
    background = Color(0xFF141517),
    surfaceBg = Color(0xFF1A1C1E),
    cardBg = Color(0xFF222428),
    cardBorder = Color(0xFF373940),
    headerBg = Color(0xFF1E2024),
    promptGreen = Color(0xFF28C840),
    promptPrefix = Color(0xFF34A853),
    cyanAccent = Color(0xFF48CAE4),
    amberAccent = Color(0xFFFEBC2E),
    roseAccent = Color(0xFFFF5F57),
    purpleAccent = Color(0xFFC77DFF),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFFC4C6D0),
    textMuted = Color(0xFF8E9099),
    textDim = Color(0xFF64666E),
    dotRed = Color(0xFFFF5F57),
    dotYellow = Color(0xFFFEBC2E),
    dotGreen = Color(0xFF28C840)
  )

  val Solarized = TerminalColorScheme(
    type = TerminalThemeType.SOLARIZED,
    background = Color(0xFF002B36), // Solarized base03
    surfaceBg = Color(0xFF073642), // Solarized base02
    cardBg = Color(0xFF0A4452),
    cardBorder = Color(0xFF268BD2).copy(alpha = 0.35f),
    headerBg = Color(0xFF052B35),
    promptGreen = Color(0xFF859900), // Solarized green
    promptPrefix = Color(0xFF2AA198), // Solarized cyan
    cyanAccent = Color(0xFF2AA198), // Solarized cyan
    amberAccent = Color(0xFFB58900), // Solarized yellow
    roseAccent = Color(0xFFDC322F), // Solarized red
    purpleAccent = Color(0xFF6C71C4), // Solarized violet
    textPrimary = Color(0xFF93A1A1), // Solarized base1
    textSecondary = Color(0xFF839496), // Solarized base0
    textMuted = Color(0xFF586E75), // Solarized base01
    textDim = Color(0xFF586E75).copy(alpha = 0.7f),
    dotRed = Color(0xFFDC322F),
    dotYellow = Color(0xFFB58900),
    dotGreen = Color(0xFF859900)
  )

  fun getScheme(type: TerminalThemeType): TerminalColorScheme {
    return when (type) {
      TerminalThemeType.DARK_MINIMAL -> DarkMinimal
      TerminalThemeType.SOLARIZED -> Solarized
    }
  }
}
