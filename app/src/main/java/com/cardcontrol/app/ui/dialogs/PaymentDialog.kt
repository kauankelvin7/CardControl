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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
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
import com.cardcontrol.app.ui.theme.LiquidPillSelector
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.CcShapes

/**
 * Diálogo modal para registrar pagamentos, reembolsos ou transferências Pix de titulares.
 *
 * O valor informado é convertido para centavos inteiros (Long) e pode ser opcionalmente
 * associado a um cartão específico para liberação direta de limite rotativo.
 */
@Composable
fun PaymentDialog(
    holders: List<HolderEntity>,
    cards: List<CreditCardEntity>,
    onDismissRequest: () -> Unit,
    onConfirm: (holderId: Long, cardId: Long?, amountCents: Long, notes: String) -> Unit
) {
    if (holders.isEmpty()) return

    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedHolderId by remember { mutableLongStateOf(holders.first().id) }
    var selectedCardId by remember { mutableStateOf(cards.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        shape = CcShapes.dialog,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CcShapes.dialog),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, tint = CCTheme.extras.semantics.success)
                Spacer(Modifier.width(8.dp))
                Text("Registrar Abatimento / Pix", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Quem Pagou:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LiquidPillSelector(
                        options = holders.map { it.id to it.name },
                        selectedId = selectedHolderId,
                        onSelect = { selectedHolderId = it }
                    )
                }
                if (cards.isNotEmpty()) {
                    Text("Liberar Limite do Cartão:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        LiquidPillSelector(
                            options = cards.map { it.id to it.name },
                            selectedId = selectedCardId,
                            onSelect = { selectedCardId = it }
                        )
                    }
                }
                GlassTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Valor Recebido (R$)",
                    placeholder = "0,00",
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = CCTheme.extras.semantics.success) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Observação",
                    placeholder = "Ex: Pix fatura adiantada",
                    leadingIcon = { Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
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
                    if (v > 0L) {
                        onConfirm(selectedHolderId, selectedCardId, v, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CCTheme.extras.semantics.success,
                    contentColor = CCTheme.extras.semantics.onSuccess
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Abater", fontWeight = FontWeight.Bold)
            }
        }
    )
}
