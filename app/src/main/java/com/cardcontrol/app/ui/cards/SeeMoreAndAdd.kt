package com.cardcontrol.app.ui.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cardcontrol.app.ui.designsystem.CcMotion
import com.cardcontrol.app.ui.designsystem.CcType

/**
 * Grupo de Destinos "Ver mais" + "+" (C8 e D2.5).
 * Composição nativa: altura 56dp, espaço entre segmentos 2dp.
 * Cantos: segmento esquerdo (28dp externo / 6dp interno), segmento direito (6dp interno / 28dp externo).
 * Morph suave de raio (28dp -> 16dp) ao pressionar.
 */
@Composable
fun SeeMoreAndAdd(
    onSeeMore: () -> Unit,
    onAddCard: () -> Unit,
    modifier: Modifier = Modifier,
    seeMoreLabel: String = "Ver faturas e limites"
) {
    Row(
        modifier = modifier.height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Segmento Esquerdo: "Ver mais"
        val leftInteraction = remember { MutableInteractionSource() }
        val leftPressed by leftInteraction.collectIsPressedAsState()
        val leftOuterCorner by animateDpAsState(
            targetValue = if (leftPressed) 16.dp else 28.dp,
            animationSpec = CcMotion.spatialFast(),
            label = "leftMorph"
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .widthIn(min = 140.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = leftOuterCorner,
                        bottomStart = leftOuterCorner,
                        topEnd = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(
                    interactionSource = leftInteraction,
                    indication = null,
                    role = Role.Button,
                    onClick = onSeeMore
                )
                .semantics { contentDescription = "$seeMoreLabel. Abrir detalhes gerais dos cartões" }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = seeMoreLabel,
                    style = CcType.buttonLabel,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Segmento Direito: "+" (Cadastrar cartão)
        val rightInteraction = remember { MutableInteractionSource() }
        val rightPressed by rightInteraction.collectIsPressedAsState()
        val rightOuterCorner by animateDpAsState(
            targetValue = if (rightPressed) 16.dp else 28.dp,
            animationSpec = CcMotion.spatialFast(),
            label = "rightMorph"
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 6.dp,
                        bottomStart = 6.dp,
                        topEnd = rightOuterCorner,
                        bottomEnd = rightOuterCorner
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    interactionSource = rightInteraction,
                    indication = null,
                    role = Role.Button,
                    onClick = onAddCard
                )
                .semantics { contentDescription = "Adicionar novo cartão" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
