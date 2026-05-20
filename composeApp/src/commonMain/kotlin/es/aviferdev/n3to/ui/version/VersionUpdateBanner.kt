package es.aviferdev.n3to.ui.version

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.SecondaryTeal
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Banner contextual que informa al usuario de que hay una nueva versión disponible.
 *
 * Se muestra en la sección de banners del Home cuando [visible] es true.
 * Sigue el patrón de [PriceReminderBanner] y [BackupReminderBanner]:
 * animación de entrada/salida, icono, texto, botón de acción y botón de cerrar.
 *
 * @param visible       Controla la visibilidad con animación.
 * @param latestVersion Versión más reciente disponible (para mostrar al usuario).
 * @param onUpdateNow   Acción al pulsar "Actualizar" (abre la store).
 * @param onDismiss     Acción al pulsar la X (descarta el banner).
 */
@Composable
fun VersionUpdateBanner(
    visible: Boolean,
    latestVersion: String,
    onUpdateNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryTeal.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.SystemUpdate,
                contentDescription = null,
                tint = SecondaryTeal,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Versión $latestVersion disponible",
                fontSize = 12.sp,
                color = SecondaryTeal,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryTeal.copy(alpha = 0.15f))
                    .clickable { onUpdateNow() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Actualizar",
                    fontSize = 11.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDismiss() }
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("×", fontSize = 16.sp, color = SecondaryTeal.copy(alpha = 0.6f))
            }
        }
    }
}

@Preview
@Composable
private fun VersionUpdateBannerVisiblePreview() {
    N3toTheme {
        VersionUpdateBanner(
            visible = true,
            latestVersion = "1.3.0",
            onUpdateNow = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun VersionUpdateBannerHiddenPreview() {
    N3toTheme {
        VersionUpdateBanner(
            visible = false,
            latestVersion = "1.3.0",
            onUpdateNow = {},
            onDismiss = {}
        )
    }
}
