package dev.tanuki.core.designsystem

import androidx.compose.material3.darkColorScheme
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

// --- Tanuki dark palette ("Luminous Hearth Dark" from DESIGN.md) ---
// Deep-space indigo surfaces, peach-orange brand accents, WCAG-AA on dark.

internal val TanukiDarkColorScheme = darkColorScheme(
    // Vivid brand orange as primary (AA-compliant on dark per DESIGN.md) so links, selected
    // states, and filled buttons all read the brand color, matching the dark mock.
    primary = Color(0xFFFF783D),
    onPrimary = Color(0xFF5A1C00),
    primaryContainer = Color(0xFFFF783D),
    onPrimaryContainer = Color(0xFF642000),
    inversePrimary = Color(0xFFA63B00),
    secondary = Color(0xFFB9C8DE),
    onSecondary = Color(0xFF233143),
    secondaryContainer = Color(0xFF39485A),
    onSecondaryContainer = Color(0xFFA7B6CC),
    tertiary = Color(0xFFFCB973),
    onTertiary = Color(0xFF492900),
    tertiaryContainer = Color(0xFFD19351),
    onTertiaryContainer = Color(0xFF522E00),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1326),
    onBackground = Color(0xFFDAE2FD),
    surface = Color(0xFF0B1326),
    onSurface = Color(0xFFDAE2FD),
    surfaceVariant = Color(0xFF2D3449),
    onSurfaceVariant = Color(0xFFE0C0B4),
    surfaceBright = Color(0xFF31394D),
    surfaceDim = Color(0xFF0B1326),
    surfaceContainerLowest = Color(0xFF060E20),
    surfaceContainerLow = Color(0xFF131B2E),
    surfaceContainer = Color(0xFF171F33),
    surfaceContainerHigh = Color(0xFF222A3D),
    surfaceContainerHighest = Color(0xFF2D3449),
    outline = Color(0xFFA78B80),
    outlineVariant = Color(0xFF584239),
    inverseSurface = Color(0xFFDAE2FD),
    inverseOnSurface = Color(0xFF283044),
    surfaceTint = Color(0xFFFFB599),
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

/** Light extended colors (defaults). */
internal val TanukiLightColors = TanukiColors()

/**
 * Dark extended colors: subtle dark tints behind diff rows, bright accents for +/− signs
 * and status, so they stay legible on the deep-space surfaces.
 */
internal val TanukiDarkColors = TanukiColors(
    diffAddedBackground = Color(0xFF13331F),
    diffAddedAccent = Color(0xFF4ADE80),
    diffRemovedBackground = Color(0xFF3A191C),
    diffRemovedAccent = Color(0xFFFB7185),
    success = Color(0xFF34D399),
    warning = Color(0xFFFCB973),
)
