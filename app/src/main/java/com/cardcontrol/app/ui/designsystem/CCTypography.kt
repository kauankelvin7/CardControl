package com.cardcontrol.app.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.R

/**
 * Tipografia oficial do CardControl v3 com Inter única variável (D3.2).
 *
 * Utiliza o arquivo físico `inter_variable.ttf` em `res/font/` e
 * configura números tabulares (fontFeatureSettings = "tnum") para perfeito alinhamento contábil.
 */

val InterFontFamily = FontFamily(
    Font(resId = R.font.inter_variable, weight = FontWeight.Normal),
    Font(resId = R.font.inter_variable, weight = FontWeight.Medium),
    Font(resId = R.font.inter_variable, weight = FontWeight.SemiBold),
    Font(resId = R.font.inter_variable, weight = FontWeight.Bold)
)

@Immutable
data class CCTypography(
    val displayHero: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.01).sp,
        fontFeatureSettings = "tnum"
    ),
    val displayLg: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.005).sp,
        fontFeatureSettings = "tnum"
    ),
    val titleLg: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    val titleMd: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    val titleSm: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    val bodyLg: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    val bodyMd: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    val bodySm: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    val labelLg: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.001.sp
    ),
    val labelMd: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.02.sp
    ),
    val labelCaps: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.06.sp
    ),
    val numLg: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    ),
    val numMd: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    ),
    val numSm: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    ),
    val monoSm: TextStyle = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = "tnum"
    )
)
