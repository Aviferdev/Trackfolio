package es.aviferdev.n3to.ui.settings.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.common.N3toLabel

import es.aviferdev.n3to.ui.theme.ExpenseRed

import es.aviferdev.n3to.ui.theme.formatAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.settings_add_account
import n3to.composeapp.generated.resources.settings_backup_reminder_off
import n3to.composeapp.generated.resources.settings_backup_reminder_title
import n3to.composeapp.generated.resources.settings_biometric_disabled
import n3to.composeapp.generated.resources.settings_biometric_enabled
import n3to.composeapp.generated.resources.settings_biometric_lock
import n3to.composeapp.generated.resources.settings_configure_cd
import n3to.composeapp.generated.resources.settings_dark_theme
import n3to.composeapp.generated.resources.settings_edit_cd
import n3to.composeapp.generated.resources.settings_interval_15d
import n3to.composeapp.generated.resources.settings_interval_30d
import n3to.composeapp.generated.resources.settings_interval_7d
import n3to.composeapp.generated.resources.settings_reconciliation_reminder_title
import n3to.composeapp.generated.resources.settings_language
import n3to.composeapp.generated.resources.settings_theme_disabled
import n3to.composeapp.generated.resources.settings_theme_enabled
import n3to.composeapp.generated.resources.language_spanish
import n3to.composeapp.generated.resources.language_english
import n3to.composeapp.generated.resources.language_system
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsSectionHeader(label: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        N3toLabel(text = label)
        if (actionLabel != null && onAction != null) {
            Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.cyanAccent, modifier = Modifier.clickable { onAction() })
        }
    }
}

@Composable
internal fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
internal fun SettingsRowDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = MaterialTheme.appColors.navyBorder, thickness = 0.5.dp)
}

@Composable
internal fun SettingsNavigableRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.appColors.textTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun SettingsInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(32.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
    }
}

@Composable
internal fun SettingsBiometricRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(Res.string.settings_biometric_lock), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
            Text(if (enabled) stringResource(Res.string.settings_biometric_enabled) else stringResource(Res.string.settings_biometric_disabled), fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
        }
        Switch(checked = enabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.appColors.cyanAccent, uncheckedThumbColor = Color.White, uncheckedTrackColor = MaterialTheme.appColors.navySurface))
    }
}

@Composable
internal fun SettingsThemeRow(isDark: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(Res.string.settings_dark_theme), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
            Text(if (isDark) stringResource(Res.string.settings_theme_enabled) else stringResource(Res.string.settings_theme_disabled), fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
        }
        Switch(checked = isDark, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.appColors.cyanAccent, uncheckedThumbColor = Color.White, uncheckedTrackColor = MaterialTheme.appColors.navySurface))
    }
}

@Composable
internal fun SettingsLanguageRow(
    currentLanguage: String,
    isSystemDefault: Boolean,
    onClick: () -> Unit
) {
    val subtitle = if (isSystemDefault) {
        stringResource(Res.string.language_system)
    } else when (currentLanguage) {
        "en" -> stringResource(Res.string.language_english)
        else -> stringResource(Res.string.language_spanish)
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Language, contentDescription = null,
            tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(Res.string.settings_language),
                fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary)
            Text(subtitle,
                fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.appColors.textTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun SettingsReconciliationIntervalRow(
    interval: Int,
    onIntervalChange: (Int) -> Unit,
) {
    val options = listOf(
        0  to stringResource(Res.string.settings_backup_reminder_off),
        7  to stringResource(Res.string.settings_interval_7d),
        15 to stringResource(Res.string.settings_interval_15d),
        30 to stringResource(Res.string.settings_interval_30d)
    )
    val label = options.find { it.first == interval }?.second ?: stringResource(Res.string.settings_interval_30d)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Sync, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.settings_reconciliation_reminder_title), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
                Text(label, fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (days, text) ->
                val selected = interval == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            if (selected) 1.5.dp else 0.5.dp,
                            if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navyBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onIntervalChange(days) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text,
                        fontSize   = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingsBackupReminderIntervalRow(
    interval: Int,
    onIntervalChange: (Int) -> Unit,
) {
    val options = listOf(
        0  to stringResource(Res.string.settings_backup_reminder_off),
        7  to stringResource(Res.string.settings_interval_7d),
        15 to stringResource(Res.string.settings_interval_15d),
        30 to stringResource(Res.string.settings_interval_30d)
    )
    val label = options.find { it.first == interval }?.second ?: stringResource(Res.string.settings_interval_30d)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.SaveAlt, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.settings_backup_reminder_title), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
                Text(label, fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (days, text) ->
                val selected = interval == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            if (selected) 1.5.dp else 0.5.dp,
                            if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navyBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onIntervalChange(days) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text,
                        fontSize   = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyAccountsCard(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAdd).border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.settings_add_account), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.cyanAccent)
        }
    }
}

@Composable
internal fun SettingsAccountCard(
    account: Account,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConfigure: () -> Unit = {}
) {
    val borderColor = if (isSelected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navyBorder
    val borderWidth = if (isSelected) 1.dp else 0.5.dp
    Card(
        modifier = Modifier.fillMaxWidth().border(borderWidth, borderColor, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.textPrimary)
                Text("€ · ${formatAmount(account.computedBalance)}", fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
            }
            IconButton(onClick = onConfigure, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.AccountBalance, contentDescription = stringResource(Res.string.settings_configure_cd), tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.settings_edit_cd), tint = MaterialTheme.appColors.textSecondary, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.common_delete), tint = MaterialTheme.appColors.expense, modifier = Modifier.size(16.dp)) }
        }
    }
}
