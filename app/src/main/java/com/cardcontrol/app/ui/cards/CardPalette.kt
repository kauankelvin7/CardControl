package com.cardcontrol.app.ui.cards

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.cardcontrol.app.data.local.SupportedBank
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/**
 * Paleta de cores do cartão (frente e verso), calculada para contraste garantido (B4.1 e D2.4).
 */
@Immutable
data class CardPalette(
    val name: String,
    val start: Color,
    val end: Color,
    val back0: Color,
    val back1: Color
)

enum class CardColorPreset(val displayName: String) {
    GRAFITE("Grafite"),
    PETROLEO("Petróleo"),
    SAFIRA("Safira"),
    INDIGO("Índigo"),
    AMETISTA("Ametista"),
    ROSE("Rosé"),
    RUBI("Rubi"),
    CORAL("Coral"),
    COBRE("Cobre"),
    AMBAR("Âmbar"),
    OLIVA("Oliva"),
    ESMERALDA("Esmeralda")
}

sealed class CardColorSeed {
    object Auto : CardColorSeed()
    data class Preset(val preset: CardColorPreset) : CardColorSeed()
    data class Custom(val color: Color) : CardColorSeed()
}

/**
 * As 12 Predefinições Oficiais Vitrine (B4.2).
 */
object CcCardPresets {
    val Grafite = CardPalette(
        name = "Grafite",
        start = Color(0xFF515B62),
        end = Color(0xFF2E363C),
        back0 = Color(0xFF2E363B),
        back1 = Color(0xFF22292D)
    )

    val Petroleo = CardPalette(
        name = "Petróleo",
        start = Color(0xFF006469),
        end = Color(0xFF003B3F),
        back0 = Color(0xFF003B3F),
        back1 = Color(0xFF002D30)
    )

    val Safira = CardPalette(
        name = "Safira",
        start = Color(0xFF005E93),
        end = Color(0xFF00375A),
        back0 = Color(0xFF00375A),
        back1 = Color(0xFF002A46)
    )

    val Indigo = CardPalette(
        name = "Índigo",
        start = Color(0xFF4153A5),
        end = Color(0xFF1A3070),
        back0 = Color(0xFF1F306C),
        back1 = Color(0xFF142456)
    )

    val Ametista = CardPalette(
        name = "Ametista",
        start = Color(0xFF77458C),
        end = Color(0xFF4B245C),
        back0 = Color(0xFF4A255A),
        back1 = Color(0xFF391B47)
    )

    val Rose = CardPalette(
        name = "Rosé",
        start = Color(0xFF993366),
        end = Color(0xFF65103E),
        back0 = Color(0xFF63153E),
        back1 = Color(0xFF4E0C2F)
    )

    val Rubi = CardPalette(
        name = "Rubi",
        start = Color(0xFFA52C32),
        end = Color(0xFF6E0716),
        back0 = Color(0xFF6B0E18),
        back1 = Color(0xFF55060F)
    )

    val Coral = CardPalette(
        name = "Coral",
        start = Color(0xFF993C1B),
        end = Color(0xFF641B00),
        back0 = Color(0xFF621D03),
        back1 = Color(0xFF4B1700)
    )

    val Cobre = CardPalette(
        name = "Cobre",
        start = Color(0xFF884910),
        end = Color(0xFF542A00),
        back0 = Color(0xFF542A00),
        back1 = Color(0xFF402000)
    )

    val Ambar = CardPalette(
        name = "Âmbar",
        start = Color(0xFF765303),
        end = Color(0xFF473100),
        back0 = Color(0xFF473100),
        back1 = Color(0xFF362500)
    )

    val Oliva = CardPalette(
        name = "Oliva",
        start = Color(0xFF4D6111),
        end = Color(0xFF2C3A00),
        back0 = Color(0xFF2C3A00),
        back1 = Color(0xFF222B00)
    )

    val Esmeralda = CardPalette(
        name = "Esmeralda",
        start = Color(0xFF006833),
        end = Color(0xFF003E1C),
        back0 = Color(0xFF003E1C),
        back1 = Color(0xFF002F14)
    )

    val allPresets = listOf(
        Grafite, Petroleo, Safira, Indigo, Ametista, Rose,
        Rubi, Coral, Cobre, Ambar, Oliva, Esmeralda
    )

    val presetMap = mapOf(
        CardColorPreset.GRAFITE to Grafite,
        CardColorPreset.PETROLEO to Petroleo,
        CardColorPreset.SAFIRA to Safira,
        CardColorPreset.INDIGO to Indigo,
        CardColorPreset.AMETISTA to Ametista,
        CardColorPreset.ROSE to Rose,
        CardColorPreset.RUBI to Rubi,
        CardColorPreset.CORAL to Coral,
        CardColorPreset.COBRE to Cobre,
        CardColorPreset.AMBAR to Ambar,
        CardColorPreset.OLIVA to Oliva,
        CardColorPreset.ESMERALDA to Esmeralda
    )

    // As 11 predefinições coloridas para atribuição determinística automática (B4.2)
    val coloredPresets = listOf(
        Petroleo, Safira, Indigo, Ametista, Rose,
        Rubi, Coral, Cobre, Ambar, Oliva, Esmeralda
    )
}

object CardPaletteResolver {

    /**
     * Resolve uma cor automática com base no identificador persistente do banco (B4.2).
     * Determinístico: o mesmo banco recebe sempre a mesma cor; desconhecido -> Grafite.
     */
    fun resolveAuto(bankId: String?): CardPalette {
        if (bankId.isNullOrBlank() || bankId == SupportedBank.OTHER.id) {
            return CcCardPresets.Grafite
        }
        val bank = SupportedBank.fromId(bankId)
        if (bank == SupportedBank.OTHER) {
            return CcCardPresets.Grafite
        }
        val visual = BankCardVisuals.resolve(bank)
        return CardPalette(
            name = visual.brandLabel,
            start = visual.startColor,
            end = visual.endColor,
            back0 = visual.backStart,
            back1 = visual.backEnd
        )
    }

    /**
     * Algoritmo de derivação cromática L* 38 -> 22 para cores personalizadas (B4.1 e D2.4).
     * Conversão matemática pura sRGB -> CIELAB -> LCh -> sRGB sem dependências de framework nativo.
     */
    fun derive(color: Color): CardPalette {
        fun srgbToLinear(c: Float): Double {
            val d = c.toDouble()
            return if (d <= 0.04045) d / 12.92 else ((d + 0.055) / 1.055).pow(2.4)
        }
        val rLin = srgbToLinear(color.red)
        val gLin = srgbToLinear(color.green)
        val bLin = srgbToLinear(color.blue)

        val x = (0.4124 * rLin + 0.3576 * gLin + 0.1805 * bLin) / 0.95047
        val y = (0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin) / 1.00000
        val z = (0.0193 * rLin + 0.1192 * gLin + 0.9505 * bLin) / 1.08883

        fun f(t: Double): Double {
            val delta = 6.0 / 29.0
            return if (t > delta * delta * delta) t.pow(1.0 / 3.0) else (t / (3.0 * delta * delta)) + (4.0 / 29.0)
        }

        val fx = f(x)
        val fy = f(y)
        val fz = f(z)

        val labA = 500.0 * (fx - fy)
        val labB = 200.0 * (fy - fz)

        // LCh (croma e matiz)
        val c = hypot(labA, labB)
        val hRad = atan2(labB, labA)
        val cClamped = c.coerceIn(8.0, 56.0)

        fun labToColor(targetL: Double, targetC: Double): Color {
            val tA = targetC * cos(hRad)
            val tB = targetC * sin(hRad)

            val tFy = (targetL + 16.0) / 116.0
            val tFx = (tA / 500.0) + tFy
            val tFz = tFy - (tB / 200.0)

            fun finv(t: Double): Double {
                val delta = 6.0 / 29.0
                return if (t > delta) t * t * t else 3.0 * delta * delta * (t - (4.0 / 29.0))
            }

            val tX = 0.95047 * finv(tFx)
            val tY = 1.00000 * finv(tFy)
            val tZ = 1.08883 * finv(tFz)

            val r = 3.2406 * tX - 1.5372 * tY - 0.4986 * tZ
            val g = -0.9689 * tX + 1.8758 * tY + 0.0415 * tZ
            val b = 0.0557 * tX - 0.2040 * tY + 1.0570 * tZ

            fun linearToSrgb(channel: Double): Float {
                val clamped = channel.coerceIn(0.0, 1.0)
                val s = if (clamped <= 0.0031308) 12.92 * clamped else 1.055 * clamped.pow(1.0 / 2.4) - 0.055
                return s.toFloat().coerceIn(0f, 1f)
            }

            return Color(linearToSrgb(r), linearToSrgb(g), linearToSrgb(b))
        }

        val start = labToColor(38.0, cClamped)
        val end = labToColor(22.0, cClamped * 0.85)
        val back0 = labToColor(22.0, cClamped * 0.80)
        val back1 = labToColor(16.0, cClamped * 0.70)

        return CardPalette(
            name = "Personalizada",
            start = start,
            end = end,
            back0 = back0,
            back1 = back1
        )
    }

    fun resolve(seed: CardColorSeed, bankId: String?): CardPalette = when (seed) {
        is CardColorSeed.Auto -> resolveAuto(bankId)
        is CardColorSeed.Preset -> CcCardPresets.presetMap[seed.preset] ?: resolveAuto(bankId)
        is CardColorSeed.Custom -> derive(seed.color)
    }

    /**
     * Calcula o tingimento do palco no terço superior da tela (B4.6).
     * α = 0.14 no tema claro · 0.20 no tema escuro.
     */
    fun stageTint(surface: Color, cardStart: Color, isDark: Boolean): Color {
        val alpha = if (isDark) 0.20f else 0.14f
        return lerp(surface, cardStart, alpha)
    }
}
