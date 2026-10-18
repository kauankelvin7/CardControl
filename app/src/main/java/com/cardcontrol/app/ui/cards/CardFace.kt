package com.cardcontrol.app.ui.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Frente do Cartão Vitrine com Design System 3D e Modelos Personalizados por Banco (Google Wallet style).
 *
 * Invariantes de Design:
 * 1. Proporção física padrão ISO/IEC 7810 ID-1 (1,586:1) e cantos de 24dp (CcShapes.card).
 * 2. Chip EMV e ícone Contactless renderizados via Canvas nativo em Compose.
 * 3. Identidade visual customizada por SupportedBank com texturas sutis e gradientes de alto contraste (WCAG >= 4.5:1).
 * 4. Zero dados sensíveis expostos (apenas últimos 4 dígitos mascarados e nome do titular).
 */
@Composable
fun CardFace(
    card: CardUi,
    isSelected: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val visual = remember(card.bankId) {
        BankCardVisuals.resolveByBankId(card.bankId)
    }

    val palette = remember(card.colorSeed, card.bankId) {
        CardPaletteResolver.resolve(card.colorSeed, card.bankId)
    }

    // Se colorSeed for Auto, utiliza o gradiente rico bespoke do banco; se Custom/Preset, respeita a paleta
    val cardBrush = remember(card.colorSeed, visual, palette) {
        when (card.colorSeed) {
            is CardColorSeed.Auto -> visual.backgroundBrush
            else -> Brush.linearGradient(
                colors = listOf(palette.start, palette.end),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }
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
                        // Tema escuro: borda sutil de 1dp com branco 12% para realçar profundidade
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.14f), CcShapes.card)
                    } else {
                        // Tema claro: sombra tingida e rica pelo tom primário do cartão
                        Modifier.shadow(
                            elevation = 12.dp,
                            shape = CcShapes.card,
                            spotColor = visual.startColor.copy(alpha = 0.40f),
                            ambientColor = Color.Black.copy(alpha = 0.18f)
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
        val isCompactWidth = maxWidth < 320.dp
        val internalPadding = if (isCompactWidth) 14.dp else 18.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = cardBrush)
                .drawBehind {
                    // Brilho specular suave no canto superior-esquerdo
                    val radius = size.width * 0.85f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(0f, 0f)
                    )
                }
        ) {
            // Textura procedural sutil por banco (grade, fibra, arcos ou reflexo)
            BankCardTexture(
                textureType = visual.textureType,
                accentColor = visual.accentColor
            )

            // Reflexo holográfico iridescente de alto padrão com bisel especular
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Borda de chanfro especular superior e esquerda (profundidade física do plástico laminado)
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    start = Offset(0f, 0.5f),
                    end = Offset(w, 0.5f),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawLine(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    start = Offset(0.5f, 0f),
                    end = Offset(0.5f, h),
                    strokeWidth = 1.2.dp.toPx()
                )

                // Feixe holográfico iridescente transversal (efeito foil/metalizado)
                val holoBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1870E0FF), // Ciano iridescente
                        Color(0x28FFFFFF), // Specular puro
                        Color(0x20FF70E0), // Magenta iridescente
                        Color(0x12FFDF7A), // Ouro sutil
                        Color.Transparent
                    ),
                    start = Offset(w * 0.18f, 0f),
                    end = Offset(w * 0.82f, h)
                )
                drawRect(brush = holoBrush)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(internalPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Linha Superior: Monograma/Marca + Nome do Banco e Titular à esquerda; NFC + Virar à direita
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Monograma 38dp com cantos arredondados
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CcShapes.monogram)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.monogram,
                                style = CcType.cardBank.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = card.bankName,
                                style = CcType.cardBank.copy(fontSize = 15.sp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!card.holderName.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial = card.holderName.firstOrNull()?.uppercase() ?: "T"
                                        Text(
                                            text = initial,
                                            style = CcType.cardMeta.copy(
                                                fontSize = 9.sp,
                                                lineHeight = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = card.holderName,
                                        style = CcType.cardHolder.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.88f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Lado direito superior: Ícone Contactless (aproximação) + Botão de Virar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContactlessWave(
                            tint = Color.White.copy(alpha = 0.82f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (isSelected) {
                            IconButton(
                                onClick = onFlip,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.32f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Virar cartão para ver detalhes",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Linha Intermediária: EMV Chip inteligente à esquerda; Sublimite/tipo à direita
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmvChip(isGold = visual.isChipGold)

                    val subtitle = buildString {
                        if (card.nickname.isNotBlank() && card.nickname != card.bankName) {
                            append(card.nickname)
                            append(" · ")
                        }
                        append(card.kind ?: "Crédito")
                    }
                    Text(
                        text = subtitle,
                        style = CcType.cardMeta.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3. Linha Inferior: Número mascarado com tracking amplo + Titular & Bandeira
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (card.lastFourDigits.isNotBlank()) {
                        Text(
                            text = "••••  ••••  ••••  ${card.lastFourDigits}",
                            style = CcType.cardMask.copy(
                                fontSize = if (isCompactWidth) 14.sp else 16.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (card.holderName ?: card.nickname).uppercase(),
                            style = CcType.cardMeta.copy(
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val flagOrBrand = card.flag?.uppercase() ?: visual.brandLabel.uppercase()
                        Box(
                            modifier = Modifier
                                .clip(CcShapes.chip)
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = flagOrBrand,
                                style = CcType.cardMeta.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
