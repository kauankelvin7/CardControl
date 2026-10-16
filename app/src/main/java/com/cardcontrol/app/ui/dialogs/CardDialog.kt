package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.SupportedBank
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.theme.AppBackground
import com.cardcontrol.app.ui.theme.AppBackgroundTop
import com.cardcontrol.app.ui.theme.GlassBorderTopLeft
import com.cardcontrol.app.ui.theme.LiquidPillSelector
import com.cardcontrol.app.ui.theme.OverlineText
import com.cardcontrol.app.ui.theme.PrimaryAccent
import com.cardcontrol.app.ui.theme.SecondaryAccent
import com.cardcontrol.app.ui.theme.SemanticDanger
import com.cardcontrol.app.ui.theme.SemanticSuccess
import com.cardcontrol.app.ui.theme.TextPrimary
import com.cardcontrol.app.ui.theme.TextSecondary

/**
 * Diálogo modal para criação ou edição de cartão de crédito.
 *
 * Configura o banco emissor para desacoplamento de notificações push (Room v5),
 * apelido customizado pelo usuário, últimos 4 dígitos para desambiguação,
 * limite total em centavos e as datas de corte da fatura (closingDay e dueDay no intervalo 1..28).
 */
@Composable
fun CardDialog(
    cardToEdit: CreditCardEntity?,
    holders: List<HolderEntity>,
    onDismissRequest: () -> Unit,
    onAddCard: (name: String, bank: String, digits: String, limitCents: Long, closingDay: Int, dueDay: Int, holderId: Long?) -> Unit,
    onUpdateCard: (id: Long, name: String, bank: String, digits: String, limitCents: Long, closingDay: Int, dueDay: Int, holderId: Long?) -> Unit
) {
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
        mutableStateOf(if (cardToEdit != null) cardToEdit.defaultHolderId else holders.firstOrNull()?.id)
    }
    var formError by remember(cardToEdit?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                        options = listOf(0L to "Sem vínculo") + holders.map { it.id to it.name },
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
                onClick = onDismissRequest
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
                        val holderId = defaultHolderId?.takeIf { id -> holders.any { it.id == id } }
                        if (cardToEdit == null) {
                            onAddCard(
                                name,
                                selectedBank.id,
                                digits,
                                limitCents,
                                closingDay,
                                dueDay,
                                holderId
                            )
                        } else {
                            onUpdateCard(
                                cardToEdit.id,
                                name,
                                selectedBank.id,
                                digits,
                                limitCents,
                                closingDay,
                                dueDay,
                                holderId
                            )
                        }
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
