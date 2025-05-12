package com.example.quotex.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Futuristic color palette
val DeepSpace = Color(0xFF0A0E17)
val CosmicBlack = Color(0xFF121721)
val NebulaPurple = Color(0xFF8E44EC)
val CyberBlue = Color(0xFF00A7FE)
val NeonPink = Color(0xFFFF2A6D)
val ElectricGreen = Color(0xFF00F5A0)
val StarWhite = Color(0xFFEEF2FF)
val MoonGray = Color(0xFFAEB6CC)

// Surface colors with transparency
val GlassSurface = Color(0x80162036)
val GlassSurfaceLight = Color(0x40162036)
val GlassSurfaceDark = Color(0xCC162036)

// Accent colors for highlighting
val AccentOrange = Color(0xFFFF8E42)
val AccentYellow = Color(0xFFFFC045)

private val DarkColorScheme = darkColorScheme(
    primary = NebulaPurple,
    onPrimary = StarWhite,
    primaryContainer = GlassSurface,
    onPrimaryContainer = StarWhite,

    secondary = CyberBlue,
    onSecondary = StarWhite,
    secondaryContainer = GlassSurfaceLight,
    onSecondaryContainer = StarWhite,

    tertiary = NeonPink,
    onTertiary = StarWhite,
    tertiaryContainer = GlassSurfaceDark,
    onTertiaryContainer = StarWhite,

    background = DeepSpace,
    onBackground = StarWhite,

    surface = CosmicBlack,
    onSurface = StarWhite,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = MoonGray,

    error = AccentOrange,
    onError = StarWhite
)

private val LightColorScheme = lightColorScheme(
    primary = NebulaPurple,
    onPrimary = StarWhite,
    primaryContainer = Color(0xFFEDE0FF),
    onPrimaryContainer = Color(0xFF25005A),

    secondary = CyberBlue,
    onSecondary = StarWhite,
    secondaryContainer = Color(0xFFD5E4F7),
    onSecondaryContainer = Color(0xFF001C38),

    tertiary = NeonPink,
    onTertiary = StarWhite,
    tertiaryContainer = Color(0xFFFFD9E0),
    onTertiaryContainer = Color(0xFF3F0018),

    background = Color(0xFFF8F8FC),
    onBackground = Color(0xFF1A1A1A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF474747),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun QuoteXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}