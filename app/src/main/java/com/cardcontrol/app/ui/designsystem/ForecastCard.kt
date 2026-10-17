package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * ForecastCard & RecurringChargesSummary — Mini gráficos e projeções futuras (Wireframe S1).
 *
 * Corrige P1: Os rótulos de comprometimento mensal e impacto no limite
 * possuem colunas com larguras e hierarquias bem definidas, eliminando a colisão "MENSALLIMITE".
 */

@Composable
fun ForecastCard(
    modifier: Modifier = Modifier
) {
    val months = listOf("Out" to 0.85f, "Nov" to 0.65f, "Dez" to 0.50f, "Jan" to 0.40f, "Fev" to 0.30f, "Mar" to 0.15f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CCTheme.colors.surface1)
            .border(1.dp, CCTheme.colors.outlineSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Próximos 6 meses",
                style = CCTheme.typography.titleSm.copy(color = CCTheme.colors.textPrimary)
            )
            Text(
                text = "F6 Projeção",
                style = CCTheme.typography.bodySm.copy(color = CCTheme.colors.textTertiary)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            months.forEachIndexed { index, (month, heightFraction) ->
                val isCurrent = index == 0
                val barColor = if (isCurrent) CCTheme.colors.accentDynamic else CCTheme.colors.surface3

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(heightFraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor)
                    )
                    Text(
                        text = month,
                        style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.textTertiary),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecurringChargesSummaryCard(
    monthlyCents: Long,
    limitImpactPercentage: Double,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CCTheme.colors.surface1)
            .border(1.dp, CCTheme.colors.outlineSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "COMPROMETIMENTO MENSAL",
                style = CCTheme.typography.labelCaps.copy(color = CCTheme.colors.textTertiary)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MoneyText(
                    cents = monthlyCents,
                    style = CCTheme.typography.numLg,
                    hidden = hidden
                )
                Text(
                    text = "/ mês",
                    style = CCTheme.typography.bodySm.copy(color = CCTheme.colors.textSecondary)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "IMPACTO NO LIMITE",
                style = CCTheme.typography.labelCaps.copy(color = CCTheme.colors.textTertiary)
            )
            Text(
                text = String.format(java.util.Locale("pt", "BR"), "%.1f%%", limitImpactPercentage),
                style = CCTheme.typography.numLg.copy(color = CCTheme.colors.warning)
            )
        }
    }
}
