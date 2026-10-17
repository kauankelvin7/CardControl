package com.cardcontrol.app.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import com.cardcontrol.app.ui.analytics.AnalyticsScreen
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import com.cardcontrol.app.ui.cards.CardUi
import com.cardcontrol.app.ui.cards.CardsScreen
import com.cardcontrol.app.ui.designsystem.AppearanceSection
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.ThemeMode
import com.cardcontrol.app.ui.designsystem.resolveDark
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.components.RecurringChargesCard
import com.cardcontrol.app.ui.dialogs.CardDialog
import com.cardcontrol.app.ui.dialogs.DeleteCardDialog
import com.cardcontrol.app.ui.dialogs.ExpenseDialog
import com.cardcontrol.app.ui.dialogs.HolderDialog
import com.cardcontrol.app.ui.dialogs.PaymentDialog
import com.cardcontrol.app.ui.theme.CardControlTheme
import com.cardcontrol.app.ui.theme.CurrencyText
import com.cardcontrol.app.ui.theme.OverlineText
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.util.ReportEngine
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
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
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange,
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
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
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
    val lifecycleOwner = context.findLifecycleOwner()
    var notificationListenerEnabled by remember(context) {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        )
    }
    DisposableEffect(context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationListenerEnabled = NotificationManagerCompat
                    .getEnabledListenerPackages(context)
                    .contains(context.packageName)
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettingsScreen) { showSettingsScreen = false }

    var expandedHolderId by remember { mutableStateOf<Long?>(initialExpandedHolderId) }
    var cardBeingEdited by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardPendingDeletion by remember { mutableStateOf<CreditCardEntity?>(null) }
    var selectedVitrineCardId by rememberSaveable { mutableStateOf<Long?>(null) }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseInitialCardId by remember { mutableStateOf<Long?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showHolderDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            snackbarHostState.showSnackbar("Configurações de e-mail salvas com sucesso!")
            showSavedMessage = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    if (showSettingsScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showSettingsScreen = false },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = "Configurações",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        val tabTitle = when (selectedTab) {
                            0 -> "Faturas"
                            1 -> "Cartões"
                            else -> "Análises"
                        }
                        val tabIcon = when (selectedTab) {
                            0 -> Icons.AutoMirrored.Filled.ReceiptLong
                            1 -> Icons.Default.CreditCard
                            else -> Icons.Default.Insights
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                when (selectedTab) {
                                    0 -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onChangeMonth(-1L) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronLeft,
                                                    contentDescription = "Mês anterior",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                "%02d/%d".format(state.selectedMonth, state.selectedYear),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFeatureSettings = "tnum"
                                                )
                                            )
                                            IconButton(
                                                onClick = { onChangeMonth(1L) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = "Próximo mês",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    1 -> {
                                        IconButton(
                                            onClick = {
                                                cardBeingEdited = null
                                                showCardDialog = true
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Adicionar cartão",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    else -> {}
                                }

                                IconButton(
                                    onClick = { showSettingsScreen = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Configurações",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!showSettingsScreen) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                    val navItemColors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryColor.copy(alpha = 0.15f),
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        unselectedIconColor = onSurfaceVariant,
                        unselectedTextColor = onSurfaceVariant
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 2.dp
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
                            icon = { Icon(Icons.Default.Insights, contentDescription = "Análises") },
                            label = { Text("Análises") },
                            colors = navItemColors
                        )
                    }
                }
            },
            floatingActionButton = {
                val hasPurchases = state.holderSummaries.any { it.monthlyInstallments.isNotEmpty() }
                if (!showSettingsScreen && selectedTab == 0 && state.cards.isNotEmpty() && state.holders.isNotEmpty() && hasPurchases) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            expenseInitialCardId = null
                            showExpenseDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nova compra", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        ) { padding ->
            if (showSettingsScreen) {
                SettingsScreenContent(
                    modifier = Modifier.padding(padding),
                    notificationListenerEnabled = notificationListenerEnabled,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    initialSmtpEmail = initialSmtpEmail,
                    initialSmtpPass = initialSmtpPass,
                    onSaveSmtpCredentials = onSaveSmtpCredentials,
                    onShowSavedMessage = { showSavedMessage = true },
                    holders = state.holders,
                    onAddHolder = { showHolderDialog = true }
                )
            } else {
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
                    when (selectedTab) {
                    0 -> {
                        val remainingInvoice = (state.totalMonthInvoice - state.totalMonthPaid).coerceAtLeast(0L)
                        val paidRatio = if (state.totalMonthInvoice > 0L) (state.totalMonthPaid.toFloat() / state.totalMonthInvoice.toFloat()).coerceIn(0f, 1f) else 0f
                        val paidPercent = (paidRatio * 100f).toInt()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Fatura Consolidada do Mês",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                CurrencyText(
                                    amountCents = remainingInvoice,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Bruto: ${ReportEngine.formatCurrency(state.totalMonthInvoice)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Abatido: ${ReportEngine.formatCurrency(state.totalMonthPaid)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                        color = if (state.totalMonthPaid > 0L) CCTheme.extras.semantics.success else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { paidRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = CCTheme.extras.semantics.success,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (state.totalMonthInvoice > 0L) "$paidPercent% quitado" else "Sem fatura pendente",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (state.totalMonthPaid > 0L) {
                                        Text(
                                            "Economia / Saldo positivo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CCTheme.extras.semantics.success
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
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("Nova Pessoa", fontWeight = FontWeight.SemiBold)
                            }
                            if (state.holders.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { showPaymentDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = CCTheme.extras.semantics.success)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Abater / Pix", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))

                        if (state.cards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                    .padding(22.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                                    Text("Nenhum cartão cadastrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "Cadastre seus cartões na aba Cartões para visualizar faturas e lançar compras.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { selectedTab = 1 },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Ir para Cartões")
                                    }
                                }
                            }
                        } else if (state.holders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                    .padding(22.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                                    Text("Nenhuma pessoa cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "Cadastre as pessoas para quem você divide ou organiza os limites dos seus cartões.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { showHolderDialog = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cadastrar Pessoa")
                                    }
                                }
                            }
                        } else if (state.holderSummaries.all { it.monthlyInstallments.isEmpty() }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                    .padding(22.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                                    Text("Nenhuma compra registrada neste mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "Suas compras e parcelas deste período aparecerão aqui.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { showExpenseDialog = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Lançar compra")
                                    }
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
                                        usageRatio >= 1f -> MaterialTheme.colorScheme.error
                                        usageRatio >= 0.8f -> CCTheme.extras.semantics.warning
                                        else -> MaterialTheme.colorScheme.primary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainer)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                            .clickable { expandedHolderId = if (isExpanded) null else item.holder.id }
                                            .padding(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(item.holder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text("A Pagar no Mês: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                    }) { Icon(Icons.Default.Share, contentDescription = "Exportar PDF/WhatsApp", tint = MaterialTheme.colorScheme.primary) }
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }
                                            }

                                            if (item.holder.subLimit > 0L) {
                                                val percentSubLimit = (usageRatio * 100f).toInt()
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Sublimite: $percentSubLimit% usado", style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("Teto: ${ReportEngine.formatCurrency(item.holder.subLimit)}", style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { usageRatio.coerceIn(0f, 1f) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = barColor,
                                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                                )
                                            }

                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                    if (item.monthlyInstallments.isEmpty()) {
                                                        Text("Nenhuma parcela neste mês.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                                                                modifier = Modifier.size(16.dp)
                                                                            )
                                                                        }
                                                                    } else {
                                                                        Text(
                                                                            "Parcelamento",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                    Text("• ${inst.merchantSnapshot} (${inst.installmentNumber}/${inst.totalInstallments})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                                                }
                                                                CurrencyText(
                                                                    amountCents = inst.amount,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
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
                        val uiCards = remember(state.cards, state.holders) {
                            state.cards.map { card ->
                                val holderName = state.holders.firstOrNull { it.id == card.defaultHolderId }?.name
                                CardUi.fromEntity(card, holderName = holderName)
                            }
                        }
                        CardsScreen(
                            cards = uiCards,
                            isDark = resolveDark(themeMode, isSystemInDarkTheme()),
                            selectedCardId = selectedVitrineCardId ?: uiCards.firstOrNull()?.id,
                            onCardSelected = { selectedVitrineCardId = it },
                            onAddCard = {
                                cardBeingEdited = null
                                showCardDialog = true
                            },
                            onEditCard = { uiCard ->
                                cardBeingEdited = state.cards.firstOrNull { it.id == uiCard.id }
                                showCardDialog = true
                            },
                            onDeleteCard = { uiCard ->
                                cardPendingDeletion = state.cards.firstOrNull { it.id == uiCard.id }
                            },
                            onAddExpenseForCard = { card ->
                                expenseInitialCardId = card.id
                                showExpenseDialog = true
                            },
                            onSeeMore = {
                                selectedTab = 0
                            }
                        )
                    }

                    2 -> {
                        AnalyticsScreen(
                            installments = if (state.allInstallments.isNotEmpty()) state.allInstallments else state.holderSummaries.flatMap { it.monthlyInstallments },
                            payments = state.allPayments,
                            cards = state.cards,
                            holders = state.holders,
                            recurringCharges = state.recurringCharges,
                            initialCardId = selectedVitrineCardId,
                            onCardFilterChange = { selectedVitrineCardId = it }
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
            initialCardId = expenseInitialCardId,
            onDismissRequest = {
                showExpenseDialog = false
                expenseInitialCardId = null
            },
            onConfirm = { cardId, holderId, merchant, amountCents, parcels ->
                onAddExpense(cardId, holderId, merchant, amountCents, parcels)
                showExpenseDialog = false
                expenseInitialCardId = null
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

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    notificationListenerEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    initialSmtpEmail: String,
    initialSmtpPass: String,
    onSaveSmtpCredentials: ((String, String) -> Unit)?,
    onShowSavedMessage: () -> Unit,
    holders: List<HolderEntity>,
    onAddHolder: () -> Unit
) {
    var email by remember { mutableStateOf(initialSmtpEmail) }
    var pass by remember { mutableStateOf(initialSmtpPass) }
    var showPassword by remember { mutableStateOf(false) }
    var isSmtpExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // 1. Grupo: Captura automática
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Captura de notificações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Escuta compras emitidas pelos aplicativos dos seus bancos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (notificationListenerEnabled) {
                                CCTheme.extras.semantics.successContainer
                            } else {
                                CCTheme.extras.semantics.warningContainer
                            }
                        ) {
                            Text(
                                text = if (notificationListenerEnabled) "Permissão ativa" else "Permissão pendente",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (notificationListenerEnabled) {
                                    CCTheme.extras.semantics.onSuccessContainer
                                } else {
                                    CCTheme.extras.semantics.onWarningContainer
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gerenciar permissão de notificações", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 2. Grupo: Aparência
        AppearanceSection(
            currentMode = themeMode,
            onModeChange = onThemeModeChange
        )

        // 3. Grupo: Titulares / Pessoas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Pessoas cadastradas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${holders.size} pessoa(s) para rateio de faturas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onAddHolder,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // 4. Grupo: Envio por e-mail (recolhível)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSmtpExpanded = !isSmtpExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Envio por e-mail",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Envio opcional de comprovantes via Gmail SMTP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isSmtpExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = isSmtpExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        GlassTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Gmail Remetente",
                            placeholder = "seuemail@gmail.com",
                            supportingText = "Envio automático e seguro via SMTP TLS",
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )

                        GlassTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = "Senha de App Google",
                            placeholder = "xxxx xxxx xxxx xxxx",
                            supportingText = "Código de 16 caracteres gerado na sua Conta Google",
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Ocultar senha" else "Ver senha",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                        )

                        Button(
                            onClick = {
                                onSaveSmtpCredentials?.invoke(email, pass)
                                onShowSavedMessage()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar configurações", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "CardControl • 100% no aparelho",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 96.dp),
            textAlign = TextAlign.Center
        )
    }
}

private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.findLifecycleOwner()
    else -> null
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
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
        }
    }
}
