package com.cardcontrol.app.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Medidor de Limite Canônico (LimitMeter — Etapa 5.2.2 & 5.10).
 *
 * Corrige P12:
 * - Na visão resumida exibe arredondamento contábil half-up (ex: 87%).
 * - No detalhe exibe exatamente uma casa decimal (ex: 86,9%).
 * - Limiares semânticos: <70% neutro/accent, 70-89% warning, >=90% danger.
 */

@Composable
fun LimitMeter(
    usedCents: Long,
    totalLimitCents: Long,
    modifier: Modifier = Modifier,
    showOneDecimal: Boolean = false,
    hidden: Boolean = false
) {
    val progress = if (totalLimitCents > 0L) {
        (usedCents.toFloat() / totalLimitCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percentageDouble = if (totalLimitCents > 0L) {
        (usedCents.toDouble() / totalLimitCents.toDouble()) * 100.0
    } else 0.0

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "LimitMeterProgress"
    )

    val fillColor = when {
        percentageDouble >= 90.0 -> CCTheme.colors.danger
        percentageDouble >= 70.0 -> CCTheme.colors.warning
        else -> CCTheme.colors.accent
    }

    val freeCents = (totalLimitCents - usedCents).coerceAtLeast(0L)

    val percentageLabel = if (showOneDecimal) {
        String.format(java.util.Locale("pt", "BR"), "Usado %.1f%%", percentageDouble)
    } else {
        "Usado ${percentageDouble.roundToInt()}%"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Trilho de 7dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(CCTheme.colors.surface3)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(fillColor)
            )
        }

        // Rótulos externos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = percentageLabel,
                style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.textSecondary)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Livre",
                    style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.textTertiary)
                )
                MoneyText(
                    cents = freeCents,
                    style = CCTheme.typography.numSm,
                    hidden = hidden
                )
            }
        }
    }
}
