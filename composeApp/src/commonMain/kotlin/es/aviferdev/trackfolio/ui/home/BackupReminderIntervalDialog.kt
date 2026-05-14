package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Diálogo que permite al usuario seleccionar cuándo volver a recordarle
 * que haga una copia de seguridad.
 *
 * Opciones disponibles: 7, 15, 30 días.
 * No hay opción de desactivar — eso solo se puede desde Ajustes,
 * tal como se indica en el mensaje informativo del footer.
 *
 * @param currentInterval Intervalo actualmente configurado (para preselección).
 * @param onIntervalSelected Callback con el intervalo seleccionado.
 * @param onDismiss Cerrar el diálogo sin cambios.
 */
@Composable
fun BackupReminderIntervalDialog(
    currentInterval: Int,
    onIntervalSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        7  to "Cada 7 días",
        15 to "Cada 15 días",
        30 to "Cada 30 días"
    )
    // Si el intervalo actual no está entre las opciones, seleccionar 30 por defecto
    var selectedOption by remember {
        mutableStateOf(
            options.find { it.first == currentInterval }?.first ?: 30
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Recordatorio de backup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¿Cuándo quieres que te lo recordemos de nuevo?",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opciones de intervalo
                options.forEach { (days, label) ->
                    val isSelected = selectedOption == days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = days }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedOption = days },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryDark,
                                unselectedColor = TextTertiary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }

                // Mensaje informativo: desactivar solo desde Ajustes
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Si quieres desactivar este recordatorio, ve a Ajustes → Recordatorios.",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntervalSelected(selectedOption) }
            ) {
                Text(
                    "Aceptar",
                    color = PrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = TextSecondary
                )
            }
        }
    )
}

@Preview
@Composable
private fun BackupReminderIntervalDialogPreview() {
    TrackfolioTheme {
        BackupReminderIntervalDialog(
            currentInterval = 15,
            onIntervalSelected = {},
            onDismiss = {}
        )
    }
}
