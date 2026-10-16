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
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.theme.AppBackground
import com.cardcontrol.app.ui.theme.AppBackgroundTop
import com.cardcontrol.app.ui.theme.GlassBorderTopLeft
import com.cardcontrol.app.ui.theme.LiquidPillSelector
import com.cardcontrol.app.ui.theme.PrimaryAccent
import com.cardcontrol.app.ui.theme.SecondaryAccent
import com.cardcontrol.app.ui.theme.SemanticSuccess
import com.cardcontrol.app.ui.theme.TextPrimary
import com.cardcontrol.app.ui.theme.TextSecondary

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
    onConfirm: (cardId: Long, holderId: Long, merchant: String, amountCents: Long, parcels: Int) -> Unit
) {
    if (cards.isEmpty() || holders.isEmpty()) return

    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var parcels by remember { mutableStateOf("1") }
    var selectedCardId by remember { mutableLongStateOf(cards.first().id) }
    var selectedHolderId by remember {
        mutableLongStateOf(
            cards.first().defaultHolderId
                ?.takeIf { holderId -> holders.any { it.id == holderId } }
                ?: holders.first().id
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                Text("Responsável pelo Gasto:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
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
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = TextSecondary)
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
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = AppBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar", color = AppBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}
