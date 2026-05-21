package es.aviferdev.n3to.ui.common.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun StepSlider(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueLabel: (Int) -> String = { it.toString() },
    minLabel: String = min.toString(),
    maxLabel: String = max.toString()
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.appColors.textTertiary
            )
            Text(
                text = valueLabel(value),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.appColors.cyanAccent,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value.coerceIn(min, max).toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.appColors.cyanAccent,
                activeTrackColor = MaterialTheme.appColors.cyanAccent,
                inactiveTrackColor = MaterialTheme.appColors.navyBorder,
                activeTickColor = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.4f),
                inactiveTickColor = MaterialTheme.appColors.navyBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = minLabel, fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
            Text(text = maxLabel, fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
        }
    }
}
