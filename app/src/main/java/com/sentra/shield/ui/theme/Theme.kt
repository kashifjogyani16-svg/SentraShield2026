package com.sentra.shield.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SentraPrimary = Color(0xFF00B4D8)
val SentraSafe = Color(0xFF00E676)
val SentraInfo = Color(0xFF00B4D8)
val SentraWarning = Color(0xFFFFB300)
val SentraDanger = Color(0xFFFF5252)

private val DarkColors = darkColorScheme(
    primary = SentraPrimary,
    onPrimary = Color.Black,
    secondary = SentraSafe,
    background = Color(0xFF0B0F14),
    surface = Color(0xFF121820),
    onBackground = Color(0xFFE6EEF3),
    onSurface = Color(0xFFE6EEF3),
    error = SentraDanger
)

private val LightColors = lightColorScheme(
    primary = SentraPrimary,
    onPrimary = Color.White,
    secondary = SentraSafe,
    background = Color(0xFFF5F8FA),
    surface = Color.White,
    error = SentraDanger
)

@Composable
fun SentraShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        ),
        content = content
    )
}

val MonoLabelStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
