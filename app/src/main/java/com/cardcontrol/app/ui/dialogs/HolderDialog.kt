package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.util.MoneyUtils
import com.cardcontrol.app.ui.components.GlassTextField
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.ui.designsystem.CcShapes

/**
 * Diálogo modal para cadastro ou edição de titular / pessoa.
 */
@Composable
fun HolderDialog(
    holderToEdit: HolderEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, whats: String, email: String, subLimitCents: Long) -> Unit,
    onUpdateHolder: ((id: Long, name: String, whats: String, email: String, subLimitCents: Long) -> Unit)? = null
) {
    val isEdit = holderToEdit != null
    var name by remember(holderToEdit) { mutableStateOf(holderToEdit?.name ?: "") }
    var whats by remember(holderToEdit) { mutableStateOf(holderToEdit?.whatsapp ?: "") }
    var email by remember(holderToEdit) { mutableStateOf(holderToEdit?.email ?: "") }
    var subLimit by remember(holderToEdit) {
        mutableStateOf(
            if (holderToEdit != null && holderToEdit.subLimit > 0L) {
                String.format(java.util.Locale("pt", "BR"), "%.2f", holderToEdit.subLimit / 100.0)
            } else {
                ""
            }
        )
    }
    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        shape = CcShapes.dialog,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CcShapes.dialog),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEdit) Icons.Default.Edit else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "Editar Pessoa" else "Nova Pessoa",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        formError = null
                    },
                    label = "Nome",
                    placeholder = "Ex: Carlos Silva",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = whats,
                    onValueChange = { whats = it },
                    label = "WhatsApp com DDD (opcional)",
                    placeholder = "Ex: 5511999998888",
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = CCTheme.extras.semantics.success) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail para Alertas (opcional)",
                    placeholder = "carlos@gmail.com",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
                GlassTextField(
                    value = subLimit,
                    onValueChange = { subLimit = it },
                    label = "Sublimite Mensal (R$)",
                    placeholder = "0,00 (vazio ou 0 para sem teto)",
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = CCTheme.extras.semantics.warning) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
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
                    if (name.isNotBlank()) {
                        formError = null
                        val subLimitCents = MoneyUtils.parseToCents(subLimit) ?: 0L
                        if (isEdit && holderToEdit != null && onUpdateHolder != null) {
                            onUpdateHolder(holderToEdit.id, name.trim(), whats.trim(), email.trim(), subLimitCents)
                        } else {
                            onConfirm(name.trim(), whats.trim(), email.trim(), subLimitCents)
                        }
                    } else {
                        formError = "Informe o nome da pessoa."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEdit) "Salvar alterações" else "Salvar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
