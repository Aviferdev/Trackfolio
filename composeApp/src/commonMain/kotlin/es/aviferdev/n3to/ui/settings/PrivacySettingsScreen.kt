package es.aviferdev.n3to.ui.settings

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
import es.aviferdev.n3to.core.browser.rememberUrlOpener
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.CyanSubtle
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
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
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "¿Revocar todo el consentimiento?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "Se desactivarán el análisis de uso y los informes de errores. Esta acción se aplica inmediatamente.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRevokeAll) {
                    Text("Revocar todo", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRevokeConfirmation) {
                    Text("Cancelar", color = CyanAccent)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(NavyDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarApp(title = "Privacidad y datos", navigateBack = onBack)

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // ── Sección 1: Consentimiento ──────────────────────────────
                    SettingsSectionHeader(label = "Consentimiento")
                    SettingsGroupCard {
                        PrivacyToggleItem(
                            icon = Icons.Default.Build,
                            title = "Ayúdanos a mejorar",
                            desc = "Análisis de uso anónimo para mejorar la app.",
                            checked = uiState.preferences.analytics,
                            onCheckedChange = viewModel::updateAnalytics
                        )
                        SettingsRowDivider()
                        PrivacyToggleItem(
                            icon = Icons.Default.BugReport,
                            title = "Avísame si algo falla",
                            desc = "Informes de errores anónimos para corregir fallos.",
                            checked = uiState.preferences.crashReporting,
                            onCheckedChange = viewModel::updateCrashReporting
                        )
                    }

                    uiState.preferences.consentTimestamp?.let { ts ->
                        val dt = Instant.fromEpochMilliseconds(ts)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Consentimiento registrado el ${dt.date}",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sección 2: Trackfolio Premium ──────────────────────────
                    SettingsSectionHeader(label = "Trackfolio Premium")
                    PrivacyPremiumCard(isPremium = uiState.premiumStatus.isPremium) {
                        if (uiState.premiumStatus.isPremium) {
                            PrivacyInfoRow(
                                icon = Icons.Default.Star,
                                label = "Premium activo",
                                value = if (uiState.premiumStatus.isLifetime) "Acceso vitalicio" else "Suscripción activa"
                            )
                            if (!uiState.premiumStatus.isLifetime) {
                                val expiryDate = uiState.premiumStatus.expiryDate
                                if (expiryDate != null) {
                                    val expiry = Instant.fromEpochMilliseconds(expiryDate)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                    SettingsRowDivider()
                                    PrivacyInfoRow(
                                        icon = Icons.Default.CalendarMonth,
                                        label = "Válido hasta",
                                        value = "${expiry.date}"
                                    )
                                }
                            }
                            if (uiState.premiumStatus.managementUrl != null) {
                                SettingsRowDivider()
                                PrivacyNavigableRow(
                                    icon = Icons.Default.Settings,
                                    label = "Gestionar suscripción",
                                    onClick = { urlOpener.openUrl(uiState.premiumStatus.managementUrl!!) }
                                )
                            }
                        } else {
                            PrivacyInfoRow(
                                icon = Icons.Default.WorkspacePremium,
                                label = "Plan actual",
                                value = "Gratuito"
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
                            label = "Restaurar compra",
                            onClick = { viewModel.restorePurchases() }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sección 3: Datos recopilados ───────────────────────────
                    SettingsSectionHeader(label = "Datos recopilados")
                    SettingsGroupCard {
                        PrivacyDataRow(
                            icon = if (uiState.preferences.analytics) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            label = "Pantallas visitadas",
                            active = uiState.preferences.analytics
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = if (uiState.preferences.crashReporting) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            label = "Informes de errores",
                            active = uiState.preferences.crashReporting
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Cancel,
                            label = "Datos financieros",
                            active = false
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Cancel,
                            label = "Información personal",
                            active = false
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Info,
                            label = "Gestión de suscripción (RevenueCat)",
                            active = true,
                            isInfo = true
                        )
                        SettingsRowDivider()
                        PrivacyDataRow(
                            icon = Icons.Default.Info,
                            label = "Sugerencias y feedback",
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
                            containerColor = ExpenseRed.copy(alpha = 0.12f),
                            contentColor = ExpenseRed
                        )
                    ) {
                        Text(
                            "Revocar todo el consentimiento",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Al revocar, todos los datos de seguimiento se desactivan inmediatamente.",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { urlOpener.openUrl(PRIVACY_POLICY_URL) }) {
                        Text("Política de Privacidad", color = CyanAccent, fontSize = 13.sp)
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

// ─── Premium card — resalta el borde con CyanAccent cuando premium está activo ──
@Composable
private fun PrivacyPremiumCard(
    isPremium: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isPremium) CyanAccent.copy(alpha = 0.35f) else NavyBorder
    Card(
        modifier = Modifier.fillMaxWidth().border(0.5.dp, borderColor, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
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
            tint = if (checked) CyanAccent else TextTertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = NavySurface
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
        Icon(icon, null, tint = CyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(value, fontSize = 13.sp, color = CyanSubtle)
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
        Icon(icon, null, tint = CyanAccent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = TextTertiary,
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
        isInfo -> CyanAccent
        active -> IncomeGreen
        else   -> ExpenseRed
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 13.sp, color = TextPrimary)
    }
}
