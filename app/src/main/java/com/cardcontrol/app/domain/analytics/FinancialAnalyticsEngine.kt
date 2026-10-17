package com.cardcontrol.app.domain.analytics

import androidx.compose.ui.graphics.Color
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.InstallmentEntity
import com.cardcontrol.app.data.local.PaymentEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.SupportedBank
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Períodos de análise financeira suportados pelo dashboard.
 */
enum class AnalyticsPeriod(val displayName: String, val monthsCount: Int) {
    CURRENT_MONTH("Mês atual", 1),
    LAST_3_MONTHS("3 meses", 3),
    LAST_6_MONTHS("6 meses", 6),
    LAST_12_MONTHS("1 ano", 12)
}

/**
 * Categorias semânticas de despesa financeira.
 */
enum class SpendingCategory(
    val displayName: String,
    val color: Color
) {
    ALIMENTACAO("Alimentação", Color(0xFFD35400)),
    TRANSPORTE("Transporte", Color(0xFF00796B)),
    SAUDE("Saúde", Color(0xFF2E7D32)),
    LAZER("Lazer & Viagem", Color(0xFF6A1B9A)),
    ASSINATURAS("Assinaturas", Color(0xFF1565C0)),
    SERVICOS("Serviços & Compras", Color(0xFFB78103)),
    OUTROS("Não categorizado", Color(0xFF546E7A))
}

/**
 * KPIs consolidados de um período financeiro.
 * Todos os valores monetários são estritamente em centavos inteiros (Long).
 */
data class AnalyticsKpis(
    val grossSpendingCents: Long,
    val paymentsCents: Long,
    val netSpendingCents: Long,
    val transactionCount: Int,
    val averageTicketCents: Long,
    val periodOverPeriodChangeBps: Long? // Variação em basis points (ex: +1250 bps = +12.50%)
)

/**
 * Barra mensal para o gráfico de tendência.
 */
data class MonthlySpendingBar(
    val year: Int,
    val month: Int,
    val label: String,
    val amountCents: Long,
    val paymentsCents: Long
)

/**
 * Fatia de distribuição por categoria para o gráfico Donut.
 */
data class CategorySpendingDonut(
    val category: SpendingCategory,
    val amountCents: Long,
    val percentage: Float
)

/**
 * Decomposição por modalidade de compra.
 */
data class ModalitySpending(
    val recorrenteCents: Long,
    val parceladoCents: Long,
    val aVistaCents: Long
)

/**
 * Participação por cartão/banco.
 */
data class CardShare(
    val cardId: Long,
    val cardName: String,
    val bankName: String,
    val amountCents: Long,
    val percentage: Float
)

/**
 * Ranking de maiores estabelecimentos.
 */
data class MerchantRank(
    val merchant: String,
    val amountCents: Long,
    val count: Int,
    val category: SpendingCategory
)

/**
 * Estado completo do painel de análises financeiras.
 */
data class AnalyticsDashboardState(
    val period: AnalyticsPeriod,
    val selectedYear: Int,
    val selectedMonth: Int,
    val filterCardId: Long?,
    val filterHolderId: Long?,
    val kpis: AnalyticsKpis,
    val monthlyTrend: List<MonthlySpendingBar>,
    val categories: List<CategorySpendingDonut>,
    val modality: ModalitySpending,
    val cardShares: List<CardShare>,
    val topMerchants: List<MerchantRank>,
    val filteredInstallments: List<InstallmentEntity>
)

/**
 * Motor determinístico de análises financeiras 100% offline.
 * Executa todas as operações em centavos Long e BigDecimal (HALF_UP), prevenindo imprecisão de ponto flutuante.
 */
object FinancialAnalyticsEngine {

    private val PT_BR = Locale("pt", "BR")

    /**
     * Calcula o estado completo de análise para os filtros selecionados.
     */
    fun computeDashboard(
        installments: List<InstallmentEntity>,
        payments: List<PaymentEntity>,
        cards: List<CreditCardEntity>,
        @Suppress("UNUSED_PARAMETER") holders: List<HolderEntity>,
        recurringCharges: List<RecurringChargeEntity>,
        period: AnalyticsPeriod,
        filterCardId: Long? = null,
        filterHolderId: Long? = null,
        referenceDate: LocalDate = LocalDate.now()
    ): AnalyticsDashboardState {
        // 1. Determina a lista de meses cobertos pelo período
        val targetMonths = getTargetMonths(referenceDate, period.monthsCount)
        val targetMonthSet = targetMonths.map { it.first to it.second }.toSet()

        // 2. Filtra os lançamentos do período atual e aplica filtros de cartão/titular
        val currentInstallments = installments.filter { inst ->
            (inst.refYear to inst.refMonth) in targetMonthSet &&
                (filterCardId == null || inst.cardId == filterCardId) &&
                (filterHolderId == null || inst.holderId == filterHolderId)
        }

        val currentPayments = payments.filter { pay ->
            (pay.refYear to pay.refMonth) in targetMonthSet &&
                (filterCardId == null || pay.cardId == filterCardId) &&
                (filterHolderId == null || pay.holderId == filterHolderId)
        }

        // 3. Cálculos de KPIs do período atual
        val grossSpendingCents = currentInstallments.sumOf { it.amount }
        val paymentsCents = currentPayments.sumOf { it.amountPaid }
        val netSpendingCents = grossSpendingCents - paymentsCents
        val txCount = currentInstallments.size
        val averageTicketCents = if (txCount > 0) {
            BigDecimal(grossSpendingCents)
                .divide(BigDecimal(txCount), 0, RoundingMode.HALF_UP)
                .toLong()
        } else {
            0L
        }

        // 4. Período anterior para cálculo de variação (PoP - Period over Period)
        val previousTargetMonths = getTargetMonths(
            referenceDate.minusMonths(period.monthsCount.toLong()),
            period.monthsCount
        ).map { it.first to it.second }.toSet()

        val previousGrossCents = installments.filter { inst ->
            (inst.refYear to inst.refMonth) in previousTargetMonths &&
                (filterCardId == null || inst.cardId == filterCardId) &&
                (filterHolderId == null || inst.holderId == filterHolderId)
        }.sumOf { it.amount }

        val periodOverPeriodBps: Long? = if (previousGrossCents > 0L) {
            val delta = BigDecimal(grossSpendingCents - previousGrossCents)
            delta.multiply(BigDecimal(10000))
                .divide(BigDecimal(previousGrossCents), 0, RoundingMode.HALF_UP)
                .toLong()
        } else {
            null
        }

        val kpis = AnalyticsKpis(
            grossSpendingCents = grossSpendingCents,
            paymentsCents = paymentsCents,
            netSpendingCents = netSpendingCents,
            transactionCount = txCount,
            averageTicketCents = averageTicketCents,
            periodOverPeriodChangeBps = periodOverPeriodBps
        )

        // 5. Tendência Mensal (barras cronológicas)
        // Se o período for 1 mês, exibimos os últimos 6 meses no gráfico para dar contexto
        val trendMonthsCount = if (period.monthsCount == 1) 6 else period.monthsCount
        val trendMonths = getTargetMonths(referenceDate, trendMonthsCount).reversed()

        val monthlyTrend = trendMonths.map { (year, month) ->
            val monthGross = installments.filter { inst ->
                inst.refYear == year && inst.refMonth == month &&
                    (filterCardId == null || inst.cardId == filterCardId) &&
                    (filterHolderId == null || inst.holderId == filterHolderId)
            }.sumOf { it.amount }

            val monthPaid = payments.filter { pay ->
                pay.refYear == year && pay.refMonth == month &&
                    (filterCardId == null || pay.cardId == filterCardId) &&
                    (filterHolderId == null || pay.holderId == filterHolderId)
            }.sumOf { it.amountPaid }

            val label = formatMonthLabel(year, month)
            MonthlySpendingBar(
                year = year,
                month = month,
                label = label,
                amountCents = monthGross,
                paymentsCents = monthPaid
            )
        }

        // 6. Distribuição por Categoria
        val recurringMerchants = recurringCharges.filter { it.isActive }.map { normalizeString(it.merchant) }.toSet()
        val categoryMap = mutableMapOf<SpendingCategory, Long>()

        for (inst in currentInstallments) {
            val cat = inferCategory(inst.merchantSnapshot, inst.totalInstallments, recurringMerchants)
            categoryMap[cat] = (categoryMap[cat] ?: 0L) + inst.amount
        }

        val categories = SpendingCategory.entries.mapNotNull { cat ->
            val amount = categoryMap[cat] ?: 0L
            if (amount > 0L && grossSpendingCents > 0L) {
                val pct = BigDecimal(amount)
                    .divide(BigDecimal(grossSpendingCents), 4, RoundingMode.HALF_UP)
                    .toFloat()
                CategorySpendingDonut(cat, amount, pct)
            } else {
                null
            }
        }.sortedByDescending { it.amountCents }

        // 7. Decomposição por Modalidade
        var recorrenteCents = 0L
        var parceladoCents = 0L
        var aVistaCents = 0L

        for (inst in currentInstallments) {
            val isRec = normalizeString(inst.merchantSnapshot) in recurringMerchants
            when {
                isRec -> recorrenteCents += inst.amount
                inst.totalInstallments > 1 -> parceladoCents += inst.amount
                else -> aVistaCents += inst.amount
            }
        }

        val modality = ModalitySpending(
            recorrenteCents = recorrenteCents,
            parceladoCents = parceladoCents,
            aVistaCents = aVistaCents
        )

        // 8. Participação por Cartão
        val cardMap = cards.associateBy { it.id }
        val cardGrouped = currentInstallments.groupBy { it.cardId }
        val cardShares = cardGrouped.mapNotNull { (cardId, insts) ->
            val card = cardMap[cardId] ?: return@mapNotNull null
            val cardTotal = insts.sumOf { it.amount }
            val pct = if (grossSpendingCents > 0L) {
                BigDecimal(cardTotal).divide(BigDecimal(grossSpendingCents), 4, RoundingMode.HALF_UP).toFloat()
            } else 0f
            val bank = SupportedBank.fromId(card.bank)
            val displayName = if (bank != SupportedBank.OTHER) bank.displayName else card.name
            CardShare(
                cardId = cardId,
                cardName = card.name,
                bankName = displayName,
                amountCents = cardTotal,
                percentage = pct
            )
        }.sortedByDescending { it.amountCents }

        // 9. Maiores Estabelecimentos (Top 5)
        val topMerchants = currentInstallments.groupBy { normalizeString(it.merchantSnapshot) }
            .map { (_, insts) ->
                val rawMerchant = insts.first().merchantSnapshot
                val total = insts.sumOf { it.amount }
                val cat = inferCategory(rawMerchant, insts.first().totalInstallments, recurringMerchants)
                MerchantRank(
                    merchant = rawMerchant,
                    amountCents = total,
                    count = insts.size,
                    category = cat
                )
            }
            .sortedByDescending { it.amountCents }
            .take(5)

        return AnalyticsDashboardState(
            period = period,
            selectedYear = referenceDate.year,
            selectedMonth = referenceDate.monthValue,
            filterCardId = filterCardId,
            filterHolderId = filterHolderId,
            kpis = kpis,
            monthlyTrend = monthlyTrend,
            categories = categories,
            modality = modality,
            cardShares = cardShares,
            topMerchants = topMerchants,
            filteredInstallments = currentInstallments
        )
    }

    /**
     * Gera lista de pares (Ano, Mês) retroativos a partir de uma data de referência.
     */
    private fun getTargetMonths(refDate: LocalDate, count: Int): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var cursor = refDate.withDayOfMonth(1)
        for (i in 0 until count) {
            result.add(cursor.year to cursor.monthValue)
            cursor = cursor.minusMonths(1)
        }
        return result
    }

    /**
     * Formata rótulo compacto do mês (ex: "Out/26").
     */
    fun formatMonthLabel(year: Int, month: Int): String {
        val date = LocalDate.of(year, month, 1)
        val monthShort = date.format(DateTimeFormatter.ofPattern("MMM", PT_BR))
            .replaceFirstChar { it.uppercase() }
            .replace(".", "")
        val yearShort = (year % 100).toString().padStart(2, '0')
        return "$monthShort/$yearShort"
    }

    /**
     * Infere a categoria de despesa com base no nome do estabelecimento e parâmetros da compra.
     */
    fun inferCategory(
        merchant: String,
        @Suppress("UNUSED_PARAMETER") installmentsCount: Int = 1,
        recurringMerchants: Set<String> = emptySet()
    ): SpendingCategory {
        val clean = normalizeString(merchant)

        if (clean in recurringMerchants) {
            return SpendingCategory.ASSINATURAS
        }

        // 1. Assinaturas / Streaming
        val subscriptionKeywords = setOf(
            "netflix", "spotify", "prime video", "amazon prime", "apple.com", "google play",
            "disney", "hbo", "max", "youtube", "deezer", "globo", "starz", "crunchyroll"
        )
        if (subscriptionKeywords.any { clean.contains(it) }) return SpendingCategory.ASSINATURAS

        // 2. Serviços / Varejo online / E-commerce (verificado antes de supermercados para desambiguar "mercado livre")
        val serviceKeywords = setOf(
            "amazon", "mercado livre", "mercadolivre", "shopee", "aliexpress", "magalu",
            "magazine luiza", "kabum", "enel", "copel", "sanepar", "sabesp", "claro",
            "vivo", "tim", "oi", "internet", "energia", "agua"
        )
        if (serviceKeywords.any { clean.contains(it) }) return SpendingCategory.SERVICOS

        // 3. Alimentação / Mercado / Delivery
        val foodKeywords = setOf(
            "mercado", "supermercado", "carrefour", "pao de acucar", "assai", "atacadao",
            "ifood", "rappi", "restaurante", "padaria", "burger", "mcdonalds", "bk",
            "subway", "pizza", "churrascaria", "acougue", "hortifruti", "lanchonete",
            "bar", "cafe", "sorveteria", "bistro", "panificadora"
        )
        if (foodKeywords.any { clean.contains(it) }) return SpendingCategory.ALIMENTACAO

        // 4. Transporte / Combustível
        val transportKeywords = setOf(
            "uber", "99", "posto", "combustivel", "gasolina", "etanol", "diesel",
            "shell", "ipiranga", "br distribuidora", "estacionamento", "pedagio",
            "sem parar", "conectcar", "veloe", "metro", "cptm", "onibus"
        )
        if (transportKeywords.any { clean.contains(it) }) return SpendingCategory.TRANSPORTE

        // 5. Saúde / Farmácia
        val healthKeywords = setOf(
            "farmacia", "drogaria", "drogasil", "droga raia", "pague menos", "sao paulo",
            "panvel", "hospital", "clinica", "medico", "laboratorio", "consulta",
            "dentista", "odont", "psicolog", "otica"
        )
        if (healthKeywords.any { clean.contains(it) }) return SpendingCategory.SAUDE

        // 6. Lazer / Viagem / Entretenimento
        val leisureKeywords = setOf(
            "cinema", "cinemark", "cinepolis", "ingresso", "sympla", "eventim", "show",
            "hotel", "pousada", "airbnb", "booking", "decolar", "gol linhas", "latam",
            "azul linhas", "voegol", "passagem", "livraria", "teatro"
        )
        if (leisureKeywords.any { clean.contains(it) }) return SpendingCategory.LAZER

        return SpendingCategory.OUTROS
    }

    private fun normalizeString(text: String): String {
        return Normalizer.normalize(text.lowercase(PT_BR), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
            .trim()
    }
}
