package com.cardcontrol.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cardcontrol.app.data.local.*
import com.cardcontrol.app.data.repository.CardRepository
import com.cardcontrol.app.data.security.CredentialStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HolderSummaryUi(
    val holder: HolderEntity,
    val monthlyInstallments: List<InstallmentEntity>,
    val monthlySpent: Long,
    val monthlyPaid: Long,
    val monthlyNetDue: Long,
    val globalCommitted: Long,
    val availableSubLimit: Long
)

data class CardSummaryUi(
    val card: CreditCardEntity,
    val globalCommitted: Long,
    val availableLimit: Long
)

data class DashboardUiState(
    val isInitializing: Boolean = false,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val holders: List<HolderEntity> = emptyList(),
    val cards: List<CreditCardEntity> = emptyList(),
    val holderSummaries: List<HolderSummaryUi> = emptyList(),
    val cardSummaries: List<CardSummaryUi> = emptyList(),
    val totalMonthInvoice: Long = 0L,
    val totalMonthPaid: Long = 0L,
    val recurringCharges: List<RecurringChargeEntity> = emptyList(),
    val totalMonthlyRecurring: Long = 0L,
    val recurringLimitImpactPercent: Double = 0.0,
    val priceCreepAlertCount: Int = 0,
    val allInstallments: List<InstallmentEntity> = emptyList(),
    val allPayments: List<PaymentEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CardRepository = CardRepository(AppDatabase.getInstance(application).appDao()),
    val credentialStore: CredentialStore = CredentialStore(application)
) : AndroidViewModel(application) {
    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val uiMessages = _uiMessages.asSharedFlow()
    private val manualSelectedPeriod = MutableStateFlow<LocalDate?>(null)
    private val selectedPeriod = MutableStateFlow(LocalDate.now().withDayOfMonth(1))

    init {
        viewModelScope.launch {
            repository.sanitizeDuplicateHolders()
            repository.syncRecurringCharges()
        }
    }

    val uiState: StateFlow<DashboardUiState> = buildDashboardUiStateFlow(
        referenceDate = selectedPeriod.value,
        manualPeriodFlow = manualSelectedPeriod,
        repository = repository
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isInitializing = true))

    fun changeMonth(delta: Long) {
        val current = manualSelectedPeriod.value ?: LocalDate.of(uiState.value.selectedYear, uiState.value.selectedMonth, 1)
        val updated = current.plusMonths(delta)
        manualSelectedPeriod.value = updated
        selectedPeriod.value = updated
    }

    fun selectMonthYear(month: Int, year: Int) {
        val updated = LocalDate.of(year, month.coerceIn(1, 12), 1)
        manualSelectedPeriod.value = updated
        selectedPeriod.value = updated
    }
    fun addHolder(name: String, whatsapp: String, email: String, subLimit: Long) {
        viewModelScope.launch {
            val created = repository.addHolder(name, whatsapp, email, subLimit)
            _uiMessages.emit(
                if (created) "Pessoa cadastrada com sucesso."
                else "Esta pessoa já estava cadastrada; dados atualizados."
            )
        }
    }

    fun updateHolder(id: Long, name: String, whatsapp: String, email: String, subLimit: Long) {
        viewModelScope.launch {
            val updated = repository.updateHolder(id, name, whatsapp, email, subLimit)
            _uiMessages.emit(
                if (updated) "Dados da pessoa atualizados."
                else "Não foi possível salvar. Verifique o nome informado."
            )
        }
    }

    fun deleteHolder(id: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteHolder(id)
            _uiMessages.emit(
                if (deleted) "Pessoa removida do sistema."
                else "Esta pessoa possui compras, pagamentos ou cobranças recorrentes vinculadas. Para preservar o histórico financeiro, não é possível excluí-la."
            )
        }
    }


    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            if (repository.deleteTransaction(transactionId)) {
                _uiMessages.emit("Compra removida da fatura.")
            }
        }
    }

    fun addCard(
        name: String,
        bank: String = SupportedBank.OTHER.id,
        lastFour: String,
        limit: Long,
        closing: Int,
        due: Int,
        defaultHolderId: Long?,
        issuerBank: String? = null
    ) {
        viewModelScope.launch {
            repository.addCard(
                name = name,
                bank = bank,
                lastFour = lastFour,
                totalLimit = limit,
                closingDay = closing,
                dueDay = due,
                defaultHolderId = defaultHolderId,
                issuerBank = issuerBank
            )
            _uiMessages.emit("Cartão cadastrado.")
        }
    }

    fun updateCard(
        id: Long,
        name: String,
        bank: String,
        lastFour: String,
        limit: Long,
        closing: Int,
        due: Int,
        defaultHolderId: Long?
    ) {
        viewModelScope.launch {
            val updated = repository.updateCard(id, name, bank, lastFour, limit, closing, due, defaultHolderId)
            _uiMessages.emit(
                if (updated) "Alterações do cartão salvas."
                else "Não foi possível salvar. Confira os dados e a pessoa vinculada."
            )
        }
    }

    fun deleteCard(id: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteCardIfUnused(id)
            _uiMessages.emit(
                if (deleted) "Cartão excluído."
                else "Este cartão possui compras, pagamentos ou cobranças recorrentes vinculadas. Para preservar o histórico, não é possível excluí-lo."
            )
        }
    }

    fun addExpense(cardId: Long, holderId: Long, merchant: String, amount: Long, installments: Int) {
        viewModelScope.launch {
            val txId = repository.registerExpense(cardId, holderId, merchant, amount, installments)
            if (txId > 0L) {
                val card = uiState.value.cards.firstOrNull { it.id == cardId }
                val targetMonth = CardRepository.calculateFirstInstallmentMonth(
                    LocalDate.now(),
                    card?.closingDay ?: 10
                )
                manualSelectedPeriod.value = targetMonth
                selectedPeriod.value = targetMonth
                _uiMessages.emit(
                    "Compra lançada na fatura de %02d/%d.".format(targetMonth.monthValue, targetMonth.year)
                )
            }
        }
    }

    fun addPayment(holderId: Long, cardId: Long?, amount: Long, notes: String) {
        val period = manualSelectedPeriod.value ?: LocalDate.of(uiState.value.selectedYear, uiState.value.selectedMonth, 1)
        viewModelScope.launch {
            repository.registerPayment(holderId, cardId, amount, period.monthValue, period.year, notes)
            _uiMessages.emit("Pagamento registrado em %02d/%d.".format(period.monthValue, period.year))
        }
    }
    fun setRecurringChargeActive(id: Long, isActive: Boolean) {
        viewModelScope.launch { repository.setRecurringChargeActive(id, isActive) }
    }
    fun markTransactionRecurring(transactionId: Long, isRecurring: Boolean) {
        viewModelScope.launch {
            val updated = repository.markTransactionRecurring(transactionId, isRecurring)
            _uiMessages.emit(
                when {
                    updated && isRecurring -> "Compra marcada como recorrente."
                    updated -> "Marca de recorrência removida."
                    else -> "Só compras sem parcelamento podem ser marcadas como recorrentes."
                }
            )
        }
    }

    companion object {
        internal fun resolveInitialInvoicePeriod(
            referenceDate: LocalDate,
            installments: List<InstallmentEntity>
        ): LocalDate {
            val base = referenceDate.withDayOfMonth(1)
            if (installments.isEmpty()) return base
            if (installments.any { it.refYear == base.year && it.refMonth == base.monthValue }) {
                return base
            }
            val referenceYearMonth = java.time.YearMonth.from(base)
            val parsedMonths = installments.asSequence()
                .mapNotNull { installment ->
                    runCatching { java.time.YearMonth.of(installment.refYear, installment.refMonth) }.getOrNull()
                }
                .toList()
            if (parsedMonths.isEmpty()) return base

            val futureMonth = parsedMonths.filter { it > referenceYearMonth }.minOrNull()
            return futureMonth?.atDay(1) ?: base
        }

        internal fun buildDashboardUiStateFlow(
            referenceDate: LocalDate,
            manualPeriodFlow: Flow<LocalDate?>,
            repository: CardRepository
        ): Flow<DashboardUiState> {
            var lockedInitialPeriod: LocalDate? = null

            return combine(
                manualPeriodFlow,
                repository.holders,
                repository.cards,
                repository.allInstallments,
                repository.allPayments,
                repository.allTransactions,
                repository.allRecurringCharges
            ) { args: Array<Any?> ->
                val manualPeriod = args[0] as? LocalDate
                @Suppress("UNCHECKED_CAST")
                val holders = args[1] as List<HolderEntity>
                @Suppress("UNCHECKED_CAST")
                val cards = args[2] as List<CreditCardEntity>
                @Suppress("UNCHECKED_CAST")
                val allInst = args[3] as List<InstallmentEntity>
                @Suppress("UNCHECKED_CAST")
                val allPay = args[4] as List<PaymentEntity>
                @Suppress("UNCHECKED_CAST")
                val allTx = args[5] as List<TransactionEntity>
                @Suppress("UNCHECKED_CAST")
                val recurring = args[6] as List<RecurringChargeEntity>

                val effectiveDate: LocalDate = when {
                    manualPeriod != null -> {
                        lockedInitialPeriod = manualPeriod
                        manualPeriod
                    }
                    lockedInitialPeriod != null -> lockedInitialPeriod!!
                    else -> {
                        val resolved = resolveInitialInvoicePeriod(referenceDate, allInst)
                        lockedInitialPeriod = resolved
                        resolved
                    }
                }

                val monthInst = allInst.filter { it.refYear == effectiveDate.year && it.refMonth == effectiveDate.monthValue }
                val monthPay = allPay.filter { it.refYear == effectiveDate.year && it.refMonth == effectiveDate.monthValue }

                val holderSummaries = holders.map { h ->
                    val hMonthInst = monthInst.filter { it.holderId == h.id }
                    val mSpent = hMonthInst.sumOf { it.amount }
                    val mPaid = monthPay.filter { it.holderId == h.id }.sumOf { it.amountPaid }
                    val gSpent = allInst.filter { it.holderId == h.id }.sumOf { it.amount }
                    val gPaid = allPay.filter { it.holderId == h.id }.sumOf { it.amountPaid }
                    val gCommitted = (gSpent - gPaid).coerceAtLeast(0L)

                    HolderSummaryUi(
                        holder = h,
                        monthlyInstallments = hMonthInst,
                        monthlySpent = mSpent,
                        monthlyPaid = mPaid,
                        monthlyNetDue = (mSpent - mPaid).coerceAtLeast(0L),
                        globalCommitted = gCommitted,
                        availableSubLimit = h.subLimit - gCommitted
                    )
                }

                val cardSummaries = cards.map { c ->
                    val cSpent = allInst.filter { it.cardId == c.id }.sumOf { it.amount }
                    val cPaid = allPay.filter { it.cardId == c.id }.sumOf { it.amountPaid }
                    val cCommitted = (cSpent - cPaid).coerceAtLeast(0L)
                    CardSummaryUi(
                        card = c,
                        globalCommitted = cCommitted,
                        availableLimit = c.totalLimit - cCommitted
                    )
                }

                val activeRecurring = recurring.filter { it.isActive }
                val totalRecurring = com.cardcontrol.app.util.SubscriptionTracker.calculateCommittedFixedCosts(activeRecurring)
                val totalCardsLimit = cards.sumOf { it.totalLimit }
                val limitImpactPercent = com.cardcontrol.app.util.SubscriptionTracker.calculateLimitImpactPercent(totalRecurring, totalCardsLimit)
                val creepAlerts = activeRecurring.count { it.previousAmount != null && it.lastAmount > it.previousAmount }

                DashboardUiState(
                    isInitializing = false,
                    selectedMonth = effectiveDate.monthValue,
                    selectedYear = effectiveDate.year,
                    holders = holders,
                    cards = cards,
                    holderSummaries = holderSummaries,
                    cardSummaries = cardSummaries,
                    totalMonthInvoice = monthInst.sumOf { it.amount },
                    totalMonthPaid = monthPay.sumOf { it.amountPaid },
                    recurringCharges = recurring,
                    totalMonthlyRecurring = totalRecurring,
                    recurringLimitImpactPercent = limitImpactPercent,
                    priceCreepAlertCount = creepAlerts,
                    allInstallments = allInst,
                    allPayments = allPay,
                    allTransactions = allTx
                )
            }
        }
    }
}
