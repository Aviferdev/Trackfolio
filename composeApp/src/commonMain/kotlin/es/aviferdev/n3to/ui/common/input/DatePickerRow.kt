package es.aviferdev.n3to.ui.common.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Selector de fecha con picker dialog integrado.
 *
 * @param label Etiqueta del campo.
 * @param dateMillis Fecha actual en epoch millis (0 si no hay fecha seleccionada).
 * @param onDateSelected Callback con la nueva fecha en epoch millis.
 * @param modifier Modifier para personalizar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerRow(
    label: String,
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val dateText = if (dateMillis > 0) {
        val instant = Instant.fromEpochMilliseconds(dateMillis)
        val ld = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val months = listOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        )
        "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
    } else {
        "Seleccionar fecha"
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextTertiary
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
                .clickable { showPicker = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dateMillis > 0) TextPrimary else TextTertiary
            )
            Text(
                text = "📅",
                fontSize = 16.sp
            )
        }
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dateMillis > 0) dateMillis else null
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedUtc ->
                        // DatePicker devuelve UTC midnigh → convertir a local midnight
                        val utcDateTime = Instant.fromEpochMilliseconds(selectedUtc)
                            .toLocalDateTime(TimeZone.UTC)
                        val localInstant = LocalDateTime(
                            utcDateTime.year, utcDateTime.monthNumber, utcDateTime.dayOfMonth,
                            0, 0, 0, 0
                        ).toInstant(TimeZone.currentSystemDefault())
                        onDateSelected(localInstant.toEpochMilliseconds())
                    }
                    showPicker = false
                }) {
                    Text("Aceptar", color = PrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar", color = TextTertiary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview
@Composable
private fun DatePickerRowPreview() {
    N3toTheme {
        DatePickerRow(
            label = "Fecha",
            dateMillis = 1700000000000L,
            onDateSelected = {}
        )
    }
}
