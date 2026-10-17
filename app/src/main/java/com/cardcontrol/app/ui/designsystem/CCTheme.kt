package com.cardcontrol.app.ui.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Modos de Tema do CardControl Vitrine (B2.1 e D2.1).
 * O app controla a sua própria paleta de cores.
 * Quando [SYSTEM], o Android determina apenas a luminosidade (claro/escuro).
 * Dynamic Color / cores do papel de parede NUNCA afetam a paleta do app.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppearancePrefs(
    val mode: ThemeMode = ThemeMode.SYSTEM
)

/**
 * Função pura e testável para resolução de tema escuro (D2.1).
 */
fun resolveDark(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * Retorna o ColorScheme oficial da Vitrine: Petróleo & Cobre (claro) ou Grafite (escuro).
 */
fun resolveScheme(dark: Boolean): ColorScheme = if (dark) CcGrafiteDark else CcLightPetroleo

/**
 * Retorna os tokens extras correspondentes à luminosidade resolvida.
 */
fun resolveExtras(dark: Boolean): CcExtras = if (dark) CcExtrasDark else CcExtrasLight

val LocalCCColors = staticCompositionLocalOf<CCColors> {
    CCGrafiteDarkLegacy
}

val LocalCCTypography = staticCompositionLocalOf {
    CCTypography()
}

val LocalAppearancePrefs = staticCompositionLocalOf {
    AppearancePrefs()
}

object CCTheme {
    val colors: CCColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCCColors.current

    val typography: CCTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCCTypography.current

    val extras: CcExtras
        @Composable
        @ReadOnlyComposable
        get() = LocalCcExtras.current

    val shapes: CcShapesTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCcShapes.current

    val type: CcTypeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCcType.current

    val reduceMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCcReduceMotion.current
}

/**
 * Tema Oficial CardControl Vitrine (B2.4 e D2.1).
 *
 * Configura barras do sistema (status bar e navigation bar) para edge-to-edge transparente
 * com ícones de alto contraste garantidos (B2.5), resolve esquemas e provê tokens centralizados.
 */
@Composable
fun CardControlTheme(
    prefs: AppearancePrefs = AppearancePrefs(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = resolveDark(prefs.mode, systemDark)
    val scheme = remember(dark) { resolveScheme(dark) }
    val extras = remember(dark) { resolveExtras(dark) }
    val reduceMotion by rememberReduceMotion()

    // Configuração das barras do sistema para edge-to-edge contínuo (B2.5)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Fundo transparente para permitir que o background Vitrine se estenda sob as barras
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Ícones escuros na status bar quando o tema for claro; ícones claros quando escuro
                insetsController.isAppearanceLightStatusBars = !dark
                insetsController.isAppearanceLightNavigationBars = !dark
            }
        }
    }

    val legacyColors = remember(dark) {
        if (dark) CCGrafiteDarkLegacy else CCLightLegacy
    }

    CompositionLocalProvider(
        LocalAppearancePrefs provides prefs,
        LocalCcExtras provides extras,
        LocalCcReduceMotion provides reduceMotion,
        LocalCcShapes provides CcShapes,
        LocalCcType provides CcType,
        LocalCCColors provides legacyColors,
        LocalCCTypography provides CCTypography()
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CcMaterialTypography,
            shapes = MaterialCcShapes,
            content = content
        )
    }
}

/**
 * Wrapper de compatibilidade com chamadas anteriores de CCTheme(direction = ...)
 */
@Composable
fun CCTheme(
    direction: ColorDirection = ColorDirection.GRAFITE,
    content: @Composable () -> Unit
) {
    val mode = when (direction) {
        ColorDirection.GRAFITE -> ThemeMode.DARK
        ColorDirection.EMBER,
        ColorDirection.OBSIDIAN_AZUL -> ThemeMode.DARK
    }
    CardControlTheme(prefs = AppearancePrefs(mode = mode), content = content)
}
