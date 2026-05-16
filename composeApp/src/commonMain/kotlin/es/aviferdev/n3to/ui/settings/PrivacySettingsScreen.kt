package es.aviferdev.n3to.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.core.browser.rememberUrlOpener
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/** URL de la política de privacidad */
const val PRIVACY_POLICY_URL = "https://www.n3to.avifer.dev/privacy-policy"

/**
 * Pantalla unificada de Privacidad y datos.
 *
 * Consolida en una sola vista:
 * - Consentimiento (2 toggles: analytics, crash reporting)
 * - Trackfolio Premium (estado + comprar/restaurar)
 * - Datos recopilados (informativo, según toggles)
 * - Revocar consentimiento
 * - Política de privacidad
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    // Observar eventos de restauración
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

    // ── Diálogo revocación ──────────────────────────────────────
    if (uiState.showRevokeConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRevokeConfirmation,
            title = { Text("¿Revocar todo el consentimiento?") },
            text = {
                Text("Se desactivarán el análisis de uso y los informes de errores. Esta acción se aplica inmediatamente.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRevokeAll) {
                    Text("Revocar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRevokeConfirmation) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidad y datos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ══════════════════════════════════════════════════════
            // SECCIÓN 1: CONSENTIMIENTO
            // ══════════════════════════════════════════════════════
            SectionHeader("Consentimiento")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Column {
                    PrivacyToggleItem(
                        icon = Icons.Default.Build,
                        title = "Ayúdanos a mejorar",
                        desc = "Análisis de uso anónimo para mejorar la app.",
                        checked = uiState.preferences.analytics,
                        onCheckedChange = viewModel::updateAnalytics
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    PrivacyToggleItem(
                        icon = Icons.Default.BugReport,
                        title = "Avísame si algo falla",
                        desc = "Informes de errores anónimos para corregir fallos.",
                        checked = uiState.preferences.crashReporting,
                        onCheckedChange = viewModel::updateCrashReporting
                    )
                }
            }

            uiState.preferences.consentTimestamp?.let { ts ->
                val dt = Instant.fromEpochMilliseconds(ts)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                Spacer(Modifier.height(4.dp))
                Text(
                    "Consentimiento registrado el ${dt.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ══════════════════════════════════════════════════════
            // SECCIÓN 2: TRACKFOLIO PREMIUM
            // ══════════════════════════════════════════════════════
            SectionHeader("Trackfolio Premium")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Column {
                    if (uiState.premiumStatus.isPremium) {
                        PremiumInfoRow(
                            icon = Icons.Default.Star,
                            label = "Premium activo",
                            value = if (uiState.premiumStatus.isLifetime) "Acceso vitalicio" else "Suscripción activa"
                        )
                        if (!uiState.premiumStatus.isLifetime) {
                            val expiryDate = uiState.premiumStatus.expiryDate
                            if (expiryDate != null) {
                                val expiry = Instant.fromEpochMilliseconds(expiryDate)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                                PremiumInfoRow(
                                    icon = Icons.Default.CalendarMonth,
                                    label = "Válido hasta",
                                    value = "${expiry.date}"
                                )
                            }
                        }
                        if (uiState.premiumStatus.managementUrl != null) {
                            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                            PremiumNavigableRow(
                                icon = Icons.Default.Settings,
                                label = "Gestionar suscripción",
                                onClick = { urlOpener.openUrl(uiState.premiumStatus.managementUrl!!) }
                            )
                        }
                    } else {
                        PremiumInfoRow(
                            icon = Icons.Default.WorkspacePremium,
                            label = "Plan actual",
                            value = "Gratuito"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                        PremiumNavigableRow(
                            icon = Icons.Default.Star,
                            label = "Hazte Premium",
                            onClick = onNavigateToPremium
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                        PremiumInfoRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Sin anuncios",
                            value = "Al hacerte Premium"
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    PremiumNavigableRow(
                        icon = Icons.Default.Restore,
                        label = "Restaurar compra",
                        onClick = { viewModel.restorePurchases() }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ══════════════════════════════════════════════════════
            // SECCIÓN 3: DATOS RECOPILADOS (informativo)
            // ══════════════════════════════════════════════════════
            SectionHeader("Datos recopilados")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Column {
                    DataInfoRow(
                        icon = if (uiState.preferences.analytics) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        label = "Pantallas visitadas",
                        active = uiState.preferences.analytics
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    DataInfoRow(
                        icon = if (uiState.preferences.crashReporting) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        label = "Informes de errores",
                        active = uiState.preferences.crashReporting
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    DataInfoRow(
                        icon = Icons.Default.Cancel,
                        label = "Datos financieros",
                        active = false
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    DataInfoRow(
                        icon = Icons.Default.Cancel,
                        label = "Información personal",
                        active = false
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    DataInfoRow(
                        icon = Icons.Default.Info,
                        label = "Gestión de suscripción (RevenueCat)",
                        active = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ══════════════════════════════════════════════════════
            // ACCIONES FINALES
            // ══════════════════════════════════════════════════════
            Button(
                onClick = viewModel::requestRevokeAll,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Revocar todo el consentimiento")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Al revocar, todos los datos de seguimiento se desactivan inmediatamente.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = {
                    urlOpener.openUrl(PRIVACY_POLICY_URL)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Política de Privacidad")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Helpers UI ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun PrivacyToggleItem(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PremiumInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PremiumNavigableRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.KeyboardArrowRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DataInfoRow(icon: ImageVector, label: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, null,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
