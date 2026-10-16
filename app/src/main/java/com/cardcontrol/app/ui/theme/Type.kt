package com.cardcontrol.app.ui.theme

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.util.ReportEngine

// Tabular figures enabled for aligned numbers
private const val TABULAR_NUMBERS = "tnum"

val OverlineStyle = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.2.sp,
    color = TextSecondary
)

val TabularHeadlineLarge = TextStyle(
    fontWeight = FontWeight.ExtraBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    color = TextPrimary
)

val TabularHeadlineMedium = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    color = TextPrimary
)

val TabularHeadlineSmall = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    color = TextPrimary
)

val TabularBodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    color = TextPrimary
)

val TabularBodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
    color = TextSecondary
)

val CardControlTypography = Typography(
    headlineLarge = TabularHeadlineLarge,
    headlineMedium = TabularHeadlineMedium,
    headlineSmall = TabularHeadlineSmall,
    bodyLarge = TabularBodyLarge,
    bodyMedium = TabularBodyMedium,
    labelSmall = OverlineStyle
)

@Composable
fun OverlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextSecondary
) {
    Text(
        text = text.uppercase(),
        style = OverlineStyle.copy(color = color),
        modifier = modifier
    )
}

@Composable
fun CurrencyText(
    amountCents: Long,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    prefixAlpha: Float = 0.6f
) {
    val fullFormatted = ReportEngine.formatCurrency(amountCents)
    CurrencyText(
        formattedText = fullFormatted,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        prefixAlpha = prefixAlpha
    )
}

@Composable
fun CurrencyText(
    formattedText: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    prefixAlpha: Float = 0.6f
) {
    val prefix = "R$ "
    val hasPrefix = formattedText.startsWith(prefix)
    val numberPart = if (hasPrefix) formattedText.removePrefix(prefix) else formattedText
    val prefixFontSize = (fontSize.value * 0.65f).sp

    val annotated = buildAnnotatedString {
        if (hasPrefix) {
            withStyle(
                SpanStyle(
                    fontSize = prefixFontSize,
                    fontWeight = FontWeight.Normal,
                    color = color.copy(alpha = prefixAlpha),
                    fontFeatureSettings = TABULAR_NUMBERS
                )
            ) {
                append(prefix)
            }
        }
        withStyle(
            SpanStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color,
                fontFeatureSettings = TABULAR_NUMBERS
            )
        ) {
            append(numberPart)
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = TextStyle(fontFeatureSettings = TABULAR_NUMBERS)
    )
}
