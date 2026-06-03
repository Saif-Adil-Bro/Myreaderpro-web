package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryGreen,
    secondary = AccentGreen,
    tertiary = CardBgAlt,
    background = DarkBg,
    surface = CardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = CardBgAlt,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
  )

private val LightColorScheme = DarkColorScheme // Standardize on dark theme for Sophisticated Dark aesthetic

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default for Sophisticated Dark
  dynamicColor: Boolean = false, // Disable dynamic color to preserve custom Sophisticated Dark branding
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
