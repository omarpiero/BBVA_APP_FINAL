package com.example.appbanco_s8.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema oscuro (principal — igual que BBVA dark mode)
private val DarkColorScheme = darkColorScheme(
    primary          = AzulBanco,
    onPrimary        = BlancoTexto,
    primaryContainer = AzulOscuro,

    secondary        = AzulClaro,
    onSecondary      = BlancoTexto,

    tertiary         = DoradoBanco,
    onTertiary       = AzulMarino,

    background       = AzulMarino,
    onBackground     = BlancoTexto,

    surface          = AzulMedio,
    onSurface        = BlancoTexto,
    surfaceVariant   = GrisSurface,
    onSurfaceVariant = GrisTexto,

    outline          = GrisBorde,
    error            = RojoError,
    onError          = BlancoTexto
)

// Esquema claro (fallback)
private val LightColorScheme = lightColorScheme(
    primary          = AzulBanco,
    onPrimary        = BlancoTexto,
    primaryContainer = Color(0xFFD6E4FF),

    secondary        = AzulClaro,
    onSecondary      = BlancoTexto,

    tertiary         = DoradoOscuro,
    onTertiary       = BlancoTexto,

    background       = Color(0xFFF4F6FB),
    onBackground     = Color(0xFF0D1F3C),

    surface          = BlancoTexto,
    onSurface        = Color(0xFF0D1F3C),
    surfaceVariant   = Color(0xFFE8EFF8),
    onSurfaceVariant = Color(0xFF4A5568),

    outline          = Color(0xFFCBD5E0),
    error            = RojoError,
    onError          = BlancoTexto
)

@Composable
fun Appbanco_s8Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // dynamicColor desactivado — usamos paleta propia del banco
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AzulMarino.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}