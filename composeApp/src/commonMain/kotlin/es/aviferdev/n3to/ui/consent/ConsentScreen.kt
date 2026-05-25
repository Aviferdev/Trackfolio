package es.aviferdev.n3to.ui.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.browser.rememberUrlOpener
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConsentScreen(
    onConsentSaved: () -> Unit,
    viewModel: ConsentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val urlOpener = rememberUrlOpener()
    val colors = MaterialTheme.appColors

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            viewModel.onNavigationHandled()
            onConsentSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.navyDeep)
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.cyanGlow.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text(
                text = stringResource(Res.string.onboarding_welcome_subtitle),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(Res.string.consent_offline_description),
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(28.dp))

            // Info box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.navySurface)
                    .border(0.5.dp, colors.navyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        stringResource(Res.string.consent_collect_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.cyanAccent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(Res.string.consent_collect_screens), fontSize = 13.sp, color = colors.textSecondary)
                    Text(stringResource(Res.string.consent_collect_crashes), fontSize = 13.sp, color = colors.textSecondary)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(Res.string.consent_never_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.expense
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(Res.string.consent_never_financial), fontSize = 13.sp, color = colors.textSecondary)
                    Text(stringResource(Res.string.consent_never_personal), fontSize = 13.sp, color = colors.textSecondary)
                    Text(stringResource(Res.string.consent_never_location), fontSize = 13.sp, color = colors.textSecondary)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(Res.string.consent_feedback_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.cyanAccent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(Res.string.consent_feedback_suggestion), fontSize = 13.sp, color = colors.textSecondary)
                    Text(stringResource(Res.string.consent_feedback_no_personal), fontSize = 13.sp, color = colors.textSecondary)

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = colors.navyBorder, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))

                    Text(
                        stringResource(Res.string.consent_data_stays_device),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textTertiary
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Toggles
            ConsentToggleItem(
                icon = Icons.Default.Build,
                title = stringResource(Res.string.privacy_analytics_title),
                description = stringResource(Res.string.consent_analytics_description),
                checked = uiState.analytics,
                onCheckedChange = viewModel::onAnalyticsToggle
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = colors.navyBorder,
                thickness = 0.5.dp
            )
            ConsentToggleItem(
                icon = Icons.Default.BugReport,
                title = stringResource(Res.string.privacy_crash_title),
                description = stringResource(Res.string.consent_crash_description),
                checked = uiState.crashReporting,
                onCheckedChange = viewModel::onCrashReportingToggle
            )

            Spacer(Modifier.height(32.dp))

            // Botón principal — Aceptar
            Button(
                onClick = viewModel::acceptAll,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanAccent,
                    disabledContainerColor = colors.cyanAccent.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    stringResource(Res.string.consent_accept),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.navyDeep
                )
            }

            Spacer(Modifier.height(10.dp))

            // Botón secundario — Rechazar
            Button(
                onClick = viewModel::rejectAll,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.navySurface,
                    disabledContainerColor = colors.navySurface.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    stringResource(Res.string.consent_decline),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(Res.string.consent_both_buttons),
                fontSize = 12.sp,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = {
                urlOpener.openUrl(es.aviferdev.n3to.ui.settings.PRIVACY_POLICY_URL)
            }) {
                Text(
                    stringResource(Res.string.consent_privacy_policy),
                    fontSize = 13.sp,
                    color = colors.cyanAccent
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConsentToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) colors.cyanAccent else colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = colors.textSecondary,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.navyDeep,
                checkedTrackColor = colors.cyanAccent,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.navySurface,
                uncheckedBorderColor = colors.navyBorder
            )
        )
    }
}
