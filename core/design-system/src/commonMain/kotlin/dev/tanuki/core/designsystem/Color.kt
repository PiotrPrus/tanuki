package dev.tanuki.core.designsystem

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Tanuki palette (from the Stitch "Tanuki Mobile Design System") ---
// Warm, light, "Earth Tech": GitLab Orange + Deep Indigo + Cedar Brown on Almond Cream.

internal val TanukiOrange = Color(0xFFB32109)      // primary
internal val TanukiOrangeContainer = Color(0xFFD63B22)
internal val DeepIndigo = Color(0xFF585893)         // secondary
internal val DeepIndigoContainer = Color(0xFFBBBBFD)
internal val CedarBrown = Color(0xFF725647)         // tertiary
internal val CedarBrownContainer = Color(0xFF8C6E5F)

internal val AlmondCream = Color(0xFFFBF9F7)         // background / surface
internal val OnSurface = Color(0xFF1B1C1B)
internal val OnSurfaceVariant = Color(0xFF5B403B)
internal val ErrorRed = Color(0xFFBA1A1A)

internal val TanukiLightColorScheme = lightColorScheme(
    primary = TanukiOrange,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = TanukiOrangeContainer,
    onPrimaryContainer = Color(0xFFFFFBFF),
    inversePrimary = Color(0xFFFFB4A6),
    secondary = DeepIndigo,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = DeepIndigoContainer,
    onSecondaryContainer = Color(0xFF484982),
    tertiary = CedarBrown,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = CedarBrownContainer,
    onTertiaryContainer = Color(0xFFFFFBFF),
    error = ErrorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = AlmondCream,
    onBackground = OnSurface,
    surface = AlmondCream,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE4E2E0),
    onSurfaceVariant = OnSurfaceVariant,
    surfaceBright = AlmondCream,
    surfaceDim = Color(0xFFDBDAD8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F3F1),
    surfaceContainer = Color(0xFFEFEDEC),
    surfaceContainerHigh = Color(0xFFEAE8E6),
    surfaceContainerHighest = Color(0xFFE4E2E0),
    outline = Color(0xFF8F706A),
    outlineVariant = Color(0xFFE4BEB7),
    inverseSurface = Color(0xFF30302F),
    inverseOnSurface = Color(0xFFF2F0EE),
    surfaceTint = TanukiOrange,
)

/**
 * Semantic colors not covered by [androidx.compose.material3.ColorScheme] — currently the
 * diff view. Exposed via [LocalTanukiColors] and `TanukiTheme.colors`.
 */
data class TanukiColors(
    // Saturated enough to clearly stand out from the cream (unchanged) surface.
    val diffAddedBackground: Color = Color(0xFFCFEFD6),
    // Dark green / dark red so +/- read clearly on the light surfaces.
    val diffAddedAccent: Color = Color(0xFF166534),
    val diffRemovedBackground: Color = Color(0xFFF9D2D2),
    val diffRemovedAccent: Color = Color(0xFFB3261E),
    val success: Color = Color(0xFF166534),
    val warning: Color = Color(0xFFB08800),
)
