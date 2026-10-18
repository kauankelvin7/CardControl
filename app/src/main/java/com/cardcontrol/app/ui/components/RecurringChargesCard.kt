package com.cardcontrol.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.ui.designsystem.CCTheme
import com.cardcontrol.app.util.ReportEngine
import com.cardcontrol.app.ui.theme.CurrencyText
import com.cardcontrol.app.ui.theme.OverlineText

/**
 * Card visual para exibição e gestão de assinaturas e despesas recorrentes detectadas.
 *
 * Permite ao usuário inspecionar o comprometimento mensal acumulado de gastos fixos,
 * o percentual de impacto no limite global e alternar o status ativo/pausado de cada
 * recorrência. Destaca visualmente variações de preço para cima (price creep).
 */
@Composable
fun RecurringChargesCard(
    recurringCharges: List<RecurringChargeEntity>,
    totalMonthlyRecurring: Long,
    limitImpactPercent: Double,
    priceCreepAlertCount: Int,
    onToggleActive: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val colors = MaterialTheme.colorScheme
    val semantics = CCTheme.extras.semantics

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = if (priceCreepAlertCount > 0) semantics.warning else colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Assinaturas & Recorrências",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (priceCreepAlertCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = semantics.warningContainer,
                            border = BorderStroke(1.dp, semantics.warning)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Alerta de aumento",
                                    tint = semantics.warning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "$priceCreepAlertCount alta",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = semantics.warning
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = colors.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    OverlineText("Comprometimento Mensal")
                    CurrencyText(
                        amountCents = totalMonthlyRecurring,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    OverlineText("Impacto no Limite")
                    Text(
                        String.format(java.util.Locale.US, "%.1f%%", limitImpactPercent),
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = if (limitImpactPercent > 30.0) semantics.warning else colors.primary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = colors.outlineVariant)

                    if (recurringCharges.isEmpty()) {
                        Text(
                            "Nenhuma assinatura detectada ainda. Compras recorrentes a cada ~30 dias serão identificadas automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    } else {
                        recurringCharges.forEach { charge ->
                            val hasCreep = charge.previousAmount != null && charge.lastAmount > charge.previousAmount
                            val delta = if (hasCreep) charge.lastAmount - charge.previousAmount!! else 0L

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            charge.merchant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (charge.isActive) colors.onSurface else colors.onSurfaceVariant
                                        )
                                        if (hasCreep && charge.isActive) {
                                            Surface(
                                                shape = CircleShape,
                                                    color = colors.errorContainer,
                                                    border = BorderStroke(1.dp, colors.error)
                                            ) {
                                                Text(
                                                    "+${ReportEngine.formatCurrency(delta)}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.error
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "Ciclo ~${charge.billingCycleDays} dias • ${if (charge.isActive) "Ativa" else "Pausada"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CurrencyText(
                                        amountCents = charge.lastAmount,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (charge.isActive) colors.primary else colors.onSurfaceVariant
                                    )
                                    Switch(
                                        checked = charge.isActive,
                                        onCheckedChange = { active -> onToggleActive(charge.id, active) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.primary,
                                            checkedTrackColor = colors.primaryContainer,
                                            uncheckedThumbColor = colors.onSurfaceVariant,
                                            uncheckedTrackColor = colors.surfaceContainerHigh
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
