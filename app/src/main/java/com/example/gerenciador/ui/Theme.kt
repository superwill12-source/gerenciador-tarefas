package com.example.gerenciador.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Dark = darkColorScheme(
    primary = Color(0xFF22D3EE),
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF0F172A),
    surface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFF1E293B),
    primaryContainer = Color(0xFF155E75),
)

private val Light = lightColorScheme(
    primary = Color(0xFF0E7490),
    secondary = Color(0xFF059669),
    tertiary = Color(0xFFD97706),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFFE2E8F0),
    primaryContainer = Color(0xFFCFFAFE),
)

@Composable
fun GerenciadorTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
