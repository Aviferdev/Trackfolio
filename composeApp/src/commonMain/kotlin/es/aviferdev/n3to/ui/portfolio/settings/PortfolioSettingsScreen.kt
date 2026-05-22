package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.AddEditPlatformSheet
import es.aviferdev.n3to.ui.portfolio.AddEditPortfolioBottomSheet
import es.aviferdev.n3to.ui.portfolio.PlatformError
import es.aviferdev.n3to.ui.portfolio.RegionManagementSheet
import es.aviferdev.n3to.ui.portfolio.SectorManagementSheet
import es.aviferdev.n3to.ui.portfolio.settings.components.CategoryListSection
import es.aviferdev.n3to.ui.portfolio.settings.components.PlatformListSection
import es.aviferdev.n3to.ui.portfolio.settings.components.PortfolioListSection
import es.aviferdev.n3to.ui.portfolio.settings.components.RegionListSection
import es.aviferdev.n3to.ui.portfolio.settings.components.ReminderIntervalSection
import es.aviferdev.n3to.ui.portfolio.settings.components.SectorListSection
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_platform_already_exists
import n3to.composeapp.generated.resources.portfolio_error_no_account_message
import n3to.composeapp.generated.resources.portfolio_error_no_account_title
import n3to.composeapp.generated.resources.portfolio_settings_add
import n3to.composeapp.generated.resources.portfolio_settings_category_count_many
import n3to.composeapp.generated.resources.portfolio_settings_category_count_one
import n3to.composeapp.generated.resources.portfolio_settings_category_section
import n3to.composeapp.generated.resources.portfolio_settings_delete_cd
import n3to.composeapp.generated.resources.portfolio_settings_delete_portfolio_message
import n3to.composeapp.generated.resources.portfolio_settings_delete_portfolio_title
import n3to.composeapp.generated.resources.portfolio_settings_edit_cd
import n3to.composeapp.generated.resources.portfolio_settings_manage
import n3to.composeapp.generated.resources.portfolio_settings_platform_empty
import n3to.composeapp.generated.resources.portfolio_settings_platform_section
import n3to.composeapp.generated.resources.portfolio_settings_portfolio_section
import n3to.composeapp.generated.resources.portfolio_settings_region_empty
import n3to.composeapp.generated.resources.portfolio_settings_region_section
import n3to.composeapp.generated.resources.portfolio_settings_reminder_desc
import n3to.composeapp.generated.resources.portfolio_settings_reminder_section
import n3to.composeapp.generated.resources.portfolio_settings_reminder_title
import n3to.composeapp.generated.resources.portfolio_settings_sector_empty
import n3to.composeapp.generated.resources.portfolio_settings_sector_section
import n3to.composeapp.generated.resources.portfolio_settings_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioSettingsScreen(
    onBack: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToPlatformDetail: (Platform) -> Unit,
    viewModel: PortfolioSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    PortfolioSettingsContent(
        state = state,
        onBack = onBack,
        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        onNavigateToPlatformDetail = onNavigateToPlatformDetail,
        onIntervalChange = { viewModel.setReminderInterval(it) },
        onOpenPlatformAdd = { viewModel.openAddPlatformSheet() },
        onOpenPlatformEdit = { viewModel.openEditPlatformSheet(it) },
        onOpenSectorSheet = { viewModel.openSectorSheet() },
        onOpenRegionSheet = { viewModel.openRegionSheet() },
        onEditPortfolio = { viewModel.openEditPortfolioSheet(it) },
        onDeletePortfolio = { viewModel.requestDeletePortfolio(it) },
        onAddPortfolio = { viewModel.openAddPortfolioSheet() }
    )

    // ── Platform sheets ──────────────────────────────────────────────────────
    if (state.showAddPlatformSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> viewModel.addPlatform(name, icon, notes) },
            onDismiss = { viewModel.closeAddPlatformSheet() }
        )
    }
    state.editingPlatform?.let { platform ->
        AddEditPlatformSheet(
            initial = platform,
            onSave = { name, icon, notes -> viewModel.renamePlatform(platform.id, name, icon, notes) },
            onDismiss = { viewModel.closeEditPlatformSheet() }
        )
    }
    state.platformError?.let { error ->
        val errorText = when (error) {
            is PlatformError.AlreadyExists -> stringResource(Res.string.error_platform_already_exists)
            is PlatformError.Unknown -> error.message ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { viewModel.clearPlatformError() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(errorText, fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPlatformError() }) {
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

    // ── Sector / Region sheets ────────────────────────────────────────────────
    if (state.showSectorSheet) {
        SectorManagementSheet(onDismiss = { viewModel.closeSectorSheet() })
    }
    if (state.showRegionSheet) {
        RegionManagementSheet(onDismiss = { viewModel.closeRegionSheet() })
    }

    // ── Portfolio sheets ─────────────────────────────────────────────────────
    if (state.showAddPortfolioSheet) {
        AddEditPortfolioBottomSheet(
            existing = null,
            onSave = { name, desc -> viewModel.addPortfolio(name, desc) },
            onDismiss = { viewModel.closeAddPortfolioSheet() }
        )
    }
    state.editingPortfolio?.let { portfolio ->
        AddEditPortfolioBottomSheet(
            existing = portfolio,
            onSave = { name, desc -> viewModel.updatePortfolioEntry(portfolio, name, desc) },
            onDismiss = { viewModel.closeEditPortfolioSheet() }
        )
    }
    state.deletingPortfolio?.let { portfolio ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeletePortfolio() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.portfolio_settings_delete_portfolio_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.portfolio_settings_delete_portfolio_message, portfolio.name),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePortfolioEntry(portfolio.id) }) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeletePortfolio() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (state.noAccountError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNoAccountError() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.portfolio_error_no_account_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.portfolio_error_no_account_message),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissNoAccountError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PortfolioSettingsContent(
    state: PortfolioSettingsUiState,
    onBack: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToPlatformDetail: (Platform) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onOpenPlatformAdd: () -> Unit,
    onOpenPlatformEdit: (Platform) -> Unit,
    onOpenSectorSheet: () -> Unit,
    onOpenRegionSheet: () -> Unit,
    modifier: Modifier = Modifier,
    onEditPortfolio: (Portfolio) -> Unit = {},
    onDeletePortfolio: (Portfolio) -> Unit = {},
    onAddPortfolio: () -> Unit = {}
) {
    val assetsByCategory = state.assets.groupBy { it.assetCategoryId }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.portfolio_settings_title),
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PortfolioListSection(portfolios = state.portfolios, onAdd = onAddPortfolio, onEdit = onEditPortfolio, onDelete = onDeletePortfolio) }

            item { Spacer(Modifier.height(8.dp)) }
            item { CategoryListSection(categories = state.categories, assetsByCategory = assetsByCategory, onCategoryClick = onNavigateToCategoryDetail) }

            item { Spacer(Modifier.height(8.dp)) }
            item { ReminderIntervalSection(currentInterval = state.priceReminderInterval, onIntervalChange = onIntervalChange) }

            item { Spacer(Modifier.height(8.dp)) }
            item { PlatformListSection(platforms = state.platforms, onAdd = onOpenPlatformAdd, onEdit = onOpenPlatformEdit) }

            item { Spacer(Modifier.height(8.dp)) }
            item { SectorListSection(sectors = state.allSectors, onManage = onOpenSectorSheet) }

            item { Spacer(Modifier.height(8.dp)) }
            item { RegionListSection(regions = state.allRegions, onManage = onOpenRegionSheet) }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Preview
@Composable
private fun PortfolioSettingsContentPreview() {
    N3toTheme {
        PortfolioSettingsContent(
            state = PortfolioSettingsUiState(
                categories = listOf(
                    AssetCategory(id = "cat1", name = "Acciones", icon = "📈", sortOrder = 0, createdAt = 0L),
                    AssetCategory(id = "cat2", name = "ETFs", icon = "📊", sortOrder = 1, createdAt = 0L)
                ),
                allSectors = listOf(
                    AssetSector(id = "s1", name = "Tecnología", icon = "💻", createdAt = 0L)
                ),
                allRegions = listOf(
                    AssetRegion(id = "r1", name = "EE.UU.", createdAt = 0L)
                ),
                platforms = listOf(
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L)
                ),
                priceReminderInterval = 7
            ),
            onBack = {},
            onNavigateToCategoryDetail = {},
            onNavigateToPlatformDetail = {},
            onIntervalChange = {},
            onOpenPlatformAdd = {},
            onOpenPlatformEdit = {},
            onOpenSectorSheet = {},
            onOpenRegionSheet = {}
        )
    }
}


