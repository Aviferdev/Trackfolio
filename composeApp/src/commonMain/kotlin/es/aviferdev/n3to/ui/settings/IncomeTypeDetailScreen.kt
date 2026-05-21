package es.aviferdev.n3to.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_archive
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.common_edit
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_issuer_already_exists
import n3to.composeapp.generated.resources.income_type_archive_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de detalle para un tipo de ingreso concreto.
 * Muestra el listado de emisores asociados a ese tipo y permite añadir/editar/archivar.
 */
// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun IncomeTypeDetailScreen(
    incomeType: IncomeType,
    onBack: () -> Unit,
    issuerViewModel: IssuerViewModel = koinViewModel()
) {
    val issuerState by issuerViewModel.uiState.collectAsState()
    val issuerType = incomeType.issuerType
    val issuers = issuerState.issuersByType[issuerType] ?: emptyList()

    IncomeTypeDetailContent(
        issuers = issuers,
        title = "${incomeType.emoji} ${incomeType.label}",
        sectionLabel = incomeType.issuerLabel.uppercase(),
        emptyLabel = "Sin ${incomeType.issuerLabel.lowercase()}s",
        onAdd = { issuerViewModel.openAddSheet(issuerType) },
        onEdit = { issuer -> issuerViewModel.openEditSheet(issuer) },
        onDelete = { issuer -> issuerViewModel.requestDelete(issuer) },
        onBack = onBack
    )

    // ── Issuer sheets y diálogos ─────────────────────────────────────────────
    if (issuerState.showAddSheet) {
        AddEditIssuerSheet(
            initial = null,
            type = issuerState.addType,
            onSave = { name, icon -> issuerViewModel.addIssuer(name, icon, issuerState.addType) },
            onDismiss = { issuerViewModel.closeAddSheet() }
        )
    }

    issuerState.editing?.let { editing ->
        AddEditIssuerSheet(
            initial = editing,
            type = editing.type,
            onSave = { name, icon -> issuerViewModel.rename(editing.id, name, icon, editing.type) },
            onDismiss = { issuerViewModel.closeEditSheet() }
        )
    }

    issuerState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { issuerViewModel.cancelDelete() },
            containerColor = MaterialTheme.appColors.navySurface,
            icon = { Text(pending.icon, fontSize = 28.sp) },
            title = {
                Text(
                    stringResource(Res.string.income_type_archive_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    "Se archivará «${pending.name}». No aparecerá en los selectores de ingresos nuevos, pero los movimientos históricos conservarán la referencia.",
                    fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { issuerViewModel.confirmDelete() }) {
                    Text(
                        stringResource(Res.string.common_archive),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { issuerViewModel.cancelDelete() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.cyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    issuerState.error?.let {
        val msg = when (it) {
            is es.aviferdev.n3to.ui.settings.IssuerError.AlreadyExists -> stringResource(Res.string.error_issuer_already_exists)
            is es.aviferdev.n3to.ui.settings.IssuerError.Unknown -> it.message
                ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { issuerViewModel.clearError() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = { Text(msg, fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { issuerViewModel.clearError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.cyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── CONTENT ────────────────────────────────────────────────────────────────────
@Composable
fun IncomeTypeDetailContent(
    issuers: List<Issuer>,
    title: String,
    sectionLabel: String,
    emptyLabel: String,
    onAdd: () -> Unit,
    onEdit: (Issuer) -> Unit,
    onDelete: (Issuer) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = title,
            navigateBack = onBack,
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    label = sectionLabel,
                    actionLabel = "+ Nuevo",
                    onAction = onAdd
                )
            }
            item {
                SettingsGroupCard {
                    if (issuers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                emptyLabel,
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary
                            )
                        }
                    } else {
                        issuers.forEachIndexed { index, issuer ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(issuer.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = issuer.name,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.appColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onEdit(issuer) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        stringResource(Res.string.common_edit),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.appColors.textSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(issuer) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        stringResource(Res.string.common_delete),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.appColors.expense
                                    )
                                }
                            }
                            if (index < issuers.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.navyBorder,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

// ─── PREVIEW ────────────────────────────────────────────────────────────────────
@Preview
@Composable
fun IncomeTypeDetailContentPreview() {
    N3toTheme {
        IncomeTypeDetailContent(
            issuers = listOf(
                Issuer(
                    id = "1",
                    accountId = "a1",
                    name = "Google",
                    type = IssuerType.EMPLOYER,
                    icon = "🏢",
                    archived = false,
                    createdAt = 0L
                ),
                Issuer(
                    id = "2",
                    accountId = "a1",
                    name = "Meta",
                    type = IssuerType.EMPLOYER,
                    icon = "🏢",
                    archived = false,
                    createdAt = 0L
                )
            ),
            title = "💼 Salario",
            sectionLabel = "EMPRESA",
            emptyLabel = "sin empresas",
            onAdd = {},
            onEdit = {},
            onDelete = {},
            onBack = {}
        )
    }
}

