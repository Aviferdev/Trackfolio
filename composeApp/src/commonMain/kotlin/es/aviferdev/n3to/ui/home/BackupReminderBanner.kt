package es.aviferdev.n3to.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_backup_action
import n3to.composeapp.generated.resources.home_backup_days_format
import n3to.composeapp.generated.resources.home_backup_never
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Banner que recuerda al usuario hacer una copia de seguridad.
 *
 * @param visible Controla la visibilidad con animación.
 * @param neverBackup Si es true, el usuario nunca ha hecho un backup.
 * @param daysSinceLastBackup Días transcurridos desde el último backup (usado cuando neverBackup = false).
 * @param onBackupClick Acción al pulsar "Hacer backup".
 * @param onDismiss Acción al pulsar la X (abre el diálogo de intervalo).
 */
@Composable
fun BackupReminderBanner(
    visible: Boolean,
    neverBackup: Boolean,
    daysSinceLastBackup: Int = 0,
    onBackupClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = visible,
        enter    = expandVertically() + fadeIn(),
        exit     = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryDark.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Outlined.SaveAlt,
                contentDescription = null,
                tint               = PrimaryDark,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (neverBackup) stringResource(Res.string.home_backup_never)
                       else stringResource(Res.string.home_backup_days_format, daysSinceLastBackup),
                fontSize   = 12.sp,
                color      = PrimaryDark,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryDark.copy(alpha = 0.15f))
                    .clickable { onBackupClick() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = stringResource(Res.string.home_backup_action),
                    fontSize   = 11.sp,
                    color      = PrimaryDark,
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
                Text("×", fontSize = 16.sp, color = PrimaryDark.copy(alpha = 0.6f))
            }
        }
    }
}

@Preview
@Composable
private fun BackupReminderBannerNeverBackupPreview() {
    N3toTheme {
        BackupReminderBanner(
            visible = true,
            neverBackup = true,
            onBackupClick = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun BackupReminderBannerDaysPreview() {
    N3toTheme {
        BackupReminderBanner(
            visible = true,
            neverBackup = false,
            daysSinceLastBackup = 45,
            onBackupClick = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun BackupReminderBannerHiddenPreview() {
    N3toTheme {
        BackupReminderBanner(
            visible = false,
            neverBackup = false,
            onBackupClick = {},
            onDismiss = {}
        )
    }
}
