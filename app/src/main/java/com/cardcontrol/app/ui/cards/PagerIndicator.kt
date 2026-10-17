package com.cardcontrol.app.ui.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.ui.designsystem.CcMotion

/**
 * Indicador de posição do carrossel de cartões (C5).
 * - 0 ou 1 cartão: Invisível (altura de 16dp reservada para não causar salto de layout).
 * - 2 a 7 cartões: Pílulas animadas com expansão suave para 18x6dp.
 * - 8+ cartões: Contador textual compacto ("3 de 12").
 */
@Composable
fun PagerIndicator(
    totalCards: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            totalCards <= 1 -> {
                // Espaço reservado vazio: previne saltos de layout (C1.2)
            }
            totalCards in 2..7 -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalCards) { index ->
                        val isCurrent = index == selectedIndex
                        val pillWidth by animateDpAsState(
                            targetValue = if (isCurrent) 18.dp else 6.dp,
                            animationSpec = CcMotion.spatialFast(),
                            label = "indicatorWidth"
                        )
                        val color = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }

                        Box(
                            modifier = Modifier
                                .size(width = pillWidth, height = 6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
            else -> {
                // Modo contador textual (8+ cartões)
                val displayPosition = (selectedIndex + 1).coerceIn(1, totalCards)
                Text(
                    text = "$displayPosition de $totalCards",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
