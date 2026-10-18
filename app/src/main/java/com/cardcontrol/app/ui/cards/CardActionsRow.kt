package com.cardcontrol.app.ui.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.ui.designsystem.CcMotion
import com.cardcontrol.app.ui.designsystem.CcType

internal fun shouldStackCardPrimaryActions(availableWidthDp: Float, fontScale: Float): Boolean =
    availableWidthDp < 400f || fontScale > 1.15f

/**
 * Cabeçalho contextual do cartão selecionado com menu ⋮ (C6 e Seção 3.2).
 * Apresenta dados complementares (titular, status) sem redundância com o que já está na frente do cartão.
 */
@Composable
fun CardContextualHeader(
    card: CardUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = card,
        transitionSpec = { fadeIn(CcMotion.effectsDefault()) togetherWith fadeOut(CcMotion.effectsFast()) },
        label = "contextualHeader",
        modifier = modifier.fillMaxWidth()
    ) { currentCard ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val headerTitle = if (!currentCard.holderName.isNullOrBlank()) {
                    "Titular: ${currentCard.holderName}"
                } else {
                    "Cartão ativo"
                }
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Limite: ${com.cardcontrol.app.util.MoneyUtils.formatCents(currentCard.totalLimit)} · Fecha dia ${currentCard.closingDay}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções do cartão",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar cartão") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Excluir cartão",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Duas Ações Primárias Visuais Equilibradas (Seção 3.2):
 * "Lançar compra" e "Ver faturas".
 * Elimina os três blocos gigantes anteriores e a duplicidade com botões adicionais.
 */
@Composable
fun CardPrimaryActions(
    onAddExpense: () -> Unit,
    onSeeInvoices: () -> Unit,
    modifier: Modifier = Modifier,
    brandColor: Color = MaterialTheme.colorScheme.primary
) {
    val fontScale = LocalConfiguration.current.fontScale
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackActions = shouldStackCardPrimaryActions(maxWidth.value, fontScale)
        if (stackActions) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CardPrimaryActionButton(
                    label = "Lançar compra",
                    icon = Icons.Default.Add,
                    primary = true,
                    onClick = onAddExpense,
                    modifier = Modifier.fillMaxWidth(),
                    brandColor = brandColor
                )
                CardPrimaryActionButton(
                    label = "Ver faturas",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    primary = false,
                    onClick = onSeeInvoices,
                    modifier = Modifier.fillMaxWidth(),
                    brandColor = brandColor
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardPrimaryActionButton(
                    label = "Lançar compra",
                    icon = Icons.Default.Add,
                    primary = true,
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f),
                    brandColor = brandColor
                )
                CardPrimaryActionButton(
                    label = "Ver faturas",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    primary = false,
                    onClick = onSeeInvoices,
                    modifier = Modifier.weight(1f),
                    brandColor = brandColor
                )
            }
        }
    }
}

@Composable
private fun CardPrimaryActionButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brandColor: Color = MaterialTheme.colorScheme.primary
) {
    val isLightForeground = brandColor.luminance() < 0.55f
    val foreground = if (primary) {
        if (isLightForeground) Color.White else Color(0xFF11161B)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val backgroundModifier = if (primary) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    brandColor,
                    brandColor.copy(alpha = 0.88f)
                )
            )
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f))
    }

    val borderBrush = if (primary) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.45f),
                brandColor.copy(alpha = 0.80f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(backgroundModifier)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (primary) foreground else brandColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (primary) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = foreground,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Painel Executivo Bento do Cartão Selecionado:
 * Exibe utilização de limite real, datas de fechamento/vencimento e ações rápidas visíveis.
 */
@Composable
fun CardMetricsBentoSection(
    card: CardUi,
    committedCents: Long,
    availableCents: Long,
    onFlipCard: () -> Unit,
    onEditCard: () -> Unit,
    onDeleteCard: () -> Unit,
    modifier: Modifier = Modifier,
    brandColor: Color = MaterialTheme.colorScheme.primary
) {
    val totalLimit = card.totalLimit.coerceAtLeast(0L)
    val usageRatio = if (totalLimit > 0L) {
        (committedCents.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val usagePct = (usageRatio * 100f).toInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bloco 1: Capacidade de Limite do Cartão (Glassmorphism & Brand Accent)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.90f),
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.75f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            brandColor.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIMITE DISPONÍVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = brandColor
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(brandColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$usagePct% em uso",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = brandColor
                    )
                }
            }

            Text(
                text = com.cardcontrol.app.util.MoneyUtils.formatCents(availableCents.coerceAtLeast(0L)),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                if (usageRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(usageRatio)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brandColor)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comprometido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.cardcontrol.app.util.MoneyUtils.formatCents(committedCents),
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Limite total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.cardcontrol.app.util.MoneyUtils.formatCents(totalLimit),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bloco 2: Grid 2 Colunas (Fechamento e Vencimento com Vidro Fosco)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.80f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Fechamento",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Todo dia %02d".format(card.closingDay),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "Melhor compra após esta data",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = brandColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.80f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Vencimento",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Todo dia %02d".format(card.dueDay),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (!card.holderName.isNullOrBlank()) "Titular: ${card.holderName}" else "Fatura mensal",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Bloco 3: Barra de Gestão Rápida do Cartão (Editar / Virar 3D / Excluir)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(role = Role.Button, onClick = onEditCard)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Editar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(role = Role.Button, onClick = onFlipCard)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Girar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.30f))
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onDeleteCard)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Excluir",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
