package com.ottertondev.metroatbkk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = TransitGreenDark,
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF005227),
    onPrimaryContainer = Color(0xFFB5F2C0),
    secondary = TransitBlueDark,
    onSecondary = Color(0xFF00315F),
    secondaryContainer = Color(0xFF004787),
    onSecondaryContainer = Color(0xFFD5E3FF),
    tertiary = TransitRedDark,
    onTertiary = Color(0xFF690005),
    tertiaryContainer = Color(0xFF93000A),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = BangkokSurfaceDark,
    onBackground = Color(0xFFE8E2D8),
    surface = Color(0xFF1E1D19),
    onSurface = Color(0xFFE8E2D8),
    surfaceContainer = Color(0xFF24231F),
    surfaceContainerLow = Color(0xFF1A1915),
    surfaceContainerHigh = Color(0xFF302F2A),
    surfaceContainerHighest = Color(0xFF3B3934),
    outline = Color(0xFF999386),
    outlineVariant = Color(0xFF4C473E),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = TransitGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F5C2),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = TransitBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001B3D),
    tertiary = TransitRed,
    onTertiary = Color.White,
    tertiaryContainer = TransitYellowContainer,
    onTertiaryContainer = Color(0xFF231B00),
    background = BangkokSurface,
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBF3),
    onSurface = Color(0xFF1F1B16),
    surfaceContainer = BangkokSurface,
    surfaceContainerLow = Color(0xFFFFFCF7),
    surfaceContainerHigh = Color(0xFFECE6DC),
    surfaceContainerHighest = Color(0xFFE1DBD1),
    outline = Color(0xFF7C766B),
    outlineVariant = Color(0xFFCEC5B8),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val MetroShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MetroAtBKKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        shapes = MetroShapes,
        content = content
    )
}
