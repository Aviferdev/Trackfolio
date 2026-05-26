package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import es.aviferdev.n3to.ui.common.toMaterialIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.fixedincome.FixedIncomePositionCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_archive
import n3to.composeapp.generated.resources.common_edit
import n3to.composeapp.generated.resources.error_account_required
import n3to.composeapp.generated.resources.error_asset_ticker_required
import n3to.composeapp.generated.resources.error_cannot_archive_asset
import n3to.composeapp.generated.resources.error_platform_already_exists
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.portfolio_category_add_platform
import n3to.composeapp.generated.resources.portfolio_settings_manage
import n3to.composeapp.generated.resources.portfolio_category_archive_confirm
import n3to.composeapp.generated.resources.portfolio_category_archive_cd
import n3to.composeapp.generated.resources.portfolio_category_archive_message
import n3to.composeapp.generated.resources.portfolio_category_archive_title
import n3to.composeapp.generated.resources.portfolio_category_assets_section
import n3to.composeapp.generated.resources.portfolio_category_edit_cd
import n3to.composeapp.generated.resources.portfolio_category_empty
import n3to.composeapp.generated.resources.portfolio_category_fixed_income_section
import n3to.composeapp.generated.resources.portfolio_category_new_asset
import n3to.composeapp.generated.resources.portfolio_category_no_platforms
import n3to.composeapp.generated.resources.portfolio_category_platforms_section
import n3to.composeapp.generated.resources.portfolio_category_archived_section
import n3to.composeapp.generated.resources.portfolio_category_restore
import n3to.composeapp.generated.resources.portfolio_category_title
import org.jetbrains.compose.resources.stringResource
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
            categoryName = state.category!!.name,
            linkedPlatforms = state.categoryPlatforms,
            allPlatforms = state.allPlatforms,
            onLink = { viewModel.linkPlatform(it) },
            onUnlink = { viewModel.unlinkPlatform(it) },
            onCreate = { name, icon, notes -> viewModel.createAndLinkPlatform(name, icon, notes) },
            onDismiss = { viewModel.closeLinkPlatformSheet() }
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
            portfolios = state.allPortfolios,
            onValidateIsin = { identifier, catId -> viewModel.validateIsin(identifier, catId) },
            onSave = { ticker, name, notes, _, currentPrice, isin, platformIds, maturityDate, fixedPct, sectorIds, regionPercents, portfolioId ->
                viewModel.addAsset(
                    ticker,
                    name,
                    notes,
                    currentPrice,
                    isin,
                    platformIds,
                    maturityDate,
                    fixedPct,
                    sectorIds,
                    regionPercents,
                    portfolioId
                )
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
            onValidateIsin = { identifier, catId -> viewModel.validateIsin(identifier, catId) },
            onSave = { ticker, name, notes, catId, currentPrice, isin, platformIds, maturityDate, fixedPct, sectorIds, regionPercents, portfolioId ->
                viewModel.editAsset(
                    editing,
                    ticker,
                    name,
                    notes,
                    catId,
                    currentPrice,
                    isin,
                    platformIds,
                    maturityDate,
                    fixedPct,
                    sectorIds,
                    regionPercents,
                    portfolioId
                )
            },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }

    state.pendingArchive?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelArchive() },
            containerColor = MaterialTheme.appColors.surface,
            icon = {
                Icon(
                    Icons.Outlined.Archive,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.appColors.primary
                )
            },
            title = {
                Text(
                    stringResource(Res.string.portfolio_category_archive_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(
                        Res.string.portfolio_category_archive_message,
                        pending.name,
                        pending.ticker
                    ),
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmArchive() }) {
                    Text(
                        stringResource(Res.string.portfolio_category_archive_confirm),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelArchive() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    state.error?.let { err ->
        val msg = when (err) {
            is es.aviferdev.n3to.ui.portfolio.CategoryDetailError.AccountRequired -> stringResource(
                Res.string.error_account_required
            )

            is es.aviferdev.n3to.ui.portfolio.CategoryDetailError.TickerAndNameRequired -> stringResource(
                Res.string.error_asset_ticker_required
            )

            is es.aviferdev.n3to.ui.portfolio.CategoryDetailError.CannotArchive -> stringResource(
                Res.string.error_cannot_archive_asset,
                err.ticker
            )

            is es.aviferdev.n3to.ui.portfolio.CategoryDetailError.PlatformAlreadyExists -> stringResource(
                Res.string.error_platform_already_exists
            )

            is es.aviferdev.n3to.ui.portfolio.CategoryDetailError.Unknown -> err.message
                ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = MaterialTheme.appColors.surface,
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
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.Medium
                    )
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
        modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = state.category?.name ?: stringResource(Res.string.portfolio_category_title),
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    label = stringResource(Res.string.portfolio_category_assets_section),
                    actionLabel = stringResource(Res.string.portfolio_category_new_asset),
                    onAction = onOpenAddSheet
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    if (state.activeAssets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(Res.string.portfolio_category_empty),
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary
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
                                        color = MaterialTheme.appColors.border,
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
                    SectionHeader(
                        label = stringResource(Res.string.portfolio_category_fixed_income_section),
                        actionLabel = "(${state.activeFixedIncome.size})",
                        onAction = { }
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
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
                                        color = MaterialTheme.appColors.border,
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
                    SectionHeader(
                        label = stringResource(Res.string.portfolio_category_platforms_section),
                        actionLabel = stringResource(Res.string.portfolio_settings_manage),
                        onAction = onOpenLinkPlatformSheet
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
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
                                        stringResource(Res.string.portfolio_category_no_platforms),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.appColors.textSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    TextButton(onClick = onOpenLinkPlatformSheet) {
                                        Text(
                                            stringResource(Res.string.portfolio_category_add_platform),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.appColors.primary,
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
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = platform.icon.toMaterialIcon(),
                                            contentDescription = null,
                                            tint = MaterialTheme.appColors.cyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            platform.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.appColors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (index < state.categoryPlatforms.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.appColors.border,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = 50.dp)
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
                        stringResource(Res.string.portfolio_category_archived_section),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
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
                                        color = MaterialTheme.appColors.border,
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
    N3toTheme {
        AssetCategoryDetailContent(
            state = AssetCategoryDetailUiState(
                category = AssetCategory(
                    id = "cat1",
                    name = "Acciones",
                    icon = "📈",
                    sortOrder = 0,
                    createdAt = 0L
                ),
                activeAssets = listOf(
                    Asset(
                        id = "a1",
                        accountId = "acc1",
                        ticker = "AAPL",
                        name = "Apple Inc.",
                        notes = null,
                        createdAt = 0L,
                        assetCategoryId = "cat1",
                        currentPrice = 150.0
                    ),
                    Asset(
                        id = "a2",
                        accountId = "acc1",
                        ticker = "MSFT",
                        name = "Microsoft Corp.",
                        notes = null,
                        createdAt = 0L,
                        assetCategoryId = "cat1",
                        currentPrice = 250.0
                    )
                ),
                categoryPlatforms = listOf(
                    Platform(
                        id = "p1",
                        name = "Interactive Brokers",
                        icon = "🏦",
                        sortOrder = 0,
                        createdAt = 0L
                    )
                ),
                allPlatforms = listOf(
                    Platform(
                        id = "p1",
                        name = "Interactive Brokers",
                        icon = "🏦",
                        sortOrder = 0,
                        createdAt = 0L
                    )
                ),
                allCategories = listOf(
                    AssetCategory(
                        id = "cat1",
                        name = "Acciones",
                        icon = "📈",
                        sortOrder = 0,
                        createdAt = 0L
                    )
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
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.appColors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.ticker.take(3),
                fontSize = if (asset.ticker.length > 3) 9.sp else 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                asset.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1
            )
            Text(
                asset.ticker,
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary
            )
        }
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.appColors.textSecondary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = MaterialTheme.appColors.surfaceElevated
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.common_edit),
                            color = MaterialTheme.appColors.textPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.textSecondary
                        )
                    },
                    onClick = { showMenu = false; onEdit() }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.common_archive),
                            color = MaterialTheme.appColors.expense
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.expense
                        )
                    },
                    onClick = { showMenu = false; onArchive() }
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.appColors.textTertiary,
            modifier = Modifier.size(18.dp)
        )
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
                .background(MaterialTheme.appColors.textSecondary.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.ticker.take(3),
                fontSize = if (asset.ticker.length > 3) 8.sp else 10.sp,
                color = MaterialTheme.appColors.textSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                asset.name,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textSecondary,
                maxLines = 1
            )
            Text(
                asset.ticker,
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.7f)
            )
        }
        TextButton(onClick = onRestore) {
            Text(
                stringResource(Res.string.portfolio_category_restore),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// SectionHeaderWithAction reemplazado por es.aviferdev.n3to.ui.common.SectionHeader
