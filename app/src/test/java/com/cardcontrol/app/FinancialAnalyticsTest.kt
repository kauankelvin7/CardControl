package com.cardcontrol.app

import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.InstallmentEntity
import com.cardcontrol.app.data.local.PaymentEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.domain.analytics.AnalyticsPeriod
import com.cardcontrol.app.domain.analytics.FinancialAnalyticsEngine
import com.cardcontrol.app.domain.analytics.SpendingCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FinancialAnalyticsTest {
    private val referenceDate = LocalDate.of(2026, 10, 17)
    private val testCards = listOf(
        CreditCardEntity(id = 1L, name = "Inter Black", bank = "inter", lastFourDigits = "1234", totalLimit = 1_000_000L, closingDay = 10, dueDay = 17, defaultHolderId = 1L),
        CreditCardEntity(id = 2L, name = "Nubank Ultravioleta", bank = "nubank", lastFourDigits = "5678", totalLimit = 500_000L, closingDay = 5, dueDay = 12, defaultHolderId = 1L)
    )
    private val testHolders = listOf(
        HolderEntity(id = 1L, name = "Kauan Kelvin", whatsapp = "11999999999", subLimit = 1_500_000L),
        HolderEntity(id = 2L, name = "Outra Pessoa", whatsapp = "", subLimit = 500_000L)
    )

    private fun timestamp(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun transaction(
        id: Long,
        amount: Long,
        date: LocalDate,
        cardId: Long = 1L,
        holderId: Long = 1L,
        merchant: String = "Loja do Bairro",
        installments: Int = 1,
        recurring: Boolean = false
    ) = TransactionEntity(
        id = id,
        cardId = cardId,
        holderId = holderId,
        merchant = merchant,
        totalAmount = amount,
        installmentsCount = installments,
        purchaseTimestamp = timestamp(date),
        isRecurring = recurring
    )

    private fun installment(id: Long, transactionId: Long, amount: Long, number: Int, count: Int) = InstallmentEntity(
        id = id,
        transactionId = transactionId,
        holderId = 1L,
        cardId = 1L,
        merchantSnapshot = "Notebook Loja",
        installmentNumber = number,
        totalInstallments = count,
        amount = amount,
        refMonth = 10,
        refYear = 2026,
        purchaseTimestamp = timestamp(referenceDate)
    )

    private fun analyze(
        transactions: List<TransactionEntity>,
        installments: List<InstallmentEntity> = emptyList(),
        payments: List<PaymentEntity> = emptyList(),
        period: AnalyticsPeriod = AnalyticsPeriod.CURRENT_MONTH,
        cardId: Long? = null,
        holderId: Long? = null
    ) = FinancialAnalyticsEngine.computeDashboard(
        installments = installments,
        payments = payments,
        transactions = transactions,
        cards = testCards,
        holders = testHolders,
        recurringCharges = emptyList(),
        period = period,
        filterCardId = cardId,
        filterHolderId = holderId,
        referenceDate = referenceDate
    )

    @Test
    fun threeInstallmentPurchase_countsOnceAtFullPurchaseAmount() {
        val tx = transaction(1, 30_000L, referenceDate, merchant = "Notebook Loja", installments = 3)
        val installments = listOf(
            installment(1, 1, 10_000L, 1, 3),
            installment(2, 1, 10_000L, 2, 3),
            installment(3, 1, 10_000L, 3, 3)
        )

        val state = analyze(listOf(tx), installments)

        assertEquals(30_000L, state.kpis.grossSpendingCents)
        assertEquals(1, state.kpis.transactionCount)
        assertEquals(30_000L, state.kpis.averageTicketCents)
        assertEquals(30_000L, state.modality.parceladoCents)
        assertEquals(30_000L, state.categories.single().amountCents)
        assertEquals(10_000L, state.categories.single().percentageBps)
        assertEquals(30_000L, state.cardShares.single().amountCents)
        assertEquals(10_000L, state.cardShares.single().percentageBps)
        assertEquals(30_000L, state.topMerchants.single().amountCents)
        assertEquals(1, state.topMerchants.single().count)
        assertEquals(30_000L, state.monthlyTrend.last().amountCents)
        assertEquals(3, state.filteredInstallments.size)
    }

    @Test
    fun lastThreeMonthsIncludesCurrentAndTwoPreviousPurchaseMonths() {
        val transactions = listOf(
            transaction(1, 99_000L, LocalDate.of(2026, 7, 31)),
            transaction(2, 10_000L, LocalDate.of(2026, 8, 1)),
            transaction(3, 10_000L, LocalDate.of(2026, 9, 15)),
            transaction(4, 10_000L, LocalDate.of(2026, 10, 31)),
            transaction(5, 88_000L, LocalDate.of(2026, 11, 1))
        )

        val state = analyze(transactions, period = AnalyticsPeriod.LAST_3_MONTHS)

        assertEquals(30_000L, state.kpis.grossSpendingCents)
        assertEquals(3, state.kpis.transactionCount)
        assertEquals(listOf(10_000L, 10_000L, 10_000L), state.monthlyTrend.map { it.amountCents })
    }

    @Test
    fun periodStartIsInclusiveAndNextMonthStartIsExclusiveInLocalZone() {
        val transactions = listOf(
            transaction(1, 10_000L, LocalDate.of(2026, 9, 30)),
            transaction(2, 20_000L, LocalDate.of(2026, 10, 1)),
            transaction(3, 30_000L, LocalDate.of(2026, 10, 31)),
            transaction(4, 40_000L, LocalDate.of(2026, 11, 1))
        )

        val state = analyze(transactions)

        assertEquals(50_000L, state.kpis.grossSpendingCents)
        assertEquals(listOf(2L, 3L), state.filteredTransactions.map { it.id })
    }

    @Test
    fun cardAndHolderFiltersApplyToPurchaseTransactions() {
        val transactions = listOf(
            transaction(1, 30_000L, referenceDate, cardId = 1L, holderId = 1L),
            transaction(2, 20_000L, referenceDate, cardId = 2L, holderId = 1L),
            transaction(3, 40_000L, referenceDate, cardId = 1L, holderId = 2L)
        )

        val cardState = analyze(transactions, cardId = 2L)
        val holderState = analyze(transactions, holderId = 2L)

        assertEquals(20_000L, cardState.kpis.grossSpendingCents)
        assertEquals(listOf(2L), cardState.filteredTransactions.map { it.id })
        assertEquals(40_000L, holderState.kpis.grossSpendingCents)
        assertEquals(listOf(3L), holderState.filteredTransactions.map { it.id })
    }

    @Test
    fun paymentsAreSeparateAndDoNotReduceGrossPurchaseSpending() {
        val payments = listOf(
            PaymentEntity(id = 1L, holderId = 1L, cardId = 1L, amountPaid = 25_000L, refMonth = 10, refYear = 2026)
        )

        val state = analyze(listOf(transaction(1, 30_000L, referenceDate)), payments = payments)

        assertEquals(30_000L, state.kpis.grossSpendingCents)
        assertEquals(25_000L, state.kpis.paymentsCents)
        assertEquals(5_000L, state.kpis.netSpendingCents)
    }

    @Test
    fun purchaseAnalyticsDoesNotMutateOrPersistInputFinancialData() {
        val tx = transaction(1, 30_000L, referenceDate, installments = 3)
        val transactionSnapshot = listOf(tx)
        val installmentSnapshot = listOf(installment(1, 1, 10_000L, 1, 3))
        val paymentSnapshot = listOf(PaymentEntity(id = 1, holderId = 1, amountPaid = 5_000L, refMonth = 10, refYear = 2026))
        val transactionsBefore = transactionSnapshot.toList()
        val installmentsBefore = installmentSnapshot.toList()
        val paymentsBefore = paymentSnapshot.toList()

        analyze(transactionSnapshot, installmentSnapshot, paymentSnapshot)

        assertEquals(transactionsBefore, transactionSnapshot)
        assertEquals(installmentsBefore, installmentSnapshot)
        assertEquals(paymentsBefore, paymentSnapshot)
    }

    @Test
    fun monthSelectionUsesPurchaseMonthInsteadOfInvoiceMonth() {
        val nextInvoiceInstallment = InstallmentEntity(
            id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L,
            merchantSnapshot = "Compra após fechamento", installmentNumber = 1, totalInstallments = 1,
            amount = 12_345L, refMonth = 11, refYear = 2026, purchaseTimestamp = timestamp(referenceDate)
        )
        val state = analyze(
            transactions = listOf(transaction(1, 12_345L, referenceDate)),
            installments = listOf(nextInvoiceInstallment)
        )

        assertEquals(10, state.selectedMonth)
        assertEquals(2026, state.selectedYear)
        assertEquals(12_345L, state.kpis.grossSpendingCents)
    }

    @Test
    fun periodOverPeriodUsesPurchaseTotalsOnce() {
        val transactions = listOf(
            transaction(1, 50_000L, LocalDate.of(2026, 10, 5)),
            transaction(2, 40_000L, LocalDate.of(2026, 9, 5))
        )

        val state = analyze(transactions)

        assertNotNull(state.kpis.periodOverPeriodChangeBps)
        assertEquals(2_500L, state.kpis.periodOverPeriodChangeBps)
    }

    @Test
    fun periodOverPeriodIsNullWhenPreviousPeriodHasNoPurchases() {
        val state = analyze(listOf(transaction(1, 50_000L, referenceDate)))
        assertNull(state.kpis.periodOverPeriodChangeBps)
    }

    @Test
    fun modalityDistinguishesRecurringInstallmentAndCashPurchases() {
        val recurring = RecurringChargeEntity(
            id = 1, cardId = 1, holderId = 1, merchant = "Netflix.com",
            expectedAmount = 5_590L, lastAmount = 5_590L, isActive = true
        )
        val transactions = listOf(
            transaction(1, 5_590L, referenceDate, merchant = "Netflix.com", recurring = true),
            transaction(2, 30_000L, referenceDate, merchant = "Notebook Loja", installments = 3),
            transaction(3, 3_500L, referenceDate, merchant = "Padaria Pão Quente")
        )
        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = emptyList(), payments = emptyList(), transactions = transactions,
            cards = testCards, holders = testHolders, recurringCharges = listOf(recurring),
            period = AnalyticsPeriod.CURRENT_MONTH, referenceDate = referenceDate
        )

        assertEquals(5_590L, state.modality.recorrenteCents)
        assertEquals(30_000L, state.modality.parceladoCents)
        assertEquals(3_500L, state.modality.aVistaCents)
    }

    @Test
    fun categoryInferenceKeepsExistingMerchantRules() {
        assertEquals(SpendingCategory.ALIMENTACAO, FinancialAnalyticsEngine.inferCategory("Supermercado Carrefour"))
        assertEquals(SpendingCategory.ALIMENTACAO, FinancialAnalyticsEngine.inferCategory("iFood Restaurante"))
        assertEquals(SpendingCategory.TRANSPORTE, FinancialAnalyticsEngine.inferCategory("Uber Viagem"))
        assertEquals(SpendingCategory.TRANSPORTE, FinancialAnalyticsEngine.inferCategory("Posto Shell"))
        assertEquals(SpendingCategory.SAUDE, FinancialAnalyticsEngine.inferCategory("Droga Raia Farmacia"))
        assertEquals(SpendingCategory.ASSINATURAS, FinancialAnalyticsEngine.inferCategory("Netflix Mensalidade"))
        assertEquals(SpendingCategory.LAZER, FinancialAnalyticsEngine.inferCategory("Cinemark Cinema"))
        assertEquals(SpendingCategory.SERVICOS, FinancialAnalyticsEngine.inferCategory("Amazon Compras"))
        assertEquals(SpendingCategory.OUTROS, FinancialAnalyticsEngine.inferCategory("Loja de Parafusos do Bairro"))
    }

    @Test
    fun monthLabelsUsePortugueseAndPreserveYearRollover() {
        assertEquals("Out/26", FinancialAnalyticsEngine.formatMonthLabel(2026, 10))
        assertEquals("Jan/26", FinancialAnalyticsEngine.formatMonthLabel(2026, 1))
        assertEquals("Dez/25", FinancialAnalyticsEngine.formatMonthLabel(2025, 12))
        assertTrue(FinancialAnalyticsEngine.formatMonthLabel(2027, 1).contains("27"))
    }
}
