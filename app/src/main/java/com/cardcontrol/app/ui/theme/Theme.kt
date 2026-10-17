package com.cardcontrol.app.ui.theme

import androidx.compose.runtime.Composable
import com.cardcontrol.app.ui.designsystem.AppearancePrefs
import com.cardcontrol.app.ui.designsystem.CardControlTheme as VitrineCardControlTheme

/**
 * Ponto de entrada legado de CardControlTheme redirecionando para o Design System Vitrine oficial.
 * Garante que a paleta Vitrine sobrescreva as cores do sistema em todas as telas da aplicação.
 */
@Composable
fun CardControlTheme(
    prefs: AppearancePrefs = AppearancePrefs(),
    content: @Composable () -> Unit
) {
    VitrineCardControlTheme(
        prefs = prefs,
        content = content
    )
}
