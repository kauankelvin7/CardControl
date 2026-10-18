package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcType

/**
 * Diálogo modal de confirmação para exclusão segura de pessoa / titular.
 */
@Composable
fun DeleteHolderDialog(
    holder: HolderEntity,
    onDismissRequest: () -> Unit,
    onConfirm: (holderId: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        shape = CcShapes.dialog,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CcShapes.dialog),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Remover pessoa?",
                    style = CcType.dialogTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Text(
                text = "A pessoa \"${holder.name}\" será removida se não possuir compras ou pagamentos vinculados. Os cartões permanecerão cadastrados sem titular padrão.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Cancelar",
                    style = CcType.buttonLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(holder.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = CcShapes.button
            ) {
                Text(
                    text = "Excluir",
                    style = CcType.buttonLabel,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
