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
import com.cardcontrol.app.data.local.RecurringChargeEntity
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.SupportedBank
import com.cardcontrol.app.ui.theme.*
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.util.ReportEngine
import kotlinx.coroutines.flow.collect

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = TextSecondary.copy(alpha = 0.5f)) } },
        supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = TextSecondary)) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth().heightIn(min = 58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
            focusedLabelColor = PrimaryAccent,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = AppBackground.copy(alpha = 0.7f),
            unfocusedContainerColor = AppBackground.copy(alpha = 0.45f),
            cursorColor = PrimaryAccent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenNotificationSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedHolderId by remember { mutableStateOf<Long?>(null) }
    var cardBeingEdited by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardPendingDeletion by remember { mutableStateOf<CreditCardEntity?>(null) }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showHolderDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.uiMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

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
                                    onClick = { viewModel.changeMonth(-1) },
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
                                    onClick = { viewModel.changeMonth(1) },
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
                                        onToggleActive = { id, active -> viewModel.setRecurringChargeActive(id, active) }
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
                                                        ReportEngine.sharePdfAndTextViaWhatsApp(
                                                            context = context,
                                                            holder = item.holder,
                                                            month = state.selectedMonth,
                                                            year = state.selectedYear,
                                                            installments = item.monthlyInstallments,
                                                            paidInMonth = item.monthlyPaid,
                                                            globalCommitted = item.globalCommitted
                                                        )
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
                                                                            onClick = { viewModel.markTransactionRecurring(inst.transactionId, true) },
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
                        var email by remember { mutableStateOf(viewModel.credentialStore.getSenderEmail()) }
                        var pass by remember { mutableStateOf(viewModel.credentialStore.getAppPassword()) }
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
                                            viewModel.credentialStore.saveSmtpCredentials(email, pass)
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
        var merchant by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var parcels by remember { mutableStateOf("1") }
        var selectedCardId by remember { mutableLongStateOf(state.cards.first().id) }
        var selectedHolderId by remember {
            mutableLongStateOf(
                state.cards.first().defaultHolderId
                    ?.takeIf { holderId -> state.holders.any { it.id == holderId } }
                    ?: state.holders.first().id
            )
        }

        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            containerColor = AppBackgroundTop,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorderTopLeft, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddShoppingCart, null, tint = PrimaryAccent)
                    Spacer(Modifier.width(8.dp))
                    Text("Lançar Compra", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cartão Utilizado:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(
                            options = state.cards.map { it.id to "${it.name} (${it.lastFourDigits})" },
                            selectedId = selectedCardId,
                            onSelect = { cardId ->
                                selectedCardId = cardId
                                selectedHolderId = state.cards.find { it.id == cardId }?.defaultHolderId
                                    ?.takeIf { holderId -> state.holders.any { it.id == holderId } }
                                    ?: state.holders.first().id
                            }
                        )
                    }
                    Text("Responsável pelo Gasto:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(
                            options = state.holders.map { it.id to it.name },
                            selectedId = selectedHolderId,
                            onSelect = { selectedHolderId = it }
                        )
                    }
                    GlassTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = "Estabelecimento",
                        placeholder = "Ex: Mercado Livre, Posto...",
                        leadingIcon = { Icon(Icons.Default.Storefront, null, tint = PrimaryAccent) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Valor Total (R$)",
                        placeholder = "0,00",
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = SemanticSuccess) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = parcels,
                        onValueChange = { parcels = it },
                        label = "Parcelas (1 a 72)",
                        placeholder = "1",
                        leadingIcon = { Icon(Icons.Default.Layers, null, tint = SecondaryAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = MoneyUtils.parseToCents(amount) ?: 0L
                        if (merchant.isNotBlank() && v > 0L) {
                            viewModel.addExpense(selectedCardId, selectedHolderId, merchant, v, (parcels.toIntOrNull() ?: 1).coerceIn(1, 72))
                            showExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = AppBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar", color = AppBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPaymentDialog && state.holders.isNotEmpty()) {
        var amount by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var selectedHolderId by remember { mutableLongStateOf(state.holders.first().id) }
        var selectedCardId by remember { mutableStateOf(state.cards.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            containerColor = AppBackgroundTop,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorderTopLeft, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, null, tint = SemanticSuccess)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar Abatimento / Pix", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quem Pagou:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(options = state.holders.map { it.id to it.name }, selectedId = selectedHolderId, onSelect = { selectedHolderId = it })
                    }
                    if (state.cards.isNotEmpty()) {
                        Text("Liberar Limite do Cartão:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            LiquidPillSelector(options = state.cards.map { it.id to it.name }, selectedId = selectedCardId, onSelect = { selectedCardId = it })
                        }
                    }
                    GlassTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Valor Recebido (R$)",
                        placeholder = "0,00",
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = SemanticSuccess) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Observação",
                        placeholder = "Ex: Pix fatura adiantada",
                        leadingIcon = { Icon(Icons.Default.EditNote, null, tint = PrimaryAccent) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = MoneyUtils.parseToCents(amount) ?: 0L
                        if (v > 0L) {
                            viewModel.addPayment(selectedHolderId, selectedCardId, v, notes)
                            showPaymentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess, contentColor = AppBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Abater", color = AppBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showHolderDialog) {
        var name by remember { mutableStateOf("") }
        var whats by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var subLimit by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showHolderDialog = false },
            containerColor = AppBackgroundTop,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorderTopLeft, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, tint = PrimaryAccent)
                    Spacer(Modifier.width(8.dp))
                    Text("Novo Titular / Dependente", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome Completo",
                        placeholder = "Ex: Carlos Silva",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryAccent) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = whats,
                        onValueChange = { whats = it },
                        label = "WhatsApp com DDD",
                        placeholder = "Ex: 5511999998888",
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = SemanticSuccess) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-mail para Alertas Instantâneos",
                        placeholder = "carlos@gmail.com",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SecondaryAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = subLimit,
                        onValueChange = { subLimit = it },
                        label = "Sublimite Mensal (R$)",
                        placeholder = "0,00 (0 para sem teto)",
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = SemanticWarning) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showHolderDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val subLimitCents = MoneyUtils.parseToCents(subLimit) ?: 0L
                            viewModel.addHolder(name, whats, email, subLimitCents)
                            showHolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = AppBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar", color = AppBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (cardPendingDeletion != null) {
        val pendingCard = cardPendingDeletion!!
        AlertDialog(
            onDismissRequest = { cardPendingDeletion = null },
            containerColor = AppBackgroundTop,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorderTopLeft, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SemanticDanger)
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir cartão?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "O cartão \"${pendingCard.name}\" só será excluído se não tiver compras, parcelas ou pagamentos vinculados. Assim, o histórico financeiro não será apagado por engano.",
                    color = TextSecondary
                )
            },
            dismissButton = {
                TextButton(onClick = { cardPendingDeletion = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCard(pendingCard.id)
                        cardPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticDanger, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Excluir cartão", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showCardDialog) {
        val cardToEdit = cardBeingEdited
        var selectedBank by remember(cardToEdit?.id) {
            mutableStateOf(if (cardToEdit != null) SupportedBank.fromId(cardToEdit.bank) else SupportedBank.NUBANK)
        }
        var name by remember(cardToEdit?.id) {
            mutableStateOf(cardToEdit?.name ?: SupportedBank.NUBANK.displayName)
        }
        var lastFour by remember(cardToEdit?.id) { mutableStateOf(cardToEdit?.lastFourDigits ?: "") }
        var limit by remember(cardToEdit?.id) {
            mutableStateOf(cardToEdit?.totalLimit?.let { MoneyUtils.formatDecimal(it) } ?: "")
        }
        var closing by remember(cardToEdit?.id) { mutableStateOf(cardToEdit?.closingDay?.toString() ?: "10") }
        var due by remember(cardToEdit?.id) { mutableStateOf(cardToEdit?.dueDay?.toString() ?: "17") }
        var defaultHolderId by remember(cardToEdit?.id) {
            mutableStateOf(if (cardToEdit != null) cardToEdit.defaultHolderId else state.holders.firstOrNull()?.id)
        }
        var formError by remember(cardToEdit?.id) { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showCardDialog = false
                cardBeingEdited = null
            },
            containerColor = AppBackgroundTop,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorderTopLeft, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, null, tint = SecondaryAccent)
                    Spacer(Modifier.width(8.dp))
                    Text(if (cardToEdit == null) "Novo cartão" else "Editar cartão", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OverlineText("Banco emissor", color = PrimaryAccent)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(
                            options = SupportedBank.entries.map { it.id to it.displayName },
                            selectedId = selectedBank.id,
                            onSelect = { bankId ->
                                val previousBank = selectedBank
                                val newBank = SupportedBank.fromId(bankId)
                                selectedBank = newBank
                                if (cardToEdit == null && (name.isBlank() || name == previousBank.displayName)) {
                                    name = if (newBank != SupportedBank.OTHER) newBank.displayName else ""
                                }
                                formError = null
                            }
                        )
                    }
                    Text(
                        "Identifica a origem das notificações bancárias para captura automática.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OverlineText("Identificação do cartão", color = PrimaryAccent)
                    GlassTextField(
                        value = name,
                        onValueChange = { name = it; formError = null },
                        label = "Apelido do cartão",
                        placeholder = "Ex.: Meu Cartão Principal",
                        leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = SecondaryAccent) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Text(
                        "Você pode dar o apelido que preferir. A captura automática continuará funcionando.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    GlassTextField(
                        value = lastFour,
                        onValueChange = { value -> lastFour = value.filter { it.isDigit() }.takeLast(4); formError = null },
                        label = "Últimos 4 dígitos",
                        placeholder = "Ex.: 0359",
                        leadingIcon = { Icon(Icons.Default.Pin, null, tint = PrimaryAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    GlassTextField(
                        value = limit,
                        onValueChange = { limit = it; formError = null },
                        label = "Limite total (R$)",
                        placeholder = "Ex.: 5000,00",
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = SemanticSuccess) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(
                            value = closing,
                            onValueChange = { closing = it; formError = null },
                            label = "Fechamento (1–28)",
                            placeholder = "10",
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        GlassTextField(
                            value = due,
                            onValueChange = { due = it; formError = null },
                            label = "Vencimento (1–28)",
                            placeholder = "17",
                            leadingIcon = { Icon(Icons.Default.Event, null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 4.dp))
                    OverlineText("Pessoa responsável", color = PrimaryAccent)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(
                            options = listOf(0L to "Sem vínculo") + state.holders.map { it.id to it.name },
                            selectedId = defaultHolderId ?: 0L,
                            onSelect = { selected -> defaultHolderId = selected.takeIf { it != 0L }; formError = null }
                        )
                    }
                    Text(
                        "Essa pessoa será usada nas compras capturadas automaticamente. Os lançamentos antigos não mudam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (formError != null) {
                        Text(formError!!, style = MaterialTheme.typography.bodySmall, color = SemanticDanger)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCardDialog = false
                        cardBeingEdited = null
                    }
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val digits = lastFour.filter { it.isDigit() }.takeLast(4)
                        val limitCents = MoneyUtils.parseToCents(limit)
                        val closingDay = closing.toIntOrNull()
                        val dueDay = due.toIntOrNull()
                        if (
                            name.isBlank() ||
                            digits.length != 4 ||
                            limitCents == null ||
                            closingDay == null || closingDay !in 1..28 ||
                            dueDay == null || dueDay !in 1..28
                        ) {
                            formError = "Confira o apelido, os quatro dígitos, o limite e as datas."
                        } else {
                            val holderId = defaultHolderId?.takeIf { id -> state.holders.any { it.id == id } }
                            if (cardToEdit == null) {
                                viewModel.addCard(
                                    name = name,
                                    bank = selectedBank.id,
                                    lastFour = digits,
                                    limit = limitCents,
                                    closing = closingDay,
                                    due = dueDay,
                                    defaultHolderId = holderId
                                )
                            } else {
                                viewModel.updateCard(
                                    id = cardToEdit.id,
                                    name = name,
                                    bank = selectedBank.id,
                                    lastFour = digits,
                                    limit = limitCents,
                                    closing = closingDay,
                                    due = dueDay,
                                    defaultHolderId = holderId
                                )
                            }
                            showCardDialog = false
                            cardBeingEdited = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = AppBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (cardToEdit == null) "Cadastrar cartão" else "Salvar alterações", color = AppBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun RecurringChargesCard(
    recurringCharges: List<RecurringChargeEntity>,
    totalMonthlyRecurring: Long,
    limitImpactPercent: Double,
    priceCreepAlertCount: Int,
    onToggleActive: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassCard(
                cornerRadius = 20.dp,
                accentTint = if (priceCreepAlertCount > 0) SemanticWarning else SecondaryAccent
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = if (priceCreepAlertCount > 0) SemanticWarning else SecondaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Assinaturas & Recorrências",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (priceCreepAlertCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = SemanticWarning.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SemanticWarning.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Alerta de aumento",
                                    tint = SemanticWarning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "$priceCreepAlertCount alta",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticWarning
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    OverlineText("Comprometimento Mensal")
                    CurrencyText(
                        amountCents = totalMonthlyRecurring,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    OverlineText("Impacto no Limite")
                    Text(
                        String.format(java.util.Locale.US, "%.1f%%", limitImpactPercent),
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = if (limitImpactPercent > 30.0) SemanticWarning else PrimaryAccent
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    if (recurringCharges.isEmpty()) {
                        Text(
                            "Nenhuma assinatura detectada ainda. Compras recorrentes a cada ~30 dias serão identificadas automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        recurringCharges.forEach { charge ->
                            val hasCreep = charge.previousAmount != null && charge.lastAmount > charge.previousAmount
                            val delta = if (hasCreep) charge.lastAmount - charge.previousAmount!! else 0L

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            charge.merchant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (charge.isActive) TextPrimary else TextSecondary.copy(alpha = 0.6f)
                                        )
                                        if (hasCreep && charge.isActive) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SemanticDanger.copy(alpha = 0.2f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, SemanticDanger.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    "+${ReportEngine.formatCurrency(delta)}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                    fontWeight = FontWeight.Bold,
                                                    color = SemanticDanger
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "Ciclo ~${charge.billingCycleDays} dias • ${if (charge.isActive) "Ativa" else "Pausada"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CurrencyText(
                                        amountCents = charge.lastAmount,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (charge.isActive) PrimaryAccent else TextSecondary.copy(alpha = 0.5f)
                                    )
                                    Switch(
                                        checked = charge.isActive,
                                        onCheckedChange = { active -> onToggleActive(charge.id, active) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PrimaryAccent,
                                            checkedTrackColor = SecondaryAccent.copy(alpha = 0.5f),
                                            uncheckedThumbColor = TextSecondary,
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                        )
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
