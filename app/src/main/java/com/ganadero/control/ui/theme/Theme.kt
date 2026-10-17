package com.ganadero.control.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    primaryContainer = OrangePale,
    onPrimaryContainer = OrangeDark,
    secondary = OrangeDark,
    background = MainBg,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = MainBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderGray,
    error = Danger
)

@Composable
fun GanaderoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
