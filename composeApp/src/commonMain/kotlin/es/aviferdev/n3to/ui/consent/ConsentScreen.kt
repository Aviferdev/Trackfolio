package es.aviferdev.n3to.ui.consent

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.browser.rememberUrlOpener
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.consent_accept
import n3to.composeapp.generated.resources.consent_decline
import n3to.composeapp.generated.resources.consent_privacy_policy
import n3to.composeapp.generated.resources.onboarding_welcome_subtitle
import n3to.composeapp.generated.resources.privacy_analytics_title
import n3to.composeapp.generated.resources.privacy_crash_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de consentimiento en onboarding.
 *
 * - Lidera con lo que NUNCA se recopila
 * - Lenguaje coloquial, cero jerga legal
 * - Ambos botones abren la app (rechazar no bloquea)
 */
@Composable
fun ConsentScreen(
    onConsentSaved: () -> Unit,
    viewModel: ConsentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val urlOpener = rememberUrlOpener()

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            viewModel.onNavigationHandled()
            onConsentSaved()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Cabecera ─────────────────────────────────────────────
            Text(
                text = stringResource(Res.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Trackfolio funciona 100% sin internet. " +
                        "Tus cuentas, transacciones y balances se guardan " +
                        "solo en tu dispositivo. Eres el dueño de tus datos.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            // ── INFO BOX ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✅ Recopilamos (solo si nos ayudas):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Pantallas que visitas → para saber qué mejorar",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Informes de errores → para arreglar fallos rápido",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "❌ NUNCA recopilamos:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Tus datos financieros (transacciones, cuentas, balances)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Tu información personal (email, nombre, contactos)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Tu ubicación",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "✍️ Si envías feedback (voluntario):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Solo recibimos tu sugerencia si decides enviarla desde el formulario de la app",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Te recomendamos no incluir datos personales en tu mensaje",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "💳 Si adquieres Trackfolio Premium:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• RevenueCat procesa un identificador anónimo y tu historial de suscripción " +
                                "para gestionar tu compra (necesario para el servicio)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• No compartimos tus datos financieros con RevenueCat",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💡 Tus datos financieros nunca salen de tu dispositivo.",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            // ── Toggles ──────────────────────────────────────────────
            ConsentToggleItem(
                icon = Icons.Default.Build,
                title = stringResource(Res.string.privacy_analytics_title),
                description = "Saber qué funciones usas más nos ayuda a pulir la app. Datos anónimos.",
                checked = uiState.analytics,
                onCheckedChange = viewModel::onAnalyticsToggle
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ConsentToggleItem(
                icon = Icons.Default.BugReport,
                title = stringResource(Res.string.privacy_crash_title),
                description = "Si la app tiene un error, nos avisa para que podamos arreglarlo rápido.",
                checked = uiState.crashReporting,
                onCheckedChange = viewModel::onCrashReportingToggle
            )
            Spacer(Modifier.height(32.dp))

            // ── Botones (ambos abren la app) ─────────────────────────
            Button(
                onClick = viewModel::acceptAll,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(Res.string.consent_accept), fontSize = 15.sp) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = viewModel::rejectAll,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(Res.string.consent_decline), fontSize = 15.sp) }
            Spacer(Modifier.height(12.dp))
            Text(
                "Ambos botones abren la app. Elegir uno u otro solo cambia " +
                        "si nos dejas ayudarte a mejorarla.",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // ── Política de privacidad ───────────────────────────────
            TextButton(onClick = {
                urlOpener.openUrl(es.aviferdev.n3to.ui.settings.PRIVACY_POLICY_URL)
            }) {
                Text(stringResource(Res.string.consent_privacy_policy), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConsentToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon, contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
