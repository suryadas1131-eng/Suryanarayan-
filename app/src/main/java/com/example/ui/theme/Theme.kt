package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val PremiumLightColorScheme = lightColorScheme(
    primary = PhysioTealPrimary,
    onPrimary = Color.White,
    primaryContainer = PhysioTealContainer,
    onPrimaryContainer = PhysioTealOnContainer,
    secondary = PhysioSecondary,
    onSecondary = Color.White,
    secondaryContainer = PhysioSecondaryContainer,
    onSecondaryContainer = PhysioSecondaryDark,
    tertiary = PhysioAccentAmber,
    onTertiary = Color.White,
    background = PhysioBackgroundLight,
    onBackground = PhysioOnSurfaceLight,
    surface = PhysioSurfaceLight,
    onSurface = PhysioOnSurfaceLight,
    surfaceVariant = PhysioSurfaceVariantLight,
    onSurfaceVariant = PhysioOnSurfaceVariantLight,
    outline = PhysioOutlineLight,
    outlineVariant = PhysioOutlineLight.copy(alpha = 0.6f)
)

private val LightColorScheme = PremiumLightColorScheme

@Composable
fun PhysioCareTheme(
    darkTheme: Boolean = false, // Enforce crisp, premium healthcare Light theme by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = PremiumLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    PhysioCareTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

