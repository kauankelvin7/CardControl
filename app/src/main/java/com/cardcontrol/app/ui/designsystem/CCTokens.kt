package com.cardcontrol.app.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CardControl v3 — Design System Tokens
 * Em conformidade com a Etapa 5 e Anexo B de CardControl-Reformulacao-Design-System.md.
 */

enum class ColorDirection {
    EMBER,          // Direção A · Preto quente + Laranja (#E5591D)
    OBSIDIAN_AZUL,  // Direção B · Navy + Ciano (#38BDF8)
    GRAFITE         // Direção C · Neutros sóbrios + Botão tinta off-white (#F4F4F5) e acento dinâmico
}

@Immutable
data class CCColors(
    val bgBase: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val accentDynamic: Color = Color(0xFF56B4E9),
    val glowColor: Color,

    // Semânticos (iguais em todas as direções)
    val success: Color = Color(0xFF3DDC97),
    val warning: Color = Color(0xFFF5B93D),
    val danger: Color = Color(0xFFFF6B6B),
    val info: Color = Color(0xFF6CB6FF),
    val successContainer: Color = Color(0x293DDC97),
    val warningContainer: Color = Color(0x29F5B93D),
    val dangerContainer: Color = Color(0x29FF6B6B),
    val infoContainer: Color = Color(0x296CB6FF),
    val outlineSubtle: Color = Color(0x14FFFFFF),   // Branco @ 8%
    val outlineStrong: Color = Color(0x29FFFFFF),   // Branco @ 16%
    val scrim: Color = Color(0xA6000000)            // Preto @ 65%
)

/**
 * Instâncias oficiais dos tokens por direção de cor (Anexo B).
 */
val CCEmberDark = CCColors(
    bgBase = Color(0xFF0D0B0A),
    surface1 = Color(0xFF151210),
    surface2 = Color(0xFF1C1815),
    surface3 = Color(0xFF26201C),
    textPrimary = Color(0xFFF6F1EC),
    textSecondary = Color(0xFFBDB2A8),
    textTertiary = Color(0xFF8F857C),
    accent = Color(0xFFE5591D),
    onAccent = Color(0xFF1A0B04),
    accentContainer = Color(0x29E5591D),
    glowColor = Color(0x24E5591D)
)

val CCObsidianAzulDark = CCColors(
    bgBase = Color(0xFF080C16),
    surface1 = Color(0xFF0F1524),
    surface2 = Color(0xFF151C30),
    surface3 = Color(0xFF1D2742),
    textPrimary = Color(0xFFEEF2FA),
    textSecondary = Color(0xFFA9B4CB),
    textTertiary = Color(0xFF7C89A5),
    accent = Color(0xFF38BDF8),
    onAccent = Color(0xFF04121C),
    accentContainer = Color(0x2938BDF8),
    glowColor = Color(0x2438BDF8)
)

val CCGrafiteDark = CCColors(
    bgBase = Color(0xFF0B0B0C),
    surface1 = Color(0xFF141416),
    surface2 = Color(0xFF1B1B1E),
    surface3 = Color(0xFF242428),
    textPrimary = Color(0xFFF4F4F5),
    textSecondary = Color(0xFFB4B4BB),
    textTertiary = Color(0xFF8A8A93),
    accent = Color(0xFFF4F4F5),     // Botão tinta off-white
    onAccent = Color(0xFF0B0B0C),
    accentContainer = Color(0x1FF4F4F5),
    accentDynamic = Color(0xFFE69F00), // Cartão Inter / Categoria
    glowColor = Color(0x1AE69F00)
)

/**
 * Paleta Categórica Okabe-Ito (Pessoas e Categorias — 5.2.3).
 */
object CCCategorical {
    val InterOrange = Color(0xFFE69F00)
    val NuCyan = Color(0xFF56B4E9)
    val Teal = Color(0xFF00B386)
    val Yellow = Color(0xFFF0E442)
    val Indigo = Color(0xFF6E8BFF)
    val Coral = Color(0xFFE8743B)
    val Pink = Color(0xFFCC79A7)
    val Slate = Color(0xFFA3A3AD)
}

/**
 * Escala de Espaçamento Base 4dp (5.4).
 */
object CCSpace {
    val s1: Dp = 4.dp
    val s2: Dp = 8.dp
    val s3: Dp = 12.dp
    val s4: Dp = 16.dp
    val s5: Dp = 20.dp
    val s6: Dp = 24.dp
    val s8: Dp = 32.dp
    val s10: Dp = 40.dp
    val s12: Dp = 48.dp
    val s16: Dp = 64.dp
}
