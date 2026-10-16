package com.cardcontrol.app.util

import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.security.SecuritySanitizer
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Algoritmo 100% offline e determinístico para identificação de assinaturas,
 * despesas recorrentes e variação de valor (price creep).
 */
object SubscriptionTracker {

    /** Janela em dias para considerar uma cobrança consecutiva como assinatura mensal. */
    const val MIN_CYCLE_DAYS = 28L
    const val MAX_CYCLE_DAYS = 32L

    data class DetectionResult(
        val merchant: String,
        val cardId: Long,
        val holderId: Long,
        val expectedAmount: Long,
        val lastAmount: Long,
        val previousAmount: Long?,
        val hasPriceCreep: Boolean,
        val priceCreepDelta: Long,
        val lastChargedTimestamp: Long,
        val billingCycleDays: Int,
        val isUserConfirmed: Boolean
    )

    fun normalizeMerchant(raw: String): String {
        val sanitized = SecuritySanitizer.sanitizeHeaderField(raw, 40)
        return Normalizer.normalize(sanitized.lowercase(Locale("pt", "BR")), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
            .trim()
    }

    /**
     * Analisa a lista de transações locais e detecta padrões recorrentes.
     */
    fun detectSubscriptions(transactions: List<TransactionEntity>): List<DetectionResult> {
        // A manual recurring flag is explicit user intent and must not be lost just because
        // the source transaction is split into installments.
        val eligibleTransactions = transactions.filter { it.installmentsCount == 1 || it.isRecurring }
        val grouped = eligibleTransactions.groupBy { Pair(normalizeMerchant(it.merchant), it.cardId) }

        val results = mutableListOf<DetectionResult>()

        for ((_, txs) in grouped) {
            val sorted = txs.sortedBy { it.purchaseTimestamp }
            if (sorted.isEmpty()) continue

            val hasManualFlag = sorted.any { it.isRecurring }
            val latestTx = sorted.last()
            val previousTx = sorted.getOrNull(sorted.lastIndex - 1)
            val latestIntervalDays = previousTx?.let {
                TimeUnit.MILLISECONDS.toDays(latestTx.purchaseTimestamp - it.purchaseTimestamp)
            }
            // Validate the most recent interval, not any historical pair. An old matching
            // cycle must not hide a newer irregular charge.
            val isDetectedByInterval =
                latestIntervalDays != null && latestIntervalDays in MIN_CYCLE_DAYS..MAX_CYCLE_DAYS
            val observedIntervalDays = latestIntervalDays
                ?.takeIf { it in MIN_CYCLE_DAYS..MAX_CYCLE_DAYS }
                ?.toInt() ?: 30

            if (hasManualFlag || isDetectedByInterval) {
                val lastTx = sorted.last()
                val prevTx = if (sorted.size >= 2) sorted[sorted.size - 2] else null

                val lastAmount = lastTx.totalAmount
                val prevAmount = prevTx?.totalAmount
                val hasPriceCreep = prevAmount != null && lastAmount > prevAmount
                val priceCreepDelta = if (hasPriceCreep && prevAmount != null) lastAmount - prevAmount else 0L

                results.add(
                    DetectionResult(
                        merchant = lastTx.merchant,
                        cardId = lastTx.cardId,
                        holderId = lastTx.holderId,
                        expectedAmount = lastAmount,
                        lastAmount = lastAmount,
                        previousAmount = prevAmount,
                        hasPriceCreep = hasPriceCreep,
                        priceCreepDelta = priceCreepDelta,
                        lastChargedTimestamp = lastTx.purchaseTimestamp,
                        billingCycleDays = observedIntervalDays,
                        isUserConfirmed = hasManualFlag
                    )
                )
            }
        }

        return results
    }

    /**
     * Calcula o total mensal fixo comprometido por recorrências ativas.
     */
    fun calculateCommittedFixedCosts(activeRecurring: List<RecurringChargeEntity>): Long {
        return activeRecurring.filter { it.isActive }.sumOf { it.expectedAmount }
    }

    /**
     * Calcula a porcentagem do limite total dos cartões que está comprometida por custos fixos.
     */
    fun calculateLimitImpactPercent(committedFixedCost: Long, totalCardsLimit: Long): Double {
        if (totalCardsLimit <= 0L) return 0.0
        val percent = (committedFixedCost.toDouble() / totalCardsLimit.toDouble()) * 100.0
        // Keep values above 100% visible; clamping would hide genuine overspending.
        return percent
    }
}
