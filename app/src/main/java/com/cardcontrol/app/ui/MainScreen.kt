package com.cardcontrol.app.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import com.cardcontrol.app.ui.analytics.AnalyticsScreen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.cardcontrol.app.data.local.*
import com.cardcontrol.app.data.repository.CardRepository
import java.time.LocalDate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import com.cardcontrol.app.ui.cards.CardUi
import com.cardcontrol.app.ui.cards.CardsScreen
import com.cardcontrol.app.ui.designsystem.AppearanceSection
import com.cardcontrol.app.ui.designsystem.CCCategorical
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.ThemeMode
import com.cardcontrol.app.ui.designsystem.resolveDark
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.components.RecurringChargesCard
import com.cardcontrol.app.ui.dialogs.CardDialog
import com.cardcontrol.app.ui.dialogs.DeleteCardDialog
import com.cardcontrol.app.ui.dialogs.DeleteHolderDialog
import com.cardcontrol.app.ui.dialogs.ExpenseDialog
import com.cardcontrol.app.ui.dialogs.HolderDialog
import com.cardcontrol.app.ui.dialogs.PaymentDialog
import com.cardcontrol.app.ui.theme.CardControlTheme
import com.cardcontrol.app.ui.theme.CurrencyText
import com.cardcontrol.app.ui.theme.LiquidAmbientBackground
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.util.ReportEngine

private val avatarPalette = listOf(
    CCCategorical.InterOrange,
    CCCategorical.NuCyan,
    CCCategorical.Teal,
    CCCategorical.Indigo,
    CCCategorical.Coral,
    CCCategorical.Pink
)

private fun shortMonthNamePt(month: Int): String = when (month) {
    1 -> "Jan"
    2 -> "Fev"
    3 -> "Mar"
    4 -> "Abr"
    5 -> "Mai"
    6 -> "Jun"
    7 -> "Jul"
    8 -> "Ago"
    9 -> "Set"
    10 -> "Out"
    11 -> "Nov"
    12 -> "Dez"
    else -> "%02d".format(month)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onOpenNotificationSettings: () -> Unit,
    sharedReceiptText: String? = null,
    onSharedReceiptConsumed: () -> Unit = {}
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
        sharedReceiptText = sharedReceiptText,
        onSharedReceiptConsumed = onSharedReceiptConsumed,
        onChangeMonth = { offset -> viewModel.changeMonth(offset) },
        onSelectMonthYear = { month, year -> viewModel.selectMonthYear(month, year) },
        onToggleRecurringCharge = { id, active -> viewModel.setRecurringChargeActive(id, active) },
        onDeleteTransaction = { txId -> viewModel.deleteTransaction(txId) },
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
        onUpdateHolder = { id, name, whats, email, subLimit ->
            viewModel.updateHolder(id, name, whats, email, subLimit)
        },
        onDeleteHolder = { holderId ->
            viewModel.deleteHolder(holderId)
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
    sharedReceiptText: String? = null,
    onSharedReceiptConsumed: () -> Unit = {},
    onChangeMonth: (Long) -> Unit = {},
    onSelectMonthYear: (Int, Int) -> Unit = { _, _ -> },
    onToggleRecurringCharge: (Long, Boolean) -> Unit = { _, _ -> },
    onDeleteTransaction: (Long) -> Unit = {},
    onMarkTransactionRecurring: (Long, Boolean) -> Unit = { _, _ -> },
    onAddExpense: (cardId: Long, holderId: Long, merchant: String, amountCents: Long, parcels: Int) -> Unit = { _, _, _, _, _ -> },
    onAddPayment: (holderId: Long, cardId: Long?, amountCents: Long, notes: String) -> Unit = { _, _, _, _ -> },
    onAddHolder: (name: String, whats: String, email: String, subLimitCents: Long) -> Unit = { _, _, _, _ -> },
    onUpdateHolder: (id: Long, name: String, whats: String, email: String, subLimitCents: Long) -> Unit = { _, _, _, _, _ -> },
    onDeleteHolder: (holderId: Long) -> Unit = {},
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
    var discreteMode by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettingsScreen) { showSettingsScreen = false }

    var expandedHolderId by remember { mutableStateOf<Long?>(initialExpandedHolderId) }
    var cardBeingEdited by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardPendingDeletion by remember { mutableStateOf<CreditCardEntity?>(null) }
    var holderBeingEdited by remember { mutableStateOf<HolderEntity?>(null) }
    var holderPendingDeletion by remember { mutableStateOf<HolderEntity?>(null) }
    var selectedVitrineCardId by rememberSaveable { mutableStateOf<Long?>(null) }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseInitialCardId by remember { mutableStateOf<Long?>(null) }
    var expenseInitialText by remember { mutableStateOf("") }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showHolderDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    var showSavedMessage by remember { mutableStateOf(false) }
    var showMissingHolderExpenseMessage by remember { mutableStateOf(false) }

    val requestExpenseDialog: (Long?) -> Unit = { initialCardId ->
        if (state.holders.isEmpty()) {
            showMissingHolderExpenseMessage = true
        } else {
            expenseInitialCardId = initialCardId
            showExpenseDialog = true
        }
    }

    // O usuário decide compartilhar ou colar. A sugestão apenas abre o formulário revisável.
    LaunchedEffect(sharedReceiptText, state.isInitializing, state.cards.size, state.holders.size) {
        if (!sharedReceiptText.isNullOrBlank() && !state.isInitializing) {
            if (state.cards.isNotEmpty() && state.holders.isNotEmpty()) {
                expenseInitialText = sharedReceiptText
                selectedTab = 0
                showSettingsScreen = false
                requestExpenseDialog(null)
            } else {
                snackbarHostState.showSnackbar("Cadastre ao menos um cartão e uma pessoa para revisar a compra compartilhada.")
            }
            onSharedReceiptConsumed()
        }
    }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            snackbarHostState.showSnackbar("Configurações de e-mail salvas com sucesso!")
            showSavedMessage = false
        }
    }

    LaunchedEffect(showMissingHolderExpenseMessage) {
        if (showMissingHolderExpenseMessage) {
            snackbarHostState.showSnackbar("Cadastre uma pessoa antes de lançar uma compra.")
            showMissingHolderExpenseMessage = false
        }
    }

    LiquidAmbientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val glassTint = if (isDark) {
                    Color(0xFF0B1320).copy(alpha = 0.42f)
                } else {
                    Color.White.copy(alpha = 0.45f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    glassTint,
                                    glassTint.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                ) {
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
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
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Text(
                                    text = "Configurações",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                    .height(58.dp)
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tabIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    when (selectedTab) {
                                        0 -> {
                                            // Pílula executiva de período (< Out/26 >) sem colisão em 360dp
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f), CircleShape)
                                                    .padding(horizontal = 2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { onChangeMonth(-1L) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ChevronLeft,
                                                        contentDescription = "Mês anterior",
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "%s/%02d".format(
                                                        shortMonthNamePt(state.selectedMonth),
                                                        state.selectedYear % 100
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontFeatureSettings = "tnum",
                                                        fontSize = 11.5.sp
                                                    ),
                                                    maxLines = 1
                                                )
                                                IconButton(
                                                    onClick = { onChangeMonth(1L) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ChevronRight,
                                                        contentDescription = "Próximo mês",
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Botão Modo Discreto (Olho)
                                            IconButton(
                                                onClick = { discreteMode = !discreteMode },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = if (discreteMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (discreteMode) "Mostrar valores" else "Ocultar valores",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                        1 -> {
                                            Box(
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), CircleShape)
                                                .clickable {
                                                    cardBeingEdited = null
                                                    showCardDialog = true
                                                }
                                                .padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Adicionar cartão",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Novo",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    else -> {}
                                }

                                Spacer(Modifier.width(4.dp))

                                IconButton(
                                    onClick = { showSettingsScreen = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Configurações",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
            bottomBar = {
                if (!showSettingsScreen) {
                    val haptic = LocalHapticFeedback.current
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    // Floating Glassmorphic Dock / Island Navigation Bar (Linear / Raycast / Revolut style)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    spotColor = Color.Black.copy(alpha = if (isDark) 0.50f else 0.18f),
                                    ambientColor = Color.Black.copy(alpha = 0.18f)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = if (isDark) 0.28f else 0.55f),
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                            Color.White.copy(alpha = if (isDark) 0.06f else 0.15f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    ),
                                    shape = RoundedCornerShape(32.dp)
                                ),
                            shape = RoundedCornerShape(32.dp),
                            color = if (isDark) {
                                Color(0xFF0F172A).copy(alpha = 0.72f)
                            } else {
                                Color(0xFFF1F5F9).copy(alpha = 0.75f)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val navTabs = listOf(
                                    Triple(0, "Faturas", Icons.AutoMirrored.Filled.ReceiptLong),
                                    Triple(1, "Cartões", Icons.Default.CreditCard),
                                    Triple(2, "Análises", Icons.Default.Insights)
                                )

                                navTabs.forEach { (index, title, icon) ->
                                    val isSelected = selectedTab == index
                                    val pillScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1f else 0.94f,
                                        label = "navTabScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(26.dp))
                                            .then(
                                                if (isSelected) {
                                                    Modifier
                                                        .background(primaryColor.copy(alpha = if (isDark) 0.18f else 0.12f))
                                                        .border(
                                                            1.dp,
                                                            primaryColor.copy(alpha = if (isDark) 0.38f else 0.28f),
                                                            RoundedCornerShape(26.dp)
                                                        )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .selectable(
                                                selected = isSelected,
                                                role = Role.Tab,
                                                onClick = {
                                                    if (!isSelected) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        selectedTab = index
                                                    }
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = pillScale
                                                    scaleY = pillScale
                                                }
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = if (isSelected) primaryColor else onSurfaceVariant.copy(alpha = 0.72f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    fontSize = 11.5.sp,
                                                    letterSpacing = 0.2.sp
                                                ),
                                                color = if (isSelected) primaryColor else onSurfaceVariant.copy(alpha = 0.72f),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                    onAddHolder = {
                        holderBeingEdited = null
                        showHolderDialog = true
                    },
                    onEditHolder = { holder ->
                        holderBeingEdited = holder
                        showHolderDialog = true
                    },
                    onDeleteHolder = { holder ->
                        holderPendingDeletion = holder
                    }
                )
            } else {
                // Sem padding horizontal global para que cada aba controle seu layout edge-to-edge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                ) {
                    when (selectedTab) {
                        0 -> {
                            FaturasExecutiveScreen(
                                state = state,
                                discreteMode = discreteMode,
                                expandedHolderId = expandedHolderId,
                                onToggleExpandHolder = { id ->
                                    expandedHolderId = if (expandedHolderId == id) null else id
                                },
                                onOpenExpenseDialog = {
                                    requestExpenseDialog(null)
                                },
                                onOpenNewHolderDialog = {
                                    holderBeingEdited = null
                                    showHolderDialog = true
                                },
                                onEditHolder = { holder ->
                                    holderBeingEdited = holder
                                    showHolderDialog = true
                                },
                                onDeleteHolder = { holder ->
                                    holderPendingDeletion = holder
                                },
                                onOpenPaymentDialog = {
                                    showPaymentDialog = true
                                },
                                onGoToCards = {
                                    selectedTab = 1
                                },
                                onSelectMonthYear = onSelectMonthYear,
                                onToggleRecurringCharge = onToggleRecurringCharge,
                                onMarkTransactionRecurring = onMarkTransactionRecurring,
                                onDeleteTransaction = onDeleteTransaction,
                                onShareHolderInvoice = { item ->
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
                                }
                            )
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
                                cardSummaries = state.cardSummaries,
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
                                    requestExpenseDialog(card.id)
                                },
                                onSeeMore = {
                                    val activeCardId = selectedVitrineCardId ?: uiCards.firstOrNull()?.id
                                    val cardEntity = state.cards.firstOrNull { it.id == activeCardId }
                                    val cardInstallments = if (activeCardId != null) {
                                        state.allInstallments.filter { it.cardId == activeCardId }
                                    } else {
                                        state.allInstallments
                                    }
                                    val hasCurrentMonth = cardInstallments.any {
                                        it.refMonth == state.selectedMonth && it.refYear == state.selectedYear
                                    }
                                    if (!hasCurrentMonth) {
                                        val futureInst = cardInstallments
                                            .filter { it.refYear > state.selectedYear || (it.refYear == state.selectedYear && it.refMonth > state.selectedMonth) }
                                            .minWithOrNull(compareBy<InstallmentEntity> { it.refYear }.thenBy { it.refMonth })
                                        if (futureInst != null) {
                                            onSelectMonthYear(futureInst.refMonth, futureInst.refYear)
                                        } else if (cardEntity != null) {
                                            val openMonth = CardRepository.calculateFirstInstallmentMonth(LocalDate.now(), cardEntity.closingDay)
                                            onSelectMonthYear(openMonth.monthValue, openMonth.year)
                                        }
                                    }
                                    selectedTab = 0
                                }
                            )
                        }

                        2 -> {
                            AnalyticsScreen(
                                installments = if (state.allInstallments.isNotEmpty()) state.allInstallments else state.holderSummaries.flatMap { it.monthlyInstallments },
                                payments = state.allPayments,
                                transactions = state.allTransactions,
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
            initialText = expenseInitialText,
            onDismissRequest = {
                showExpenseDialog = false
                expenseInitialCardId = null
                expenseInitialText = ""
            },
            onConfirm = { cardId, holderId, merchant, amountCents, parcels ->
                onAddExpense(cardId, holderId, merchant, amountCents, parcels)
                showExpenseDialog = false
                expenseInitialCardId = null
                expenseInitialText = ""
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
            holderToEdit = holderBeingEdited,
            onDismissRequest = {
                showHolderDialog = false
                holderBeingEdited = null
            },
            onConfirm = { name, whats, email, subLimitCents ->
                onAddHolder(name, whats, email, subLimitCents)
                showHolderDialog = false
                holderBeingEdited = null
            },
            onUpdateHolder = { id, name, whats, email, subLimitCents ->
                onUpdateHolder(id, name, whats, email, subLimitCents)
                showHolderDialog = false
                holderBeingEdited = null
            }
        )
    }

    holderPendingDeletion?.let { pendingHolder ->
        DeleteHolderDialog(
            holder = pendingHolder,
            onDismissRequest = { holderPendingDeletion = null },
            onConfirm = { holderId ->
                onDeleteHolder(holderId)
                holderPendingDeletion = null
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
private fun InvoiceActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    primary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isLightPrimary = primaryColor.luminance() > 0.55f
    val foreground = if (primary) {
        if (isLightPrimary) Color(0xFF11161B) else MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val backgroundModifier = if (primary) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.88f)
                )
            )
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f))
    }

    val borderBrush = if (primary) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.45f),
                primaryColor.copy(alpha = 0.80f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.20f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                Color.Transparent
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(backgroundModifier)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (primary) foreground else primaryColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (primary) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = foreground,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun FaturasExecutiveScreen(
    state: DashboardUiState,
    discreteMode: Boolean,
    expandedHolderId: Long?,
    onToggleExpandHolder: (Long) -> Unit,
    onOpenExpenseDialog: () -> Unit,
    onOpenNewHolderDialog: () -> Unit,
    onEditHolder: (HolderEntity) -> Unit,
    onDeleteHolder: (HolderEntity) -> Unit,
    onOpenPaymentDialog: () -> Unit,
    onGoToCards: () -> Unit,
    onSelectMonthYear: (Int, Int) -> Unit,
    onToggleRecurringCharge: (Long, Boolean) -> Unit,
    onMarkTransactionRecurring: (Long, Boolean) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onShareHolderInvoice: (HolderSummaryUi) -> Unit
) {
    val remainingInvoice = (state.totalMonthInvoice - state.totalMonthPaid).coerceAtLeast(0L)
    val paidRatio = if (state.totalMonthInvoice > 0L) {
        (state.totalMonthPaid.toFloat() / state.totalMonthInvoice.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val totalCardsLimit = state.cards.sumOf { it.totalLimit }
    val totalGlobalCommitted = state.cardSummaries.sumOf { it.globalCommitted }
    val totalAvailableLimit = (totalCardsLimit - totalGlobalCommitted).coerceAtLeast(0L)
    val limitUsedRatio = if (totalCardsLimit > 0L) {
        (totalGlobalCommitted.toFloat() / totalCardsLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 148.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Card Executivo da Fatura Consolidada & Limite (Glassmorphic Card)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 600f)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FATURA · %s/%d".format(
                                shortMonthNamePt(state.selectedMonth).uppercase(),
                                state.selectedYear
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        val statusText = when {
                            state.totalMonthInvoice == 0L -> "Em dia"
                            remainingInvoice == 0L -> "Quitada"
                            else -> "Aberta"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CCTheme.extras.semantics.successContainer.copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "● $statusText",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CCTheme.extras.semantics.onSuccessContainer
                            )
                        }
                    }

                    if (discreteMode) {
                        Text(
                            text = "R$ ••••••",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        CurrencyText(
                            amountCents = remainingInvoice,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (discreteMode) "Bruto: R$ ••••" else "Bruto: ${ReportEngine.formatCurrency(state.totalMonthInvoice)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (discreteMode) "Valor pago: R$ ••••" else "Valor pago: ${ReportEngine.formatCurrency(state.totalMonthPaid)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            color = if (state.totalMonthPaid > 0L) CCTheme.extras.semantics.success else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (state.cards.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Limite livre na carteira",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (discreteMode) "R$ ••••" else MoneyUtils.formatCents(totalAvailableLimit),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    if (state.totalMonthInvoice > 0L) paidRatio else (1f - limitUsedRatio).coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (state.totalMonthInvoice > 0L) CCTheme.extras.semantics.success else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    } else {
                        LinearProgressIndicator(
                            progress = { paidRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CCTheme.extras.semantics.success,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }

                    val canLaunchExpense = state.cards.isNotEmpty() && state.holders.isNotEmpty()
                    val compactActions = LocalConfiguration.current.fontScale > 1.1f
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val stacked = maxWidth < 450.dp || compactActions
                        if (stacked) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InvoiceActionButton(
                                    label = "Lançar compra",
                                    icon = Icons.Default.Add,
                                    primary = true,
                                    onClick = {
                                        if (canLaunchExpense) onOpenExpenseDialog()
                                        else if (state.cards.isEmpty()) onGoToCards()
                                        else onOpenNewHolderDialog()
                                    }
                                )
                                InvoiceActionButton(
                                    label = "Nova pessoa",
                                    icon = Icons.Default.PersonAdd,
                                    onClick = onOpenNewHolderDialog
                                )
                                if (state.holders.isNotEmpty()) {
                                    InvoiceActionButton(
                                        label = "Registrar pagamento",
                                        icon = Icons.Default.Payments,
                                        onClick = onOpenPaymentDialog
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InvoiceActionButton(
                                    label = "Lançar compra",
                                    icon = Icons.Default.Add,
                                    primary = true,
                                    onClick = {
                                        if (canLaunchExpense) onOpenExpenseDialog()
                                        else if (state.cards.isEmpty()) onGoToCards()
                                        else onOpenNewHolderDialog()
                                    },
                                    modifier = Modifier.weight(1.25f)
                                )
                                InvoiceActionButton(
                                    label = "Nova pessoa",
                                    icon = Icons.Default.PersonAdd,
                                    onClick = onOpenNewHolderDialog,
                                    modifier = Modifier.weight(1f)
                                )
                                if (state.holders.isNotEmpty()) {
                                    InvoiceActionButton(
                                        label = "Registrar pagamento",
                                        icon = Icons.Default.Payments,
                                        onClick = onOpenPaymentDialog,
                                        modifier = Modifier.weight(1.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Card de Assinaturas e Recorrências (quando houver)
        if (state.recurringCharges.isNotEmpty()) {
            item {
                RecurringChargesCard(
                    recurringCharges = state.recurringCharges,
                    totalMonthlyRecurring = state.totalMonthlyRecurring,
                    limitImpactPercent = state.recurringLimitImpactPercent,
                    priceCreepAlertCount = state.priceCreepAlertCount,
                    onToggleActive = { id, active -> onToggleRecurringCharge(id, active) }
                )
            }
        }

        // 3. Seção de Pessoas & Rateio SEMPRE VISÍVEL quando há pessoas cadastradas
        if (state.holders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pessoas e Rateio (${state.holders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+ Nova pessoa",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenNewHolderDialog() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            itemsIndexed(state.holderSummaries, key = { _, item -> item.holder.id }) { index, item ->
                val isExpanded = expandedHolderId == item.holder.id
                val usageRatio = if (item.holder.subLimit > 0L) {
                    (item.globalCommitted.toFloat() / item.holder.subLimit.toFloat())
                } else 0f
                val barColor = when {
                    usageRatio >= 1f -> MaterialTheme.colorScheme.error
                    usageRatio >= 0.8f -> CCTheme.extras.semantics.warning
                    else -> MaterialTheme.colorScheme.primary
                }
                val avatarColor = avatarPalette[index % avatarPalette.size]
                val initial = item.holder.name.trim().firstOrNull()?.uppercase() ?: "P"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { onToggleExpandHolder(item.holder.id) }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(avatarColor.copy(alpha = 0.18f))
                                    .border(1.dp, avatarColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = avatarColor
                                    )
                                )
                            }
                            Text(
                                text = item.holder.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val countLabel = if (item.monthlyInstallments.isEmpty()) {
                            "Sem compras"
                        } else if (item.monthlyInstallments.size == 1) {
                            "1 parcela"
                        } else {
                            "${item.monthlyInstallments.size} parcelas"
                        }
                        val netLabel = if (discreteMode) "R$ ••••" else MoneyUtils.formatCents(item.monthlyNetDue)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = countLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = netLabel,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = if (item.monthlyNetDue > 0L) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEditHolder(item.holder) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar pessoa",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteHolder(item.holder) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir pessoa",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onShareHolderInvoice(item) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Exportar PDF/WhatsApp",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onToggleExpandHolder(item.holder.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Recolher detalhes" else "Expandir detalhes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (item.holder.subLimit > 0L) {
                            val percentSubLimit = (usageRatio * 100f).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Sublimite: $percentSubLimit% usado",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Teto: ${ReportEngine.formatCurrency(item.holder.subLimit)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { usageRatio.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                if (item.monthlyInstallments.isEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Nenhuma parcela neste mês.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (state.cards.isNotEmpty()) {
                                            Text(
                                                text = "+ Lançar compra",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onOpenExpenseDialog() }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
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
                                                Text(
                                                    "• ${inst.merchantSnapshot} (${inst.installmentNumber}/${inst.totalInstallments})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                CurrencyText(
                                                    amountCents = inst.amount,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(
                                                    onClick = { onDeleteTransaction(inst.transactionId) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Remover compra",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(16.dp)
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

        // 4. Estados Guiados de Primeiros Passos (Cartões / Pessoas / Compras do Mês)
        if (state.cards.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            "Nenhum cartão cadastrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Cadastre seu primeiro cartão na aba Cartões para controlar limites e faturas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = onGoToCards,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ir para Cartões", maxLines = 1)
                        }
                    }
                }
            }
        } else if (state.holders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            "Nenhuma pessoa cadastrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Cadastre as pessoas que utilizam seus cartões para organizar o rateio mensal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = onOpenNewHolderDialog,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cadastrar Pessoa", maxLines = 1)
                        }
                    }
                }
            }
        } else if (state.holderSummaries.all { it.monthlyInstallments.isEmpty() }) {
            val nextOpenInstallment = state.allInstallments
                .filter { it.refYear > state.selectedYear || (it.refYear == state.selectedYear && it.refMonth > state.selectedMonth) }
                .minWithOrNull(compareBy<InstallmentEntity> { it.refYear }.thenBy { it.refMonth })
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        "Sem compras em %s/%02d".format(shortMonthNamePt(state.selectedMonth), state.selectedYear % 100),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        if (nextOpenInstallment != null) {
                                            "Existem lançamentos na fatura de %s/%02d.".format(
                                                shortMonthNamePt(nextOpenInstallment.refMonth),
                                                nextOpenInstallment.refYear % 100
                                            )
                                        } else {
                                            "Toque ao lado para registrar uma despesa."
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            if (nextOpenInstallment != null) {
                                Button(
                                    onClick = { onSelectMonthYear(nextOpenInstallment.refMonth, nextOpenInstallment.refYear) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Ver %s/%02d".format(shortMonthNamePt(nextOpenInstallment.refMonth), nextOpenInstallment.refYear % 100),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onOpenExpenseDialog,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Lançar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
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
    onAddHolder: () -> Unit,
    onEditHolder: (HolderEntity) -> Unit = {},
    onDeleteHolder: (HolderEntity) -> Unit = {}
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Captura automática",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Lê alertas de compra dos seus bancos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (notificationListenerEnabled) {
                            CCTheme.extras.semantics.successContainer
                        } else {
                            CCTheme.extras.semantics.warningContainer
                        }
                    ) {
                        Text(
                            text = if (notificationListenerEnabled) "Ativa" else "Pendente",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (notificationListenerEnabled) {
                                CCTheme.extras.semantics.onSuccessContainer
                            } else {
                                CCTheme.extras.semantics.onWarningContainer
                            }
                        )
                    }
                }

                Button(
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Permissão de notificações",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. Grupo: Aparência
        AppearanceSection(
            currentMode = themeMode,
            onModeChange = onThemeModeChange
        )

        // 3. Grupo: Titulares / Pessoas (com lista completa, Editar e Excluir!)
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Pessoas (${holders.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "Titulares e dependentes no rateio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onAddHolder,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nova", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                    }
                }

                if (holders.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        holders.forEachIndexed { idx, holder ->
                            val avatarColor = avatarPalette[idx % avatarPalette.size]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(avatarColor.copy(alpha = 0.2f))
                                            .border(1.dp, avatarColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = holder.name.trim().firstOrNull()?.uppercase() ?: "P",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = avatarColor
                                            )
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = holder.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val metaText = if (holder.subLimit > 0L) {
                                            "Teto: ${MoneyUtils.formatCents(holder.subLimit)}"
                                        } else {
                                            "Sem teto de sublimite"
                                        }
                                        Text(
                                            text = metaText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onEditHolder(holder) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar ${holder.name}",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteHolder(holder) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir ${holder.name}",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Envio por e-mail",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Comprovantes opcionais via Gmail SMTP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
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
                            Text("Salvar configurações", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "CardControl • 100% no aparelho · Sem nuvem",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
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
