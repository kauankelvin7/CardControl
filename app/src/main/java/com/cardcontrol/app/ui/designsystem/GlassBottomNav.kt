package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * GlassBottomNav — Navegação de 4 Abas em Glass L4 (Etapa 4.2 & 5.10).
 *
 * Fallback para superfície sólida quando redução de transparência estiver ativa.
 */

enum class NavDestination(val label: String, val icon: ImageVector) {
    HOME("Início", Icons.Default.Home),
    PEOPLE("Pessoas", Icons.Default.People),
    CARDS("Cartões", Icons.Default.CreditCard),
    INSIGHTS("Insights", Icons.Default.Insights)
}

@Composable
fun GlassBottomNav(
    selectedDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    reduceTransparency: Boolean = false
) {
    val navBackground = if (reduceTransparency) {
        CCTheme.colors.surface2
    } else {
        CCTheme.colors.surface1.copy(alpha = 0.88f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(navBackground)
            .border(1.dp, CCTheme.colors.outlineSubtle)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = destination == selectedDestination
                val contentColor = if (isSelected) CCTheme.colors.textPrimary else CCTheme.colors.textTertiary

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.background(CCTheme.colors.surface3) else Modifier
                        )
                        .clickable { onDestinationSelected(destination) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor
                        )
                        Text(
                            text = destination.label,
                            style = CCTheme.typography.labelMd.copy(color = contentColor)
                        )
                    }
                }
            }
        }
    }
}
