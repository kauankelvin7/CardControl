package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * PersonRow — Linha da Pessoa / Rateio (Etapa 5.10).
 */

@Composable
fun PersonRow(
    name: String,
    initial: String,
    avatarColor: Color,
    amountCents: Long,
    sublimitCents: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    val usagePercentage = if (sublimitCents > 0L) {
        ((amountCents.toDouble() / sublimitCents.toDouble()) * 100.0).toInt()
    } else 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CCTheme.colors.surface1)
            .border(1.dp, CCTheme.colors.outlineSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar Okabe-Ito (5.2.3)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.18f))
                    .border(1.dp, avatarColor.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = CCTheme.typography.titleSm.copy(color = avatarColor)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = CCTheme.typography.titleSm.copy(color = CCTheme.colors.textPrimary)
                )
                Text(
                    text = "Sublimite R$ ${(sublimitCents / 100)} ($usagePercentage%)",
                    style = CCTheme.typography.bodySm.copy(color = CCTheme.colors.textTertiary)
                )
            }
        }

        MoneyText(
            cents = amountCents,
            style = CCTheme.typography.numMd,
            hidden = hidden
        )
    }
}
