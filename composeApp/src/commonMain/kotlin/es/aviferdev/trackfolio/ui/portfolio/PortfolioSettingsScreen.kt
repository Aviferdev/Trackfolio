package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import es.aviferdev.trackfolio.domain.usecase.asset.GetPriceReminderIntervalUseCase
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PortfolioSettingsScreen(
    onBack: () -> Unit,
    assetCatalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val assetCatalogState by assetCatalogViewModel.uiState.collectAsState()
    val platformState by platformViewModel.uiState.collectAsState()
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId by accountViewModel.selectedAccountId.collectAsState()

    // Recordatorio de precios
    val reminderIntervalUseCase = koinInject<GetPriceReminderIntervalUseCase>()
    var selectedInterval by remember { mutableIntStateOf(reminderIntervalUseCase.get()) }

    // Agrupar activos por categoría
    val assetsByCategory: Map<String?, List<Asset>> = remember(assetCatalogState.assets) {
        assetCatalogState.assets.groupBy { it.assetCategoryId }
    }
    val categories = assetCatalogState.categories
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
                }
                Text(
                    "Ajustes de Portfolio",
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
            // ── Sección: Categorías y Activos ─────────────────────────────
            item {
                Text(
                    "CATEGORÍAS Y ACTIVOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            items(categories, key = { it.id }) { category ->
                val assetsInCategory = assetsByCategory[category.id].orEmpty()
                val isExpanded = category.id in expandedCategories

                SettingsGroupCard {
                    // Cabecera de categoría (fija, no editable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategories = if (isExpanded)
                                    expandedCategories - category.id
                                else
                                    expandedCategories + category.id
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.icon, fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                category.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                "${assetsInCategory.size} ${if (assetsInCategory.size == 1) "activo" else "activos"}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Lista de activos dentro de la categoría
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                            if (assetsInCategory.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sin activos en esta categoría",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                assetsInCategory.forEachIndexed { index, asset ->
                                    AssetSettingsRow(
                                        asset = asset,
                                        onEdit = { assetCatalogViewModel.openEditSheet(asset) },
                                        onDelete = { assetCatalogViewModel.requestDelete(asset) }
                                    )
                                    if (index < assetsInCategory.lastIndex) {
                                        HorizontalDivider(
                                            color = BorderGray,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = 56.dp)
                                        )
                                    }
                                }
                            }

                            // Botón "Añadir activo" dentro de la categoría
                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { assetCatalogViewModel.openAddSheetForCategory(category.id) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PrimaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Añadir activo",
                                    fontSize = 13.sp,
                                    color = PrimaryDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ── Sección: Recordatorio de precios ──────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text(
                    "RECORDATORIO DE PRECIOS",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextSecondary
                )
            }
            item {
                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "Frecuencia de recordatorio",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color      = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Te recordaré actualizar los precios de tus activos cada cierto tiempo.",
                            fontSize = 12.sp,
                            color    = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(7, 14, 30).forEach { days ->
                                val isSelected = selectedInterval == days
                                OutlinedButton(
                                    onClick = {
                                        selectedInterval = days
                                        reminderIntervalUseCase.set(days)
                                    },
                                    shape   = RoundedCornerShape(8.dp),
                                    colors  = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) PrimaryDark else Color.Transparent,
                                        contentColor   = if (isSelected) Color.White else TextPrimary
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryDark else BorderGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text       = "${days}d",
                                        fontSize   = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Sección: Plataformas ──────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    title = "PLATAFORMAS",
                    actionLabel = "+ Nueva",
                    onAction = { platformViewModel.openAddSheet() }
                )
            }
            item {
                SettingsGroupCard {
                    if (platformState.platforms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin plataformas. Define los brokers, exchanges o bancos que usas.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        platformState.platforms.forEachIndexed { index, p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = p.name,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { platformViewModel.openEditSheet(p) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit, "Editar",
                                        modifier = Modifier.size(14.dp),
                                        tint = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { platformViewModel.requestDelete(p) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete, "Eliminar",
                                        modifier = Modifier.size(14.dp),
                                        tint = ExpenseRed
                                    )
                                }
                            }
                            if (index < platformState.platforms.lastIndex) {
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

    // Asset Catalog
    if (assetCatalogState.showAddSheet) {
        AddEditAssetBottomSheet(
            asset = null,
            categories = assetCatalogState.categories.filter { !it.archived },
            currencyCode = accountState.accounts.firstOrNull { it.id == selectedId }?.currency ?: "EUR",
            preselectedCategoryId = assetCatalogState.addForCategoryId,
            allPlatforms = platformState.platforms,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds ->
                assetCatalogViewModel.addAsset(ticker, name, notes, categoryId, currentPrice, platformIds)
            },
            onDismiss = { assetCatalogViewModel.closeAddSheet() }
        )
    }
    assetCatalogState.editing?.let { editing ->
        AddEditAssetBottomSheet(
            asset = editing,
            categories = assetCatalogState.categories.filter { !it.archived },
            currencyCode = accountState.accounts.firstOrNull { it.id == selectedId }?.currency ?: "EUR",
            allPlatforms = platformState.platforms,
            linkedPlatformIds = assetCatalogState.editingPlatformIds,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds ->
                assetCatalogViewModel.editAsset(editing, ticker, name, notes, categoryId, currentPrice, platformIds)
            },
            onDismiss = { assetCatalogViewModel.closeEditSheet() }
        )
    }
    assetCatalogState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { assetCatalogViewModel.cancelDelete() },
            containerColor = SurfaceWhite,
            icon = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text(
                    "Eliminar activo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "Se eliminará «${pending.name} (${pending.ticker})» y todos sus movimientos. Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { assetCatalogViewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { assetCatalogViewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    assetCatalogState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { assetCatalogViewModel.clearError() },
            containerColor = SurfaceWhite,
            title = {
                Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { assetCatalogViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Platforms
    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon -> platformViewModel.addPlatform(name, icon) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }
    platformState.editing?.let { editing ->
        AddEditPlatformSheet(
            initial = editing,
            onSave = { name, icon -> platformViewModel.renamePlatform(editing.id, name, icon) },
            onDismiss = { platformViewModel.closeEditSheet() }
        )
    }
    platformState.pendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { platformViewModel.cancelDelete() },
            containerColor = SurfaceWhite,
            icon = { Text(pending.icon, fontSize = 28.sp) },
            title = {
                Text(
                    "Archivar plataforma",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "Se archivará «${pending.name}». Los movimientos históricos conservarán la referencia.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { platformViewModel.confirmDelete() }) {
                    Text("Archivar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { platformViewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    platformState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { platformViewModel.clearError() },
            containerColor = SurfaceWhite,
            title = {
                Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { platformViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ── Componentes locales ──────────────────────────────────────────────────────

@Composable
private fun AssetSettingsRow(
    asset: Asset,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.ticker.take(3),
                fontSize = if (asset.ticker.length > 3) 8.sp else 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = asset.ticker,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = TextSecondary)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(14.dp), tint = ExpenseRed)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(actionLabel, fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) { Column(content = content) }
}
