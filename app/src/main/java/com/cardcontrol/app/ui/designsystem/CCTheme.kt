package com.cardcontrol.app.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CardControl v3 Theme Provider (Etapa 9.2).
 */

val LocalCCColors = staticCompositionLocalOf<CCColors> {
    error("CCColors não provido. Envolva em CCTheme.")
}

val LocalCCTypography = staticCompositionLocalOf<CCTypography> {
    CCTypography()
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
}

@Composable
fun CCTheme(
    direction: ColorDirection = ColorDirection.GRAFITE,
    content: @Composable () -> Unit
) {
    val colors = when (direction) {
        ColorDirection.GRAFITE,
        ColorDirection.EMBER,
        ColorDirection.OBSIDIAN_AZUL -> CCGrafiteDark
    }


    val materialColorScheme = darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        primaryContainer = colors.accentContainer,
        background = colors.bgBase,
        onBackground = colors.textPrimary,
        surface = colors.surface1,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.surface2,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.outlineStrong,
        error = colors.danger
    )

    CompositionLocalProvider(
        LocalCCColors provides colors,
        LocalCCTypography provides CCTypography()
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
