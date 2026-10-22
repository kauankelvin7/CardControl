package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.repository.CardRepository
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.util.TransactionTextParser
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.theme.LiquidPillSelector

/**
 * Diálogo modal para lançamento manual de despesa no cartão de crédito.
 *
 * Converte o valor digitado diretamente para centavos canônicos (Long) e
 * valida o limite de parcelas (1 a 72). Vincula automaticamente o titular
 * padrão do cartão selecionado, se configurado.
 */
@Composable
fun ExpenseDialog(
    cards: List<CreditCardEntity>,
    holders: List<HolderEntity>,
    onDismissRequest: () -> Unit,
    onConfirm: (cardId: Long, holderId: Long, merchant: String, amountCents: Long, parcels: Int) -> Unit,
    initialCardId: Long? = null,
    initialText: String = ""
) {
    if (cards.isEmpty() || holders.isEmpty()) return

    val suggestion = remember(initialText) { TransactionTextParser.parse(initialText) }
    var rawText by remember(initialText) { mutableStateOf(initialText.take(4_000)) }
    var interpretationMessage by remember(initialText) {
        mutableStateOf(if (initialText.isBlank()) "" else if (suggestion != null) "Sugestão preenchida. Confira os dados." else "Não foi possível interpretar o texto. Você pode preencher manualmente.")
    }
    var merchant by remember(initialText) { mutableStateOf(suggestion?.merchant.orEmpty()) }
    var amount by remember(initialText) { mutableStateOf(suggestion?.amountCents?.let { cents -> centsInput(cents) }.orEmpty()) }
    var parcels by remember(initialText) { mutableStateOf(suggestion?.installments?.toString() ?: "1") }
    var formError by remember { mutableStateOf<String?>(null) }
    var selectedCardId by remember(cards, initialCardId, initialText) {
        val matchingLastFour = cards.filter { it.lastFourDigits == suggestion?.lastFourDigits }
        mutableLongStateOf(
            cards.firstOrNull { it.id == initialCardId }?.id
                ?: matchingLastFour.singleOrNull()?.id
                ?: cards.first().id
        )
    }
    var selectedHolderId by remember(cards, holders, initialCardId, initialText) {
        mutableLongStateOf(
            cards.firstOrNull { it.id == selectedCardId }?.defaultHolderId
                ?.takeIf { holderId -> holders.any { it.id == holderId } }
                ?: holders.first().id
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        shape = CcShapes.dialog,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CcShapes.dialog),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Lançar Compra", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Cole o texto de uma compra ou compartilhe-o de outro app. A interpretação é local e nada é salvo sem sua confirmação.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                GlassTextField(
                    value = rawText,
                    onValueChange = { rawText = it.take(4_000); interpretationMessage = "" },
                    label = "Texto do comprovante (opcional)",
                    placeholder = "Compra de R$ 45,90 em Mercado...",
                    singleLine = false,
                    modifier = Modifier.heightIn(max = 128.dp)
                )
                TextButton(onClick = {
                    val parsed = TransactionTextParser.parse(rawText)
                    if (parsed == null) {
                        interpretationMessage = "Não encontrei uma compra válida. Preencha abaixo manualmente."
                    } else {
                        merchant = parsed.merchant.ifBlank { merchant }
                        amount = centsInput(parsed.amountCents)
                        parcels = parsed.installments.toString()
                        val matchingCards = cards.filter { it.lastFourDigits == parsed.lastFourDigits }
                        matchingCards.singleOrNull()?.let { matched ->
                            selectedCardId = matched.id
                            selectedHolderId = matched.defaultHolderId
                                ?.takeIf { id -> holders.any { it.id == id } }
                                ?: holders.first().id
                        }
                        interpretationMessage = "Valor sugerido. Confira cartão, titular, parcelas e estabelecimento."
                        formError = null
                    }
                }) { Text("Interpretar texto") }
                if (interpretationMessage.isNotBlank()) {
                    Text(interpretationMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Cartão Utilizado:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LiquidPillSelector(
                        options = cards.map { it.id to "${it.name} (${it.lastFourDigits})" },
                        selectedId = selectedCardId,
                        onSelect = { cardId ->
                            formError = null
                            selectedCardId = cardId
                            selectedHolderId = cards.find { it.id == cardId }?.defaultHolderId
                                ?.takeIf { holderId -> holders.any { it.id == holderId } }
                                ?: holders.first().id
                        }
                    )
                }
                val selectedCard = cards.firstOrNull { it.id == selectedCardId } ?: cards.first()
                val targetInvoiceDate = CardRepository.calculateFirstInstallmentMonth(
                    java.time.LocalDate.now(),
                    selectedCard.closingDay
                )
                Text(
                    text = "Fatura destino: %02d/%d (fecha dia %d)".format(
                        targetInvoiceDate.monthValue,
                        targetInvoiceDate.year,
                        selectedCard.closingDay
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Responsável pelo Gasto:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LiquidPillSelector(
                        options = holders.map { it.id to it.name },
                        selectedId = selectedHolderId,
                        onSelect = {
                            selectedHolderId = it
                            formError = null
                        }
                    )
                }
                GlassTextField(
                    value = merchant,
                    onValueChange = {
                        merchant = it
                        formError = null
                    },
                    label = "Estabelecimento",
                    placeholder = "Ex: Mercado Livre, Posto...",
                    leadingIcon = { Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        formError = null
                    },
                    label = "Valor Total (R$)",
                    placeholder = "0,00",
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = CCTheme.extras.semantics.success) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = parcels,
                    onValueChange = {
                        parcels = it
                        formError = null
                    },
                    label = "Parcelas (1 a 72)",
                    placeholder = "1",
                    leadingIcon = { Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                formError?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = MoneyUtils.parseToCents(amount) ?: 0L
                    if (merchant.isNotBlank() && v > 0L) {
                        formError = null
                        onConfirm(
                            selectedCardId,
                            selectedHolderId,
                            merchant,
                            v,
                            (parcels.toIntOrNull() ?: 1).coerceIn(1, 72)
                        )
                    } else {
                        formError = "Informe o estabelecimento e um valor maior que R$ 0,00."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/** Constrói campo de moeda com centavos inteiros, sem usar ponto flutuante. */
private fun centsInput(cents: Long): String =
    (cents / 100).toString() + "," + (cents % 100).toString().padStart(2, '0')
