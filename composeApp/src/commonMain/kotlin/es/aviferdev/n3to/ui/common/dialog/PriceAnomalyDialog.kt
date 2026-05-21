package es.aviferdev.n3to.ui.common.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro

/**
 * Diálogo que se muestra cuando se detecta un precio anómalo (sospechoso).
 *
 * - [Warning]: Variación >50%, pregunta si confirmar.
 * - [Suspicious]: Variación >500%, sugiere corrección y bloquea.
 */
@Composable
fun PriceAnomalyDialog(
    previousPrice: Double,
    newPrice: Double,
    percentChange: Double,
    likelyCause: String?,
    isBlocking: Boolean,
    onCorrect: (Double) -> Unit,
    onForceSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (isBlocking) "⚠️ Precio sospechoso" else "Precio inusual",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = buildString {
                        val changeStr = "${(kotlin.math.abs(percentChange) * 10).toInt() / 10.0}"
                        appendLine("El precio que has introducido (${formatAmountEuro(newPrice)})")
                        appendLine("varía un ${changeStr}%")
                        appendLine("respecto al precio anterior (${formatAmountEuro(previousPrice)}).")
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary,
                    lineHeight = 20.sp
                )
                if (likelyCause != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = likelyCause,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.expense,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isBlocking) {
                // Para sospechoso: sugerir corrección
                Button(
                    onClick = {
                        val corrected = if (newPrice / previousPrice in 9.0..11.0) {
                            newPrice / 10.0
                        } else if (newPrice / previousPrice in 0.09..0.11) {
                            newPrice * 10.0
                        } else {
                            previousPrice
                        }
                        onCorrect(corrected)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.appColors.primary
                    )
                ) {
                    Text(
                        text = if (newPrice / previousPrice in 9.0..11.0 || newPrice / previousPrice in 0.09..0.11) {
                            "Corregir (÷10)"
                        } else {
                            "Usar precio anterior"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onForceSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Mantener precio introducido",
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
            } else {
                // Para warning: confirmar
                Button(
                    onClick = onForceSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.appColors.primary
                    )
                ) {
                    Text(
                        "Sí, guardar de todas formas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary)
            }
        }
    )
}
