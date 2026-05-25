package es.aviferdev.n3to.ui.settings.taxprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.ui.common.AlertBanner
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.common.help.HelpTooltipIcon
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.common_save
import n3to.composeapp.generated.resources.help_tax_system_body
import n3to.composeapp.generated.resources.help_tax_system_title
import n3to.composeapp.generated.resources.tax_profile_add_cd
import n3to.composeapp.generated.resources.tax_profile_country_label
import n3to.composeapp.generated.resources.tax_profile_delete_cd
import n3to.composeapp.generated.resources.tax_profile_delete_title
import n3to.composeapp.generated.resources.tax_profile_empty_subtitle
import n3to.composeapp.generated.resources.tax_profile_empty_title
import n3to.composeapp.generated.resources.tax_profile_history_hint
import n3to.composeapp.generated.resources.tax_profile_history_section
import n3to.composeapp.generated.resources.tax_profile_add_hint
import n3to.composeapp.generated.resources.tax_profile_country_custom
import n3to.composeapp.generated.resources.tax_profile_country_de
import n3to.composeapp.generated.resources.tax_profile_country_es
import n3to.composeapp.generated.resources.tax_profile_country_gb
import n3to.composeapp.generated.resources.tax_profile_country_us
import n3to.composeapp.generated.resources.tax_profile_delete_message
import n3to.composeapp.generated.resources.tax_profile_empty_description
import n3to.composeapp.generated.resources.tax_profile_new_title
import n3to.composeapp.generated.resources.tax_profile_title
import n3to.composeapp.generated.resources.tax_profile_valid_from_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaxProfileSettingsScreen(
    onBack: () -> Unit,
    viewModel: TaxProfileSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.appColors.navyDeep,
        topBar = {
            TopBarWithActionsApp(
                title = stringResource(Res.string.tax_profile_title),
                navigateBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = MaterialTheme.appColors.primary,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.tax_profile_add_cd)
                )
            }
        }
    ) { innerPadding ->
        if (state.snapshots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(Res.string.tax_profile_empty_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(Res.string.tax_profile_empty_subtitle),
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(Res.string.tax_profile_empty_description),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { SectionHeader(label = stringResource(Res.string.tax_profile_history_section)) }
                item {
                    AlertBanner(
                        icon = "ℹ️",
                        label = stringResource(Res.string.tax_profile_history_hint),
                        color = MaterialTheme.appColors.primary
                    )
                }
                items(state.snapshots, key = { it.id }) { snapshot ->
                    TaxProfileSnapshotRow(
                        snapshot = snapshot,
                        onDelete = { viewModel.requestDelete(snapshot.id) }
                    )
                }
            }
        }
    }

    // ── Add sheet ────────────────────────────────────────────────────────────
    if (state.showAddSheet) {
        AddTaxProfileSheet(
            selectedProfile = state.selectedProfile,
            effectiveDateMillis = state.effectiveDateMillis,
            isSaving = state.isSaving,
            isValid = state.isAddValid,
            onProfileChange = { viewModel.onProfileChange(it) },
            onDateChange = { viewModel.onDateChange(it) },
            onConfirm = { viewModel.confirmAdd() },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }

    // ── Delete confirm ───────────────────────────────────────────────────────
    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.tax_profile_delete_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.tax_profile_delete_message),
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Error ────────────────────────────────────────────────────────────────
    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = { Text(msg, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ── Snapshot row ─────────────────────────────────────────────────────────────

@Composable
private fun TaxProfileSnapshotRow(
    snapshot: TaxProfileSnapshot,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = profileFlag(snapshot.profile.countryCode),
                fontSize = 24.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profileLabel(snapshot.profile.countryCode),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
                Text(
                    text = "${snapshot.profile.currency}  ·  desde ${snapshot.effectiveFrom}",
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.tax_profile_delete_cd),
                    tint = MaterialTheme.appColors.expense,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Add sheet ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaxProfileSheet(
    selectedProfile: TaxProfile,
    effectiveDateMillis: Long,
    isSaving: Boolean,
    isValid: Boolean,
    onProfileChange: (TaxProfile) -> Unit,
    onDateChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .background(MaterialTheme.appColors.dragHandle, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.tax_profile_new_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // ── País ─────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(Res.string.tax_profile_country_label),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textTertiary
                    )
                    HelpTooltipIcon(
                        title = stringResource(Res.string.help_tax_system_title),
                        body = stringResource(Res.string.help_tax_system_body)
                    )
                }
                TaxProfile.ALL.forEach { profile ->
                    val isSelected = profile.countryCode == selectedProfile.countryCode
                    SelectableChip(
                        label = "${profileFlag(profile.countryCode)}  ${profileLabel(profile.countryCode)}",
                        selected = isSelected,
                        onClick = { onProfileChange(profile) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Fecha de vigencia ─────────────────────────────────────────────
            DatePickerRow(
                label = stringResource(Res.string.tax_profile_valid_from_label),
                dateMillis = effectiveDateMillis,
                onDateSelected = onDateChange
            )
            Text(
                stringResource(Res.string.tax_profile_add_hint),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary
            )

            // ── Guardar ───────────────────────────────────────────────────────
            Button(
                onClick = onConfirm,
                enabled = isValid && !isSaving,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(Res.string.common_save),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun profileLabel(countryCode: String?): String = when (countryCode) {
    "ES" -> stringResource(Res.string.tax_profile_country_es)
    "GB" -> stringResource(Res.string.tax_profile_country_gb)
    "US" -> stringResource(Res.string.tax_profile_country_us)
    "DE" -> stringResource(Res.string.tax_profile_country_de)
    else -> stringResource(Res.string.tax_profile_country_custom)
}

private fun profileFlag(countryCode: String?): String = when (countryCode) {
    "ES" -> "🇪🇸"
    "GB" -> "🇬🇧"
    "US" -> "🇺🇸"
    "DE" -> "🇩🇪"
    else -> "🌐"
}
