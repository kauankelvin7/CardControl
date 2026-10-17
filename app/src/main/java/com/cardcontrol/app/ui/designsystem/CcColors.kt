package com.cardcontrol.app.ui.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design System Vitrine v2.1 — Tokens Cromáticos Oficiais (B3).
 * Fonte ÚNICA de cores do CardControl.
 *
 * Modo Claro: Petróleo & Cobre (B3.1)
 * Modo Escuro: Grafite (B3.2)
 */

val CcLightPetroleo: ColorScheme = lightColorScheme(
    primary = Color(0xFF006972),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8FF2FE),
    onPrimaryContainer = Color(0xFF001F23),
    secondary = Color(0xFF416468),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC3E9EE),
    onSecondaryContainer = Color(0xFF001F23),
    tertiary = Color(0xFF83532D), // Cobre
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC4),
    onTertiaryContainer = Color(0xFF2C1700),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF4FBFB),
    onBackground = Color(0xFF181C1D),
    surface = Color(0xFFF4FBFB),
    onSurface = Color(0xFF181C1D),
    surfaceVariant = Color(0xFFD6E5E7),
    onSurfaceVariant = Color(0xFF3C494B),
    outline = Color(0xFF6C7A7B),
    outlineVariant = Color(0xFFBAC9CB),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2C3132),
    inverseOnSurface = Color(0xFFEBF2F3),
    inversePrimary = Color(0xFF72D5E1)
)

val CcGrafiteDark: ColorScheme = darkColorScheme(
    primary = Color(0xFF72D5E1),
    onPrimary = Color(0xFF00363C),
    primaryContainer = Color(0xFF004F56),
    onPrimaryContainer = Color(0xFF8FF2FE),
    secondary = Color(0xFFA8CDD2),
    onSecondary = Color(0xFF113539),
    secondaryContainer = Color(0xFF294C50),
    onSecondaryContainer = Color(0xFFC3E9EE),
    tertiary = Color(0xFFF5BA8F), // Cobre claro
    onTertiary = Color(0xFF4D2600),
    tertiaryContainer = Color(0xFF683C17),
    onTertiaryContainer = Color(0xFFFFDCC4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF111717),
    onBackground = Color(0xFFDDE4E5),
    surface = Color(0xFF111717),
    onSurface = Color(0xFFDDE4E5),
    surfaceVariant = Color(0xFF384446),
    onSurfaceVariant = Color(0xFFB5C4C6),
    outline = Color(0xFF808E90),
    outlineVariant = Color(0xFF3C494B),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFDDE4E5),
    inverseOnSurface = Color(0xFF2C3132),
    inversePrimary = Color(0xFF006972)
)

/**
 * Cores semânticas adicionais com contraste WCAG garantido (B3.3).
 */
@Immutable
data class CcSemantics(
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)

val CcSemanticsLight = CcSemantics(
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    success = Color(0xFF246B3A),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFABF3B9),
    onSuccessContainer = Color(0xFF002107),
    warning = Color(0xFF73550F),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDEAA),
    onWarningContainer = Color(0xFF251A00),
    info = Color(0xFF006493),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFCCE5FF),
    onInfoContainer = Color(0xFF001E30)
)

val CcSemanticsDark = CcSemantics(
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    success = Color(0xFF8FD79E),
    onSuccess = Color(0xFF003916),
    successContainer = Color(0xFF015224),
    onSuccessContainer = Color(0xFFABF3B9),
    warning = Color(0xFFEAC078),
    onWarning = Color(0xFF402D00),
    warningContainer = Color(0xFF5D4200),
    onWarningContainer = Color(0xFFFFDEAA),
    info = Color(0xFF90CDFF),
    onInfo = Color(0xFF00344E),
    infoContainer = Color(0xFF004B70),
    onInfoContainer = Color(0xFFCCE5FF)
)

/**
 * Extras do Design System Vitrine (B3.4 e B4.6).
 */
@Immutable
data class CcExtras(
    val semantics: CcSemantics,
    val cardSheen: Color = Color.White.copy(alpha = 0.08f),
    val cardOutline: Color = Color.White.copy(alpha = 0.12f),
    val scrimSoft: Color = Color.Black.copy(alpha = 0.54f),
    val focusRing: Color,
    val stageAlpha: Float,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color
)

val CcExtrasLight = CcExtras(
    semantics = CcSemanticsLight,
    focusRing = Color(0xFF006972),
    stageAlpha = 0.14f,
    surfaceDim = Color(0xFFD5DBDC),
    surfaceBright = Color(0xFFF4FBFB),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF5F6),
    surfaceContainer = Color(0xFFE8EFF0),
    surfaceContainerHigh = Color(0xFFE3E9EA),
    surfaceContainerHighest = Color(0xFFDDE4E5)
)

val CcExtrasDark = CcExtras(
    semantics = CcSemanticsDark,
    focusRing = Color(0xFF72D5E1),
    stageAlpha = 0.20f,
    surfaceDim = Color(0xFF0E1415),
    surfaceBright = Color(0xFF343A3B),
    surfaceContainerLowest = Color(0xFF071010),
    surfaceContainerLow = Color(0xFF171C1D),
    surfaceContainer = Color(0xFF1C2222),
    surfaceContainerHigh = Color(0xFF242A2B),
    surfaceContainerHighest = Color(0xFF2E3434)
)

val LocalCcExtras = staticCompositionLocalOf { CcExtrasDark }
