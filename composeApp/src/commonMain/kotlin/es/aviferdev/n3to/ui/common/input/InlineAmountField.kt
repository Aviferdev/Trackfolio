package es.aviferdev.n3to.ui.common.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Campo de texto o numérico inline con label y placeholder sobre fondo elevado.
 * Similar a AmountInputField pero en formato más compacto (inline).
 *
 * @param label Etiqueta del campo.
 * @param value Texto del campo.
 * @param onChange Callback cuando el texto cambia.
 * @param placeholder Texto de placeholder.
 * @param suffix Sufijo opcional (ej: "€", "%").
 * @param singleLine Si es de una sola línea.
 * @param modifier Modifier para personalizar.
 */
@Composable
fun InlineAmountField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    suffix: String? = null,
    singleLine: Boolean = false,
    containerColor: Color = MaterialTheme.appColors.surfaceElevated,
    cursorColor: Color = MaterialTheme.appColors.primary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.appColors.textTertiary
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(containerColor)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textTertiary.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    singleLine = singleLine,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textPrimary
                    ),
                    cursorBrush = SolidColor(cursorColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (suffix != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}

@Preview
@Composable
private fun InlineAmountFieldPreview() {
    N3toTheme {
        InlineAmountField(
            label = "Importe",
            value = "1250",
            onChange = {},
            placeholder = "0,00",
            suffix = "€"
        )
    }
}

@Preview
@Composable
private fun InlineAmountFieldEmptyPreview() {
    N3toTheme {
        InlineAmountField(
            label = "Porcentaje IRPF",
            value = "",
            onChange = {},
            placeholder = "15",
            suffix = "%",
            singleLine = true
        )
    }
}
