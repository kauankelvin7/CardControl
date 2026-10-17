package com.cardcontrol.app.ui.cards

import androidx.compose.runtime.Immutable
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.SupportedBank

/**
 * Modelo de apresentação imutável para a tela e componentes de cartão Vitrine (D1).
 */
@Immutable
data class CardUi(
    val id: Long,
    val bankId: String,
    val bankName: String,
    val holderName: String? = null,
    val lastFourDigits: String = "",
    val nickname: String = "",
    val totalLimit: Long = 0L,
    val closingDay: Int = 10,
    val dueDay: Int = 17,
    val colorSeed: CardColorSeed = CardColorSeed.Auto,
    val flag: String? = null, // ex: "VISA", "MASTERCARD"
    val kind: String? = "Crédito"
) {
    val monogram: String get() = computeBankMonogram(bankName)
    val maskedNumber: String get() = if (lastFourDigits.isNotBlank()) "•••• $lastFourDigits" else ""

    companion object {
        fun fromEntity(
            card: CreditCardEntity,
            holderName: String? = null,
            seed: CardColorSeed = CardColorSeed.Auto
        ): CardUi {
            val bank = SupportedBank.fromId(card.bank)
            val displayName = if (bank != SupportedBank.OTHER) bank.displayName else card.name
            return CardUi(
                id = card.id,
                bankId = card.bank,
                bankName = displayName,
                holderName = holderName,
                lastFourDigits = card.lastFourDigits,
                nickname = card.name,
                totalLimit = card.totalLimit,
                closingDay = card.closingDay,
                dueDay = card.dueDay,
                colorSeed = seed
            )
        }
    }
}

/**
 * Regra oficial de monograma do banco (B8):
 * - Ignorar conectivos (de, da, do, das, dos, e)
 * - Com 2+ palavras significativas: inicial das duas primeiras em maiúsculas (ex: "Banco do Brasil" -> "BB", "Mercado Pago" -> "MP")
 * - Com 1 palavra: as duas primeiras letras com a segunda minúscula (ex: "Nubank" -> "Nu", "Itaú" -> "It", "Inter" -> "In")
 */
fun computeBankMonogram(bankName: String): String {
    val clean = bankName.trim()
    if (clean.isBlank()) return "CC"

    val connectives = setOf("de", "da", "do", "das", "dos", "e")
    val words = clean.split("\\s+".toRegex())
        .map { it.trim() }
        .filter { it.isNotBlank() && !connectives.contains(it.lowercase()) }

    return when {
        words.size >= 2 -> {
            val first = words[0].firstOrNull()?.uppercaseChar() ?: 'C'
            val second = words[1].firstOrNull()?.uppercaseChar() ?: 'C'
            "$first$second"
        }
        words.size == 1 -> {
            val word = words[0]
            if (word.length >= 2) {
                "${word[0].uppercaseChar()}${word[1].lowercaseChar()}"
            } else {
                "${word[0].uppercaseChar()}C"
            }
        }
        else -> "CC"
    }
}
