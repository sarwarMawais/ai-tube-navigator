package com.londontubeai.navigator.ui.theme

import androidx.compose.ui.graphics.Color

// Primary brand colors inspired by the London Underground
val TubePrimary = Color(0xFF003688)        // Piccadilly blue
val TubePrimaryDark = Color(0xFF002366)
val TubePrimaryLight = Color(0xFF4D6FAF)

val TubeSecondary = Color(0xFF0098D4)      // Victoria line teal
val TubeSecondaryDark = Color(0xFF006B9A)
val TubeSecondaryLight = Color(0xFF4DB8E0)

val TubeAccent = Color(0xFFE32017)         // Central line red
val TubeAccentLight = Color(0xFFFF5A4F)

// Warm surface palette (Trainline-style — warm off-white, not cold grey)
val TubeWhite      = Color(0xFFFFFFFF)
val TubeOffWhite   = Color(0xFFFAFAF9)   // warm background — the key difference
val TubeSurface2   = Color(0xFFF4F3F1)   // warm light grey
val TubeSurface3   = Color(0xFFECEAE7)   // warm medium grey
val TubeInk        = Color(0xFF0F0F0E)   // near-black
val TubeInk2       = Color(0xFF4A4845)   // warm secondary text
val TubeInk3       = Color(0xFF9B9793)   // warm tertiary text
val TubeBorder     = Color(0xFFE8E6E2)   // warm border
val TubeBorder2    = Color(0xFFD4D1CC)   // warm stronger border

// Legacy surface names (kept for compatibility)
val SurfaceLight = TubeOffWhite
val SurfaceDark = Color(0xFF0F0F0E)
val CardLight = TubeWhite
val CardDark = Color(0xFF1A1A18)
val CardDarkElevated = Color(0xFF242422)

// Text
val TextPrimaryLight = TubeInk
val TextSecondaryLight = TubeInk2
val TextPrimaryDark = Color(0xFFF0F0EE)
val TextSecondaryDark = Color(0xFF9B9793)

// Status colors — warmer tones for better legibility
val StatusGood    = Color(0xFF16A34A)
val StatusGoodBg  = Color(0xFFF0FDF4)
val StatusMinor   = Color(0xFFD97706)
val StatusMinorBg = Color(0xFFFFFBEB)
val StatusSevere  = Color(0xFFDC2626)
val StatusSevereBg = Color(0xFFFEF2F2)
val StatusClosed  = Color(0xFF9B0056)
val StatusInfo    = Color(0xFF0369A1)

// Tube line official colors
object TubeLineColors {
    val Bakerloo        = Color(0xFFB36305)
    val Central         = Color(0xFFE32017)
    val Circle          = Color(0xFFFFD300)
    val District        = Color(0xFF00782A)
    val HammersmithCity = Color(0xFFD4618A)  // darker pink for readability
    val Jubilee         = Color(0xFF6F7378)   // darker grey for readability
    val Metropolitan    = Color(0xFF9B0056)
    val Northern        = Color(0xFF1A1A1A)   // near-black instead of pure black
    val Piccadilly      = Color(0xFF003688)
    val Victoria        = Color(0xFF0098D4)
    val WaterlooCity    = Color(0xFF6BA894)   // darker teal for readability
    val Elizabeth       = Color(0xFF6950A1)
    val DLR             = Color(0xFF00A4A7)
    val Overground      = Color(0xFFCC6600)   // darker orange for readability
}

// Material 3 scheme - Light (warm)
val md_theme_light_primary = TubePrimary
val md_theme_light_onPrimary = Color.White
val md_theme_light_primaryContainer = Color(0xFFD4E3FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001C38)
val md_theme_light_secondary = TubeSecondary
val md_theme_light_onSecondary = Color.White
val md_theme_light_secondaryContainer = Color(0xFFCDE5FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001D32)
val md_theme_light_tertiary = Color(0xFF6950A1)
val md_theme_light_onTertiary = Color.White
val md_theme_light_tertiaryContainer = Color(0xFFE9DDFF)
val md_theme_light_onTertiaryContainer = Color(0xFF230F5A)
val md_theme_light_error = Color(0xFFDC2626)
val md_theme_light_onError = Color.White
val md_theme_light_errorContainer = Color(0xFFFEF2F2)
val md_theme_light_onErrorContainer = Color(0xFF7F1D1D)
val md_theme_light_background = TubeOffWhite
val md_theme_light_onBackground = TubeInk
val md_theme_light_surface = TubeWhite
val md_theme_light_onSurface = TubeInk
val md_theme_light_surfaceVariant = TubeSurface2
val md_theme_light_onSurfaceVariant = TubeInk2
val md_theme_light_outline = TubeBorder

// Material 3 scheme - Dark
val md_theme_dark_primary = Color(0xFFA5C8FF)
val md_theme_dark_onPrimary = Color(0xFF00315B)
val md_theme_dark_primaryContainer = Color(0xFF004880)
val md_theme_dark_onPrimaryContainer = Color(0xFFD4E3FF)
val md_theme_dark_secondary = Color(0xFF8DCEFF)
val md_theme_dark_onSecondary = Color(0xFF003352)
val md_theme_dark_secondaryContainer = Color(0xFF004B75)
val md_theme_dark_onSecondaryContainer = Color(0xFFCDE5FF)
val md_theme_dark_tertiary = Color(0xFFCFBDFF)
val md_theme_dark_onTertiary = Color(0xFF3A2370)
val md_theme_dark_tertiaryContainer = Color(0xFF513888)
val md_theme_dark_onTertiaryContainer = Color(0xFFE9DDFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = SurfaceDark
val md_theme_dark_onBackground = TextPrimaryDark
val md_theme_dark_surface = Color(0xFF1A1A18)
val md_theme_dark_onSurface = TextPrimaryDark
val md_theme_dark_surfaceVariant = Color(0xFF2E2E2C)
val md_theme_dark_onSurfaceVariant = TextSecondaryDark
val md_theme_dark_outline = Color(0xFF52524E)
