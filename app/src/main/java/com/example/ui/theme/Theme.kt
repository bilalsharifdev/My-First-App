package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WhiteAndNavyColorScheme = lightColorScheme(
    primary = NavyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = NavyBlueContainer,
    onPrimaryContainer = OnNavyBlueContainer,
    secondary = RoyalNavySecondary,
    onSecondary = Color.White,
    secondaryContainer = RoyalNavyContainer,
    onSecondaryContainer = OnRoyalNavyContainer,
    tertiary = NavyBlueLight,
    onTertiary = Color.White,
    background = BackgroundPureWhite,
    onBackground = TextPrimaryNavyInk,
    surface = SurfacePureWhite,
    onSurface = TextPrimaryNavyInk,
    surfaceVariant = SurfaceIceWhite,
    onSurfaceVariant = TextSecondarySlateNavy,
    outline = OutlineNavyTint,
    error = CrimsonDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WhiteAndNavyColorScheme,
        typography = Typography,
        content = content
    )
}
