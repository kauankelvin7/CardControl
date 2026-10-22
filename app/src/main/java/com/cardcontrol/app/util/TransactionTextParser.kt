package com.cardcontrol.app.util

/**
 * Interpreta texto fornecido voluntariamente pelo usuário (colar/compartilhar).
 * Não acessa notificações, bancos ou rede; nunca registra despesas sem confirmação.
 */
data class TransactionTextSuggestion(
    val amountCents: Long,
    val merchant: String,
    val installments: Int,
    val lastFourDigits: String?
)

object TransactionTextParser {
    private const val MAX_TEXT_LENGTH = 4_000
    private val amountRegex = Regex("""(?i)R\$\s*(\d+(?:\.\d{3})*(?:,\d{1,2})?)""")
    private val preferredAmountRegex = Regex(
        """(?iu)(?:valor\s+(?:total|da compra)|total(?:\s+da\s+compra)?|compra\s+(?:de|no\s+valor\s+de))\s*:?\s*R\$\s*(\d+(?:\.\d{3})*(?:,\d{1,2})?)"""
    )
    private val merchantLabelRegex = Regex(
        """(?iu)(?:estabelecimento|comerciante|loja)\s*[:\-]\s*([^\n,;.!]{2,80})"""
    )
    private val merchantAfterAmountRegex = Regex(
        """(?iu)R\$\s*[\d.,]+\s+(?:(?:aprovad[ao]|realizada)\s+)?(?:em|na|no)\s+([^\n,;.!]{2,80})"""
    )
    private val installmentRegex = Regex("""(?iu)\b(\d{1,2})\s*[xX]\s*(?:de\s*)?(?:R\$)?""")
    private val installmentLabelRegex = Regex("""(?iu)parcelad[ao]\s+em\s+(\d{1,2})\b""")
    private val lastFourRegex = Regex(
        """(?iu)\b(?:final|terminad[oa]\s+em)\s*(?:do\s+cart[aã]o\s*)?(\d{4})\b"""
    )
    private val excludedRegex = Regex(
        """(?iu)\b(?:estorno|estornada|estornado|reembolso|cancelad[ao]|pix\s+recebido|pagamento\s+de\s+fatura|c[oó]digo\s+de\s+verifica[cç][aã]o|token\s+de\s+acesso)\b"""
    )
    private val trailingCardRegex = Regex(
        """(?iu)\s+(?:com\s+(?:o\s+)?cart[aã]o|cart[aã]o\s+final|final\s+\d{4}|em\s+\d{1,2}x|parcelad[ao]\s+em).*"""
    )

    fun parse(raw: String?): TransactionTextSuggestion? {
        if (raw.isNullOrBlank()) return null
        val text = raw.take(MAX_TEXT_LENGTH).replace('\u0000', ' ').trim()
        if (excludedRegex.containsMatchIn(text)) return null

        // O valor total explícito prevalece sobre o valor unitário da parcela.
        val amountRaw = preferredAmountRegex.find(text)?.groupValues?.get(1)
            ?: amountRegex.find(text)?.groupValues?.get(1)
            ?: return null
        val amount = MoneyUtils.parseToCents(amountRaw)?.takeIf { it > 0L } ?: return null

        val candidate = (
            merchantLabelRegex.find(text)?.groupValues?.get(1)
                ?: merchantAfterAmountRegex.find(text)?.groupValues?.get(1)
                ?: ""
            ).replace(trailingCardRegex, "").trim().take(40)

        val installments = (installmentRegex.find(text)?.groupValues?.get(1)
            ?: installmentLabelRegex.find(text)?.groupValues?.get(1))
            ?.toIntOrNull()?.coerceIn(1, 72) ?: 1

        return TransactionTextSuggestion(
            amountCents = amount,
            merchant = candidate,
            installments = installments,
            lastFourDigits = lastFourRegex.find(text)?.groupValues?.get(1)
        )
    }
}
