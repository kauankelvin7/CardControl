package com.cardcontrol.app.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcType

/**
 * Frente do Cartão Vitrine (C2).
 * Proporção 1,586:1, cantos 24dp, gradiente linear 135° e sheen branco 8% no topo-esquerdo.
 */
@Composable
fun CardFace(
    card: CardUi,
    isSelected: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val palette = remember(card.colorSeed, card.bankId) {
        CardPaletteResolver.resolve(card.colorSeed, card.bankId)
    }

    val contentDesc = buildString {
        append("Cartão ${card.bankName}")
        card.holderName?.let { append(", titular $it") }
        if (card.lastFourDigits.isNotBlank()) append(", final ${card.lastFourDigits}")
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .then(
                if (isSelected) {
                    if (isDark) {
                        // Tema escuro: sem sombra pesada; borda sutil de 1dp com branco 10%
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), CcShapes.card)
                    } else {
                        // Tema claro: sombra tingida pela cor do cartão (B4.3)
                        Modifier.shadow(
                            elevation = 10.dp,
                            shape = CcShapes.card,
                            spotColor = palette.start.copy(alpha = 0.45f),
                            ambientColor = Color.Black.copy(alpha = 0.16f)
                        )
                    }
                } else {
                    if (isDark) {
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.06f), CcShapes.card)
                    } else {
                        Modifier.shadow(3.dp, shape = CcShapes.card, ambientColor = Color.Black.copy(alpha = 0.10f))
                    }
                }
            )
            .clip(CcShapes.card)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDesc
                this.stateDescription = "Mostrando a frente"
                if (isSelected) {
                    this.role = Role.Button
                }
            }
    ) {
        val internalPadding = if (maxWidth < 300.dp) 16.dp else 20.dp

        // Fundo com Gradiente Linear 135° e Brilho Radial 8% no canto superior-esquerdo (B4.1)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(palette.start, palette.end),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .drawBehind {
                    // Sheen / Brilho branco 8% no canto superior-esquerdo
                    val radius = size.width * 0.9f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(0f, 0f)
                    )
                }
                .padding(internalPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Topo: Monograma + Nome do Banco + Titular + Botão Virar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Monograma 40dp (B8)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CcShapes.monogram)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.monogram,
                                style = CcType.cardBank.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = card.bankName,
                                style = CcType.cardBank,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!card.holderName.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Avatar de iniciais do titular (20dp círculo)
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial = card.holderName.firstOrNull()?.uppercase() ?: "T"
                                        Text(
                                            text = initial,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = card.holderName,
                                        style = CcType.cardHolder,
                                        color = Color.White.copy(alpha = 0.88f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Botão Virar visível apenas no cartão selecionado (C2)
                    if (isSelected) {
                        IconButton(
                            onClick = onFlip,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Virar cartão",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Base: Apelido / Tipo + Bandeira e Final Mascarado (C2)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subtitle = buildString {
                            if (card.nickname.isNotBlank() && card.nickname != card.bankName) {
                                append(card.nickname)
                                append(" · ")
                            }
                            append(card.kind ?: "Crédito")
                        }
                        Text(
                            text = subtitle,
                            style = CcType.cardMeta,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!card.flag.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(CcShapes.chip)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = card.flag.uppercase(),
                                    style = CcType.cardMeta.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (card.lastFourDigits.isNotBlank()) {
                        Text(
                            text = card.maskedNumber,
                            style = CcType.cardMask,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
