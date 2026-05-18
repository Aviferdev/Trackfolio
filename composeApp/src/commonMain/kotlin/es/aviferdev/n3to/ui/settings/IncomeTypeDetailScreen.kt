package es.aviferdev.n3to.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
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
            containerColor = NavySurface,
            icon = { Text(pending.icon, fontSize = 28.sp) },
            title = {
                Text("Archivar emisor", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = {
                Text(
                    "Se archivará «${pending.name}». No aparecerá en los selectores de ingresos nuevos, pero los movimientos históricos conservarán la referencia.",
                    fontSize = 14.sp, color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { issuerViewModel.confirmDelete() }) {
                    Text("Archivar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { issuerViewModel.cancelDelete() }) {
                    Text("Cancelar", color = CyanAccent, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    issuerState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { issuerViewModel.clearError() },
            containerColor = NavySurface,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { issuerViewModel.clearError() }) {
                    Text("Aceptar", color = CyanAccent, fontWeight = FontWeight.Medium)
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
        modifier = modifier.fillMaxSize().background(NavyDeep)
    ) {
        TopBarApp(
            title = title,
            navigateBack = onBack,
            containerColor = NavySurface,
            dividerColor = NavyBorder
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
                                color = TextSecondary
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
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onEdit(issuer) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = TextSecondary)
                                }
                                IconButton(
                                    onClick = { onDelete(issuer) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                }
                            }
                            if (index < issuers.lastIndex) {
                                HorizontalDivider(
                                    color = NavyBorder,
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
                Issuer(id = "1", accountId = "a1", name = "Google", type = IssuerType.EMPLOYER, icon = "🏢", archived = false, createdAt = 0L),
                Issuer(id = "2", accountId = "a1", name = "Meta", type = IssuerType.EMPLOYER, icon = "🏢", archived = false, createdAt = 0L)
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


