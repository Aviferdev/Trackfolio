package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.data.database.CategoryEntity
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun ExpenseSettingsScreen(
    onBack: () -> Unit,
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val categoryState by categoryViewModel.uiState.collectAsState()

    ExpenseSettingsContent(
        expenseCategories = categoryState.expenseCategories,
        onAdd = { categoryViewModel.openAddSheet() },
        onEdit = { cat -> categoryViewModel.openEditSheet(cat) },
        onDelete = { cat -> categoryViewModel.requestDelete(cat) },
        onBack = onBack
    )

    // ── Sheets y diálogos ────────────────────────────────────────────────────
    if (categoryState.showAddSheet) {
        AddCategorySheet(
            type = TransactionType.EXPENSE,
            onSave = { name -> categoryViewModel.addCategory(name) },
            onDismiss = { categoryViewModel.closeAddSheet() }
        )
    }

    categoryState.editing?.let { editing ->
        EditCategorySheet(
            currentName = editing.name,
            type = TransactionType.EXPENSE,
            onSave = { newName -> categoryViewModel.renameCategory(editing.id, newName) },
            onDismiss = { categoryViewModel.closeEditSheet() }
        )
    }

    categoryState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { categoryViewModel.cancelDelete() },
            containerColor = SurfaceWhite,
            icon = { Text("\uD83D\uDDC2\uFE0F", fontSize = 28.sp) },
            title = {
                Text(
                    "Eliminar categoría",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "Se eliminará «${pending.name}» del listado. Los movimientos que ya tengan asignada esta categoría conservarán su nombre y no se perderán datos.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { categoryViewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryViewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    categoryState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { categoryViewModel.clearError() },
            containerColor = SurfaceWhite,
            title = {
                Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { categoryViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── CONTENT ────────────────────────────────────────────────────────────────────
@Composable
fun ExpenseSettingsContent(
    expenseCategories: List<CategoryEntity>,
    onAdd: () -> Unit,
    onEdit: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        TopBarApp(title = "Gastos", navigateBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "CATEGORÍAS",
                    actionLabel = "+ Nueva",
                    onAction = onAdd
                )
            }
            item {
                SettingsGroupCard {
                    if (expenseCategories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin categorías de gastos", fontSize = 13.sp, color = TextSecondary)
                        }
                    } else {
                        expenseCategories.forEachIndexed { index, cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(8.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onEdit(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = TextSecondary)
                                }
                                IconButton(
                                    onClick = { onDelete(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                }
                            }
                            if (index < expenseCategories.lastIndex) {
                                HorizontalDivider(
                                    color = BorderGray,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 36.dp)
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
fun ExpenseSettingsContentPreview() {
    TrackfolioTheme {
        ExpenseSettingsContent(
            expenseCategories = listOf(
                CategoryEntity(id = "1", name = "Comida", type = "EXPENSE", isDefault = 0, archived = 0),
                CategoryEntity(id = "2", name = "Transporte", type = "EXPENSE", isDefault = 0, archived = 0)
            ),
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
