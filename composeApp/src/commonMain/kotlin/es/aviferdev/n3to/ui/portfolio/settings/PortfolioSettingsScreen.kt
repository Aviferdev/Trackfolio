package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.AddEditPortfolioBottomSheet
import es.aviferdev.n3to.ui.portfolio.settings.components.PortfolioListSection
import es.aviferdev.n3to.ui.portfolio.settings.components.PortfolioSettingsNavigableRow
import es.aviferdev.n3to.ui.portfolio.settings.components.ReminderIntervalSection
import es.aviferdev.n3to.ui.portfolio.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.portfolio_error_no_account_message
import n3to.composeapp.generated.resources.portfolio_error_no_account_title
import n3to.composeapp.generated.resources.portfolio_settings_assets_available
import n3to.composeapp.generated.resources.portfolio_settings_categories_count_many
import n3to.composeapp.generated.resources.portfolio_settings_categories_count_one
import n3to.composeapp.generated.resources.portfolio_settings_delete_portfolio_message
import n3to.composeapp.generated.resources.portfolio_settings_delete_portfolio_title
import n3to.composeapp.generated.resources.portfolio_settings_management_section
import n3to.composeapp.generated.resources.portfolio_settings_platforms_count_many
import n3to.composeapp.generated.resources.portfolio_settings_platforms_count_one
import n3to.composeapp.generated.resources.portfolio_settings_platforms_row
import n3to.composeapp.generated.resources.portfolio_settings_regions_count_many
import n3to.composeapp.generated.resources.portfolio_settings_regions_count_one
import n3to.composeapp.generated.resources.portfolio_settings_regions_row
import n3to.composeapp.generated.resources.portfolio_settings_sectors_count_many
import n3to.composeapp.generated.resources.portfolio_settings_sectors_count_one
import n3to.composeapp.generated.resources.portfolio_settings_sectors_row
import n3to.composeapp.generated.resources.portfolio_settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioSettingsScreen(
    onBack: () -> Unit,
    onNavigateToAssetTypes: () -> Unit,
    onNavigateToPlatforms: () -> Unit,
    onNavigateToSectors: () -> Unit,
    onNavigateToRegions: () -> Unit,
    viewModel: PortfolioSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    PortfolioSettingsContent(
        state = state,
        onBack = onBack,
        onNavigateToAssetTypes = onNavigateToAssetTypes,
        onNavigateToPlatforms = onNavigateToPlatforms,
        onNavigateToSectors = onNavigateToSectors,
        onNavigateToRegions = onNavigateToRegions,
        onIntervalChange = { viewModel.setReminderInterval(it) },
        onEditPortfolio = { viewModel.openEditPortfolioSheet(it) },
        onDeletePortfolio = { viewModel.requestDeletePortfolio(it) },
        onAddPortfolio = { viewModel.openAddPortfolioSheet() }
    )

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
    onNavigateToAssetTypes: () -> Unit,
    onNavigateToPlatforms: () -> Unit,
    onNavigateToSectors: () -> Unit,
    onNavigateToRegions: () -> Unit,
    onIntervalChange: (Int) -> Unit,
    onEditPortfolio: (Portfolio) -> Unit = {},
    onDeletePortfolio: (Portfolio) -> Unit = {},
    onAddPortfolio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.portfolio_settings_title),
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Carteras creadas ──────────────────────────────────────────────
            item {
                PortfolioListSection(
                    portfolios = state.portfolios,
                    onAdd = onAddPortfolio,
                    onEdit = onEditPortfolio,
                    onDelete = onDeletePortfolio
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Gestión ───────────────────────────────────────────────────────
            item {
                SectionHeader(label = stringResource(Res.string.portfolio_settings_management_section))
                SettingsGroupCard {
                    val categoriesSubtitle = if (state.categoriesCount == 1)
                        stringResource(Res.string.portfolio_settings_categories_count_one, state.categoriesCount)
                    else
                        stringResource(Res.string.portfolio_settings_categories_count_many, state.categoriesCount)

                    val platformsSubtitle = if (state.platformsCount == 1)
                        stringResource(Res.string.portfolio_settings_platforms_count_one, state.platformsCount)
                    else
                        stringResource(Res.string.portfolio_settings_platforms_count_many, state.platformsCount)

                    val sectorsSubtitle = if (state.sectorsCount == 1)
                        stringResource(Res.string.portfolio_settings_sectors_count_one, state.sectorsCount)
                    else
                        stringResource(Res.string.portfolio_settings_sectors_count_many, state.sectorsCount)

                    val regionsSubtitle = if (state.regionsCount == 1)
                        stringResource(Res.string.portfolio_settings_regions_count_one, state.regionsCount)
                    else
                        stringResource(Res.string.portfolio_settings_regions_count_many, state.regionsCount)

                    PortfolioSettingsNavigableRow(
                        icon = Icons.Outlined.Category,
                        label = stringResource(Res.string.portfolio_settings_assets_available),
                        subtitle = categoriesSubtitle,
                        onClick = onNavigateToAssetTypes,
                        showDivider = true
                    )
                    PortfolioSettingsNavigableRow(
                        icon = Icons.Outlined.AccountBalance,
                        label = stringResource(Res.string.portfolio_settings_platforms_row),
                        subtitle = platformsSubtitle,
                        onClick = onNavigateToPlatforms,
                        showDivider = true
                    )
                    PortfolioSettingsNavigableRow(
                        icon = Icons.Outlined.Business,
                        label = stringResource(Res.string.portfolio_settings_sectors_row),
                        subtitle = sectorsSubtitle,
                        onClick = onNavigateToSectors,
                        showDivider = true
                    )
                    PortfolioSettingsNavigableRow(
                        icon = Icons.Outlined.Public,
                        label = stringResource(Res.string.portfolio_settings_regions_row),
                        subtitle = regionsSubtitle,
                        onClick = onNavigateToRegions,
                        showDivider = false
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Recordatorios ─────────────────────────────────────────────────
            item {
                ReminderIntervalSection(
                    currentInterval = state.priceReminderInterval,
                    onIntervalChange = onIntervalChange
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
