package com.traxxiontech.syncplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.traxxiontech.syncplayer.model.ThemeType

data class GlassmorphicColors(
    val surfaceGlass: Color,
    val surfaceGlassSecondary: Color,
    val borderGlass: Color,
    val glowColor: Color,
    val skeuoHighlight: Color,
    val skeuoShadow: Color,
    val backgroundGradient: List<Color>,
    val waveColor: Color
)

val LocalGlassmorphicColors = staticCompositionLocalOf {
    GlassmorphicColors(
        surfaceGlass = Color(0x2BFFFFFF),
        surfaceGlassSecondary = Color(0x1AFFFFFF),
        borderGlass = Color(0x33FFFFFF),
        glowColor = Color(0xFF00E5FF),
        skeuoHighlight = Color(0x66FFFFFF),
        skeuoShadow = Color(0x99000000),
        backgroundGradient = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF090D16)),
        waveColor = Color(0xFF00F5D4)
    )
}

object ThemeEngine {

    fun getColorScheme(theme: ThemeType, isSystemDark: Boolean): Pair<ColorScheme, GlassmorphicColors> {
        return when (theme) {
            ThemeType.SYSTEM -> {
                if (isSystemDark) getDarkScheme() else getLightScheme()
            }
            ThemeType.LIGHT -> getLightScheme()
            ThemeType.DARK -> getDarkScheme()
            ThemeType.AMOLED -> Pair(
                darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFF00363A),
                    secondary = Color(0xFFB388FF),
                    background = Color(0xFF000000),
                    surface = Color(0xFF050505),
                    surfaceVariant = Color(0xFF101010),
                    onBackground = Color(0xFFE0E0E0),
                    onSurface = Color(0xFFE0E0E0)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x24FFFFFF),
                    surfaceGlassSecondary = Color(0x14FFFFFF),
                    borderGlass = Color(0x2600E5FF),
                    glowColor = Color(0xFF00E5FF),
                    skeuoHighlight = Color(0x40FFFFFF),
                    skeuoShadow = Color(0xDD000000),
                    backgroundGradient = listOf(Color(0xFF000000), Color(0xFF080808), Color(0xFF000000)),
                    waveColor = Color(0xFF00E5FF)
                )
            )
            ThemeType.NEON -> Pair(
                darkColorScheme(
                    primary = Color(0xFF00FFCC),
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFF004D40),
                    secondary = Color(0xFFFF007F),
                    background = Color(0xFF070B19),
                    surface = Color(0xFF0E1428),
                    onBackground = Color(0xFFF0F6FC),
                    onSurface = Color(0xFFF0F6FC)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x33101E3D),
                    surfaceGlassSecondary = Color(0x2200FFCC),
                    borderGlass = Color(0x6600FFCC),
                    glowColor = Color(0xFF00FFCC),
                    skeuoHighlight = Color(0x8000FFCC),
                    skeuoShadow = Color(0xEE000511),
                    backgroundGradient = listOf(Color(0xFF050814), Color(0xFF0F1A34), Color(0xFF180A28)),
                    waveColor = Color(0xFF00FFCC)
                )
            )
            ThemeType.SPACE -> Pair(
                darkColorScheme(
                    primary = Color(0xFF7C4DFF),
                    onPrimary = Color.White,
                    secondary = Color(0xFFE040FB),
                    background = Color(0xFF08071A),
                    surface = Color(0xFF131033),
                    onBackground = Color(0xFFF3E5F5),
                    onSurface = Color(0xFFF3E5F5)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x33281E52),
                    surfaceGlassSecondary = Color(0x227C4DFF),
                    borderGlass = Color(0x44B388FF),
                    glowColor = Color(0xFFB388FF),
                    skeuoHighlight = Color(0x66FFFFFF),
                    skeuoShadow = Color(0xCC050215),
                    backgroundGradient = listOf(Color(0xFF0A051D), Color(0xFF160D3D), Color(0xFF240E48)),
                    waveColor = Color(0xFFE040FB)
                )
            )
            ThemeType.OCEAN -> Pair(
                darkColorScheme(
                    primary = Color(0xFF00B4D8),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF90E0EF),
                    background = Color(0xFF031024),
                    surface = Color(0xFF0A2240),
                    onBackground = Color(0xFFCAF0F8),
                    onSurface = Color(0xFFCAF0F8)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x330077B6),
                    surfaceGlassSecondary = Color(0x2200B4D8),
                    borderGlass = Color(0x4490E0EF),
                    glowColor = Color(0xFF00B4D8),
                    skeuoHighlight = Color(0x6690E0EF),
                    skeuoShadow = Color(0xCC000B18),
                    backgroundGradient = listOf(Color(0xFF010E1E), Color(0xFF022B42), Color(0xFF014F69)),
                    waveColor = Color(0xFF48CAE4)
                )
            )
            ThemeType.SUNSET -> Pair(
                darkColorScheme(
                    primary = Color(0xFFFF7A00),
                    onPrimary = Color.Black,
                    secondary = Color(0xFFFF0055),
                    background = Color(0xFF1F0D15),
                    surface = Color(0xFF331422),
                    onBackground = Color(0xFFFFE0E6),
                    onSurface = Color(0xFFFFE0E6)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x3855172C),
                    surfaceGlassSecondary = Color(0x22FF7A00),
                    borderGlass = Color(0x55FF7A00),
                    glowColor = Color(0xFFFF7A00),
                    skeuoHighlight = Color(0x66FFA040),
                    skeuoShadow = Color(0xDD12040B),
                    backgroundGradient = listOf(Color(0xFF1A0A10), Color(0xFF380E24), Color(0xFF541427)),
                    waveColor = Color(0xFFFF7A00)
                )
            )
            ThemeType.FOREST -> Pair(
                darkColorScheme(
                    primary = Color(0xFF00E676),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF69F0AE),
                    background = Color(0xFF09170E),
                    surface = Color(0xFF112B1B),
                    onBackground = Color(0xFFE8F5E9),
                    onSurface = Color(0xFFE8F5E9)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x33144425),
                    surfaceGlassSecondary = Color(0x2200E676),
                    borderGlass = Color(0x4469F0AE),
                    glowColor = Color(0xFF00E676),
                    skeuoHighlight = Color(0x66A7F3D0),
                    skeuoShadow = Color(0xDD040E08),
                    backgroundGradient = listOf(Color(0xFF051109), Color(0xFF0F2C1A), Color(0xFF144124)),
                    waveColor = Color(0xFF00E676)
                )
            )
            ThemeType.MIDNIGHT -> Pair(
                darkColorScheme(
                    primary = Color(0xFF448AFF),
                    onPrimary = Color.White,
                    secondary = Color(0xFF82B1FF),
                    background = Color(0xFF060D1E),
                    surface = Color(0xFF0E1A38),
                    onBackground = Color(0xFFE3F2FD),
                    onSurface = Color(0xFFE3F2FD)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x331E3A70),
                    surfaceGlassSecondary = Color(0x22448AFF),
                    borderGlass = Color(0x4482B1FF),
                    glowColor = Color(0xFF448AFF),
                    skeuoHighlight = Color(0x66B3D4FF),
                    skeuoShadow = Color(0xDD030711),
                    backgroundGradient = listOf(Color(0xFF040A17), Color(0xFF0C1935), Color(0xFF132854)),
                    waveColor = Color(0xFF448AFF)
                )
            )
            ThemeType.AURORA -> Pair(
                darkColorScheme(
                    primary = Color(0xFF00F5D4),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF7B2CBF),
                    background = Color(0xFF0B1320),
                    surface = Color(0xFF14243B),
                    onBackground = Color(0xFFF0FDF4),
                    onSurface = Color(0xFFF0FDF4)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x301E3558),
                    surfaceGlassSecondary = Color(0x2200F5D4),
                    borderGlass = Color(0x5500F5D4),
                    glowColor = Color(0xFF00F5D4),
                    skeuoHighlight = Color(0x7700F5D4),
                    skeuoShadow = Color(0xDD060B12),
                    backgroundGradient = listOf(Color(0xFF08101C), Color(0xFF102837), Color(0xFF281E48)),
                    waveColor = Color(0xFF00F5D4)
                )
            )
            ThemeType.GALAXY -> Pair(
                darkColorScheme(
                    primary = Color(0xFFFF2A85),
                    onPrimary = Color.White,
                    secondary = Color(0xFF8A2BE2),
                    background = Color(0xFF12071F),
                    surface = Color(0xFF240E3E),
                    onBackground = Color(0xFFFDF4FF),
                    onSurface = Color(0xFFFDF4FF)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x353E176A),
                    surfaceGlassSecondary = Color(0x25FF2A85),
                    borderGlass = Color(0x55FF2A85),
                    glowColor = Color(0xFFFF2A85),
                    skeuoHighlight = Color(0x66FF8AC2),
                    skeuoShadow = Color(0xDD0A0312),
                    backgroundGradient = listOf(Color(0xFF0F041B), Color(0xFF250942), Color(0xFF430E5B)),
                    waveColor = Color(0xFFFF2A85)
                )
            )
            ThemeType.CYBERPUNK -> Pair(
                darkColorScheme(
                    primary = Color(0xFFFFEE00),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF00F0FF),
                    background = Color(0xFF0F0F14),
                    surface = Color(0xFF1C1C26),
                    onBackground = Color(0xFFFFF066),
                    onSurface = Color(0xFFFAFAFA)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x35333342),
                    surfaceGlassSecondary = Color(0x25FFEE00),
                    borderGlass = Color(0x66FFEE00),
                    glowColor = Color(0xFFFFEE00),
                    skeuoHighlight = Color(0x88FFEE00),
                    skeuoShadow = Color(0xEE050508),
                    backgroundGradient = listOf(Color(0xFF0A0A0E), Color(0xFF181523), Color(0xFF24151C)),
                    waveColor = Color(0xFF00F0FF)
                )
            )
            ThemeType.ICE -> Pair(
                lightColorScheme(
                    primary = Color(0xFF0077B6),
                    onPrimary = Color.White,
                    secondary = Color(0xFF00B4D8),
                    background = Color(0xFFEBF8FF),
                    surface = Color(0xFFD8F1FF),
                    onBackground = Color(0xFF023E8A),
                    onSurface = Color(0xFF023E8A)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x66FFFFFF),
                    surfaceGlassSecondary = Color(0x44BEE3F8),
                    borderGlass = Color(0x88FFFFFF),
                    glowColor = Color(0xFF0077B6),
                    skeuoHighlight = Color(0xAAFFFFFF),
                    skeuoShadow = Color(0x330077B6),
                    backgroundGradient = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFFEBF8FF)),
                    waveColor = Color(0xFF0284C7)
                )
            )
            ThemeType.GOLD -> Pair(
                darkColorScheme(
                    primary = Color(0xFFFFD700),
                    onPrimary = Color.Black,
                    secondary = Color(0xFFFFA000),
                    background = Color(0xFF191407),
                    surface = Color(0xFF2C2208),
                    onBackground = Color(0xFFFFF8E1),
                    onSurface = Color(0xFFFFF8E1)
                ),
                GlassmorphicColors(
                    surfaceGlass = Color(0x3845340B),
                    surfaceGlassSecondary = Color(0x25FFD700),
                    borderGlass = Color(0x55FFD700),
                    glowColor = Color(0xFFFFD700),
                    skeuoHighlight = Color(0x88FFE66D),
                    skeuoShadow = Color(0xDD0D0A02),
                    backgroundGradient = listOf(Color(0xFF120E03), Color(0xFF281E05), Color(0xFF382A07)),
                    waveColor = Color(0xFFFFD700)
                )
            )
        }
    }

    private fun getDarkScheme(): Pair<ColorScheme, GlassmorphicColors> {
        return Pair(
            darkColorScheme(
                primary = Color(0xFF00E5FF),
                onPrimary = Color.Black,
                secondary = Color(0xFF7C4DFF),
                background = Color(0xFF0F141C),
                surface = Color(0xFF18202C),
                onBackground = Color(0xFFF0F6FC),
                onSurface = Color(0xFFF0F6FC)
            ),
            GlassmorphicColors(
                surfaceGlass = Color(0x2BFFFFFF),
                surfaceGlassSecondary = Color(0x1AFFFFFF),
                borderGlass = Color(0x33FFFFFF),
                glowColor = Color(0xFF00E5FF),
                skeuoHighlight = Color(0x55FFFFFF),
                skeuoShadow = Color(0x99000000),
                backgroundGradient = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF0B1320)),
                waveColor = Color(0xFF00F5D4)
            )
        )
    }

    private fun getLightScheme(): Pair<ColorScheme, GlassmorphicColors> {
        return Pair(
            lightColorScheme(
                primary = Color(0xFF0070F3),
                onPrimary = Color.White,
                secondary = Color(0xFF7928CA),
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            ),
            GlassmorphicColors(
                surfaceGlass = Color(0x80FFFFFF),
                surfaceGlassSecondary = Color(0x4DFFFFFF),
                borderGlass = Color(0x40000000),
                glowColor = Color(0xFF0070F3),
                skeuoHighlight = Color(0xCCFFFFFF),
                skeuoShadow = Color(0x1F000000),
                backgroundGradient = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFF8FAFC)),
                waveColor = Color(0xFF0070F3)
            )
        )
    }
}
