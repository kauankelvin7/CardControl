package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * HeroBalanceCard — Cartão Principal da Fatura (Etapa 5.10).
 *
 * Resolve P3: Os botões Abater/Pix e Compartilhar são secundários tonais,
 * deixando o FAB Menu como único primário evidente na tela.
 */

@Composable
fun HeroBalanceCard(
    invoiceTitle: String,
    statusText: String,
    amountCents: Long,
    closingAndDueDateText: String,
    usedLimitCents: Long,
    totalLimitCents: Long,
    onAbaterClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CCTheme.colors.surface1)
            .border(1.dp, CCTheme.colors.outlineSubtle, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho: Título em Caps + Status Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = invoiceTitle.uppercase(),
                style = CCTheme.typography.labelCaps.copy(color = CCTheme.colors.textTertiary)
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CCTheme.colors.successContainer)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "● $statusText",
                    style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.success)
                )
            }
        }

        // Valor da Fatura (displayHero)
        MoneyText(
            cents = amountCents,
            style = CCTheme.typography.displayHero,
            hidden = hidden
        )

        // Subtítulo informativo
        Text(
            text = closingAndDueDateText,
            style = CCTheme.typography.bodySm.copy(color = CCTheme.colors.textSecondary)
        )

        // Medidor de Limite
        LimitMeter(
            usedCents = usedLimitCents,
            totalLimitCents = totalLimitCents,
            hidden = hidden
        )

        // Ações Secundárias Tonais (P3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onAbaterClick,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CCTheme.colors.surface2,
                    contentColor = CCTheme.colors.textPrimary
                )
            ) {
                Text(
                    text = "Registrar Pagamento",
                    style = CCTheme.typography.labelMd
                )
            }

            FilledTonalButton(
                onClick = onShareClick,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CCTheme.colors.surface2,
                    contentColor = CCTheme.colors.textPrimary
                )
            ) {
                Text(
                    text = "Compartilhar",
                    style = CCTheme.typography.labelMd
                )
            }
        }
    }
}
