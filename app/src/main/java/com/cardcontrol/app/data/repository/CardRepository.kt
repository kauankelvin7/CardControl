package com.cardcontrol.app.data.repository

import com.cardcontrol.app.data.local.AppDao
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.InstallmentEntity
import com.cardcontrol.app.data.local.PaymentEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.SupportedBank
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.security.SecuritySanitizer
import com.cardcontrol.app.service.AutoCaptureResult
import com.cardcontrol.app.service.BankNotificationParser
import com.cardcontrol.app.service.BankNotificationProcessor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Resultado da associação de uma notificação a um cartão cadastrado. */
sealed interface CardResolution {
    data class Found(val card: CreditCardEntity) : CardResolution
    /** Nenhum cartão cadastrado com o final informado. */
    data object NoMatch : CardResolution
    /** Há candidatos, mas o emissor não pôde ser confirmado de forma única. */
    data object Ambiguous : CardResolution
}

class CardRepository(private val dao: AppDao) {
    val holders = dao.observeHolders()
    val cards = dao.observeCards()
    val allInstallments = dao.observeAllInstallments()
    val allPayments = dao.observeAllPayments()
    val activeRecurringCharges = dao.observeActiveRecurringCharges()
    val allRecurringCharges = dao.observeAllRecurringCharges()

    fun observeInstallmentsForMonth(month: Int, year: Int) = dao.observeInstallmentsByPeriod(month, year)
    fun observePaymentsForMonth(month: Int, year: Int) = dao.observePaymentsByPeriod(month, year)

    suspend fun addHolder(name: String, whatsapp: String, email: String, subLimit: Long) {
        dao.insertHolder(
            HolderEntity(
                name = SecuritySanitizer.sanitizeHeaderField(name, 50),
                whatsapp = whatsapp.filter { it.isDigit() }.take(15),
                email = email.trim().take(80),
                subLimit = SecuritySanitizer.validateLimitAmount(subLimit)
            )
        )
    }

    suspend fun addCard(
        name: String,
        bank: String = SupportedBank.OTHER.id,
        lastFour: String,
        totalLimit: Long,
        closingDay: Int,
        dueDay: Int,
        defaultHolderId: Long?,
        issuerBank: String? = null
    ) {
        val resolvedBank = when {
            bank != SupportedBank.OTHER.id -> SupportedBank.fromId(bank).id
            issuerBank != null -> {
                SupportedBank.entries.firstOrNull {
                    it.displayName.equals(issuerBank, ignoreCase = true) || it.id.equals(issuerBank, ignoreCase = true)
                }?.id ?: SupportedBank.OTHER.id
            }
            else -> SupportedBank.inferFromCardName(name).id
        }
        dao.insertCard(
            CreditCardEntity(
                name = SecuritySanitizer.sanitizeHeaderField(name, 40),
                bank = resolvedBank,
                lastFourDigits = lastFour.filter { it.isDigit() }.takeLast(4),
                totalLimit = SecuritySanitizer.validateLimitAmount(totalLimit),
                closingDay = closingDay.coerceIn(1, 28),
                dueDay = dueDay.coerceIn(1, 28),
                defaultHolderId = defaultHolderId
            )
        )
    }

    suspend fun updateCard(
        cardId: Long,
        name: String,
        bank: String = SupportedBank.OTHER.id,
        lastFour: String,
        totalLimit: Long,
        closingDay: Int,
        dueDay: Int,
        defaultHolderId: Long?
    ): Boolean {
        val existing = dao.getCardById(cardId) ?: return false
        val safeName = SecuritySanitizer.sanitizeHeaderField(name, 40)
        val digits = lastFour.filter { it.isDigit() }.takeLast(4)
        if (safeName.isBlank() || digits.length != 4) return false
        if (defaultHolderId != null && dao.getHolderById(defaultHolderId) == null) return false

        val safeBank = SupportedBank.fromId(bank).id
        val updated = existing.copy(
            name = safeName,
            bank = safeBank,
            lastFourDigits = digits,
            totalLimit = SecuritySanitizer.validateLimitAmount(totalLimit),
            closingDay = closingDay.coerceIn(1, 28),
            dueDay = dueDay.coerceIn(1, 28),
            defaultHolderId = defaultHolderId
        )
        return dao.updateCard(updated) > 0
    }

    suspend fun deleteCardIfUnused(cardId: Long): Boolean {
        return dao.deleteCardIfUnused(cardId) > 0
    }

    suspend fun isDuplicateNotification(dedupHash: String, timestamp: Long): Boolean {
        if (dedupHash.isBlank()) return false
        return dao.countRecentDuplicate(dedupHash, timestamp - 120_000L) > 0
    }

    /**
     * Associa a notificação a um cartão somente quando o emissor é confirmado:
     * o banco efetivo do cartão é [CreditCardEntity.bank] ou, para cartões legados com bank "other",
     * o inferido por palavra inteira do apelido. Mais de um candidato ou emissor não confirmado é
     * [CardResolution.Ambiguous]; nunca escolhe um cartão arbitrariamente.
     */
    suspend fun resolveCard(packageName: String, lastFourDigits: String?): CardResolution {
        val targetBank = SupportedBank.fromPackageName(packageName)
        if (targetBank == SupportedBank.OTHER) return CardResolution.Ambiguous

        val cards = dao.getAllCardsSnapshot()
        if (cards.isEmpty()) return CardResolution.NoMatch

        val candidates = if (lastFourDigits != null) cards.filter { it.lastFourDigits == lastFourDigits } else cards
        if (candidates.isEmpty()) return CardResolution.NoMatch

        // Sem final informado, só vale se houver exatamente um cartão cadastrado.
        if (lastFourDigits == null && candidates.size != 1) return CardResolution.Ambiguous

        val confirmed = candidates.filter { card ->
            val effectiveBank = if (card.bank != SupportedBank.OTHER.id) {
                SupportedBank.fromId(card.bank)
            } else {
                SupportedBank.inferFromCardName(card.name)
            }
            effectiveBank == targetBank
        }
        return confirmed.singleOrNull()?.let { CardResolution.Found(it) } ?: CardResolution.Ambiguous
    }

    suspend fun resolveCardForNotification(packageName: String, lastFourDigits: String?): CreditCardEntity? =
        (resolveCard(packageName, lastFourDigits) as? CardResolution.Found)?.card

    /**
     * Processa uma notificação bancária delegando ao [BankNotificationProcessor] canônico.
     *
     * Mantém o contrato legado em forma de par (ID da transação, cartão vinculado) para
     * compatibilidade com testes e fluxos síncronos, aplicando deduplicação de 120s,
     * trava atômica de captura e desambiguação segura de emissores.
     */
    suspend fun processBankNotification(
        packageName: String,
        title: String,
        text: String,
        timestamp: Long
    ): Pair<Long, CreditCardEntity?> {
        val result = BankNotificationProcessor(dao, this).process(packageName, title, text, timestamp)
        return when (result) {
            is AutoCaptureResult.Registered -> Pair(result.transactionId, result.card)
            is AutoCaptureResult.Ignored -> Pair(-1L, null)
        }
    }

    suspend fun registerExpense(
        cardId: Long,
        holderId: Long,
        merchant: String,
        totalAmount: Long,
        installmentsCount: Int,
        purchaseTimestamp: Long = System.currentTimeMillis(),
        isAutoCaptured: Boolean = false,
        dedupHash: String = ""
    ): Long = expenseMutex.withLock {
        if (isAutoCaptured && dedupHash.isNotBlank()) {
            if (dao.countRecentDuplicate(dedupHash, purchaseTimestamp - 120_000L) > 0) {
                return@withLock -1L
            }
        }
        val card = dao.getCardById(cardId) ?: throw IllegalArgumentException("Cartao nao encontrado")
        val validAmount = SecuritySanitizer.validateFinancialAmount(totalAmount)
        val safeInstallments = SecuritySanitizer.validateInstallments(installmentsCount)
        val safeMerchant = SecuritySanitizer.sanitizeHeaderField(merchant, 40)

        val purchaseDate: LocalDate = Instant.ofEpochMilli(purchaseTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val startYearMonth = calculateFirstInstallmentMonth(purchaseDate, card.closingDay)
        val amounts = splitInstallmentAmounts(validAmount, safeInstallments)

        val tx = TransactionEntity(
            cardId = cardId,
            holderId = holderId,
            merchant = safeMerchant,
            totalAmount = validAmount,
            installmentsCount = safeInstallments,
            purchaseTimestamp = purchaseTimestamp,
            isAutoCaptured = isAutoCaptured,
            dedupHash = dedupHash
        )

            val finalTxId = dao.insertTransactionWithInstallments(tx) { txId ->
                amounts.mapIndexed { index, parcelAmount ->
                    val targetDate = startYearMonth.plusMonths(index.toLong())
                    InstallmentEntity(
                        transactionId = txId,
                        holderId = holderId,
                        cardId = cardId,
                        merchantSnapshot = safeMerchant,
                        installmentNumber = index + 1,
                        totalInstallments = safeInstallments,
                        amount = parcelAmount,
                        refMonth = targetDate.monthValue,
                        refYear = targetDate.year,
                        purchaseTimestamp = purchaseTimestamp
                    )
                }
            }
            // Sincroniza assinaturas e custos recorrentes após nova transação
            syncRecurringCharges()
            finalTxId
        }

    suspend fun syncRecurringCharges() {
        val txs = dao.getAllTransactionsSnapshot()
        val detected = com.cardcontrol.app.util.SubscriptionTracker.detectSubscriptions(txs)
        val existingCharges = dao.getAllRecurringChargesSnapshot().toMutableList()

        // Saneia registros duplicados criados por versões anteriores ou concorrência antes de sincronizar.
        // Agrupa por cartão e merchant normalizado para manter uma única entidade canônica por assinatura.
        val duplicateGroups = existingCharges.groupBy {
            Pair(
                it.cardId,
                com.cardcontrol.app.util.SubscriptionTracker.normalizeMerchant(it.merchant)
            )
        }.values.filter { it.size > 1 }

        for (matches in duplicateGroups) {
            // Prioriza registros que o usuário confirmou manualmente, depois os que estão ativos,
            // e desempata pelo menor ID para garantir estabilidade determinística.
            val canonical = matches.sortedWith(
                compareByDescending<RecurringChargeEntity> { it.isUserConfirmed }
                    .thenByDescending { it.isActive }
                    .thenBy { it.id }
            ).first()
            val latest = matches.maxBy { it.lastChargedTimestamp }
            val consolidated = canonical.copy(
                holderId = latest.holderId,
                expectedAmount = latest.expectedAmount,
                lastAmount = latest.lastAmount,
                previousAmount = latest.previousAmount,
                billingCycleDays = latest.billingCycleDays,
                lastChargedTimestamp = latest.lastChargedTimestamp,
                // Preserva o consentimento do usuário: se qualquer cópia estava ativa ou confirmada,
                // a consolidação mantém esse estado para não anular uma decisão prévia do usuário.
                isActive = matches.any { it.isActive },
                isUserConfirmed = matches.any { it.isUserConfirmed }
            )
            dao.insertRecurringCharge(consolidated)
            matches.filter { it.id != canonical.id }.forEach { dao.deleteRecurringCharge(it.id) }
            existingCharges.removeAll { existing ->
                existing.cardId == canonical.cardId &&
                    com.cardcontrol.app.util.SubscriptionTracker.normalizeMerchant(existing.merchant) ==
                    com.cardcontrol.app.util.SubscriptionTracker.normalizeMerchant(canonical.merchant)
            }
            existingCharges.add(consolidated)
        }

        for (item in detected) {
            val normalizedMerchant = com.cardcontrol.app.util.SubscriptionTracker.normalizeMerchant(item.merchant)
            val existingIndex = existingCharges.indexOfFirst {
                it.cardId == item.cardId &&
                    com.cardcontrol.app.util.SubscriptionTracker.normalizeMerchant(it.merchant) == normalizedMerchant
            }
            val existing = existingCharges.getOrNull(existingIndex)
            if (existing != null) {
                // Preserva o rótulo original e o ID estável, atualizando apenas métricas contábeis e ciclo.
                val updated = existing.copy(
                    holderId = item.holderId,
                    expectedAmount = item.expectedAmount,
                    lastAmount = item.lastAmount,
                    previousAmount = item.previousAmount,
                    billingCycleDays = item.billingCycleDays,
                    lastChargedTimestamp = item.lastChargedTimestamp,
                    isUserConfirmed = existing.isUserConfirmed || item.isUserConfirmed
                )
                dao.insertRecurringCharge(updated)
                existingCharges[existingIndex] = updated
            } else {
                val newCharge = RecurringChargeEntity(
                    cardId = item.cardId,
                    holderId = item.holderId,
                    merchant = item.merchant,
                    expectedAmount = item.expectedAmount,
                    lastAmount = item.lastAmount,
                    previousAmount = item.previousAmount,
                    billingCycleDays = item.billingCycleDays,
                    lastChargedTimestamp = item.lastChargedTimestamp,
                    isActive = true,
                    isUserConfirmed = item.isUserConfirmed
                )
                val insertedId = dao.insertRecurringCharge(newCharge)
                existingCharges.add(newCharge.copy(id = insertedId))
            }
        }
    }

    suspend fun setRecurringChargeActive(id: Long, isActive: Boolean) {
        dao.setRecurringChargeActive(id, isActive)
    }

    suspend fun markTransactionRecurring(transactionId: Long, isRecurring: Boolean): Boolean {
        val updated = dao.setTransactionRecurring(transactionId, isRecurring) > 0
        if (updated) syncRecurringCharges()
        return updated
    }

    suspend fun registerPayment(
        holderId: Long,
        cardId: Long?,
        amountPaid: Long,
        refMonth: Int,
        refYear: Int,
        notes: String
    ) {
        val validAmount = SecuritySanitizer.validateFinancialAmount(amountPaid)
        dao.insertPayment(
            PaymentEntity(
                holderId = holderId,
                cardId = cardId,
                amountPaid = validAmount,
                refMonth = refMonth,
                refYear = refYear,
                notes = SecuritySanitizer.sanitizeHeaderField(notes, 80)
            )
        )
    }

    companion object {
        /**
         * Mutex de processo compartilhado entre todas as instâncias de [CardRepository].
         * Impede condições de corrida em gravações financeiras concorrentes e garante
         * a integridade da deduplicação de 120s antes de qualquer persistência no SQLite.
         */
        private val expenseMutex = Mutex()

        fun calculateFirstInstallmentMonth(purchaseDate: LocalDate, closingDay: Int): LocalDate {
            val baseMonth = LocalDate.of(purchaseDate.year, purchaseDate.monthValue, 1)
            return if (purchaseDate.dayOfMonth >= closingDay) baseMonth.plusMonths(1) else baseMonth
        }

        fun splitInstallmentAmounts(totalAmount: Long, installments: Int): List<Long> {
            val count = installments.coerceIn(1, 48)
            val base = totalAmount / count
            val remainder = (totalAmount % count).toInt()
            return List(count) { index -> if (index < remainder) base + 1L else base }
        }
    }
}
