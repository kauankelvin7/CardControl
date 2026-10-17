package com.cardcontrol.app.ui.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Tokens de movimento do Design System Vitrine (B7.1).
 * Molas espaciais e de efeitos calibradas para suavidade e reatividade.
 *
 * Invariante: Movimentos espaciais (posição, escala, rotação, forma) possuem leve oscilação;
 * efeitos (cor, opacidade) nunca oscilam (dampingRatio = 1f).
 */
object CcMotion {
    // Molas espaciais (posição, tamanho, forma, rotação)
    fun <T> spatialFast(): SpringSpec<T> = spring(
        dampingRatio = 0.6f,
        stiffness = 800f
    )

    fun <T> spatialDefault(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 380f
    )

    fun <T> spatialSlow(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 200f
    )

    // Molas de efeitos (opacidade, cor) — sem overshoot
    fun <T> effectsFast(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f
        stiffness = 3800f
    )

    fun <T> effectsDefault(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f
        stiffness = 1600f
    )

    fun <T> effectsSlow(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f
        stiffness = 800f
    )
}
