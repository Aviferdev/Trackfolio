package es.aviferdev.trackfolio.ui.common.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Diálogo de confirmación de borrado genérico.
 * Sigue el patrón visual del proyecto: título, mensaje, botón "Eliminar" en rojo y "Cancelar".
 *
 * @param title Título del diálogo (ej: "Eliminar movimiento").
 * @param message Mensaje descriptivo de la acción.
 * @param onConfirm Acción a ejecutar al confirmar el borrado.
 * @param onDismiss Acción a ejecutar al cancelar.
 */
@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                message,
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = ExpenseRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PrimaryDark)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview
@Composable
private fun DeleteConfirmDialogPreview() {
    TrackfolioTheme {
        DeleteConfirmDialog(
            title = "Eliminar movimiento",
            message = "¿Seguro que quieres eliminar este movimiento?\nEsta acción no se puede deshacer.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
