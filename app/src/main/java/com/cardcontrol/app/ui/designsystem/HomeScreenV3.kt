package com.cardcontrol.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * HomeScreenV3 — Tela Início (Wireframe S1 da Etapa 6.2).
 *
 * Correções arquiteturais aplicadas:
 * - P1: Colunas de Assinaturas estruturadas sem colisão de textos caps.
 * - P2: contentPadding inferior livre de 112dp garantindo que o FAB e a Nav não cubram valores de pessoas.
 * - P3: Um único primário visual (FAB), com ações da fatura rebaixadas para botões tonais.
 * - P12: Regra estrita de percentuais e centavos tabulares exatos.
 */

@Composable
fun HomeScreenV3(
    modifier: Modifier = Modifier,
    initialDiscreteMode: Boolean = false,
    onFabClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var discreteMode by remember { mutableStateOf(initialDiscreteMode) }
    var selectedTab by remember { mutableStateOf(NavDestination.HOME) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CCTheme.colors.bgBase)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 112.dp // P2: Margem inferior segura
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Top Bar com Seletor de Mês e Controles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CCTheme.colors.surface1)
                            .border(1.dp, CCTheme.colors.outlineSubtle, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "◀  Outubro 2026  ▶",
                            style = CCTheme.typography.titleSm.copy(color = CCTheme.colors.textPrimary)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { discreteMode = !discreteMode },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CCTheme.colors.surface1)
                                .border(1.dp, CCTheme.colors.outlineSubtle, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (discreteMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Modo Discreto",
                                tint = CCTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CCTheme.colors.surface1)
                                .border(1.dp, CCTheme.colors.outlineSubtle, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = CCTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. HeroBalanceCard (Fatura do Mês)
            item {
                HeroBalanceCard(
                    invoiceTitle = "Fatura de Outubro",
                    statusText = "Aberta",
                    amountCents = 234018L, // R$ 2.340,18
                    closingAndDueDateText = "Fecha em 4 dias · Vence dia 15",
                    usedLimitCents = 349000L,
                    totalLimitCents = 485000L,
                    onRegisterPaymentClick = {},
                    onShareClick = {},
                    hidden = discreteMode
                )
            }

            // 3. Selo "100% no Aparelho"
            item {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CCTheme.colors.surface1.copy(alpha = 0.5f))
                        .border(1.dp, CCTheme.colors.outlineSubtle, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacidade",
                        tint = CCTheme.colors.textTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "100% no aparelho · Sem nuvem",
                        style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.textTertiary)
                    )
                }
            }

            // 4. Banner de Revisão de Compras (F1)
            item {
                ReviewBanner(
                    pendingCount = 3,
                    onClick = {}
                )
            }

            // 5. Seção Pessoas (Rateio)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pessoas",
                            style = CCTheme.typography.titleMd.copy(color = CCTheme.colors.textPrimary)
                        )
                        Text(
                            text = "Ver todas",
                            style = CCTheme.typography.labelMd.copy(color = CCTheme.colors.textTertiary)
                        )
                    }

                    PersonRow(
                        name = "Lucas",
                        initial = "L",
                        avatarColor = CCCategorical.InterOrange,
                        amountCents = 121040L, // R$ 1.210,40
                        sublimitCents = 200000L, // R$ 2.000,00
                        onClick = {},
                        hidden = discreteMode
                    )

                    PersonRow(
                        name = "Maria",
                        initial = "M",
                        avatarColor = CCCategorical.NuCyan,
                        amountCents = 64010L, // R$ 640,10
                        sublimitCents = 150000L, // R$ 1.500,00
                        onClick = {},
                        hidden = discreteMode
                    )
                }
            }

            // 6. Projeção de Parcelas dos Próximos 6 Meses
            item {
                ForecastCard()
            }

            // 7. Assinaturas Recorrentes (P1 Corrigido)
            item {
                RecurringChargesSummaryCard(
                    monthlyCents = 18990L, // R$ 189,90
                    limitImpactPercentage = 4.2,
                    hidden = discreteMode
                )
            }
        }

        // 8. FAB Menu Flutuante (Único Primário da Tela)
        FloatingActionButton(
            onClick = onFabClick,
            shape = CircleShape,
            containerColor = CCTheme.colors.accent,
            contentColor = CCTheme.colors.onAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 84.dp, end = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Lançar Compra"
            )
        }

        // 9. Glass Bottom Navigation Bar Fixada
        GlassBottomNav(
            selectedDestination = selectedTab,
            onDestinationSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
