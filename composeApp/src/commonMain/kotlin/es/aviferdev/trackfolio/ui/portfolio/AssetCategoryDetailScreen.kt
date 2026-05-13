package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AssetCategoryDetailScreen(
    categoryId: String,
    onBack: () -> Unit,
    onAssetClick: (String) -> Unit,
    onFixedIncomeClick: (String) -> Unit = {},
    viewModel: AssetCategoryDetailViewModel = koinViewModel(parameters = { parametersOf(categoryId) })
) {
    val state by viewModel.uiState.collectAsState()

    AssetCategoryDetailContent(
        state = state,
        categoryId = categoryId,
        onBack = onBack,
        onAssetClick = onAssetClick,
        onFixedIncomeClick = onFixedIncomeClick,
        onOpenAddSheet = { viewModel.openAddSheet() },
        onOpenEditSheet = { asset -> viewModel.openEditSheet(asset) },
        onRequestArchive = { asset -> viewModel.requestArchive(asset) },
        onRestoreAsset = { assetId -> viewModel.restoreAsset(assetId) },
        onOpenLinkPlatformSheet = { viewModel.openLinkPlatformSheet() }
    )

    if (state.showLinkPlatformSheet && state.category != null) {
        LinkPlatformToCategorySheet(
            categoryName    = state.category!!.name,
            linkedPlatforms = state.categoryPlatforms,
            allPlatforms    = state.allPlatforms,
            onLink          = { viewModel.linkPlatform(it) },
            onUnlink        = { viewModel.unlinkPlatform(it) },
            onCreate        = { name, icon, notes -> viewModel.createAndLinkPlatform(name, icon, notes) },
            onDismiss       = { viewModel.closeLinkPlatformSheet() }
        )
    }

    if (state.showAddSheet) {
        AddEditAssetBottomSheet(
            asset = null,
            categories = state.allCategories,
                        preselectedCategoryId = categoryId,
            allPlatforms = state.categoryPlatforms,
            allSectors = state.allSectors,
            linkedSectorIds = emptySet(),
            allRegions = state.allRegions,
            linkedRegionPercents = emptyMap(),
            onSave = { ticker, name, notes, _, currentPrice, platformIds, maturityDate, fixedPct, sectorIds, regionPercents ->
                viewModel.addAsset(ticker, name, notes, currentPrice, platformIds, maturityDate, fixedPct, sectorIds, regionPercents)
            },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }

    state.editing?.let { editing ->
        AddEditAssetBottomSheet(
            asset = editing,
            categories = state.allCategories,
                        allPlatforms = state.categoryPlatforms,
            linkedPlatformIds = state.editingPlatformIds,
            allSectors = state.allSectors,
            linkedSectorIds = state.editingSectorIds,
            allRegions = state.allRegions,
            linkedRegionPercents = state.editingRegionPercents,
            linkedFixedIncomePercent = state.editingFixedIncomePercent,
            onSave = { ticker, name, notes, catId, currentPrice, platformIds, maturityDate, fixedPct, sectorIds, regionPercents ->
                viewModel.editAsset(editing, ticker, name, notes, catId, currentPrice, platformIds, maturityDate, fixedPct, sectorIds, regionPercents)
            },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }

    state.pendingArchive?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelArchive() },
            containerColor = SurfaceWhite,
            icon = { Text("📦", fontSize = 28.sp) },
            title = {
                Text(
                    "Archivar activo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    "«${pending.name} (${pending.ticker})» se archivará. Sus movimientos y P&L histórico se conservarán.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmArchive() }) {
                    Text("Archivar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelArchive() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AssetCategoryDetailContent(
    state: AssetCategoryDetailUiState,
    categoryId: String,
    onBack: () -> Unit,
    onAssetClick: (String) -> Unit,
    onFixedIncomeClick: (String) -> Unit,
    onOpenAddSheet: () -> Unit,
    onOpenEditSheet: (Asset) -> Unit,
    onRequestArchive: (Asset) -> Unit,
    onRestoreAsset: (String) -> Unit,
    onOpenLinkPlatformSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        TopBarApp(
            title = state.category?.name ?: "Categoría",
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderWithAction(
                    title = "ACTIVOS",
                    actionLabel = "+ Nuevo",
                    onAction = onOpenAddSheet
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    if (state.activeAssets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin activos en esta categoría",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Column {
                            state.activeAssets.forEachIndexed { index, asset ->
                                AssetRow(
                                    asset = asset,
                                    onClick = { onAssetClick(asset.id) },
                                    onEdit = { onOpenEditSheet(asset) },
                                    onArchive = { onRequestArchive(asset) }
                                )
                                if (index < state.activeAssets.lastIndex) {
                                    HorizontalDivider(
                                        color = BorderGray,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 56.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.activeFixedIncome.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    SectionHeaderWithAction(
                        title = "RENTA FIJA",
                        actionLabel = "(${state.activeFixedIncome.size})",
                        onAction = { }
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            state.activeFixedIncome.forEachIndexed { index, fiRow ->
                                FixedIncomePositionCard(
                                    row = fiRow,
                                                                        balancesHidden = false,
                                    onClick = { onFixedIncomeClick(fiRow.position.id) }
                                )
                                if (index < state.activeFixedIncome.lastIndex) {
                                    HorizontalDivider(
                                        color = BorderGray,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.category != null) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    SectionHeaderWithAction(
                        title = "PLATAFORMAS",
                        actionLabel = "Gestionar",
                        onAction = onOpenLinkPlatformSheet
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        if (state.categoryPlatforms.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Sin plataformas vinculadas",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    TextButton(onClick = onOpenLinkPlatformSheet) {
                                        Text(
                                            "+ Añadir plataforma",
                                            fontSize = 13.sp,
                                            color = PrimaryDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            Column {
                                state.categoryPlatforms.forEachIndexed { index, platform ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(platform.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            platform.name,
                                            fontSize = 15.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (index < state.categoryPlatforms.lastIndex) {
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
                }
            }

            if (state.archivedAssets.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    Text(
                        "ARCHIVADOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            state.archivedAssets.forEachIndexed { index, asset ->
                                ArchivedAssetRow(
                                    asset = asset,
                                    onRestore = { onRestoreAsset(asset.id) }
                                )
                                if (index < state.archivedAssets.lastIndex) {
                                    HorizontalDivider(
                                        color = BorderGray,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 56.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Preview
@Composable
private fun AssetCategoryDetailContentPreview() {
    TrackfolioTheme {
        AssetCategoryDetailContent(
            state = AssetCategoryDetailUiState(
                category = AssetCategory(id = "cat1", name = "Acciones", icon = "📈", sortOrder = 0, createdAt = 0L),
                activeAssets = listOf(
                    Asset(id = "a1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0),
                    Asset(id = "a2", accountId = "acc1", ticker = "MSFT", name = "Microsoft Corp.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 250.0)
                ),
                categoryPlatforms = listOf(
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L)
                ),
                allPlatforms = listOf(
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L)
                ),
                allCategories = listOf(
                    AssetCategory(id = "cat1", name = "Acciones", icon = "📈", sortOrder = 0, createdAt = 0L)
                ),
            ),
            categoryId = "cat1",
            onBack = {},
            onAssetClick = {},
            onFixedIncomeClick = {},
            onOpenAddSheet = {},
            onOpenEditSheet = {},
            onRequestArchive = {},
            onRestoreAsset = {},
            onOpenLinkPlatformSheet = {}
        )
    }
}

// ── Componentes locales ──────────────────────────────────────────────────────

@Composable
private fun AssetRow(
    asset: Asset,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(asset.name, fontSize = 14.sp, color = TextPrimary, maxLines = 1)
            Text(asset.ticker, fontSize = 11.sp, color = TextSecondary)
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(14.dp), tint = TextSecondary)
        }
        IconButton(onClick = onArchive, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, "Archivar", modifier = Modifier.size(14.dp), tint = TextSecondary)
        }
        Text("›", fontSize = 18.sp, color = TextSecondary)
    }
}

@Composable
private fun ArchivedAssetRow(
    asset: Asset,
    onRestore: () -> Unit
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
                .background(TextSecondary.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.ticker.take(3),
                fontSize = if (asset.ticker.length > 3) 8.sp else 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.name, fontSize = 14.sp, color = TextSecondary, maxLines = 1)
            Text(asset.ticker, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f))
        }
        TextButton(onClick = onRestore) {
            Text("Restaurar", fontSize = 12.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SectionHeaderWithAction(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        TextButton(
            onClick = onAction,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(actionLabel, fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
        }
    }
}
