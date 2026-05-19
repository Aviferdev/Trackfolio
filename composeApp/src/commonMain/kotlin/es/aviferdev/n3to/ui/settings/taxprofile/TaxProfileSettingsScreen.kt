package es.aviferdev.n3to.ui.settings.taxprofile

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import es.aviferdev.n3to.ui.common.help.HelpContent
import es.aviferdev.n3to.ui.common.help.HelpKeys
import es.aviferdev.n3to.ui.common.help.HelpTooltipIcon
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.common.navigation.TopBarApp

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.PrimaryDark

import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaxProfileSettingsScreen(
    onBack: () -> Unit,
    viewModel: TaxProfileSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.appColors.background,
        topBar = { TopBarApp(title = "Perfil fiscal", navigateBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = MaterialTheme.appColors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir perfil")
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
                    Text("Sin perfiles fiscales", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Añade uno con el botón +", fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "El perfil fiscal define los tramos IRPF y tipos de rendimiento que se aplican en el informe fiscal. Sin él, los cálculos no serán precisos.",
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
                item { SectionHeader(label = "HISTORIAL DE PERFILES") }
                item {
                    AlertBanner(
                        icon = "ℹ️",
                        label = "El perfil más reciente (por fecha de inicio) es el que se aplica a los cálculos fiscales actuales. Puedes tener uno por cada sistema fiscal que hayas usado.",
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
                Text("Eliminar perfil", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary)
            },
            text = {
                Text(
                    "Se eliminará este perfil fiscal. Los movimientos ya registrados no se verán afectados.",
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancelar", color = MaterialTheme.appColors.primary)
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
            title = { Text("Error", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary) },
            text = { Text(msg, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar", color = MaterialTheme.appColors.primary, fontWeight = FontWeight.SemiBold)
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
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.appColors.expense, modifier = Modifier.size(20.dp))
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
                text = "Nuevo perfil fiscal",
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
                    Text("País / sistema fiscal", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textTertiary)
                    HelpTooltipIcon(
                        title = HelpContent.texts[HelpKeys.IRPF_SYSTEM]?.title ?: "",
                        body  = HelpContent.texts[HelpKeys.IRPF_SYSTEM]?.body  ?: ""
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
                label = "Vigente desde",
                dateMillis = effectiveDateMillis,
                onDateSelected = onDateChange
            )
            Text(
                "Si cambias de sistema fiscal (p. ej. al mudarte de país), añade un nuevo perfil con la fecha en que comenzó el cambio.",
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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Guardar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun profileLabel(countryCode: String?): String = when (countryCode) {
    "ES" -> "España"
    "GB" -> "Reino Unido"
    "US" -> "Estados Unidos"
    "DE" -> "Alemania"
    else -> "Personalizado"
}

private fun profileFlag(countryCode: String?): String = when (countryCode) {
    "ES" -> "🇪🇸"
    "GB" -> "🇬🇧"
    "US" -> "🇺🇸"
    "DE" -> "🇩🇪"
    else -> "🌐"
}
