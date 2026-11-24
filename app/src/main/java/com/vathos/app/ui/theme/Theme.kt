
package com.vathos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vathos.app.data.AppTheme

private val VathosDarkColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF062E22),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF062E22),
    tertiary = Color(0xFF6EE7B7),
    onTertiary = Color(0xFF062E22),
    background = Color(0xFF101418),
    onBackground = Color(0xFFF1F5F3),
    surface = Color(0xFF191F24),
    onSurface = Color(0xFFF1F5F3),
    surfaceVariant = Color(0xFF252D32),
    onSurfaceVariant = Color(0xFFB5C0BC),
    outline = Color(0xFF46534E)
)

private val VathosLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    tertiary = Color(0xFF047857),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF171B24),
    surface = Color.White,
    onSurface = Color(0xFF171B24),
    surfaceVariant = Color(0xFFE9EFEC),
    onSurfaceVariant = Color(0xFF59645F),
    outline = Color(0xFFD5DDD8)
)

@Composable
fun VathosTheme(
    themePreference: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themePreference) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemDark
    }

    val colorScheme = if (darkTheme) {
        VathosDarkColorScheme
    } else {
        VathosLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
