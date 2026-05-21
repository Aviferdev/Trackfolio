package es.aviferdev.n3to.ui.home.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.backup_reminder_dialog_hint
import n3to.composeapp.generated.resources.backup_reminder_dialog_message
import n3to.composeapp.generated.resources.backup_reminder_dialog_title
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import org.jetbrains.compose.resources.stringResource
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
        7 to "Cada 7 días",
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
        containerColor = MaterialTheme.appColors.navySurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(Res.string.backup_reminder_dialog_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.backup_reminder_dialog_message),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opciones de intervalo
                options.forEach { (days, label) ->
                    val isSelected = selectedOption == days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (isSelected) MaterialTheme.appColors.navySurfaceLight else MaterialTheme.appColors.navySurface
                            )
                            .clickable { selectedOption = days }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedOption = days },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.appColors.cyanAccent,
                                unselectedColor = MaterialTheme.appColors.navyBorder
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
                        )
                    }
                }

                // Mensaje informativo: desactivar solo desde Ajustes
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.backup_reminder_dialog_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary,
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
                    stringResource(Res.string.common_accept),
                    color = MaterialTheme.appColors.cyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    )
}

@Preview
@Composable
private fun BackupReminderIntervalDialogPreview() {
    N3toTheme {
        BackupReminderIntervalDialog(
            currentInterval = 15,
            onIntervalSelected = {},
            onDismiss = {}
        )
    }
}
