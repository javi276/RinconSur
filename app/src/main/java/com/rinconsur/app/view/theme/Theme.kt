package com.rinconsur.app.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de RinconSur (Material 3): azul petróleo, coral y verde turquesa
private val LightColors = lightColorScheme(
    primary = Color(0xFF153B50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8EAF3),
    onPrimaryContainer = Color(0xFF082633),
    secondary = Color(0xFFF26F5B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD3),
    onSecondaryContainer = Color(0xFF431108),
    tertiary = Color(0xFF2A9D8F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7F3ED),
    onTertiaryContainer = Color(0xFF073D36)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD0E8),
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF174B61),
    onPrimaryContainer = Color(0xFFC7EFFF),
    secondary = Color(0xFFFFB5A5),
    onSecondary = Color(0xFF5D180F),
    secondaryContainer = Color(0xFF7D2E22),
    onSecondaryContainer = Color(0xFFFFDAD3),
    tertiary = Color(0xFF73D4C5),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFF8FF2E2)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
