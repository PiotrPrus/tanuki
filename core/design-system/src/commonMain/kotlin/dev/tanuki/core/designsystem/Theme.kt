package dev.tanuki.core.designsystem

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
    content: @Composable () -> Unit,
) {
    // The design is a light, warm "Almond Cream" system. A dark variant is a follow-up.
    CompositionLocalProvider(LocalTanukiColors provides TanukiColors()) {
        MaterialTheme(
            colorScheme = TanukiLightColorScheme,
            typography = TanukiTypography,
            shapes = TanukiShapes,
            content = content,
        )
    }
}
