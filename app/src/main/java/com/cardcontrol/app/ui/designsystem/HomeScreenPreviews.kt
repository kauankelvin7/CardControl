package com.cardcontrol.app.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Previews da Tela Início nas 3 direções de cor (Etapa 4 [D1]).
 * Permite visualização e comparação direta no Android Studio.
 */


@Preview(name = "Direção C · Grafite (Recomendado) (360x800dp)", widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenGrafitePreview() {
    CCTheme(direction = ColorDirection.GRAFITE) {
        HomeScreenV3()
    }
}

@Preview(name = "Direção C · Modo Discreto", widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenDiscreteModePreview() {
    CCTheme(direction = ColorDirection.GRAFITE) {
        HomeScreenV3(initialDiscreteMode = true)
    }
}
