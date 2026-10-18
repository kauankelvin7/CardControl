package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Formas e raios do Design System Vitrine (B6.2).
 */
@Immutable
data class CcShapesTokens(
    val panel: CornerBasedShape = RoundedCornerShape(18.dp),
    val card: CornerBasedShape = RoundedCornerShape(14.dp),
    val actionContainer: CornerBasedShape = RoundedCornerShape(16.dp),
    val button: CornerBasedShape = RoundedCornerShape(12.dp),
    val pill: CornerBasedShape = RoundedCornerShape(24.dp),
    val pillInner: CornerBasedShape = RoundedCornerShape(6.dp),
    val monogram: CornerBasedShape = RoundedCornerShape(10.dp),
    val chip: CornerBasedShape = RoundedCornerShape(8.dp),
    val dialog: CornerBasedShape = RoundedCornerShape(20.dp),
    val sheet: CornerBasedShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val field: CornerBasedShape = RoundedCornerShape(12.dp),
    val circle: CornerBasedShape = CircleShape
)

val CcShapes = CcShapesTokens()

/** Mapeamento de shapes M3 padrão para compatibilidade com componentes nativos */
val MaterialCcShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

val LocalCcShapes = staticCompositionLocalOf { CcShapes }
