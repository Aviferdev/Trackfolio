package es.aviferdev.trackfolio.ui.settings

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
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.theme.*
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
        sectionLabel = issuerType.label.uppercase(),
        emptyLabel = "Sin ${issuerType.label.lowercase()}s",
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
            containerColor = SurfaceWhite,
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
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    issuerState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { issuerViewModel.clearError() },
            containerColor = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { issuerViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
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
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        TopBarApp(
            title = title,
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = sectionLabel,
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
                                    color = BorderGray,
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
    TrackfolioTheme {
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

// ─── Private components ──────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 0.7.sp)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryDark,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(content = content)
    }
}
