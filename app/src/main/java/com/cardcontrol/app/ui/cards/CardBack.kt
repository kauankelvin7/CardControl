package com.cardcontrol.app.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcType
import com.cardcontrol.app.util.MoneyUtils

/**
 * Verso do Cartão Vitrine (C3).
 * Gradiente de verso (back0 -> back1) mais escuro, exibindo apenas informações reais de domínio.
 * Proibido: CVV, chip falso, tarja magnética ou símbolo de aproximação (D-10 e C3).
 */
@Composable
fun CardBack(
    card: CardUi,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = remember(card.bankId) {
        BankCardVisuals.resolveByBankId(card.bankId)
    }

    val palette = remember(card.colorSeed, card.bankId) {
        CardPaletteResolver.resolve(card.colorSeed, card.bankId)
    }

    val backBrush = remember(card.colorSeed, visual, palette) {
        when (card.colorSeed) {
            is CardColorSeed.Auto -> Brush.linearGradient(
                colors = listOf(visual.backStart, visual.backEnd),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
            else -> Brush.linearGradient(
                colors = listOf(palette.back0, palette.back1),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .border(1.dp, Color.White.copy(alpha = 0.12f), CcShapes.card)
            .clip(CcShapes.card)
            .semantics(mergeDescendants = true) {
                this.contentDescription = "Verso do cartão ${card.bankName}, limite de ${MoneyUtils.formatCents(card.totalLimit)}, fecha dia ${card.closingDay}, vence dia ${card.dueDay}"
                this.stateDescription = "Mostrando o verso"
            }
    ) {
        val internalPadding = if (maxWidth < 300.dp) 16.dp else 20.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backBrush)
                .padding(internalPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabeçalho do Verso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Detalhes da Fatura",
                            style = CcType.buttonLabel.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = card.bankName,
                            style = CcType.cardMeta,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }

                    IconButton(
                        onClick = onFlip,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Voltar à frente",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Grade 2x2 com dados reais (C3)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIMITE TOTAL",
                                style = CcType.cardMeta.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
                                color = Color.White.copy(alpha = 0.70f)
                            )
                            Text(
                                text = MoneyUtils.formatCents(card.totalLimit),
                                style = CcType.cardBank.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TITULAR",
                                style = CcType.cardMeta.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
                                color = Color.White.copy(alpha = 0.70f)
                            )
                            Text(
                                text = card.holderName ?: "Não vinculado",
                                style = CcType.cardBank.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FECHAMENTO",
                                style = CcType.cardMeta.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
                                color = Color.White.copy(alpha = 0.70f)
                            )
                            Text(
                                text = "Todo dia ${card.closingDay}",
                                style = CcType.cardMeta.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VENCIMENTO",
                                style = CcType.cardMeta.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
                                color = Color.White.copy(alpha = 0.70f)
                            )
                            Text(
                                text = "Todo dia ${card.dueDay}",
                                style = CcType.cardMeta.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                color = Color.White
                            )
                        }
                    }
                }

                // Rodapé discreto de toque
                Text(
                    text = "Toque no botão para voltar à frente",
                    style = CcType.cardMeta.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.60f)
                )
            }
        }
    }
}
