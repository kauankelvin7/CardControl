package com.cardcontrol.app.ui.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.cardcontrol.app.ui.designsystem.LocalCcReduceMotion
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Carrossel interativo de cartões baseado em HorizontalPager estável por ID (C4 e D2.3).
 *
 * Contrato de Seleção (C4.3):
 * 1. O ViewModel/tela controla o `selectedCardId`.
 * 2. O Pager notifica seleção somente ao assentar (settledPage).
 * 3. Mudanças externas reposicionam o pager com scrollToPage sem animação, prevenindo loops.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCarousel(
    cards: List<CardUi>,
    selectedCardId: Long?,
    onCardSelected: (Long) -> Unit,
    flipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) return

    val currentCards by rememberUpdatedState(cards)
    val currentSelectedId by rememberUpdatedState(selectedCardId)
    val scope = rememberCoroutineScope()
    val reduceMotion = LocalCcReduceMotion.current

    val initialIndex = remember {
        val found = cards.indexOfFirst { it.id == selectedCardId }
        if (found >= 0) found else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { currentCards.size }
    )

    // Modelo -> Pager: Sincroniza sem animação quando o ID selecionado muda externamente
    LaunchedEffect(selectedCardId, cards) {
        val targetIndex = cards.indexOfFirst { it.id == selectedCardId }
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    // Pager -> Modelo: Notifica a nova seleção APENAS quando o pager assenta (C4.3)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                val cardAtSettled = currentCards.getOrNull(settled)
                if (cardAtSettled != null && cardAtSettled.id != currentSelectedId) {
                    onCardSelected(cardAtSettled.id)
                }
            }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth

        // Geometria de Peek conforme largura disponível (C4.1)
        val (horizontalPadding: Dp, pageSpacing: Dp, cardMaxWidth: Dp) = when {
            cards.size <= 1 -> {
                // Com apenas 1 cartão: centralizado sem peek assimétrico
                Triple((availableWidth - 360.dp.coerceAtMost(availableWidth - 32.dp)) / 2, 0.dp, 400.dp)
            }
            availableWidth < 360.dp -> {
                Triple(32.dp, 12.dp, availableWidth - 64.dp)
            }
            availableWidth < 600.dp -> {
                Triple(40.dp, 12.dp, availableWidth - 80.dp)
            }
            else -> {
                val pad = ((availableWidth - 400.dp) / 2).coerceAtLeast(32.dp)
                Triple(pad, 16.dp, 400.dp)
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
            pageSpacing = pageSpacing,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1)
            ),
            key = { index -> currentCards.getOrNull(index)?.id ?: index.toLong() },
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val card = currentCards.getOrNull(page) ?: return@HorizontalPager
            val isSelected = card.id == selectedCardId

            // Transformações gráficas lidas exclusivamente na fase de desenho (C4.2 e D3)
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val offset = pageOffset.absoluteValue.coerceIn(0f, 1f)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (!reduceMotion) {
                            val scale = lerp(1f, 0.90f, offset)
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(1f, 0.68f, offset)
                            translationY = lerp(0f, 8.dp.toPx(), offset)
                            // Inclinação 3D sutil no eixo Y durante a rolagem para efeito de profundidade
                            rotationY = (-pageOffset * 9f).coerceIn(-14f, 14f)
                            cameraDistance = 14f * density
                        } else {
                            scaleX = 1f
                            scaleY = 1f
                            rotationY = 0f
                            alpha = if (isSelected) 1f else 0.72f
                            translationY = 0f
                        }
                    }
                    .zIndex(1f - offset)
            ) {
                Box(modifier = Modifier.width(cardMaxWidth)) {
                    FlippableCard(
                        card = card,
                        isSelected = isSelected,
                        flipped = flipped && isSelected,
                        onFlip = onFlip,
                        onClickCard = {
                            if (!isSelected) {
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        }
                    )
                }
            }
        }
    }
}
