package com.studyconnect.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenLight = Color(0xFF6FA02E)
val Green = Color(0xFF4C7F1B)
val GreenDark = Color(0xFF2F5A12)
val Background = Color(0xFFF6F7F2)
val FieldBg = Color(0xFFF0F1EC)
val ErrorRed = Color(0xFFD93025)
val ErrorBg = Color(0xFFFDECEA)
val Amber = Color(0xFFFFB300)
val TextMuted = Color(0xFF6B7280)
val TextDark = Color(0xFF1F2937)

private val Colors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = GreenLight,
    background = Background,
    surface = Color.White,
    onSurface = TextDark,
    error = ErrorRed
)

@Composable
fun StudyConnectTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
