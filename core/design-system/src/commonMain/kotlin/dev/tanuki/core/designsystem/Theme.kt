package dev.tanuki.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/** Access to the Tanuki extended (non-Material) colors, e.g. `TanukiTheme.colors.diffAddedAccent`. */
val LocalTanukiColors = staticCompositionLocalOf { TanukiColors() }

object TanukiTheme {
    val colors: TanukiColors
        @Composable
        get() = LocalTanukiColors.current
}

@Composable
fun TanukiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Light: warm "Almond Cream". Dark: "Luminous Hearth" deep-space indigo. Follows the system.
    val colorScheme = if (darkTheme) TanukiDarkColorScheme else TanukiLightColorScheme
    val tanukiColors = if (darkTheme) TanukiDarkColors else TanukiLightColors
    CompositionLocalProvider(LocalTanukiColors provides tanukiColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TanukiTypography,
            shapes = TanukiShapes,
            content = content,
        )
    }
}
