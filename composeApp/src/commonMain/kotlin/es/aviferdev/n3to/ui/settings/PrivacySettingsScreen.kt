package es.aviferdev.n3to.ui.settings

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.settings.components.*
import es.aviferdev.n3to.core.browser.rememberUrlOpener
import es.aviferdev.n3to.ui.common.navigation.TopBarApp

import es.aviferdev.n3to.ui.theme.ExpenseRed
import org.jetbrains.compose.resources.stringResource
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.privacy_analytics_desc
import n3to.composeapp.generated.resources.privacy_analytics_title
import n3to.composeapp.generated.resources.privacy_consent_date_format
import n3to.composeapp.generated.resources.privacy_crash_desc
import n3to.composeapp.generated.resources.privacy_crash_title
import n3to.composeapp.generated.resources.privacy_data_crash_reports
import n3to.composeapp.generated.resources.privacy_data_feedback
import n3to.composeapp.generated.resources.privacy_data_financial
import n3to.composeapp.generated.resources.privacy_data_personal
import n3to.composeapp.generated.resources.privacy_data_revenuecat
import n3to.composeapp.generated.resources.privacy_data_screens
import n3to.composeapp.generated.resources.privacy_policy_link
import n3to.composeapp.generated.resources.privacy_premium_active_label
import n3to.composeapp.generated.resources.privacy_premium_free
import n3to.composeapp.generated.resources.privacy_premium_lifetime_value
import n3to.composeapp.generated.resources.privacy_premium_manage
import n3to.composeapp.generated.resources.privacy_premium_plan
import n3to.composeapp.generated.resources.privacy_premium_restore
import n3to.composeapp.generated.resources.privacy_premium_subscription_value
import n3to.composeapp.generated.resources.privacy_premium_valid_until
import n3to.composeapp.generated.resources.privacy_restore_error
import n3to.composeapp.generated.resources.privacy_restore_success
import n3to.composeapp.generated.resources.privacy_revoke_all_button
import n3to.composeapp.generated.resources.privacy_revoke_all_hint
import n3to.composeapp.generated.resources.privacy_revoke_confirm
import n3to.composeapp.generated.resources.privacy_revoke_message
import n3to.composeapp.generated.resources.privacy_revoke_title
import n3to.composeapp.generated.resources.privacy_section_consent
import n3to.composeapp.generated.resources.privacy_section_data_collected
import n3to.composeapp.generated.resources.privacy_section_premium
import n3to.composeapp.generated.resources.settings_privacy_data

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/** URL de la política de privacidad */
const val PRIVACY_POLICY_URL = "https://www.n3to.avifer.dev/privacy-policy"

/**
 * Pantalla unificada de Privacidad y datos — Dark Fintech / Navy Dashboard style.
 *
 * Consolida en una sola vista:
 * - Consentimiento (2 toggles: analytics, crash reporting)
 * - Trackfolio Premium (estado + comprar/restaurar)
 * - Datos recopilados (informativo, según toggles)
 * - Revocar consentimiento
 * - Política de privacidad
 */
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: PrivacySettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val urlOpener = rememberUrlOpener()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.revokeCompleted) {
        if (uiState.revokeCompleted) {
            viewModel.onRevokeCompletedHandled()
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreEvent.collect { result ->
            when (result) {
                is RestoreResult.Success ->
                    snackbarHostState.showSnackbar("Compras restauradas correctamente")
                is RestoreResult.Error ->
                    snackbarHostState.showSnackbar("Error: ${result.message}")
            }
        }
    }

    // ── Diálogo de revocación ───────────────────────────────────────────────
    if (uiState.showRevokeConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRevokeConfirmation,
            containerColor = MaterialTheme.appColors.navySurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    stringResource(Res.string.privacy_revoke_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.privacy_revoke_message),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRevokeAll) {
                    Text(stringResource(Res.string.privacy_revoke_confirm), color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRevokeConfirmation) {
                    Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.cyanAccent)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarApp(title = stringResource(Res.string.settings_privacy_data), navigateBack = onBack)

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.cyanAccent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // ── Sección 1: Consentimiento ──────────────────────────────
                    SettingsSectionHeader(label = stringResource(Res.string.privacy_section_consent))
                    SettingsGroupCard {
                        PrivacyToggleItem(
                            icon = Icons.Default.Build,
                            title = stringResource(Res.string.privacy_analytics_title),
                            desc = stringResource(Res.string.privacy_analytics_desc),
                            checked = uiState.preferences.analytics,
                            onCheckedChange = viewModel::updateAnalytics
                        )
                        SettingsRowDivider()
                        PrivacyToggleItem(
                            icon = Icons.Default.BugReport,
                            title = stringResource(Res.string.privacy_crash_title),
                            desc = stringResource(Res.string.privacy_crash_desc),
                            checked = uiState.preferences.crashReporting,
                            onCheckedChange = viewModel::updateCrashReporting
                        )
                    }

                    uiState.preferences.consentTimestamp?.let { ts ->
                        val dt = Instant.fromEpochMilliseconds(ts)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(Res.string.privacy_consent_date_format, "${dt.date}"),
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sección 2: Trackfolio Premium ──────────────────────────
                    SettingsSectionHeader(label = stringResource(Res.string.privacy_section_premium))
                    PrivacyPremiumCard(isPremium = uiState.premiumStatus.isPremium) {
                        if (uiState.premiumStatus.isPremium) {
                            PrivacyInfoRow(
                                icon = Icons.Default.Star,
                                label = stringResource(Res.string.privacy_premium_active_label),
                                value = if (uiState.premiumStatus.isLifetime) stringResource(Res.string.privacy_premium_lifetime_value) else stringResource(Res.string.privacy_premium_subscription_value)
                            )
                            if (!uiState.premiumStatus.isLifetime) {
                                val expiryDate = uiState.premiumStatus.expiryDate
                                if (expiryDate != null) {
                                    val expiry = Instant.fromEpochMilliseconds(expiryDate)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                    SettingsRowDivider()
                                    PrivacyInfoRow(
                                        icon = Icons.Default.CalendarMonth,
                                        label = stringResource(Res.string.privacy_premium_valid_until),
                                        value = "${expiry.date}"
                                    )
                                }
                            }
                            if (uiState.premiumStatus.managementUrl != null) {
                                SettingsRowDivider()
                                PrivacyNavigableRow(
                                    icon = Icons.Default.Settings,
                                    label = stringResource(Res.string.privacy_premium_manage),
                                    onClick = { urlOpener.openUrl(uiState.premiumStatus.managementUrl!!) }
                                )
                            }
                        } else {
                            PrivacyInfoRow(
                                icon = Icons.Default.WorkspacePremium,
                                label = stringResource(Res.string.privacy_premium_plan),
                                value = stringResource(Res.string.privacy_premium_free)
                            )
                            SettingsRowDivider()
                            PrivacyNavigableRow(
                                icon = Icons.Default.Star,
                                label = "Hazte Premium",
                                onClick = onNavigateToPremium
                            )
                            SettingsRowDivider()
                        }
                        SettingsRowDivider()
                        PrivacyNavigableRow(
                            icon = Icons.Default.Restore,
                            label = stringResource(Res.string.privacy_premium_restore),
                            onClick = { viewModel.restorePurchases() }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sección 3: Datos recopilados ───────────────────────────
                    SettingsSectionHeader(label = stringResource(Res.string.privacy_section_data_collected))
                    SettingsGroupCard {
                        PrivacyDataRow(
                            icon = if (uiState.preferences.analytics) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            label = stringResource(Res.string.privacy_data_screens),
                            active = uiState.preferences.analytics
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = if (uiState.preferences.crashReporting) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            label = stringResource(Res.string.privacy_data_crash_reports),
                            active = uiState.preferences.crashReporting
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Cancel,
                            label = stringResource(Res.string.privacy_data_financial),
                            active = false
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Cancel,
                            label = stringResource(Res.string.privacy_data_personal),
                            active = false
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Info,
                            label = stringResource(Res.string.privacy_data_revenuecat),
                            active = true,
                            isInfo = true
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Info,
                            label = stringResource(Res.string.privacy_data_feedback),
                            active = true,
                            isInfo = true
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Acción: Revocar consentimiento ─────────────────────────
                    Button(
                        onClick = viewModel::requestRevokeAll,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.appColors.expense.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.appColors.expense
                        )
                    ) {
                        Text(
                            stringResource(Res.string.privacy_revoke_all_button),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(Res.string.privacy_revoke_all_hint),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { urlOpener.openUrl(PRIVACY_POLICY_URL) }) {
                        Text(stringResource(Res.string.privacy_policy_link), color = MaterialTheme.appColors.cyanAccent, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Premium card — resalta el borde con MaterialTheme.appColors.cyanAccent cuando premium está activo ──
@Composable
private fun PrivacyPremiumCard(
    isPremium: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isPremium) MaterialTheme.appColors.cyanAccent.copy(alpha = 0.35f) else MaterialTheme.appColors.navyBorder
    Card(
        modifier = Modifier.fillMaxWidth().border(0.5.dp, borderColor, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

// ─── Toggle item ──────────────────────────────────────────────────────────────
@Composable
private fun PrivacyToggleItem(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint = if (checked) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textTertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.appColors.cyanAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.appColors.navySurface
            )
        )
    }
}

// ─── Fila informativa con label + valor ───────────────────────────────────────
@Composable
private fun PrivacyInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(value, fontSize = 13.sp, color = MaterialTheme.appColors.cyanSubtle)
    }
}

// ─── Fila navegable con chevron ───────────────────────────────────────────────
@Composable
private fun PrivacyNavigableRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.appColors.cyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = MaterialTheme.appColors.textTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Fila de estado de datos recopilados ─────────────────────────────────────
@Composable
private fun PrivacyDataRow(
    icon: ImageVector,
    label: String,
    active: Boolean,
    isInfo: Boolean = false
) {
    val iconTint = when {
        isInfo -> MaterialTheme.appColors.cyanAccent
        active -> MaterialTheme.appColors.income
        else   -> MaterialTheme.appColors.expense
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 13.sp, color = MaterialTheme.appColors.textPrimary)
    }
}
