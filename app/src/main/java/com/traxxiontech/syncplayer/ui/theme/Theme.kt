package com.traxxiontech.syncplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.traxxiontech.syncplayer.model.ThemeType

@Composable
fun SyncPlayerTheme(
    selectedTheme: ThemeType = ThemeType.NEON,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val (colorScheme, glassColors) = ThemeEngine.getColorScheme(selectedTheme, isSystemDark)

    CompositionLocalProvider(LocalGlassmorphicColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
