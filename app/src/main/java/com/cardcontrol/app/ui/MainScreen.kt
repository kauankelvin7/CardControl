package com.cardcontrol.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import com.cardcontrol.app.data.local.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.components.RecurringChargesCard
import com.cardcontrol.app.ui.dialogs.CardDialog
import com.cardcontrol.app.ui.dialogs.DeleteCardDialog
import com.cardcontrol.app.ui.dialogs.ExpenseDialog
import com.cardcontrol.app.ui.dialogs.HolderDialog
import com.cardcontrol.app.ui.dialogs.PaymentDialog
import com.cardcontrol.app.ui.theme.*
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.util.ReportEngine
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenNotificationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    MainScreenContent(
        state = state,
        onOpenNotificationSettings = onOpenNotificationSettings,
        onChangeMonth = { offset -> viewModel.changeMonth(offset) },
        onToggleRecurringCharge = { id, active -> viewModel.setRecurringChargeActive(id, active) },
        onMarkTransactionRecurring = { txId, isRec -> viewModel.markTransactionRecurring(txId, isRec) },
        onAddExpense = { cardId, holderId, merchant, amount, parcels ->
            viewModel.addExpense(cardId, holderId, merchant, amount, parcels)
        },
        onAddPayment = { holderId, cardId, amount, notes ->
            viewModel.addPayment(holderId, cardId, amount, notes)
        },
        onAddHolder = { name, whats, email, subLimit ->
            viewModel.addHolder(name, whats, email, subLimit)
        },
        onDeleteCard = { cardId -> viewModel.deleteCard(cardId) },
        onAddCard = { name, bank, digits, limit, closing, due, holderId ->
            viewModel.addCard(name, bank, digits, limit, closing, due, holderId)
        },
        onUpdateCard = { id, name, bank, digits, limit, closing, due, holderId ->
            viewModel.updateCard(id, name, bank, digits, limit, closing, due, holderId)
        },
        initialSmtpEmail = viewModel.credentialStore.getSenderEmail(),
        initialSmtpPass = viewModel.credentialStore.getAppPassword(),
        onSaveSmtpCredentials = { email, pass ->
            viewModel.credentialStore.saveSmtpCredentials(email, pass)
        },
        onShareHolderInvoice = { holder, installments, paidInMonth, globalCommitted ->
            ReportEngine.sharePdfAndTextViaWhatsApp(
                context = context,
                holder = holder,
                month = state.selectedMonth,
                year = state.selectedYear,
                installments = installments,
                paidInMonth = paidInMonth,
                globalCommitted = globalCommitted
            )
        },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    state: DashboardUiState,
    initialTab: Int = 0,
    initialExpandedHolderId: Long? = null,
    onOpenNotificationSettings: () -> Unit = {},
    onChangeMonth: (Long) -> Unit = {},
    onToggleRecurringCharge: (Long, Boolean) -> Unit = { _, _ -> },
    onMarkTransactionRecurring: (Long, Boolean) -> Unit = { _, _ -> },
    onAddExpense: (cardId: Long, holderId: Long, merchant: String, amountCents: Long, parcels: Int) -> Unit = { _, _, _, _, _ -> },
    onAddPayment: (holderId: Long, cardId: Long?, amountCents: Long, notes: String) -> Unit = { _, _, _, _ -> },
    onAddHolder: (name: String, whats: String, email: String, subLimitCents: Long) -> Unit = { _, _, _, _ -> },
    onDeleteCard: (cardId: Long) -> Unit = {},
    onAddCard: (name: String, bank: String, digits: String, limitCents: Long, closingDay: Int, dueDay: Int, holderId: Long?) -> Unit = { _, _, _, _, _, _, _ -> },
    onUpdateCard: (id: Long, name: String, bank: String, digits: String, limitCents: Long, closingDay: Int, dueDay: Int, holderId: Long?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    initialSmtpEmail: String = "",
    initialSmtpPass: String = "",
    onSaveSmtpCredentials: ((String, String) -> Unit)? = null,
    onShareHolderInvoice: ((HolderEntity, List<InstallmentEntity>, Long, Long) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var expandedHolderId by remember { mutableStateOf<Long?>(initialExpandedHolderId) }
    var cardBeingEdited by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardPendingDeletion by remember { mutableStateOf<CreditCardEntity?>(null) }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showHolderDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            snackbarHostState.showSnackbar("Credenciais protegidas no Android Keystore com sucesso!")
            showSavedMessage = false
        }
    }

    LiquidAmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .liquidGlassCard(cornerRadius = 18.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CardControl",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        AnimatedVisibility(visible = selectedTab == 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onChangeMonth(-1L) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ChevronLeft,
                                        contentDescription = "Mês anterior",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    "%02d/%d".format(state.selectedMonth, state.selectedYear),
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFeatureSettings = "tnum",
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                IconButton(
                                    onClick = { onChangeMonth(1L) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Próximo mês",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                val navItemColors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryAccent.copy(alpha = 0.15f),
                    selectedIconColor = PrimaryAccent,
                    selectedTextColor = PrimaryAccent,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
                NavigationBar(
                    containerColor = AppBackgroundTop.copy(alpha = 0.95f),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Faturas") },
                        label = { Text("Faturas") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Cartões") },
                        label = { Text("Cartões") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Automação") },
                        label = { Text("Automação") },
                        colors = navItemColors
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 0 && state.cards.isNotEmpty() && state.holders.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showExpenseDialog = true },
                        containerColor = PrimaryAccent,
                        contentColor = AppBackground,
                        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = AppBackground) },
                        text = { Text("Lançar Compra", fontWeight = FontWeight.Bold, color = AppBackground) }
                    )
                } else if (selectedTab == 1) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            cardBeingEdited = null
                            showCardDialog = true
                        },
                        containerColor = PrimaryAccent,
                        contentColor = AppBackground,
                        icon = { Icon(Icons.Default.AddCard, contentDescription = null, tint = AppBackground) },
                        text = { Text("Adicionar cartão", fontWeight = FontWeight.Bold, color = AppBackground) }
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
                when (selectedTab) {
                    0 -> {
                        val remainingInvoice = (state.totalMonthInvoice - state.totalMonthPaid).coerceAtLeast(0L)
                        val paidRatio = if (state.totalMonthInvoice > 0L) (state.totalMonthPaid.toFloat() / state.totalMonthInvoice.toFloat()) else 0f
                        val paidPercent = (paidRatio * 100f).toInt()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassCard(cornerRadius = 24.dp, accentTint = PrimaryAccent)
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OverlineText("Fatura Consolidada do Mês")
                                CurrencyText(
                                    amountCents = remainingInvoice,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Bruto: ${ReportEngine.formatCurrency(state.totalMonthInvoice)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                        color = TextSecondary
                                    )
                                    Text(
                                        "Abatido: ${ReportEngine.formatCurrency(state.totalMonthPaid)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                        color = SemanticSuccess
                                    )
                                }
                                LiquidGlassBar(progress = paidRatio, accentColor = SemanticSuccess)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (state.totalMonthInvoice > 0L) "$paidPercent% quitado" else "Sem fatura pendente",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        color = TextSecondary
                                    )
                                    if (state.totalMonthPaid > 0L) {
                                        Text(
                                            "Economia / Saldo positivo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SemanticSuccess
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showHolderDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryAccent)
                                Spacer(Modifier.width(6.dp))
                                Text("Nova Pessoa", fontWeight = FontWeight.SemiBold)
                            }
                            if (state.holders.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { showPaymentDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = SemanticSuccess)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Abater / Pix", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))

                        if (state.holders.isEmpty() || state.cards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassCard(cornerRadius = 20.dp, accentTint = PrimaryAccent)
                                    .padding(22.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = PrimaryAccent, modifier = Modifier.size(42.dp))
                                    Text("Primeiros Passos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        "Cadastre pelo menos uma Pessoa e um Cartão (na aba Cartões) para começar a lançar compras, controlar sublimites e visualizar as faturas em tempo real.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
                                item {
                                    RecurringChargesCard(
                                        recurringCharges = state.recurringCharges,
                                        totalMonthlyRecurring = state.totalMonthlyRecurring,
                                        limitImpactPercent = state.recurringLimitImpactPercent,
                                        priceCreepAlertCount = state.priceCreepAlertCount,
                                        onToggleActive = { id, active -> onToggleRecurringCharge(id, active) }
                                    )
                                }
                                items(state.holderSummaries, key = { it.holder.id }) { item ->
                                    val isExpanded = expandedHolderId == item.holder.id
                                    val usageRatio = if (item.holder.subLimit > 0L) (item.globalCommitted.toFloat() / item.holder.subLimit.toFloat()) else 0f
                                    val barColor = when {
                                        usageRatio >= 1f -> SemanticDanger
                                        usageRatio >= 0.8f -> SemanticWarning
                                        else -> PrimaryAccent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(cornerRadius = 20.dp, accentTint = barColor)
                                            .clickable { expandedHolderId = if (isExpanded) null else item.holder.id }
                                            .padding(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(item.holder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text("A Pagar no Mês: ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                        CurrencyText(
                                                            amountCents = item.monthlyNetDue,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = barColor
                                                        )
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = {
                                                        if (onShareHolderInvoice != null) {
                                                            onShareHolderInvoice(
                                                                item.holder,
                                                                item.monthlyInstallments,
                                                                item.monthlyPaid,
                                                                item.globalCommitted
                                                            )
                                                        } else {
                                                            ReportEngine.sharePdfAndTextViaWhatsApp(
                                                                context = context,
                                                                holder = item.holder,
                                                                month = state.selectedMonth,
                                                                year = state.selectedYear,
                                                                installments = item.monthlyInstallments,
                                                                paidInMonth = item.monthlyPaid,
                                                                globalCommitted = item.globalCommitted
                                                            )
                                                        }
                                                    }) { Icon(Icons.Default.Share, contentDescription = "Exportar PDF/WhatsApp", tint = PrimaryAccent) }
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = TextSecondary,
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }
                                            }

                                            if (item.holder.subLimit > 0L) {
                                                val percentSubLimit = (usageRatio * 100f).toInt()
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Sublimite: $percentSubLimit% usado", style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"), color = TextSecondary)
                                                    Text("Teto: ${ReportEngine.formatCurrency(item.holder.subLimit)}", style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"), color = TextSecondary)
                                                }
                                                LiquidGlassBar(progress = usageRatio, accentColor = barColor)
                                            }

                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                                    if (item.monthlyInstallments.isEmpty()) {
                                                        Text("Nenhuma parcela neste mês.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                    } else {
                                                        item.monthlyInstallments.forEach { inst ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                    modifier = Modifier.weight(1f, fill = false)
                                                                ) {
                                                                    if (inst.totalInstallments == 1) {
                                                                        IconButton(
                                                                            onClick = { onMarkTransactionRecurring(inst.transactionId, true) },
                                                                            modifier = Modifier.size(24.dp)
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Default.Repeat,
                                                                                contentDescription = "Confirmar como Recorrente",
                                                                                tint = PrimaryAccent.copy(alpha = 0.75f),
                                                                                modifier = Modifier.size(16.dp)
                                                                            )
                                                                        }
                                                                    } else {
                                                                        Text(
                                                                            "Parcelamento",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = TextSecondary
                                                                        )
                                                                    }
                                                                    Text("• ${inst.merchantSnapshot} (${inst.installmentNumber}/${inst.totalInstallments})", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                                                }
                                                                CurrencyText(
                                                                    amountCents = inst.amount,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = PrimaryAccent
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Seus cartões",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    if (state.cards.size == 1) "1 cartão cadastrado"
                                    else "${state.cards.size} cartões cadastrados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            if (state.cards.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .liquidGlassCard(cornerRadius = 22.dp, accentTint = SecondaryAccent)
                                        .padding(22.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                                    ) {
                                        Icon(Icons.Default.AddCard, contentDescription = null, tint = SecondaryAccent, modifier = Modifier.size(42.dp))
                                        Text("Nenhum cartão por aqui", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            "Cadastre um cartão, escolha a pessoa responsável e acompanhe o limite disponível nesta tela.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                cardBeingEdited = null
                                                showCardDialog = true
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryAccent)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Cadastrar cartão", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 104.dp)
                                ) {
                                    items(state.cardSummaries, key = { it.card.id }) { item ->
                                        val ratio = if (item.card.totalLimit > 0L) {
                                            (item.globalCommitted.toFloat() / item.card.totalLimit.toFloat()).coerceIn(0f, 1f)
                                        } else 0f
                                        val linkedHolder = state.holders.firstOrNull { it.id == item.card.defaultHolderId }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .liquidGlassCard(cornerRadius = 22.dp, accentTint = SecondaryAccent)
                                                .padding(18.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val bankInfo = SupportedBank.fromId(item.card.bank)
                                                    val displayTitle = if (item.card.name.isNotBlank()) item.card.name else bankInfo.displayName
                                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            displayTitle,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )
                                                        Text(
                                                            "•••• ${item.card.lastFourDigits}",
                                                            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                                                            color = TextSecondary
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            cardBeingEdited = item.card
                                                            showCardDialog = true
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Editar cartão ${item.card.name}", tint = TextSecondary)
                                                    }
                                                    IconButton(
                                                        onClick = { cardPendingDeletion = item.card }
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Excluir cartão ${item.card.name}", tint = TextSecondary)
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.White.copy(alpha = 0.07f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = if (linkedHolder == null) SemanticWarning else PrimaryAccent,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            linkedHolder?.name?.let { "Pessoa vinculada: $it" } ?: "Nenhuma pessoa vinculada",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (linkedHolder == null) SemanticWarning else TextSecondary
                                                        )
                                                    }
                                                }

                                                Text(
                                                    "Fecha dia ${item.card.closingDay} • Vence dia ${item.card.dueDay}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                                    color = TextSecondary
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Disponível", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                                    CurrencyText(
                                                        amountCents = item.availableLimit,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SemanticSuccess
                                                    )
                                                }

                                                val percentUsed = (ratio * 100f).toInt()
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "Utilizado: $percentUsed%",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                        color = TextSecondary
                                                    )
                                                    Text(
                                                        "Limite total: ${ReportEngine.formatCurrency(item.card.totalLimit)}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                        color = TextSecondary
                                                    )
                                                }
                                                val barColor = if (ratio >= 0.9f) SemanticDanger else if (ratio >= 0.75f) SemanticWarning else SecondaryAccent
                                                LiquidGlassBar(progress = ratio, accentColor = barColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        var email by remember { mutableStateOf(initialSmtpEmail) }
                        var pass by remember { mutableStateOf(initialSmtpPass) }
                        var showPassword by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Automação & Notificações",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Captura em tempo real e cofre de credenciais",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassCard(cornerRadius = 22.dp, accentTint = SecondaryAccent)
                                    .padding(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        "Captura em Tempo Real & Cofre Keystore",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "O CardControl escuta compras emitidas pelos apps bancários e pode enviar comprovantes automáticos por e-mail com o saldo restante do titular.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Button(
                                        onClick = onOpenNotificationSettings,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SecondaryAccent,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Conceder Permissão às Notificações", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 4.dp))

                                    OverlineText("Configuração de Alerta por E-mail (Opcional)", color = PrimaryAccent)

                                    GlassTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = "Gmail Remetente",
                                        placeholder = "seuemail@gmail.com",
                                        supportingText = "Envio automático e seguro via SMTP TLS",
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryAccent) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                                    )

                                    GlassTextField(
                                        value = pass,
                                        onValueChange = { pass = it },
                                        label = "Senha de App Google",
                                        placeholder = "xxxx xxxx xxxx xxxx",
                                        supportingText = "Código de 16 caracteres gerado na sua Conta Google",
                                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = PrimaryAccent) },
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showPassword) "Ocultar senha" else "Ver senha",
                                                    tint = TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                                    )

                                    Button(
                                        onClick = {
                                            onSaveSmtpCredentials?.invoke(email, pass)
                                            showSavedMessage = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryAccent,
                                            contentColor = AppBackground
                                        )
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = AppBackground)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Criptografar no Android Keystore", color = AppBackground, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "CardControl • Motor Offline • Liquid Glass",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 96.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog && state.cards.isNotEmpty() && state.holders.isNotEmpty()) {
        ExpenseDialog(
            cards = state.cards,
            holders = state.holders,
            onDismissRequest = { showExpenseDialog = false },
            onConfirm = { cardId, holderId, merchant, amountCents, parcels ->
                onAddExpense(cardId, holderId, merchant, amountCents, parcels)
                showExpenseDialog = false
            }
        )
    }


    if (showPaymentDialog && state.holders.isNotEmpty()) {
        PaymentDialog(
            holders = state.holders,
            cards = state.cards,
            onDismissRequest = { showPaymentDialog = false },
            onConfirm = { holderId, cardId, amountCents, notes ->
                onAddPayment(holderId, cardId, amountCents, notes)
                showPaymentDialog = false
            }
        )
    }

    if (showHolderDialog) {
        HolderDialog(
            onDismissRequest = { showHolderDialog = false },
            onConfirm = { name, whats, email, subLimitCents ->
                onAddHolder(name, whats, email, subLimitCents)
                showHolderDialog = false
            }
        )
    }

    cardPendingDeletion?.let { pendingCard ->
        DeleteCardDialog(
            card = pendingCard,
            onDismissRequest = { cardPendingDeletion = null },
            onConfirm = { cardId ->
                onDeleteCard(cardId)
                cardPendingDeletion = null
            }
        )
    }


    if (showCardDialog) {
        CardDialog(
            cardToEdit = cardBeingEdited,
            holders = state.holders,
            onDismissRequest = {
                showCardDialog = false
                cardBeingEdited = null
            },
            onAddCard = { name, bank, digits, limitCents, closingDay, dueDay, holderId ->
                onAddCard(name, bank, digits, limitCents, closingDay, dueDay, holderId)
                showCardDialog = false
                cardBeingEdited = null
            },
            onUpdateCard = { id, name, bank, digits, limitCents, closingDay, dueDay, holderId ->
                onUpdateCard(id, name, bank, digits, limitCents, closingDay, dueDay, holderId)
                showCardDialog = false
                cardBeingEdited = null
            }
        )
    }
}

// ==========================================
// COMPOSE PREVIEWS & DADOS SIMULADOS DE DESIGN
// ==========================================

private object PreviewMockData {
    val holders = listOf(
        HolderEntity(
            id = 1L,
            name = "Carlos Silva",
            whatsapp = "11988887777",
            email = "carlos.silva@exemplo.com",
            subLimit = 250000L // R$ 2.500,00
        ),
        HolderEntity(
            id = 2L,
            name = "Ana Paula",
            whatsapp = "11977776666",
            email = "ana.paula@exemplo.com",
            subLimit = 150000L // R$ 1.500,00
        )
    )

    val cards = listOf(
        CreditCardEntity(
            id = 1L,
            name = "Nubank Ultravioleta",
            bank = SupportedBank.NUBANK.id,
            lastFourDigits = "4821",
            totalLimit = 1000000L, // R$ 10.000,00
            closingDay = 10,
            dueDay = 17,
            defaultHolderId = 1L
        ),
        CreditCardEntity(
            id = 2L,
            name = "Itaú Personnalité",
            bank = SupportedBank.ITAU.id,
            lastFourDigits = "9012",
            totalLimit = 800000L, // R$ 8.000,00
            closingDay = 20,
            dueDay = 27,
            defaultHolderId = 2L
        )
    )

    val installmentsCarlos = listOf(
        InstallmentEntity(
            id = 1L,
            transactionId = 101L,
            cardId = 1L,
            holderId = 1L,
            merchantSnapshot = "Supermercado Pão de Açúcar",
            installmentNumber = 1,
            totalInstallments = 1,
            amount = 38500L, // R$ 385,00
            refMonth = 10,
            refYear = 2026,
            purchaseTimestamp = 1729080000000L
        ),
        InstallmentEntity(
            id = 2L,
            transactionId = 102L,
            cardId = 1L,
            holderId = 1L,
            merchantSnapshot = "Apple Store Brasil",
            installmentNumber = 3,
            totalInstallments = 10,
            amount = 45990L, // R$ 459,90
            refMonth = 10,
            refYear = 2026,
            purchaseTimestamp = 1729080000000L
        )
    )

    val installmentsAna = listOf(
        InstallmentEntity(
            id = 3L,
            transactionId = 103L,
            cardId = 2L,
            holderId = 2L,
            merchantSnapshot = "Farmácia Droga Raia",
            installmentNumber = 1,
            totalInstallments = 1,
            amount = 12450L, // R$ 124,50
            refMonth = 10,
            refYear = 2026,
            purchaseTimestamp = 1729080000000L
        )
    )

    val recurring = listOf(
        RecurringChargeEntity(
            id = 1L,
            cardId = 1L,
            holderId = 1L,
            merchant = "Netflix",
            expectedAmount = 5990L,
            lastAmount = 5990L, // R$ 59,90
            previousAmount = 4590L, // R$ 45,90 (alta de preço detectada!)
            billingCycleDays = 30,
            isActive = true
        ),
        RecurringChargeEntity(
            id = 2L,
            cardId = 2L,
            holderId = 2L,
            merchant = "Spotify Family",
            expectedAmount = 3490L,
            lastAmount = 3490L, // R$ 34,90
            previousAmount = null,
            billingCycleDays = 30,
            isActive = true
        )
    )

    val sampleState = DashboardUiState(
        selectedMonth = 10,
        selectedYear = 2026,
        holders = holders,
        cards = cards,
        holderSummaries = listOf(
            HolderSummaryUi(
                holder = holders[0],
                monthlyInstallments = installmentsCarlos,
                monthlySpent = 84490L,
                monthlyPaid = 0L,
                monthlyNetDue = 84490L,
                globalCommitted = 350000L,
                availableSubLimit = -100000L
            ),
            HolderSummaryUi(
                holder = holders[1],
                monthlyInstallments = installmentsAna,
                monthlySpent = 12450L,
                monthlyPaid = 0L,
                monthlyNetDue = 12450L,
                globalCommitted = 45000L,
                availableSubLimit = 105000L
            )
        ),
        cardSummaries = listOf(
            CardSummaryUi(
                card = cards[0],
                globalCommitted = 450000L,
                availableLimit = 550000L
            ),
            CardSummaryUi(
                card = cards[1],
                globalCommitted = 120000L,
                availableLimit = 680000L
            )
        ),
        totalMonthInvoice = 96940L,
        totalMonthPaid = 0L,
        recurringCharges = recurring,
        totalMonthlyRecurring = 9480L,
        recurringLimitImpactPercent = 5.2,
        priceCreepAlertCount = 1
    )

    val emptyState = DashboardUiState(
        selectedMonth = 10,
        selectedYear = 2026,
        holders = emptyList(),
        cards = emptyList(),
        holderSummaries = emptyList(),
        cardSummaries = emptyList(),
        totalMonthInvoice = 0L,
        totalMonthPaid = 0L,
        recurringCharges = emptyList(),
        totalMonthlyRecurring = 0L,
        recurringLimitImpactPercent = 0.0,
        priceCreepAlertCount = 0
    )
}

@Preview(name = "1. Tela Principal - Faturas do Mês", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun MainScreenInvoicesTabPreview() {
    CardControlTheme {
        MainScreenContent(
            state = PreviewMockData.sampleState,
            initialTab = 0,
            initialExpandedHolderId = 1L
        )
    }
}

@Preview(name = "2. Tela Principal - Cartões Cadastrados", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun MainScreenCardsTabPreview() {
    CardControlTheme {
        MainScreenContent(
            state = PreviewMockData.sampleState,
            initialTab = 1
        )
    }
}

@Preview(name = "3. Tela Principal - Notificações & Cofre", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun MainScreenSettingsTabPreview() {
    CardControlTheme {
        MainScreenContent(
            state = PreviewMockData.sampleState,
            initialTab = 2,
            initialSmtpEmail = "usuario@gmail.com"
        )
    }
}

@Preview(name = "4. Tela Principal - Estado Vazio (Primeiros Passos)", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun MainScreenEmptyStatePreview() {
    CardControlTheme {
        MainScreenContent(
            state = PreviewMockData.emptyState,
            initialTab = 0
        )
    }
}

@Preview(name = "5. Card de Assinaturas e Recorrências", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun RecurringChargesCardPreview() {
    CardControlTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RecurringChargesCard(
                recurringCharges = PreviewMockData.recurring,
                totalMonthlyRecurring = 9480L,
                limitImpactPercent = 5.2,
                priceCreepAlertCount = 1,
                onToggleActive = { _, _ -> }
            )
        }
    }
}

@Preview(name = "6. Campo de Texto Liquid Glass", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun GlassTextFieldPreview() {
    CardControlTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlassTextField(
                value = "Netflix",
                onValueChange = {},
                label = "Estabelecimento",
                placeholder = "Ex: Supermercado",
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryAccent) }
            )
        }
    }
}
