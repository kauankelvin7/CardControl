package com.cardcontrol.app.ui.analytics

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.InstallmentEntity
import com.cardcontrol.app.data.local.PaymentEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.domain.analytics.AnalyticsDashboardState
import com.cardcontrol.app.domain.analytics.AnalyticsPeriod
import com.cardcontrol.app.domain.analytics.FinancialAnalyticsEngine
import com.cardcontrol.app.domain.analytics.SpendingCategory
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcType
import com.cardcontrol.app.ui.theme.LiquidPillSelector
import com.cardcontrol.app.util.MoneyUtils
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun cardCountLabel(count: Int): String =
    "$count ${if (count == 1) "cartão" else "cartões"}"

internal fun holderCountLabel(count: Int): String =
    "$count ${if (count == 1) "pessoa" else "pessoas"}"

internal fun walletCapacityUsagePercent(committedCents: Long, totalLimitCents: Long): Long =
    if (totalLimitCents > 0L) {
        java.math.BigDecimal(committedCents)
            .multiply(java.math.BigDecimal(100))
            .divide(java.math.BigDecimal(totalLimitCents), 0, java.math.RoundingMode.HALF_UP)
            .toLong()
    } else 0L

internal fun purchaseCountLabel(count: Int): String =
    if (count == 1) "1 compra" else "$count compras"

/**
 * Tela de Análises Financeiras Offline (TASK-019).
 * Agregações 100% locais em centavos Long e BigDecimal (HALF_UP), gráficos nativos interativos e filtros sincronizados.
 */
@Composable
fun AnalyticsScreen(
    installments: List<InstallmentEntity>,
    payments: List<PaymentEntity>,
    transactions: List<TransactionEntity>,
    cards: List<CreditCardEntity>,
    holders: List<HolderEntity>,
    recurringCharges: List<RecurringChargeEntity>,
    modifier: Modifier = Modifier,
    initialPeriod: AnalyticsPeriod = AnalyticsPeriod.CURRENT_MONTH,
    initialCardId: Long? = null,
    onCardFilterChange: ((Long?) -> Unit)? = null
) {
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }
    var selectedFilterCardId by remember(initialCardId) { mutableStateOf(initialCardId) }
    var selectedFilterHolderId by remember { mutableStateOf<Long?>(null) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<SpendingCategory?>(null) }
    var isDrilldownExpanded by remember { mutableStateOf(false) }

    val dashboardState = remember(
        installments,
        payments,
        transactions,
        cards,
        holders,
        recurringCharges,
        selectedPeriod,
        selectedFilterCardId,
        selectedFilterHolderId
    ) {
        FinancialAnalyticsEngine.computeDashboard(
            installments = installments,
            payments = payments,
            transactions = transactions,
            cards = cards,
            holders = holders,
            recurringCharges = recurringCharges,
            period = selectedPeriod,
            filterCardId = selectedFilterCardId,
            filterHolderId = selectedFilterHolderId
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Barra Superior de Filtros: Período e Cartão/Titular
        PeriodAndFilterSection(
            selectedPeriod = selectedPeriod,
            onPeriodSelect = {
                selectedPeriod = it
                selectedMonthIndex = null
                selectedCategoryFilter = null
            },
            cards = cards,
            selectedCardId = selectedFilterCardId,
            onCardSelect = {
                selectedFilterCardId = it
                onCardFilterChange?.invoke(it)
            },
            holders = holders,
            selectedHolderId = selectedFilterHolderId,
            onHolderSelect = { selectedFilterHolderId = it }
        )

        // 2. Painel de Capacidade da Carteira sempre visível quando há cartões cadastrados
        if (cards.isNotEmpty()) {
            WalletCapacityOverviewSection(
                cards = cards,
                holders = holders,
                allInstallments = installments,
                allPayments = payments
            )
        }

        // 3. Se não houver despesas no período: Estado vazio limpo e informativo
        if (dashboardState.kpis.grossSpendingCents == 0L && dashboardState.filteredTransactions.isEmpty()) {
            EmptyAnalyticsView(selectedPeriod = selectedPeriod)
        } else {
            // 3. Grade de KPIs Principais
            KpiCardsSection(dashboardState = dashboardState)

            // 4. Tendência Mensal de Gastos (Bar Chart)
            MonthlyTrendSection(
                dashboardState = dashboardState,
                selectedMonthIndex = selectedMonthIndex,
                onMonthSelect = {
                    selectedMonthIndex = if (selectedMonthIndex == it) null else it
                }
            )

            // 5. Distribuição por Categoria (Donut Chart)
            if (dashboardState.categories.isNotEmpty()) {
                CategoryDistributionSection(
                    dashboardState = dashboardState,
                    selectedCategory = selectedCategoryFilter,
                    onCategorySelect = { selectedCategoryFilter = it }
                )
            }

            // 6. Modalidade de Compras (Assinaturas vs Parcelado vs À Vista)
            ModalitySection(dashboardState = dashboardState)

            // 7. Participação por Cartão (apenas se houver mais de um cartão com gastos)
            if (dashboardState.cardShares.size > 1 && selectedFilterCardId == null) {
                CardShareSection(cardShares = dashboardState.cardShares)
            }

            // 8. Maiores Estabelecimentos (Top 5)
            if (dashboardState.topMerchants.isNotEmpty()) {
                TopMerchantsSection(merchants = dashboardState.topMerchants)
            }

            // 9. Lançamentos Reconciliados (Drilldown)
            DrilldownSection(
                dashboardState = dashboardState,
                isExpanded = isDrilldownExpanded,
                onToggleExpand = { isDrilldownExpanded = !isDrilldownExpanded },
                selectedCategory = selectedCategoryFilter,
                recurringCharges = recurringCharges
            )
        }

        Spacer(modifier = Modifier.height(148.dp))
    }
}

@Composable
private fun PeriodAndFilterSection(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelect: (AnalyticsPeriod) -> Unit,
    cards: List<CreditCardEntity>,
    selectedCardId: Long?,
    onCardSelect: (Long?) -> Unit,
    holders: List<HolderEntity>,
    selectedHolderId: Long?,
    onHolderSelect: (Long?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Seletor de Períodos
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsPeriod.entries.chunked(2).forEach { rowPeriods ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPeriods.forEach { period ->
                        val selected = period == selectedPeriod
                        val shape = RoundedCornerShape(10.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(shape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = shape
                                )
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = { onPeriodSelect(period) }
                                )
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.displayName,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Filtro por Cartão ou Titular (se houver dados)
        if (cards.size > 1 || holders.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filtro "Todos os cartões" / Cartão específico
                if (cards.size > 1) {
                    val cardOptions = listOf<Pair<Long?, String>>(null to "Todos os cartões") +
                        cards.map { it.id to it.name }
                    LiquidPillSelector(
                        options = cardOptions,
                        selectedId = selectedCardId,
                        onSelect = onCardSelect
                    )
                }

                // Filtro por Titular
                if (holders.size > 1) {
                    val holderOptions = listOf<Pair<Long?, String>>(null to "Todos titulares") +
                        holders.map { it.id to it.name }
                    LiquidPillSelector(
                        options = holderOptions,
                        selectedId = selectedHolderId,
                        onSelect = onHolderSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCardsSection(dashboardState: AnalyticsDashboardState) {
    val kpis = dashboardState.kpis

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Card Principal: Total Gasto Bruto e Variação PoP
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Gasto",
                        style = CcType.cardMeta.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Badge de Variação PoP (basis points)
                    kpis.periodOverPeriodChangeBps?.let { bps ->
                        val isIncrease = bps > 0L
                        val absPct = (Math.abs(bps) / 100.0)
                        val badgeColor = if (isIncrease) Color(0xFFC62828) else Color(0xFF2E7D32)
                        val icon = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%% vs ant.", absPct),
                                style = CcType.cardMeta.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = badgeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = MoneyUtils.formatCents(kpis.grossSpendingCents),
                    style = CcType.screenHeader.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Linha com 2 cards secundários: Pagamentos e Ticket Médio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 2: Pagamentos
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Pagamentos",
                        style = CcType.cardMeta.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = MoneyUtils.formatCents(kpis.paymentsCents),
                        style = CcType.cardMask.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Card 3: Ticket Médio
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ticket Médio",
                        style = CcType.cardMeta.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = MoneyUtils.formatCents(kpis.averageTicketCents),
                        style = CcType.cardMask.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = purchaseCountLabel(kpis.transactionCount),
                        style = CcType.cardMeta.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendSection(
    dashboardState: AnalyticsDashboardState,
    selectedMonthIndex: Int?,
    onMonthSelect: (Int) -> Unit
) {
    SectionContainer(title = "Tendência Mensal") {
        CcBarChart(
            data = dashboardState.monthlyTrend,
            selectedIndex = selectedMonthIndex,
            onBarSelect = onMonthSelect
        )

        // Se houver um mês selecionado no gráfico de barras, mostra detalhe
        selectedMonthIndex?.let { idx ->
            dashboardState.monthlyTrend.getOrNull(idx)?.let { monthItem ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gasto em ${monthItem.label}:",
                        style = CcType.cardMeta.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = MoneyUtils.formatCents(monthItem.amountCents),
                        style = CcType.cardMeta.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDistributionSection(
    dashboardState: AnalyticsDashboardState,
    selectedCategory: SpendingCategory?,
    onCategorySelect: (SpendingCategory?) -> Unit
) {
    SectionContainer(title = "Distribuição por Categoria") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut Chart nativo
            CcDonutChart(
                categories = dashboardState.categories,
                totalCents = dashboardState.kpis.grossSpendingCents,
                selectedCategory = selectedCategory,
                onCategorySelect = onCategorySelect,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Legenda com valores e porcentagens
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dashboardState.categories.forEach { item ->
                    val isSelected = (selectedCategory == item.category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCategorySelect(if (isSelected) null else item.category) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.category.color)
                            )
                            Text(
                                text = item.category.displayName,
                                style = CcType.cardMeta.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${item.percentageBps / 100}%",
                            style = CcType.cardMeta.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = item.category.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalitySection(dashboardState: AnalyticsDashboardState) {
    SectionContainer(title = "Modalidade de Compra") {
        CcModalityBar(modality = dashboardState.modality)
    }
}

@Composable
private fun CardShareSection(cardShares: List<com.cardcontrol.app.domain.analytics.CardShare>) {
    SectionContainer(title = "Gastos por Cartão") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cardShares.forEach { share ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = share.cardName,
                            style = CcType.buttonLabel.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = share.bankName,
                            style = CcType.cardMeta.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MoneyUtils.formatCents(share.amountCents),
                            style = CcType.cardMeta.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                        text = "${share.percentageBps / 100}% do total",
                            style = CcType.cardMeta.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopMerchantsSection(merchants: List<com.cardcontrol.app.domain.analytics.MerchantRank>) {
    SectionContainer(title = "Maiores Estabelecimentos") {
        CcMerchantRanking(merchants = merchants)
    }
}

@Composable
private fun DrilldownSection(
    dashboardState: AnalyticsDashboardState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedCategory: SpendingCategory?,
    recurringCharges: List<RecurringChargeEntity>
) {
    val itemsToShow = remember(dashboardState.filteredTransactions, selectedCategory, recurringCharges) {
        if (selectedCategory != null) {
            dashboardState.filteredTransactions.filter {
                FinancialAnalyticsEngine.categoryForTransaction(it, recurringCharges) == selectedCategory
            }
        } else {
            dashboardState.filteredTransactions
        }
    }

    SectionContainer(
        title = "Lançamentos do Período (${itemsToShow.size})",
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isExpanded) "Recolher" else "Ver todos",
                    style = CcType.cardMeta.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    ) {
        val displayItems = if (isExpanded) itemsToShow else itemsToShow.take(4)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayItems.forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.merchant,
                            style = CcType.buttonLabel.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val purchaseDate = Instant.ofEpochMilli(transaction.purchaseTimestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        val purchaseMode = when {
                            transaction.isRecurring -> "Recorrente"
                            transaction.installmentsCount > 1 -> "Parcelada em ${transaction.installmentsCount}x"
                            else -> "À vista"
                        }
                        Text(
                            text = "$purchaseMode · ${purchaseDate.format(DateTimeFormatter.ofPattern("dd/MM/yy"))}",
                            style = CcType.cardMeta.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = MoneyUtils.formatCents(transaction.totalAmount),
                        style = CcType.cardMeta.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAnalyticsView(selectedPeriod: AnalyticsPeriod) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nenhuma transação no período",
                style = CcType.buttonLabel.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Compras registradas ou capturadas de notificações para ${selectedPeriod.displayName.lowercase()} aparecerão aqui com gráficos detalhados.",
                style = CcType.cardMeta.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionContainer(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = CcType.buttonLabel.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                trailing?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}


@Composable
private fun WalletCapacityOverviewSection(
    cards: List<CreditCardEntity>,
    holders: List<HolderEntity>,
    allInstallments: List<InstallmentEntity>,
    allPayments: List<PaymentEntity>
) {
    val totalLimitCents = cards.sumOf { it.totalLimit }
    val totalCommittedCents = (allInstallments.sumOf { it.amount } - allPayments.sumOf { it.amountPaid }).coerceAtLeast(0L)
    val availableCents = (totalLimitCents - totalCommittedCents).coerceAtLeast(0L)
    val usedPct = walletCapacityUsagePercent(totalCommittedCents, totalLimitCents)
    val visualUsedRatio = if (totalLimitCents > 0L) {
        java.math.BigDecimal(totalCommittedCents)
            .divide(java.math.BigDecimal(totalLimitCents), 4, java.math.RoundingMode.HALF_UP)
            .toFloat()
            .coerceIn(0f, 1f)
    } else 0f

    Surface(
        shape = CcShapes.panel,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CcShapes.panel)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CAPACIDADE DA CARTEIRA",
                    style = CcType.cardMeta.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$usedPct% em uso",
                        style = CcType.cardMeta.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = MoneyUtils.formatCents(availableCents),
                    style = CcType.screenHeader.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Limite livre somado em ${cardCountLabel(cards.size)} · Total ${MoneyUtils.formatCents(totalLimitCents)}",
                    style = CcType.cardMeta.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                if (visualUsedRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(visualUsedRatio)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = cardCountLabel(cards.size),
                                style = CcType.buttonLabel.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = cards.firstOrNull()?.name ?: "Ativo",
                                style = CcType.cardMeta.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = holderCountLabel(holders.size),
                                style = CcType.buttonLabel.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = holders.firstOrNull()?.name ?: "Sem titular",
                                style = CcType.cardMeta.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
