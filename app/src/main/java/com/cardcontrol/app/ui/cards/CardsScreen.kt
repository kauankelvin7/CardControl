package com.cardcontrol.app.ui.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.ui.designsystem.CcMotion
import com.cardcontrol.app.ui.designsystem.CcShapes
import com.cardcontrol.app.ui.designsystem.CcSpace
import com.cardcontrol.app.ui.designsystem.CcType
import com.cardcontrol.app.ui.designsystem.LocalCcReduceMotion

/**
 * Tela de Cartões Vitrine Oficial (C1, C4, C6, C7, C8).
 * Apresenta o carrossel interativo, tingimento de palco dinâmico e ações contextuais reais.
 */
@Composable
fun CardsScreen(
    cards: List<CardUi>,
    selectedCardId: Long?,
    onCardSelected: (Long) -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (CardUi) -> Unit,
    onDeleteCard: (CardUi) -> Unit,
    onAddExpenseForCard: (CardUi) -> Unit,
    onSeeMore: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val selectedCard = remember(cards, selectedCardId) {
        cards.firstOrNull { it.id == selectedCardId } ?: cards.firstOrNull()
    }

    // Estado do flip 3D persistido por ID do cartão selecionado (C3)
    var flippedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    val isFlipped = selectedCard?.id != null && flippedCardId == selectedCard.id

    val selectedPalette = remember(selectedCard?.colorSeed, selectedCard?.bankId) {
        if (selectedCard != null) {
            CardPaletteResolver.resolve(selectedCard.colorSeed, selectedCard.bankId)
        } else {
            CcCardPresets.Grafite
        }
    }

    // Tingimento sutil do palco no terço superior (B4.6)
    val stageColor by animateColorAsState(
        targetValue = CardPaletteResolver.stageTint(
            surface = MaterialTheme.colorScheme.surface,
            cardStart = selectedPalette.start,
            isDark = isDark
        ),
        animationSpec = CcMotion.effectsSlow(),
        label = "stageTint"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        stageColor,
                        MaterialTheme.colorScheme.surface
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        if (cards.isEmpty()) {
            EmptyCardsState(
                onAddCard = onAddCard,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            )
        } else {
            val selectedIndex = cards.indexOfFirst { it.id == selectedCard?.id }.coerceAtLeast(0)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 112.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                // 1. Carrossel de Cartões (C4)
                CardCarousel(
                    cards = cards,
                    selectedCardId = selectedCard?.id,
                    onCardSelected = { newId ->
                        // Cartão que perde a seleção volta imediatamente à frente (D-07)
                        if (flippedCardId != null && flippedCardId != newId) {
                            flippedCardId = null
                        }
                        onCardSelected(newId)
                    },
                    flipped = isFlipped,
                    onFlip = {
                        flippedCardId = if (isFlipped) null else selectedCard?.id
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 2. Indicador de Posição (C5)
                PagerIndicator(
                    totalCards = cards.size,
                    selectedIndex = selectedIndex
                )

                Spacer(Modifier.height(16.dp))

                // 3. Cabeçalho Contextual (C6)
                if (selectedCard != null) {
                    CardContextualHeader(
                        card = selectedCard,
                        onEdit = { onEditCard(selectedCard) },
                        onDelete = { onDeleteCard(selectedCard) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // 4. Duas Ações Primárias Contextuais Equilibradas (C7 e Seção 3.2)
                    CardPrimaryActions(
                        card = selectedCard,
                        onAddExpense = { onAddExpenseForCard(selectedCard) },
                        onSeeInvoices = onSeeMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Estado Vazio de Cartões conforme especificação C11.
 * Contém cartão-fantasma tracejado, CTA primário e aviso de privacidade.
 */
@Composable
fun EmptyCardsState(
    onAddCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cartão-fantasma tracejado com raio de 24dp e proporção 1,586:1 (C11)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .aspectRatio(1.586f)
                .clip(CcShapes.card)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CcShapes.card
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = "Nenhum cartão cadastrado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Cadastre seus cartões para acompanhá-los por banco e por pessoa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onAddCard,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Cadastrar cartão", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "O CardControl organiza seus cartões. Ele não faz pagamentos nem acessa sua conta bancária.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
