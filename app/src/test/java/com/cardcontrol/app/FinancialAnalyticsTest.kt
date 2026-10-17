package com.cardcontrol.app

import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.InstallmentEntity
import com.cardcontrol.app.data.local.PaymentEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.domain.analytics.AnalyticsPeriod
import com.cardcontrol.app.domain.analytics.FinancialAnalyticsEngine
import com.cardcontrol.app.domain.analytics.SpendingCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Testes Unitários de Integridade Contábil e Agregação do Motor de Análises Financeiras (TASK-019).
 * Execução 100% determinística em JVM sem emulador e sem ponto flutuante para dinheiro.
 */
class FinancialAnalyticsTest {

    private val refDate = LocalDate.of(2026, 10, 17)

    private val testCards = listOf(
        CreditCardEntity(id = 1L, name = "Inter Black", bank = "inter", lastFourDigits = "1234", totalLimit = 1000000L, closingDay = 10, dueDay = 17, defaultHolderId = 1L),
        CreditCardEntity(id = 2L, name = "Nubank Ultravioleta", bank = "nubank", lastFourDigits = "5678", totalLimit = 500000L, closingDay = 5, dueDay = 12, defaultHolderId = 1L)
    )

    private val testHolders = listOf(
        HolderEntity(id = 1L, name = "Kauan Kelvin", whatsapp = "11999999999", subLimit = 1500000L)
    )

    @Test
    fun testKpis_centsPrecision_grossNetAverageTicket() {
        // 3 compras em Out/2026
        val installments = listOf(
            InstallmentEntity(id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L, merchantSnapshot = "Supermercado Pão de Açúcar", installmentNumber = 1, totalInstallments = 1, amount = 15450L, refMonth = 10, refYear = 2026, purchaseTimestamp = 1000L),
            InstallmentEntity(id = 2L, transactionId = 2L, holderId = 1L, cardId = 1L, merchantSnapshot = "Posto Shell Gasolina", installmentNumber = 1, totalInstallments = 1, amount = 22000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 2000L),
            InstallmentEntity(id = 3L, transactionId = 3L, holderId = 1L, cardId = 2L, merchantSnapshot = "Droga Raia Medicamento", installmentNumber = 1, totalInstallments = 1, amount = 6550L, refMonth = 10, refYear = 2026, purchaseTimestamp = 3000L)
        )

        // Pagamento de 100,00 (10000 centavos)
        val payments = listOf(
            PaymentEntity(id = 1L, holderId = 1L, cardId = 1L, amountPaid = 10000L, refMonth = 10, refYear = 2026, paymentTimestamp = 4000L)
        )

        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = installments,
            payments = payments,
            cards = testCards,
            holders = testHolders,
            recurringCharges = emptyList(),
            period = AnalyticsPeriod.CURRENT_MONTH,
            referenceDate = refDate
        )

        // Total bruto: 15450 + 22000 + 6550 = 44000 centavos (R$ 440,00)
        assertEquals(44000L, state.kpis.grossSpendingCents)
        // Pagamentos: 10000 centavos (R$ 100,00)
        assertEquals(10000L, state.kpis.paymentsCents)
        // Líquido: 44000 - 10000 = 34000 centavos (R$ 340,00)
        assertEquals(34000L, state.kpis.netSpendingCents)
        // Quantidade de transações: 3
        assertEquals(3, state.kpis.transactionCount)
        // Ticket médio: 44000 / 3 = 14667 centavos (HALF_UP)
        assertEquals(14667L, state.kpis.averageTicketCents)
    }

    @Test
    fun testPoPChange_basisPointsCalculation() {
        // Mês atual (Out/26): 500,00 (50000 centavos)
        val currInstallments = listOf(
            InstallmentEntity(id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L, merchantSnapshot = "Compra A", installmentNumber = 1, totalInstallments = 1, amount = 50000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 1000L)
        )

        // Mês anterior (Set/26): 400,00 (40000 centavos)
        val prevInstallments = listOf(
            InstallmentEntity(id = 2L, transactionId = 2L, holderId = 1L, cardId = 1L, merchantSnapshot = "Compra B", installmentNumber = 1, totalInstallments = 1, amount = 40000L, refMonth = 9, refYear = 2026, purchaseTimestamp = 500L)
        )

        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = currInstallments + prevInstallments,
            payments = emptyList(),
            cards = testCards,
            holders = testHolders,
            recurringCharges = emptyList(),
            period = AnalyticsPeriod.CURRENT_MONTH,
            referenceDate = refDate
        )

        // Variação: (50000 - 40000) / 40000 = +25% = +2500 basis points
        assertNotNull(state.kpis.periodOverPeriodChangeBps)
        assertEquals(2500L, state.kpis.periodOverPeriodChangeBps)
    }

    @Test
    fun testPoPChange_nullWhenPreviousIsZero() {
        val currInstallments = listOf(
            InstallmentEntity(id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L, merchantSnapshot = "Compra A", installmentNumber = 1, totalInstallments = 1, amount = 50000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 1000L)
        )

        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = currInstallments,
            payments = emptyList(),
            cards = testCards,
            holders = testHolders,
            recurringCharges = emptyList(),
            period = AnalyticsPeriod.CURRENT_MONTH,
            referenceDate = refDate
        )

        // Sem dados no período anterior: variação deve ser nula com segurança
        assertNull(state.kpis.periodOverPeriodChangeBps)
    }

    @Test
    fun testCategoryInference_heuristicsAndFallback() {
        assertEquals(SpendingCategory.ALIMENTACAO, FinancialAnalyticsEngine.inferCategory("Supermercado Carrefour"))
        assertEquals(SpendingCategory.ALIMENTACAO, FinancialAnalyticsEngine.inferCategory("iFood Restaurante"))
        assertEquals(SpendingCategory.TRANSPORTE, FinancialAnalyticsEngine.inferCategory("Uber Viagem"))
        assertEquals(SpendingCategory.TRANSPORTE, FinancialAnalyticsEngine.inferCategory("Posto Shell"))
        assertEquals(SpendingCategory.SAUDE, FinancialAnalyticsEngine.inferCategory("Droga Raia Farmacia"))
        assertEquals(SpendingCategory.SAUDE, FinancialAnalyticsEngine.inferCategory("Laboratorio Clinica"))
        assertEquals(SpendingCategory.ASSINATURAS, FinancialAnalyticsEngine.inferCategory("Netflix Mensalidade"))
        assertEquals(SpendingCategory.ASSINATURAS, FinancialAnalyticsEngine.inferCategory("Spotify Premium"))
        assertEquals(SpendingCategory.LAZER, FinancialAnalyticsEngine.inferCategory("Cinemark Cinema"))
        assertEquals(SpendingCategory.LAZER, FinancialAnalyticsEngine.inferCategory("Airbnb Reserva"))
        assertEquals(SpendingCategory.SERVICOS, FinancialAnalyticsEngine.inferCategory("Amazon Compras"))
        assertEquals(SpendingCategory.SERVICOS, FinancialAnalyticsEngine.inferCategory("Mercado Livre Entrega"))
        assertEquals(SpendingCategory.OUTROS, FinancialAnalyticsEngine.inferCategory("Loja de Parafusos do Bairro"))
    }

    @Test
    fun testModalityDecomposition_recurringInstallmentSingle() {
        val recurringMerchant = "Netflix.com"
        val recurringCharges = listOf(
            RecurringChargeEntity(id = 1L, cardId = 1L, holderId = 1L, merchant = recurringMerchant, expectedAmount = 5590L, lastAmount = 5590L, isActive = true)
        )

        val installments = listOf(
            // Assinatura: 55,90 (5590 centavos)
            InstallmentEntity(id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L, merchantSnapshot = recurringMerchant, installmentNumber = 1, totalInstallments = 1, amount = 5590L, refMonth = 10, refYear = 2026, purchaseTimestamp = 1000L),
            // Parcelado: 3x de 100,00 (10000 centavos cada)
            InstallmentEntity(id = 2L, transactionId = 2L, holderId = 1L, cardId = 1L, merchantSnapshot = "Notebook Loja", installmentNumber = 1, totalInstallments = 3, amount = 10000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 2000L),
            // À vista: 35,00 (3500 centavos)
            InstallmentEntity(id = 3L, transactionId = 3L, holderId = 1L, cardId = 1L, merchantSnapshot = "Padaria Pão Quente", installmentNumber = 1, totalInstallments = 1, amount = 3500L, refMonth = 10, refYear = 2026, purchaseTimestamp = 3000L)
        )

        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = installments,
            payments = emptyList(),
            cards = testCards,
            holders = testHolders,
            recurringCharges = recurringCharges,
            period = AnalyticsPeriod.CURRENT_MONTH,
            referenceDate = refDate
        )

        assertEquals(5590L, state.modality.recorrenteCents)
        assertEquals(10000L, state.modality.parceladoCents)
        assertEquals(3500L, state.modality.aVistaCents)
    }

    @Test
    fun testCardFilter_isolatesSelectedCard() {
        val installments = listOf(
            InstallmentEntity(id = 1L, transactionId = 1L, holderId = 1L, cardId = 1L, merchantSnapshot = "Compra Card 1", installmentNumber = 1, totalInstallments = 1, amount = 30000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 1000L),
            InstallmentEntity(id = 2L, transactionId = 2L, holderId = 1L, cardId = 2L, merchantSnapshot = "Compra Card 2", installmentNumber = 1, totalInstallments = 1, amount = 20000L, refMonth = 10, refYear = 2026, purchaseTimestamp = 2000L)
        )

        // Filtra estritamente pelo cartão ID 1
        val state = FinancialAnalyticsEngine.computeDashboard(
            installments = installments,
            payments = emptyList(),
            cards = testCards,
            holders = testHolders,
            recurringCharges = emptyList(),
            period = AnalyticsPeriod.CURRENT_MONTH,
            filterCardId = 1L,
            referenceDate = refDate
        )

        assertEquals(30000L, state.kpis.grossSpendingCents)
        assertEquals(1, state.kpis.transactionCount)
        assertEquals("Compra Card 1", state.filteredInstallments.first().merchantSnapshot)
    }

    @Test
    fun testFormatMonthLabel_portugueseFormatting() {
        assertEquals("Out/26", FinancialAnalyticsEngine.formatMonthLabel(2026, 10))
        assertEquals("Jan/26", FinancialAnalyticsEngine.formatMonthLabel(2026, 1))
        assertEquals("Dez/25", FinancialAnalyticsEngine.formatMonthLabel(2025, 12))
    }
}
