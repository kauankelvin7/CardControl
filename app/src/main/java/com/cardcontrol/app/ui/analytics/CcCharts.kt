package com.cardcontrol.app.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.domain.analytics.CategorySpendingDonut
import com.cardcontrol.app.domain.analytics.MerchantRank
import com.cardcontrol.app.domain.analytics.ModalitySpending
import com.cardcontrol.app.domain.analytics.MonthlySpendingBar
import com.cardcontrol.app.domain.analytics.SpendingCategory
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcType
import com.cardcontrol.app.ui.designsystem.LocalCcReduceMotion
import com.cardcontrol.app.util.MoneyUtils
import kotlin.math.atan2

/**
 * Gráficos nativos 100% Jetpack Compose Canvas (CcCharts).
 * Zero dependências externas, alto desempenho, interatividade tátil e suporte a TalkBack / WCAG 2.1.
 */

/**
 * Gráfico de Barras interativo de Tendência Mensal.
 */
@Composable
fun CcBarChart(
    data: List<MonthlySpendingBar>,
    selectedIndex: Int?,
    onBarSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxAmount = remember(data) { data.maxOfOrNull { it.amountCents }?.coerceAtLeast(1L) ?: 1L }
    val primaryColor = MaterialTheme.colorScheme.primary
    val subtleColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val count = data.size
                        if (count > 0) {
                            val slotWidth = size.width / count
                            val clicked = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                            onBarSelect(clicked)
                        }
                    }
                }
                .semantics {
                    contentDescription = "Gráfico de barras de gastos mensais com ${data.size} meses"
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val count = data.size
                val slotWidth = w / count
                val barWidth = (slotWidth * 0.55f).coerceIn(12.dp.toPx(), 36.dp.toPx())
                val bottomAxisY = h - 22.dp.toPx()
                val chartHeight = bottomAxisY - 16.dp.toPx()

                // Linhas de grade horizontais sutis para profundidade de gráfico fintech
                val gridLines = 3
                for (g in 1..gridLines) {
                    val gridY = bottomAxisY - (chartHeight * (g.toFloat() / (gridLines + 1)))
                    drawLine(
                        color = subtleColor.copy(alpha = 0.20f),
                        start = Offset(0f, gridY),
                        end = Offset(w, gridY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Linha de base horizontal sutil
                drawLine(
                    color = subtleColor.copy(alpha = 0.5f),
                    start = Offset(0f, bottomAxisY),
                    end = Offset(w, bottomAxisY),
                    strokeWidth = 1.dp.toPx()
                )

                // Renderiza cada barra mensal com gradientes luminosos e cantos arredondados
                for (i in 0 until count) {
                    val item = data[i]
                    val isSelected = (selectedIndex == i)
                    val centerX = (i * slotWidth) + (slotWidth / 2f)

                    val barHeight = if (maxAmount > 0) {
                        (item.amountCents.toFloat() / maxAmount.toFloat()) * chartHeight
                    } else 0f

                    val topY = bottomAxisY - barHeight.coerceAtLeast(4.dp.toPx())

                    val barBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.65f)
                            )
                        } else {
                            listOf(
                                primaryColor.copy(alpha = 0.45f),
                                primaryColor.copy(alpha = 0.12f)
                            )
                        },
                        startY = topY,
                        endY = bottomAxisY
                    )

                    val barCornerRadius = (barWidth / 3f).coerceAtMost(8.dp.toPx())

                    // Barra com cantos superiores arredondados e gradiente suave
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(centerX - (barWidth / 2f), topY),
                        size = Size(barWidth, bottomAxisY - topY),
                        cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                    )

                    // Destaque superior luminoso na barra ativa
                    if (isSelected && barHeight > 6.dp.toPx()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.70f),
                            start = Offset(centerX - (barWidth / 2f) + 2.dp.toPx(), topY + 1.dp.toPx()),
                            end = Offset(centerX + (barWidth / 2f) - 2.dp.toPx(), topY + 1.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Ponto indicador sob a barra selecionada
                    if (isSelected) {
                        drawCircle(
                            color = primaryColor,
                            radius = 3.dp.toPx(),
                            center = Offset(centerX, bottomAxisY + 8.dp.toPx())
                        )
                    }
                }
            }
        }

        // Rótulos dos meses abaixo das barras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Text(
                    text = item.label,
                    style = CcType.cardMeta.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onBarSelect(index) }
                )
            }
        }
    }
}

/**
 * Gráfico Donut interativo de Distribuição por Categoria.
 */
@Composable
fun CcDonutChart(
    categories: List<CategorySpendingDonut>,
    totalCents: Long,
    selectedCategory: SpendingCategory?,
    onCategorySelect: (SpendingCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty() || totalCents <= 0L) return

    val strokeWidth = 18.dp
    val gapAngle = 2.5f

    Box(
        modifier = modifier
            .size(155.dp)
            .semantics {
                contentDescription = "Gráfico de rosca de distribuição por categorias"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(categories) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < 0f) angle += 360f

                        // Mapeia o ângulo tocado para a categoria correspondente
                        var currentStart = -90f
                        if (currentStart < 0f) currentStart += 360f

                        var cumulative = 0f
                        var hitCategory: SpendingCategory? = null

                        for (item in categories) {
        val sweep = (item.percentageBps / 10_000f * 360f).coerceAtLeast(1f)
                            val start = (cumulative - 90f + 360f) % 360f
                            val end = (start + sweep) % 360f

                            val inSlice = if (start <= end) {
                                angle in start..end
                            } else {
                                angle >= start || angle <= end
                            }

                            if (inSlice) {
                                hitCategory = item.category
                                break
                            }
                            cumulative += sweep
                        }

                        if (hitCategory != null) {
                            onCategorySelect(if (selectedCategory == hitCategory) null else hitCategory)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val strokePx = strokeWidth.toPx()
            val diameter = w.coerceAtMost(h) - strokePx
            val topLeft = Offset((w - diameter) / 2f, (h - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Iluminação ambiente sutil no miolo do Donut ao selecionar categoria
            val activeColor = categories.firstOrNull { it.category == selectedCategory }?.category?.color
            if (activeColor != null) {
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(activeColor.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(w / 2f, h / 2f),
                        radius = (diameter / 2f) * 0.85f
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = (diameter / 2f) * 0.85f
                )
            }

            var startAngle = -90f

            for (item in categories) {
        val sweep = (item.percentageBps / 10_000f * 360f) - gapAngle
                val isSelected = (selectedCategory == item.category)

                val effectiveStroke = if (isSelected) strokePx + 4.dp.toPx() else strokePx
                val color = if (selectedCategory == null || isSelected) {
                    item.category.color
                } else {
                    item.category.color.copy(alpha = 0.35f)
                }

                drawArc(
                    color = color,
                    startAngle = startAngle + (gapAngle / 2f),
                    sweepAngle = sweep.coerceAtLeast(2f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
                )

            startAngle += (item.percentageBps / 10_000f * 360f)
            }
        }

        // Centro do Donut: Exibe total ou valor da categoria selecionada
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            val activeItem = categories.firstOrNull { it.category == selectedCategory }
            val displayLabel = activeItem?.category?.displayName ?: "Total"
            val displayAmount = activeItem?.amountCents ?: totalCents

            Text(
                text = displayLabel,
                style = CcType.cardMeta.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = MoneyUtils.formatCents(displayAmount),
                style = CcType.cardMask.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (activeItem != null) {
                Text(
        text = "${activeItem.percentageBps / 100}%",
                    style = CcType.cardMeta.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = activeItem.category.color
                )
            }
        }
    }
}

/**
 * Barra de Modalidade de Gastos (Recorrente vs Parcelado vs À Vista).
 */
@Composable
fun CcModalityBar(
    modality: ModalitySpending,
    modifier: Modifier = Modifier
) {
    val total = (modality.recorrenteCents + modality.parceladoCents + modality.aVistaCents).coerceAtLeast(1L)
    val recPct = modality.recorrenteCents.toFloat() / total.toFloat()
    val parcPct = modality.parceladoCents.toFloat() / total.toFloat()
    val vistaPct = modality.aVistaCents.toFloat() / total.toFloat()

    val recColor = Color(0xFF1565C0)  // Safira
    val parcColor = Color(0xFFC64A00) // Cobre / Laranja
    val vistaColor = Color(0xFF006972) // Petróleo

    Column(modifier = modifier.fillMaxWidth()) {
        // Barra horizontal particionada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (recPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(recPct.coerceAtLeast(0.01f))
                        .background(recColor)
                )
            }
            if (parcPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(parcPct.coerceAtLeast(0.01f))
                        .background(parcColor)
                )
            }
            if (vistaPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(vistaPct.coerceAtLeast(0.01f))
                        .background(vistaColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legenda com valores e porcentagens
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModalityLegendItem("Assinaturas", modality.recorrenteCents, recColor, (recPct * 100).toInt())
            ModalityLegendItem("Parcelado", modality.parceladoCents, parcColor, (parcPct * 100).toInt())
            ModalityLegendItem("À vista", modality.aVistaCents, vistaColor, (vistaPct * 100).toInt())
        }
    }
}

@Composable
private fun ModalityLegendItem(
    title: String,
    amountCents: Long,
    color: Color,
    pct: Int
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$title ($pct%)",
                style = CcType.cardMeta.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = MoneyUtils.formatCents(amountCents),
            style = CcType.cardMeta.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 13.dp)
        )
    }
}

/**
 * Ranking de Maiores Estabelecimentos.
 */
@Composable
fun CcMerchantRanking(
    merchants: List<MerchantRank>,
    modifier: Modifier = Modifier
) {
    if (merchants.isEmpty()) return

    val maxAmount = merchants.maxOfOrNull { it.amountCents }?.coerceAtLeast(1L) ?: 1L

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        merchants.forEachIndexed { index, item ->
            val ratio = (item.amountCents.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = CcType.cardMeta.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.merchant,
                                style = CcType.buttonLabel.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = MoneyUtils.formatCents(item.amountCents),
                            style = CcType.cardMeta.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Barra de progresso proporcional
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .clip(RoundedCornerShape(3.dp))
                                .background(item.category.color)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.category.displayName,
                            style = CcType.cardMeta.copy(fontSize = 10.sp),
                            color = item.category.color
                        )
                        Text(
                            text = "${item.count} compra${if (item.count > 1) "s" else ""}",
                            style = CcType.cardMeta.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
