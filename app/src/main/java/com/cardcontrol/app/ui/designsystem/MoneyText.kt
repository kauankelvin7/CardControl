package com.cardcontrol.app.ui.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Componente canônico de renderização monetária do CardControl v3 (Etapa 5.3 & 9.4).
 *
 * Regras:
 * 1. Opera exclusivamente com centavos inteiros (Long).
 * 2. Símbolo "R$" exibido a 55% do tamanho em cor secundária.
 * 3. Centavos mantêm o tamanho com peso ligeiramente mais leve para precisão contábil.
 * 4. Sinal de subtração utiliza o caractere tipográfico correto "−" (U+2212).
 * 5. Suporte a modo discreto com mascaramento integral (R$ ••••).
 */

private val numberFormatInt = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

@Composable
fun MoneyText(
    cents: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    val isNegative = cents < 0
    val absCents = abs(cents)
    val integerPart = absCents / 100
    val decimalPart = (absCents % 100).toString().padStart(2, '0')

    val talkbackDescription = if (hidden) {
        "Valor oculto"
    } else {
        "${if (isNegative) "menos " else ""}R$ ${numberFormatInt.format(integerPart)},$decimalPart"
    }

    val annotatedString = buildAnnotatedString {
        if (hidden) {
            append("R$ ••••")
            return@buildAnnotatedString
        }

        if (isNegative) {
            append("−") // U+2212
        }

        withStyle(
            SpanStyle(
                fontSize = style.fontSize * 0.55f,
                color = CCTheme.colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        ) {
            append("R$ ")
        }

        append(numberFormatInt.format(integerPart))
        append(",")

        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Medium
            )
        ) {
            append(decimalPart)
        }
    }

    Text(
        text = annotatedString,
        style = style.copy(
            color = CCTheme.colors.textPrimary,
            fontFeatureSettings = "tnum"
        ),
        modifier = modifier.semantics {
            contentDescription = talkbackDescription
        }
    )
}
