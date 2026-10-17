package com.cardcontrol.app.ui.cards

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.cardcontrol.app.ui.designsystem.CcMotion
import com.cardcontrol.app.ui.designsystem.LocalCcReduceMotion

/**
 * Cartão com efeito Flip 3D no eixo Y (C3 e D2.3).
 *
 * Invariantes da mecânica:
 * 1. Apenas uma face existe na composição por vez (showBack derivado de rotation > 90f).
 * 2. O verso recebe contrarrotação de 180° para nunca renderizar espelhado.
 * 3. Com Movimento Reduzido ativado, desativa o giro 3D e realiza transição suave por Crossfade.
 */
@Composable
fun FlippableCard(
    card: CardUi,
    isSelected: Boolean,
    flipped: Boolean,
    onFlip: () -> Unit,
    onClickCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCcReduceMotion.current
    val density = LocalDensity.current.density

    if (reduceMotion) {
        // Modo Acessível de Movimento Reduzido: Fade cruzado sem rotação física
        Box(
            modifier = modifier
                .clickable(
                    role = if (isSelected) Role.Button else null,
                    onClick = if (isSelected) onFlip else onClickCard
                )
        ) {
            Crossfade(
                targetState = flipped,
                animationSpec = tween(durationMillis = 150),
                label = "flipCrossfade"
            ) { isBack ->
                if (isBack) {
                    CardBack(card = card, onFlip = onFlip)
                } else {
                    CardFace(card = card, isSelected = isSelected, onFlip = onFlip)
                }
            }
        }
    } else {
        // Modo Normal: Flip 3D no eixo Y com câmera a 16 * density
        val rotation by animateFloatAsState(
            targetValue = if (flipped) 180f else 0f,
            animationSpec = CcMotion.spatialDefault(),
            label = "cardFlipRotation"
        )

        val showBack by remember { derivedStateOf { rotation > 90f } }

        Box(
            modifier = modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 16f * density
                }
                .clickable(
                    role = if (isSelected) Role.Button else null,
                    onClick = if (isSelected) onFlip else onClickCard
                )
        ) {
            if (!showBack) {
                CardFace(
                    card = card,
                    isSelected = isSelected,
                    onFlip = onFlip
                )
            } else {
                Box(
                    modifier = Modifier.graphicsLayer {
                        // Contrarrotação essencial: impede que o texto do verso apareça espelhado
                        rotationY = 180f
                    }
                ) {
                    CardBack(
                        card = card,
                        onFlip = onFlip
                    )
                }
            }
        }
    }
}
