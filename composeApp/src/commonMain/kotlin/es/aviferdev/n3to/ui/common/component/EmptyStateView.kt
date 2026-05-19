package es.aviferdev.n3to.ui.common.component

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Estado vacío genérico para cualquier pantalla.
 * Muestra un icono, título y subtítulo centrados, con botón de acción opcional.
 *
 * @param icon Emoji (String) o ImageVector para el icono.
 * @param iconTint Color del icono (solo aplica si se usa ImageVector).
 * @param title Título principal del estado vacío.
 * @param subtitle Texto descriptivo secundario.
 * @param actionLabel Texto del botón de acción (null = sin botón).
 * @param onAction Callback al pulsar el botón.
 * @param modifier Modifier para personalizar el layout.
 */
@Composable
fun EmptyStateView(
    icon: Any,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.appColors.primary,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon is String) {
                Icon(
                    imageVector = icon.toMaterialIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = iconTint
                )
            } else if (icon is ImageVector) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = iconTint
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textTertiary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.appColors.primary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text(
                        actionLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    N3toTheme {
        EmptyStateView(
            icon = Icons.Default.Inbox,
            title = "Sin datos",
            subtitle = "No hay elementos para mostrar"
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreviewEmoji() {
    N3toTheme {
        EmptyStateView(
            icon = "📈",
            title = "Sin posiciones",
            subtitle = "Pulsa + para registrar\ntu primera inversión"
        )
    }
}
