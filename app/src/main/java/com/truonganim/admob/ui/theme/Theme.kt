package com.truonganim.admob.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.truonganim.admob.config.AppConfig

/**
 * Light Color Scheme
 * Based on white background with brand red accent
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Brand Red
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5E9),
    onPrimaryContainer = BrandRedDark,

    // Secondary - Neutral
    secondary = Color(0xFF757575),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF424242),

    // Tertiary - Accent
    tertiary = BrandRedLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFCDD2),
    onTertiaryContainer = BrandRedDark,

    // Background & Surface
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A5A5A),

    // Outline & Border
    outline = LightOutline,
    outlineVariant = Color(0xFFF0F0F0),

    // Error
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFDEDED),
    onErrorContainer = Color(0xFF8C0009)
)

/**
 * Dark Color Scheme
 * Based on #0F1112 background with brand red accent
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Brand Red
    primary = BrandRed,                        // #F82F4C
    onPrimary = Color.White,
    primaryContainer = BrandRedDark,
    onPrimaryContainer = Color(0xFFFFCDD2),

    // Secondary - Neutral grays
    secondary = Color(0xFFB0B0B0),
    onSecondary = AppDarkBackground,
    secondaryContainer = Color(0xFF2A2B2C),
    onSecondaryContainer = Color(0xFFE0E0E0),

    // Tertiary - Accent
    tertiary = BrandRedLight,
    onTertiary = Color.White,
    tertiaryContainer = BrandRedDark,
    onTertiaryContainer = Color(0xFFFFCDD2),

    // Background & Surface
    background = DarkBackground,               // #0F1112
    onBackground = DarkOnBackground,           // #FFFFFF
    surface = DarkSurface,                     // #1A1B1C (slightly lighter)
    onSurface = DarkOnSurface,                 // #E8E8E8
    surfaceVariant = DarkSurfaceVariant,       // #252627 (for cards)
    onSurfaceVariant = Color(0xFFC0C0C0),

    // Outline & Border
    outline = DarkOutline,                     // #3A3B3C
    outlineVariant = Color(0xFF2A2B2C),

    // Error
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

/**
 * Custom App Colors
 * Contains colors that are not part of Material3 ColorScheme
 */
data class AppColors(
    val imageScreenBackground: Color
)

/**
 * CompositionLocal for AppColors
 */
val LocalAppColors = staticCompositionLocalOf {
    AppColors(imageScreenBackground = AppImageBackground)
}

@Composable
fun AdMobBaseTheme(
    darkTheme: Boolean = if (AppConfig.UI.FORCE_DARK_MODE) true else isSystemInDarkTheme(),
    // Dynamic color disabled to use custom brand colors
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

    // Custom app colors based on theme
    val appColors = AppColors(
        imageScreenBackground = if (darkTheme) AppImageBackground else AppLightImageBackground
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}