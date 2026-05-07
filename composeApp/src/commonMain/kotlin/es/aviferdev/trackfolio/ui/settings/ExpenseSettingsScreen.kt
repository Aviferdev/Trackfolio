package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
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
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExpenseSettingsScreen(
    onBack: () -> Unit,
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val categoryState by categoryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
                }
                Text(
                    "Gastos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── CATEGORÍAS DE GASTOS ─────────────────────────────────────────
            item {
                SectionHeader(
                    title = "CATEGORÍAS",
                    actionLabel = "+ Nueva",
                    onAction = { categoryViewModel.openAddSheet() }
                )
            }
            item {
                SettingsGroupCard {
                    if (categoryState.expenseCategories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin categorías de gastos", fontSize = 13.sp, color = TextSecondary)
                        }
                    } else {
                        categoryState.expenseCategories.forEachIndexed { index, cat ->
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
                                    onClick = { categoryViewModel.openEditSheet(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = TextSecondary)
                                }
                                IconButton(
                                    onClick = { categoryViewModel.requestDelete(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                }
                            }
                            if (index < categoryState.expenseCategories.lastIndex) {
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
