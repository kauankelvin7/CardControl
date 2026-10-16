package com.cardcontrol.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardcontrol.app.ui.theme.AppBackground
import com.cardcontrol.app.ui.theme.PrimaryAccent
import com.cardcontrol.app.ui.theme.TextPrimary
import com.cardcontrol.app.ui.theme.TextSecondary

/**
 * Campo de texto estilizado com a estética Liquid Glass.
 *
 * Aplica fundo escuro semitranslúcido e bordas ciano em foco, mantendo contraste
 * adequado para digitação em ambientes escuros. A altura mínima de 58dp garante
 * que o texto de suporte e rótulos flutuantes não sofram cortes verticais.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = TextSecondary.copy(alpha = 0.5f)) } },
        supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = TextSecondary)) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth().heightIn(min = 58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
            focusedLabelColor = PrimaryAccent,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = AppBackground.copy(alpha = 0.7f),
            unfocusedContainerColor = AppBackground.copy(alpha = 0.45f),
            cursorColor = PrimaryAccent
        )
    )
}
