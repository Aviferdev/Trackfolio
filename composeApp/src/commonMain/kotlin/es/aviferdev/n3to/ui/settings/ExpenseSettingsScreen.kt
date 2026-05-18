package es.aviferdev.n3to.ui.settings

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
import es.aviferdev.n3to.data.database.CategoryEntity
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_category_already_exists
import org.jetbrains.compose.resources.stringResource
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
        onEditLimit = { cat -> categoryViewModel.openLimitSheet(cat) },
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
            currentLimit = categoryState.editingLimit,
            currentLimitType = categoryState.editingLimitType,
            type = TransactionType.EXPENSE,
            onSave = { newName, limit, limitType ->
                categoryViewModel.renameAndUpdateLimit(editing.id, newName, limit, limitType)
            },
            onDismiss = { categoryViewModel.closeEditSheet() }
        )
    }

    // ── Limit sheet inline (solo límite) ──────────────────────────────────────
    if (categoryState.showLimitSheet && categoryState.limitSheetCategory != null) {
        val cat = categoryState.limitSheetCategory!!
        SetCategoryLimitSheet(
            categoryName = cat.name,
            currentLimit = categoryState.limitSheetCurrentLimit,
            currentLimitType = categoryState.limitSheetCurrentLimitType,
            onSave = { limit, limitType ->
                categoryViewModel.setCategoryLimit(cat.id, limit, limitType)
            },
            onDismiss = { categoryViewModel.closeLimitSheet() }
        )
    }

    categoryState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { categoryViewModel.cancelDelete() },
            containerColor = NavySurface,
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
                    Text("Cancelar", color = CyanAccent, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    categoryState.error?.let {
        val msg = when (it) {
            is es.aviferdev.n3to.ui.settings.CategoryError.AlreadyExists -> stringResource(Res.string.error_category_already_exists)
            is es.aviferdev.n3to.ui.settings.CategoryError.InvalidName -> stringResource(Res.string.error_category_already_exists)
            is es.aviferdev.n3to.ui.settings.CategoryError.Unknown -> it.message ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { categoryViewModel.clearError() },
            containerColor = NavySurface,
            title = {
                Text(stringResource(Res.string.common_error), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { categoryViewModel.clearError() }) {
                    Text(stringResource(Res.string.common_accept), color = CyanAccent, fontWeight = FontWeight.Medium)
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
    onEditLimit: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(NavyDeep).windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopBarApp(
            title = "Gastos",
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    label = "CATEGORÍAS",
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
                                // Botón de límite (€)
                                IconButton(
                                    onClick = { onEditLimit(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text(
                                        text = "€",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                }
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
                                    color = NavyBorder,
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
    N3toTheme {
        ExpenseSettingsContent(
            expenseCategories = listOf(
                CategoryEntity(id = "1", accountId = "preview", name = "Comida", type = "EXPENSE", isDefault = 0, archived = 0),
                CategoryEntity(id = "2", accountId = "preview", name = "Transporte", type = "EXPENSE", isDefault = 0, archived = 0)
            ),
            onAdd = {},
            onEdit = {},
            onEditLimit = {},
            onDelete = {},
            onBack = {}
        )
    }
}
