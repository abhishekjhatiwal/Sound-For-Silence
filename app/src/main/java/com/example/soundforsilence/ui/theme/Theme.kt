package com.example.soundforsilence.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)

val Secondary = Color(0xFF625B71)
val OnSecondary = Color(0xFFFFFFFF)

val AppBackground = Color(0xFFFFFBFE)
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceVariant = Color(0xFFE7E0EC)

val OnSurface = Color(0xFF1C1B1F)
val OnSurfaceVariant = Color(0xFF49454F)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)

//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

@Composable
fun SoundForSilenceTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,

    background = AppBackground,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariant,

    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,

    error = Error,
    onError = OnError,
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),  // big cards
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun SoundForSilenceTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(), // you can force false if you want only light
    content: @Composable () -> Unit
) {
    // Design is clearly light; if you want to FORCE light theme:
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}