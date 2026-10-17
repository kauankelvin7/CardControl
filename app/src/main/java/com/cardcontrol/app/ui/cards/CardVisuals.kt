package com.cardcontrol.app.ui.cards

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.cardcontrol.app.data.local.SupportedBank

/**
 * Texturas procedurais e padrões nativos desenhados em Canvas para cada banco.
 */
enum class BankTextureType {
    SOLAR_GRID,      // Banco Inter: micrograde geométrica diagonal
    RADIAL_GLOW,     // Nubank: specular radial sutil no canto superior-direito
    GEOMETRIC_BADGE, // Itaú: geometria diagonal chanfrada e acento quente
    CARBON_WEAVE,    // C6 Bank: textura sutil escovada / fibra de carbono
    RUBY_SHEEN,      // Santander: feixe de luz diagonal luxuoso
    WAVE_ARC,        // Bradesco / Caixa: curva orgânica fluida
    NEON_MATRIX,     // PicPay: aura luminosa esmeralda
    AZURE_SOFT,      // Mercado Pago: gradiente e círculo suave
    METALLIC_BRUSH   // Fallback / Outros: acabamento de metal titânio escovado
}

/**
 * Modelo de especificação visual de cartão por banco emissor (Google Wallet / Fintech style).
 * Totalmente nativo em Compose, offline-first, sem assets protegidos por direitos autorais.
 */
@Immutable
data class BankVisualModel(
    val bank: SupportedBank,
    val backgroundBrush: Brush,
    val isChipGold: Boolean,
    val textureType: BankTextureType,
    val brandLabel: String,
    val accentColor: Color,
    val startColor: Color,
    val endColor: Color,
    val backStart: Color,
    val backEnd: Color,
    val monogramBgColor: Color = Color.White.copy(alpha = 0.16f),
    val monogramTextColor: Color = Color.White
)

/**
 * Catálogo canônico de identidades visuais originais para todos os bancos suportados.
 */
object BankCardVisuals {

    // 1. BANCO INTER — Laranja solar intenso com micrograde geométrica e chip prateado
    val Inter = BankVisualModel(
        bank = SupportedBank.INTER,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFC64A00), Color(0xFFA33800), Color(0xFF6E2300)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.SOLAR_GRID,
        brandLabel = "inter",
        accentColor = Color(0xFFFF9433),
        startColor = Color(0xFFC64A00),
        endColor = Color(0xFF6E2300),
        backStart = Color(0xFF6E2300),
        backEnd = Color(0xFF451400)
    )

    // 2. NUBANK — Roxo profundo com iluminação radial suave e chip prateado
    val Nubank = BankVisualModel(
        bank = SupportedBank.NUBANK,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C), Color(0xFF270650)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.RADIAL_GLOW,
        brandLabel = "nu",
        accentColor = Color(0xFFBA68C8),
        startColor = Color(0xFF7B1FA2),
        endColor = Color(0xFF270650),
        backStart = Color(0xFF38065B),
        backEnd = Color(0xFF1E0233)
    )

    // 3. ITAÚ — Azul marinho real profundo com acento geométrico laranja e chip dourado
    val Itau = BankVisualModel(
        bank = SupportedBank.ITAU,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF003087), Color(0xFF001F5C), Color(0xFF001035)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = true,
        textureType = BankTextureType.GEOMETRIC_BADGE,
        brandLabel = "itaú",
        accentColor = Color(0xFFEC7000),
        startColor = Color(0xFF003087),
        endColor = Color(0xFF001035),
        backStart = Color(0xFF001740),
        backEnd = Color(0xFF000B20)
    )

    // 4. C6 BANK — Carbon black / titânio escuro escovado com chip prateado
    val C6 = BankVisualModel(
        bank = SupportedBank.C6,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF2D3238), Color(0xFF1B1D20), Color(0xFF0E1012)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.CARBON_WEAVE,
        brandLabel = "C6 BANK",
        accentColor = Color(0xFFCFD8DC),
        startColor = Color(0xFF2D3238),
        endColor = Color(0xFF0E1012),
        backStart = Color(0xFF181A1D),
        backEnd = Color(0xFF0B0C0E)
    )

    // 5. SANTANDER — Vermelho rubi luxuoso com feixe diagonal e chip dourado
    val Santander = BankVisualModel(
        bank = SupportedBank.SANTANDER,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFB71C1C), Color(0xFF7F0000), Color(0xFF4A0000)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = true,
        textureType = BankTextureType.RUBY_SHEEN,
        brandLabel = "Santander",
        accentColor = Color(0xFFFF5252),
        startColor = Color(0xFFB71C1C),
        endColor = Color(0xFF4A0000),
        backStart = Color(0xFF5E0000),
        backEnd = Color(0xFF380000)
    )

    // 6. BRADESCO — Vinho carmim profundo com arco orgânico e chip prateado
    val Bradesco = BankVisualModel(
        bank = SupportedBank.BRADESCO,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFA81228), Color(0xFF720B1A), Color(0xFF40040E)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.WAVE_ARC,
        brandLabel = "bradesco",
        accentColor = Color(0xFFFF4060),
        startColor = Color(0xFFA81228),
        endColor = Color(0xFF40040E),
        backStart = Color(0xFF560714),
        backEnd = Color(0xFF2E020A)
    )

    // 7. CAIXA — Azul safira governamental com acento dinâmico e chip prateado
    val Caixa = BankVisualModel(
        bank = SupportedBank.CAIXA,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF005CA9), Color(0xFF003E73), Color(0xFF002342)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.WAVE_ARC,
        brandLabel = "CAIXA",
        accentColor = Color(0xFFF39200),
        startColor = Color(0xFF005CA9),
        endColor = Color(0xFF002342),
        backStart = Color(0xFF002E54),
        backEnd = Color(0xFF001B33)
    )

    // 8. PICPAY — Verde esmeralda digital luminoso com chip prateado
    val PicPay = BankVisualModel(
        bank = SupportedBank.PICPAY,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF007E3A), Color(0xFF005325), Color(0xFF003014)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.NEON_MATRIX,
        brandLabel = "PicPay",
        accentColor = Color(0xFF11C76F),
        startColor = Color(0xFF007E3A),
        endColor = Color(0xFF003014),
        backStart = Color(0xFF003D1B),
        backEnd = Color(0xFF00240F)
    )

    // 9. MERCADO PAGO — Azul celeste vívido com iluminação suave e chip prateado
    val MercadoPago = BankVisualModel(
        bank = SupportedBank.MERCADO_PAGO,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF0077B6), Color(0xFF005A8C), Color(0xFF00395C)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = false,
        textureType = BankTextureType.AZURE_SOFT,
        brandLabel = "mercado pago",
        accentColor = Color(0xFF29B6F6),
        startColor = Color(0xFF0077B6),
        endColor = Color(0xFF00395C),
        backStart = Color(0xFF00446E),
        backEnd = Color(0xFF002B47)
    )

    // 10. OTHER / FALLBACK — Metal titânio grafite elegante com chip dourado
    val Fallback = BankVisualModel(
        bank = SupportedBank.OTHER,
        backgroundBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF455059), Color(0xFF2C343A), Color(0xFF1B2024)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        isChipGold = true,
        textureType = BankTextureType.METALLIC_BRUSH,
        brandLabel = "CardControl",
        accentColor = Color(0xFF78909C),
        startColor = Color(0xFF455059),
        endColor = Color(0xFF1B2024),
        backStart = Color(0xFF242A2F),
        backEnd = Color(0xFF15191C)
    )

    /**
     * Resolve o modelo visual pelo SupportedBank com fallback seguro e determinístico.
     */
    fun resolve(bank: SupportedBank): BankVisualModel {
        return when (bank) {
            SupportedBank.INTER -> Inter
            SupportedBank.NUBANK -> Nubank
            SupportedBank.ITAU -> Itau
            SupportedBank.C6 -> C6
            SupportedBank.SANTANDER -> Santander
            SupportedBank.BRADESCO -> Bradesco
            SupportedBank.CAIXA -> Caixa
            SupportedBank.PICPAY -> PicPay
            SupportedBank.MERCADO_PAGO -> MercadoPago
            SupportedBank.OTHER -> Fallback
        }
    }

    /**
     * Resolve pelo ID persistente do banco.
     */
    fun resolveByBankId(bankId: String?): BankVisualModel {
        val bank = SupportedBank.fromId(bankId)
        return resolve(bank)
    }
}
