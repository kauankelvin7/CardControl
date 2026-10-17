package com.cardcontrol.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.cardcontrol.app.ui.cards.CardPaletteResolver
import com.cardcontrol.app.ui.cards.CcCardPresets
import com.cardcontrol.app.ui.cards.computeBankMonogram
import com.cardcontrol.app.ui.designsystem.CcExtrasDark
import com.cardcontrol.app.ui.designsystem.CcExtrasLight
import com.cardcontrol.app.ui.designsystem.CcGrafiteDark
import com.cardcontrol.app.ui.designsystem.CcLightPetroleo
import com.cardcontrol.app.ui.designsystem.ThemeMode
import com.cardcontrol.app.ui.designsystem.resolveDark
import com.cardcontrol.app.ui.designsystem.resolveScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Testes Unitários de Conformidade e Contraste do Design System Vitrine v2.1 (E1).
 * Execução 100% determinística em JVM sem relógio real e sem emulador.
 */
class VitrineThemeAndContrastTest {

    // --- 1. Resolução Pura de Tema (D2.1 e E1) ---

    @Test
    fun testResolveDark_systemFollowsSystemLuminosity() {
        assertTrue(resolveDark(ThemeMode.SYSTEM, systemDark = true))
        assertFalse(resolveDark(ThemeMode.SYSTEM, systemDark = false))
    }

    @Test
    fun testResolveDark_forcedModesIgnoreSystemLuminosity() {
        // Modo LIGHT sempre retorna false
        assertFalse(resolveDark(ThemeMode.LIGHT, systemDark = true))
        assertFalse(resolveDark(ThemeMode.LIGHT, systemDark = false))

        // Modo DARK sempre retorna true
        assertTrue(resolveDark(ThemeMode.DARK, systemDark = true))
        assertTrue(resolveDark(ThemeMode.DARK, systemDark = false))
    }

    @Test
    fun testResolveScheme_returnsFixedVitrinePalettes() {
        // Invariância de Dynamic Color: esquemas retornados são estritamente os fixos Vitrine
        val darkScheme = resolveScheme(dark = true)
        val lightScheme = resolveScheme(dark = false)

        assertEquals(CcGrafiteDark, darkScheme)
        assertEquals(CcLightPetroleo, lightScheme)

        // Cores essenciais não sofrem mutação
        assertEquals(Color(0xFF72D5E1), darkScheme.primary)
        assertEquals(Color(0xFF006972), lightScheme.primary)
        assertEquals(Color(0xFF111717), darkScheme.background)
        assertEquals(Color(0xFFF4FBFB), lightScheme.background)
    }

    // --- 2. Regras de Monogramas de Bancos (B8) ---

    @Test
    fun testBankMonograms_standardBrazilianBanks() {
        assertEquals("BB", computeBankMonogram("Banco do Brasil"))
        assertEquals("Nu", computeBankMonogram("Nubank"))
        assertEquals("MP", computeBankMonogram("Mercado Pago"))
        assertEquals("It", computeBankMonogram("Itaú"))
        assertEquals("In", computeBankMonogram("Inter"))
        assertEquals("CE", computeBankMonogram("Caixa Econômica Federal"))
        assertEquals("Br", computeBankMonogram("Bradesco"))
        assertEquals("Sa", computeBankMonogram("Santander"))
    }

    // --- 3. Auditoria Matemática de Contraste W3C WCAG 2.1 (E1) ---

    /**
     * Calcula a luminância relativa conforme W3C WCAG 2.1
     */
    private fun relativeLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Calcula a razão de contraste entre duas cores: (L1 + 0.05) / (L2 + 0.05)
     */
    private fun contrastRatio(fg: Color, bg: Color): Double {
        val l1 = relativeLuminance(fg)
        val l2 = relativeLuminance(bg)
        val brighter = max(l1, l2)
        val darker = min(l1, l2)
        return (brighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testContrast_lightSchemeCorePairs() {
        val scheme = CcLightPetroleo

        // onPrimary / primary >= 4.5:1 (alvo medido 6.4:1)
        val onPrimaryRatio = contrastRatio(scheme.onPrimary, scheme.primary)
        assertTrue("onPrimary/primary: $onPrimaryRatio", onPrimaryRatio >= 4.5)

        // onPrimaryContainer / primaryContainer >= 4.5:1 (alvo medido 13.4:1)
        val onPrimaryContainerRatio = contrastRatio(scheme.onPrimaryContainer, scheme.primaryContainer)
        assertTrue("onPrimaryContainer/primaryContainer: $onPrimaryContainerRatio", onPrimaryContainerRatio >= 4.5)

        // onSurface / surface >= 4.5:1
        val onSurfaceRatio = contrastRatio(scheme.onSurface, scheme.surface)
        assertTrue("onSurface/surface: $onSurfaceRatio", onSurfaceRatio >= 4.5)

        // onSurfaceVariant / surfaceVariant >= 4.5:1
        val onSurfaceVariantRatio = contrastRatio(scheme.onSurfaceVariant, scheme.surfaceVariant)
        assertTrue("onSurfaceVariant/surfaceVariant: $onSurfaceVariantRatio", onSurfaceVariantRatio >= 4.5)

        // outline / surface >= 3.0:1
        val outlineRatio = contrastRatio(scheme.outline, scheme.surface)
        assertTrue("outline/surface: $outlineRatio", outlineRatio >= 3.0)
    }

    @Test
    fun testContrast_darkSchemeCorePairs() {
        val scheme = CcGrafiteDark

        // onPrimary / primary >= 4.5:1 (alvo medido 7.7:1)
        val onPrimaryRatio = contrastRatio(scheme.onPrimary, scheme.primary)
        assertTrue("onPrimary/primary: $onPrimaryRatio", onPrimaryRatio >= 4.5)

        // onPrimaryContainer / primaryContainer >= 4.5:1 (alvo medido 7.2:1)
        val onPrimaryContainerRatio = contrastRatio(scheme.onPrimaryContainer, scheme.primaryContainer)
        assertTrue("onPrimaryContainer/primaryContainer: $onPrimaryContainerRatio", onPrimaryContainerRatio >= 4.5)

        // onSurface / surface >= 4.5:1
        val onSurfaceRatio = contrastRatio(scheme.onSurface, scheme.surface)
        assertTrue("onSurface/surface: $onSurfaceRatio", onSurfaceRatio >= 4.5)

        // onSurfaceVariant / surfaceVariant >= 4.5:1
        val onSurfaceVariantRatio = contrastRatio(scheme.onSurfaceVariant, scheme.surfaceVariant)
        assertTrue("onSurfaceVariant/surfaceVariant: $onSurfaceVariantRatio", onSurfaceVariantRatio >= 4.5)

        // outline / surface >= 3.0:1
        val outlineRatio = contrastRatio(scheme.outline, scheme.surface)
        assertTrue("outline/surface: $outlineRatio", outlineRatio >= 3.0)
    }

    @Test
    fun testContrast_semanticsB33() {
        // Claro
        val light = CcExtrasLight.semantics
        assertTrue(contrastRatio(light.onError, light.error) >= 4.5)
        assertTrue(contrastRatio(light.onErrorContainer, light.errorContainer) >= 4.5)
        assertTrue(contrastRatio(light.onSuccess, light.success) >= 4.5)
        assertTrue(contrastRatio(light.onSuccessContainer, light.successContainer) >= 4.5)
        assertTrue(contrastRatio(light.onWarning, light.warning) >= 4.5)
        assertTrue(contrastRatio(light.onWarningContainer, light.warningContainer) >= 4.5)
        assertTrue(contrastRatio(light.onInfo, light.info) >= 4.5)
        assertTrue(contrastRatio(light.onInfoContainer, light.infoContainer) >= 4.5)

        // Escuro
        val dark = CcExtrasDark.semantics
        assertTrue(contrastRatio(dark.onError, dark.error) >= 4.5)
        assertTrue(contrastRatio(dark.onErrorContainer, dark.errorContainer) >= 4.5)
        assertTrue(contrastRatio(dark.onSuccess, dark.success) >= 4.5)
        assertTrue(contrastRatio(dark.onSuccessContainer, dark.successContainer) >= 4.5)
        assertTrue(contrastRatio(dark.onWarning, dark.warning) >= 4.5)
        assertTrue(contrastRatio(dark.onWarningContainer, dark.warningContainer) >= 4.5)
        assertTrue(contrastRatio(dark.onInfo, dark.info) >= 4.5)
        assertTrue(contrastRatio(dark.onInfoContainer, dark.infoContainer) >= 4.5)
    }

    @Test
    fun testContrast_12CardPresets() {
        val white100 = Color.White
        val white88 = Color.White.copy(alpha = 0.88f)
        val white80 = Color.White.copy(alpha = 0.80f)

        for (preset in CcCardPresets.allPresets) {
            // Contraste do texto sobre a cor de início do cartão (start)
            val ratio100 = contrastRatio(white100, preset.start)
            val ratio88 = contrastRatio(white88, preset.start)
            val ratio80 = contrastRatio(white80, preset.start)

            assertTrue("${preset.name} texto primário >= 5.0", ratio100 >= 5.0)
            assertTrue("${preset.name} texto secundário >= 4.5", ratio88 >= 4.5)
            assertTrue("${preset.name} texto decorativo >= 4.0", ratio80 >= 4.0)

            // Contraste do verso (back0)
            val backRatio = contrastRatio(white100, preset.back0)
            assertTrue("${preset.name} verso >= 7.0", backRatio >= 7.0)
        }
    }

    @Test
    fun testContrast_randomSeedsDerivation() {
        // Teste de estresse com 250 sementes de cores aleatórias (E1)
        val random = Random(42L)
        val white100 = Color.White

        for (i in 0 until 250) {
            val r = random.nextFloat()
            val g = random.nextFloat()
            val b = random.nextFloat()
            val seedColor = Color(r, g, b, 1f)

            val derivedPalette = CardPaletteResolver.derive(seedColor)
            val ratio = contrastRatio(white100, derivedPalette.start)

            assertTrue(
                "Semente $i ($seedColor) derivou contraste insuficiente: $ratio",
                ratio >= 4.5
            )
        }
    }

    @Test
    fun testContrast_stageTintGuard() {
        // Guarda de contraste (B4.6): onSurface sobre o topo tingido mantém >= 4.5:1
        for (preset in CcCardPresets.allPresets) {
            // Claro: alpha 0.14
            val lightTint = CardPaletteResolver.stageTint(CcLightPetroleo.surface, preset.start, isDark = false)
            val lightRatio = contrastRatio(CcLightPetroleo.onSurface, lightTint)
            assertTrue("Claro palco ${preset.name} ratio $lightRatio", lightRatio >= 4.5)

            // Escuro: alpha 0.20
            val darkTint = CardPaletteResolver.stageTint(CcGrafiteDark.surface, preset.start, isDark = true)
            val darkRatio = contrastRatio(CcGrafiteDark.onSurface, darkTint)
            assertTrue("Escuro palco ${preset.name} ratio $darkRatio", darkRatio >= 4.5)
        }
    }
}
