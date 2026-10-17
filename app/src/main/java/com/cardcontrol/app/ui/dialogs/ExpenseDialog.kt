package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.util.MoneyUtils
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
    initialCardId: Long? = null
) {
    if (cards.isEmpty() || holders.isEmpty()) return

    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var parcels by remember { mutableStateOf("1") }
    var selectedCardId by remember(cards, initialCardId) {
        mutableLongStateOf(cards.firstOrNull { it.id == initialCardId }?.id ?: cards.first().id)
    }
    var selectedHolderId by remember(cards, holders, initialCardId) {
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cartão Utilizado:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LiquidPillSelector(
                        options = cards.map { it.id to "${it.name} (${it.lastFourDigits})" },
                        selectedId = selectedCardId,
                        onSelect = { cardId ->
                            selectedCardId = cardId
                            selectedHolderId = cards.find { it.id == cardId }?.defaultHolderId
                                ?.takeIf { holderId -> holders.any { it.id == holderId } }
                                ?: holders.first().id
                        }
                    )
                }
                Text("Responsável pelo Gasto:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LiquidPillSelector(
                        options = holders.map { it.id to it.name },
                        selectedId = selectedHolderId,
                        onSelect = { selectedHolderId = it }
                    )
                }
                GlassTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = "Estabelecimento",
                    placeholder = "Ex: Mercado Livre, Posto...",
                    leadingIcon = { Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Valor Total (R$)",
                    placeholder = "0,00",
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = CCTheme.extras.semantics.success) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = parcels,
                    onValueChange = { parcels = it },
                    label = "Parcelas (1 a 72)",
                    placeholder = "1",
                    leadingIcon = { Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
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
                        onConfirm(
                            selectedCardId,
                            selectedHolderId,
                            merchant,
                            v,
                            (parcels.toIntOrNull() ?: 1).coerceIn(1, 72)
                        )
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
