package com.cardcontrol.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassPalette {
    val ObsidianBg: Color get() = com.cardcontrol.app.ui.theme.AppBackground
    val DeepSlateBg: Color get() = com.cardcontrol.app.ui.theme.AppBackgroundTop
    val CyanGlow: Color get() = com.cardcontrol.app.ui.theme.PrimaryAccent
    val IndigoGlow: Color get() = com.cardcontrol.app.ui.theme.SecondaryAccent
    val EmeraldAccent: Color get() = com.cardcontrol.app.ui.theme.SemanticSuccess
    val CoralDanger: Color get() = com.cardcontrol.app.ui.theme.SemanticDanger
    val AmberWarn: Color get() = com.cardcontrol.app.ui.theme.SemanticWarning
    val TextPrimary: Color get() = com.cardcontrol.app.ui.theme.TextPrimary
    val TextSecondary: Color get() = com.cardcontrol.app.ui.theme.TextSecondary
    val TextMuted: Color get() = com.cardcontrol.app.ui.theme.TextMuted

    val AppBackground: Color get() = com.cardcontrol.app.ui.theme.AppBackground
    val GlassSurface: Color get() = com.cardcontrol.app.ui.theme.GlassSurface
    val PrimaryAccent: Color get() = com.cardcontrol.app.ui.theme.PrimaryAccent
    val SecondaryAccent: Color get() = com.cardcontrol.app.ui.theme.SecondaryAccent
    val SemanticSuccess: Color get() = com.cardcontrol.app.ui.theme.SemanticSuccess
    val SemanticWarning: Color get() = com.cardcontrol.app.ui.theme.SemanticWarning
    val SemanticDanger: Color get() = com.cardcontrol.app.ui.theme.SemanticDanger

    val SurfaceTintTop = Color(0x28FFFFFF)
    val SurfaceTintBottom = Color(0x0DFFFFFF)
    val SpecularHighlight = Color(0x66FFFFFF)
    val SpecularShadow = Color(0x14FFFFFF)
}

/**
 * Modifier oficial Liquid Glass Card.
 * Aplica cantos arredondados, superfície translúcida #0F172A (alpha 65%)
 * e reflexo de luz direcional suave no topo esquerdo do vidro.
 */
fun Modifier.liquidGlassCard(
    cornerRadius: Dp = 20.dp,
    accentTint: Color? = null
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColors = if (accentTint != null) {
        listOf(
            Color.White.copy(alpha = 0.22f),
            accentTint.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.05f),
            Color.Transparent
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.05f),
            Color.Transparent
        )
    }
    return this
        .clip(shape)
        .background(Color(0xFF0F172A).copy(alpha = 0.65f))
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset(0f, 0f),
                end = Offset(450f, 450f)
            ),
            shape = shape
        )
}

/**
 * Retrocompatibilidade com chamadas existentes de liquidGlassSurface.
 */
fun Modifier.liquidGlassSurface(
    cornerRadius: Dp = 20.dp,
    accentTint: Color = GlassPalette.CyanGlow
): Modifier = this.liquidGlassCard(cornerRadius = cornerRadius, accentTint = accentTint)

@Composable
fun LiquidAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgBottom = MaterialTheme.colorScheme.background
    val isDark = bgBottom.luminance() < 0.5f
    val bgTop = if (isDark) Color(0xFF171C1D) else Color(0xFFE8F3F4)
    val primaryGlow = MaterialTheme.colorScheme.primary
    val secondaryGlow = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgTop.copy(alpha = 0.90f), bgBottom)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Gradiente radial sutil no topo simulando iluminação ambiente de estúdio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryGlow.copy(alpha = if (isDark) 0.12f else 0.07f), Color.Transparent),
                    center = Offset(size.width * 0.20f, size.height * 0.08f),
                    radius = size.minDimension * 0.70f
                ),
                center = Offset(size.width * 0.20f, size.height * 0.08f),
                radius = size.minDimension * 0.70f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryGlow.copy(alpha = if (isDark) 0.10f else 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.40f),
                    radius = size.minDimension * 0.65f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.40f),
                radius = size.minDimension * 0.65f
            )
        }
        content()
    }
}

/**
 * Barra de progresso com design Liquid Glass:
 * Altura de 6.dp, bordas arredondadas e trilha em Color.White (alpha = 0.08f).
 */
@Composable
fun LiquidGlassBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryAccent
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor.copy(alpha = 0.75f), accentColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun <T> LiquidPillSelector(
    options: List<Pair<T, String>>,
    selectedId: T?,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (id, label) ->
            val isSelected = id == selectedId
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Preview(name = "Liquid Glass Design System", showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
private fun LiquidGlassPreview() {
    CardControlTheme {
        LiquidAmbientBackground {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, accentTint = PrimaryAccent)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Card Obsidian",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Superfície translúcida Obsidian (#0B0F19) com iluminação ambiente e reflexo specular.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        LiquidGlassBar(progress = 0.65f, accentColor = PrimaryAccent)
                    }
                }

                LiquidPillSelector(
                    options = listOf("todos" to "Todos", "nubank" to "Nubank", "itau" to "Itaú"),
                    selectedId = "nubank",
                    onSelect = {}
                )
            }
        }
    }
}
