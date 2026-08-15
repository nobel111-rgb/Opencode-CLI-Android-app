package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanMinimalismColorScheme = lightColorScheme(
  primary = MinimalPrimary,
  onPrimary = Color.White,
  primaryContainer = MinimalPrimaryContainer,
  onPrimaryContainer = MinimalOnPrimaryContainer,

  secondary = MinimalTextSecondary,
  onSecondary = Color.White,
  secondaryContainer = MinimalSurfaceVariant,
  onSecondaryContainer = MinimalTextPrimary,

  tertiary = MinimalSuccess,
  onTertiary = Color.White,
  tertiaryContainer = MinimalSuccessContainer,
  onTertiaryContainer = MinimalTextPrimary,

  error = MinimalDanger,
  onError = Color.White,
  errorContainer = MinimalDangerContainer,
  onErrorContainer = MinimalDanger,

  background = MinimalBackground,
  onBackground = MinimalTextPrimary,

  surface = MinimalSurface,
  onSurface = MinimalTextPrimary,
  surfaceVariant = MinimalSurfaceVariant,
  onSurfaceVariant = MinimalTextSecondary,

  outline = MinimalOutline,
  outlineVariant = MinimalOutlineVariant
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CleanMinimalismColorScheme,
    typography = Typography,
    content = content
  )
}
