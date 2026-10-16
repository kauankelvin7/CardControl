package com.cardcontrol.app.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.ui.theme.AppBackgroundTop
import com.cardcontrol.app.ui.theme.GlassBorderTopLeft
import com.cardcontrol.app.ui.theme.SemanticDanger
import com.cardcontrol.app.ui.theme.TextPrimary
import com.cardcontrol.app.ui.theme.TextSecondary

/**
 * Diálogo modal de confirmação para exclusão de cartão de crédito.
 *
 * Alerta o usuário de que a exclusão só será permitida se o cartão não possuir
 * transações, faturas ou parcelas vinculadas, garantindo integridade referencial.
 */
@Composable
fun DeleteCardDialog(
    card: CreditCardEntity,
    onDismissRequest: () -> Unit,
    onConfirm: (cardId: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                "O cartão \"${card.name}\" só será excluído se não tiver compras, parcelas ou pagamentos vinculados. Assim, o histórico financeiro não será apagado por engano.",
                color = TextSecondary
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(card.id) },
                colors = ButtonDefaults.buttonColors(containerColor = SemanticDanger, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Excluir cartão", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
