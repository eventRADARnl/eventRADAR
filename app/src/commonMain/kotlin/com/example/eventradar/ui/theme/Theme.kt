package com.example.eventradar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun EventRADARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isDarkModeOverride: Boolean? = null,
    content: @Composable () -> Unit
) {
    val actualDarkTheme = isDarkModeOverride ?: darkTheme
    val colorScheme = if (actualDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
