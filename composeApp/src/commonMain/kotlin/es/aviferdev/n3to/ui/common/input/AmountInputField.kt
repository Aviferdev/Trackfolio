package es.aviferdev.n3to.ui.common.input

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Campo de importe monetario con label y texto grande centrado.
 *
 * @param label Etiqueta del campo.
 * @param value Texto del importe.
 * @param onChange Callback cuando el texto cambia.
 * @param placeholder Texto de placeholder (ej: "0,00").
 * @param modifier Modifier para personalizar.
 */
@Composable
fun AmountInputField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "0,00",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.appColors.textTertiary
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.appColors.surfaceElevated)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.appColors.primary),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textTertiary.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun AmountInputFieldPreview() {
    N3toTheme {
        AmountInputField(
            label = "Importe",
            value = "1250,00",
            onChange = {}
        )
    }
}
