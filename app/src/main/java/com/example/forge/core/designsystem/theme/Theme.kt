package com.example.forge.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ExtendedColors(
    val success: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Color.Unspecified
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    error = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    error = ErrorLight
)

/**
 * A simulated Material You dynamic dark color scheme for gray/white wallpaper themes,
 * used for testing high-contrast compliance in previews.
 */
val GrayMonochromeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2E2E2),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFD6D6D6),
    onPrimaryContainer = Color(0xFF121212),
    secondary = Color(0xFFC6C6C6),
    onSecondary = Color(0xFF2A2A2A),
    secondaryContainer = Color(0xFF444444),
    onSecondaryContainer = Color(0xFFE2E2E2),
    tertiary = Color(0xFFCCCCCC),
    onTertiary = Color(0xFF1F1F1F),
    tertiaryContainer = Color(0xFF383838),
    onTertiaryContainer = Color(0xFFE2E2E2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE2E2E2),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF282828),
    surfaceContainerHighest = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFC4C4C4),
    outline = Color(0xFF8E8E8E),
    outlineVariant = Color(0xFF484848)
)

@Composable
fun ForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) {
        ExtendedColors(success = SuccessDark)
    } else {
        ExtendedColors(success = SuccessLight)
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ForgeTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
