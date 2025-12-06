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



// *******************************************************************
// DELETE ALL THE EXISTING COLOR DEFINITIONS (Primary, OnPrimary, etc.)
// As they are now defined in Color.kt (SfsPrimary, SfsOnPrimary, etc.)
// *******************************************************************

private val DarkColorScheme = darkColorScheme(
    // Define a proper dark theme using the Sfs colors if you plan to support it
    primary = SfsPrimary, // Example
    secondary = SfsSecondary, // Example
    tertiary = Pink80 // Example
)

// --- Light Color Scheme (Based on App UI) ---
private val LightColorScheme = lightColorScheme(
    // Primary Brand Color
    primary = SfsPrimary,
    onPrimary = SfsOnPrimary,
    primaryContainer = SfsPrimaryVariant,
    onPrimaryContainer = SfsOnPrimary,

    // Secondary Accent
    secondary = SfsSecondary,
    onSecondary = SfsOnPrimary,
    secondaryContainer = SfsSecondary, // Use the secondary color for its container

    // Backgrounds and Surfaces
    background = SfsBackground,
    onBackground = SfsOnSurface,
    surface = SfsSurface,
    onSurface = SfsOnSurface,
    surfaceVariant = SfsBackground, // Use the background for surface variant
    onSurfaceVariant = SfsGrey,

    // Outlines and other states
    outline = SfsOutline,
    error = SfsError,
    onError = Color.White
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    // Medium is perfect for the list items/cards (My Profile, Detection Stage)
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),  // big cards
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)


@Composable
fun SoundForSilenceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (You may want to disable it to force your brand colors)
    dynamicColor: Boolean = false, // Set to false to ensure brand colors are used
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor is now false, so this block will be skipped
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // The app is clearly designed for a light theme, so let's enforce it unless requested otherwise
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure Typography is imported or defined
        shapes = AppShapes, // Add shapes here
        content = content
    )
}

// DELETE THE DUPLICATE SoundForSilenceTheme function at the end of your original file.
// Keep only the first one, now updated.
/*
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
*/