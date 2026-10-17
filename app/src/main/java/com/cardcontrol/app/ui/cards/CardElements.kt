package com.cardcontrol.app.ui.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Elementos visuais nativos de alta fidelidade para cartões bancários em Jetpack Compose.
 *
 * Atribuição / Inspiração conceitual:
 * - Conceito de EMV chip e símbolo Contactless em Canvas inspirado em Pedro-kt/compose-components (MIT License).
 * - Implementação 100% nativa sem dependências externas, compatível com Android 8.0+ (API 26+).
 */

/**
 * Chip EMV inteligente com acabamento metálico em gradiente e ranhuras micro-circuitadas (smartcard contact pads).
 */
@Composable
fun EmvChip(
    modifier: Modifier = Modifier,
    isGold: Boolean = true
) {
    val baseGradient = if (isGold) {
        listOf(
            Color(0xFFFFDF7A),
            Color(0xFFD4AF37),
            Color(0xFFA67C1E),
            Color(0xFFFFE599),
            Color(0xFFC59B27)
        )
    } else {
        listOf(
            Color(0xFFF5F5F7),
            Color(0xFFD2D5DA),
            Color(0xFFA0A5AD),
            Color(0xFFE8EBF0),
            Color(0xFFB8BCC4)
        )
    }

    val strokeColor = if (isGold) Color(0xFF8A6208) else Color(0xFF6B7280)
    val grooveColor = if (isGold) Color(0xFF704F05).copy(alpha = 0.65f) else Color(0xFF4B5563).copy(alpha = 0.65f)

    Box(
        modifier = modifier
            .size(width = 38.dp, height = 28.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(5.dp))
            .background(
                Brush.linearGradient(
                    colors = baseGradient,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(0.8.dp, strokeColor.copy(alpha = 0.7f), RoundedCornerShape(5.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 1.dp.toPx())

            // Linha divisória horizontal principal no meio
            drawLine(
                color = grooveColor,
                start = Offset(0f, h * 0.5f),
                end = Offset(w, h * 0.5f),
                strokeWidth = 1.dp.toPx()
            )

            // Ilha central (pad de contato central)
            val padLeft = w * 0.28f
            val padRight = w * 0.72f
            val padTop = h * 0.22f
            val padBottom = h * 0.78f

            drawRoundRect(
                color = grooveColor,
                topLeft = Offset(padLeft, padTop),
                size = Size(padRight - padLeft, padBottom - padTop),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = stroke
            )

            // Conexões verticais centro -> bordas
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.5f, 0f),
                end = Offset(w * 0.5f, padTop),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = grooveColor,
                start = Offset(w * 0.5f, padBottom),
                end = Offset(w * 0.5f, h),
                strokeWidth = 1.dp.toPx()
            )

            // Ranhuras diagonais de contato nas laterais
            drawLine(
                color = grooveColor,
                start = Offset(padLeft, h * 0.36f),
                end = Offset(0f, h * 0.25f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = grooveColor,
                start = Offset(padLeft, h * 0.64f),
                end = Offset(0f, h * 0.75f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = grooveColor,
                start = Offset(padRight, h * 0.36f),
                end = Offset(w, h * 0.25f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = grooveColor,
                start = Offset(padRight, h * 0.64f),
                end = Offset(w, h * 0.75f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * Ícone Contactless (pagamento por aproximação) padronizado, desenhado em Canvas com 4 arcos concêntricos.
 */
@Composable
fun ContactlessWave(
    modifier: Modifier = Modifier,
    tint: Color = Color.White.copy(alpha = 0.85f)
) {
    Canvas(modifier = modifier.size(18.dp, 18.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // 4 arcos concêntricos com abertura de 80 graus
        val radii = listOf(w * 0.28f, w * 0.50f, w * 0.72f, w * 0.94f)
        val originX = w * 0.08f
        val originY = h * 0.5f

        for (r in radii) {
            drawArc(
                color = tint,
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(originX - r, originY - r),
                size = Size(r * 2f, r * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Textura procedural sutil de fundo desenhada em Canvas de acordo com a identidade do banco.
 */
@Composable
fun BankCardTexture(
    textureType: BankTextureType,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (textureType) {
            BankTextureType.SOLAR_GRID -> {
                // Micrograde geométrica diagonal para o Inter
                val step = 18.dp.toPx()
                var x = -h
                while (x < w + h) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = Offset(x, 0f),
                        end = Offset(x + h, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += step
                }
            }

            BankTextureType.RADIAL_GLOW -> {
                // Iluminação radial suave no canto superior-direito
                val radius = w * 0.70f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(w * 0.90f, h * 0.10f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(w * 0.90f, h * 0.10f)
                )
            }

            BankTextureType.GEOMETRIC_BADGE -> {
                // Geometria diagonal chanfrada sutil para o Itaú
                val path = Path().apply {
                    moveTo(w * 0.58f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h)
                    lineTo(w * 0.38f, h)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.035f)
                )
            }

            BankTextureType.CARBON_WEAVE -> {
                // Linhas micro-horizontais escovadas de fibra de carbono para o C6 Bank
                val step = 4.dp.toPx()
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.025f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += step
                }
            }

            BankTextureType.RUBY_SHEEN -> {
                // Feixe diagonal curvado para Santander
                val path = Path().apply {
                    moveTo(0f, h * 0.30f)
                    lineTo(w * 0.45f, 0f)
                    lineTo(w * 0.85f, 0f)
                    lineTo(0f, h * 0.90f)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.045f)
                )
            }

            BankTextureType.WAVE_ARC -> {
                // Curva orgânica fluida suave para Bradesco e Caixa
                val path = Path().apply {
                    moveTo(0f, h * 0.60f)
                    cubicTo(
                        w * 0.35f, h * 0.40f,
                        w * 0.65f, h * 0.80f,
                        w, h * 0.50f
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.045f), Color.Transparent)
                    )
                )
            }

            BankTextureType.NEON_MATRIX -> {
                // Brilho luminoso esmeralda no canto inferior para o PicPay
                val radius = w * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.85f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(w * 0.15f, h * 0.85f)
                )
            }

            BankTextureType.AZURE_SOFT -> {
                // Círculo suave azul celeste para Mercado Pago
                val radius = w * 0.50f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.80f, h * 0.80f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(w * 0.80f, h * 0.80f)
                )
            }

            BankTextureType.METALLIC_BRUSH -> {
                // Efeito de metal escovado na diagonal para Fallback/Outros
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.05f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    start = Offset(0f, h * 0.25f),
                    end = Offset(w, h * 0.75f),
                    strokeWidth = 28.dp.toPx()
                )
            }
        }
    }
}
