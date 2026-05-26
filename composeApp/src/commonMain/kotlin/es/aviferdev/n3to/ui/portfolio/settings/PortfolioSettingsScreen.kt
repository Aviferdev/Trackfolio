package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.FixedIncomePosition
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
import n3to.composeapp.generated.resources.portfolio_error_no_account_message
import n3to.composeapp.generated.resources.portfolio_error_no_account_title
import n3to.composeapp.generated.resources.portfolio_settings_archive_action
import n3to.composeapp.generated.resources.portfolio_settings_archive_confirm_message
import n3to.composeapp.generated.resources.portfolio_settings_archive_confirm_title
import n3to.composeapp.generated.resources.portfolio_settings_assets_available
import n3to.composeapp.generated.resources.portfolio_settings_blocked_assets_section
import n3to.composeapp.generated.resources.portfolio_settings_blocked_fixed_income_section
import n3to.composeapp.generated.resources.portfolio_settings_blocked_message
import n3to.composeapp.generated.resources.portfolio_settings_blocked_title
import n3to.composeapp.generated.resources.portfolio_settings_categories_count_many
import n3to.composeapp.generated.resources.portfolio_settings_categories_count_one
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
import n3to.composeapp.generated.resources.portfolio_settings_transfer_action
import n3to.composeapp.generated.resources.portfolio_settings_transfer_message
import n3to.composeapp.generated.resources.portfolio_settings_transfer_no_portfolios
import n3to.composeapp.generated.resources.portfolio_settings_transfer_title
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Public
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
        onDeletePortfolio = { viewModel.requestArchivePortfolio(it) },
        onAddPortfolio = { viewModel.openAddPortfolioSheet() }
    )

    if (state.showAddPortfolioSheet) {
        AddEditPortfolioBottomSheet(
            existing = null,
            onSave = { name -> viewModel.addPortfolio(name) },
            onDismiss = { viewModel.closeAddPortfolioSheet() }
        )
    }
    state.editingPortfolio?.let { portfolio ->
        AddEditPortfolioBottomSheet(
            existing = portfolio,
            onSave = { name -> viewModel.updatePortfolioEntry(portfolio, name) },
            onDismiss = { viewModel.closeEditPortfolioSheet() }
        )
    }

    // ── Confirmación de archivado (sin posiciones abiertas) ───────────────────
    state.confirmingArchivePortfolio?.let { portfolio ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelArchive() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.portfolio_settings_archive_confirm_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.portfolio_settings_archive_confirm_message, portfolio.name),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmArchivePortfolio(portfolio.id) }) {
                    Text(
                        stringResource(Res.string.portfolio_settings_archive_action),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelArchive() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Bloqueo: posiciones abiertas ──────────────────────────────────────────
    state.archiveBlockedState?.let { blocked ->
        ArchiveBlockedDialog(
            blocked = blocked,
            onTransferAsset = { asset -> viewModel.requestTransferAsset(asset, blocked.portfolio) },
            onDismiss = { viewModel.cancelArchive() }
        )
    }

    // ── Picker de cartera destino para traspaso ───────────────────────────────
    state.transferState?.let { transfer ->
        TransferPortfolioDialog(
            transfer = transfer,
            onConfirm = { targetId -> viewModel.confirmTransfer(transfer.asset.id, targetId) },
            onDismiss = { viewModel.cancelTransfer() }
        )
    }

    if (state.noAccountError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNoAccountError() },
            containerColor = MaterialTheme.appColors.navySurface,
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
private fun ArchiveBlockedDialog(
    blocked: PortfolioArchiveBlockState,
    onTransferAsset: (Asset) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface,
        title = {
            Text(
                stringResource(Res.string.portfolio_settings_blocked_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    stringResource(Res.string.portfolio_settings_blocked_message, blocked.portfolio.name),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(12.dp))

                if (blocked.openAssets.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.portfolio_settings_blocked_assets_section),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    blocked.openAssets.forEachIndexed { index, asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                asset.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.appColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onTransferAsset(asset) }) {
                                Text(
                                    stringResource(Res.string.portfolio_settings_transfer_action),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.appColors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (index < blocked.openAssets.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.appColors.navyBorder, thickness = 0.5.dp)
                        }
                    }
                }

                if (blocked.openFixedIncome.isNotEmpty()) {
                    if (blocked.openAssets.isNotEmpty()) Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(Res.string.portfolio_settings_blocked_fixed_income_section),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    blocked.openFixedIncome.forEachIndexed { index, fi ->
                        Text(
                            fi.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                        if (index < blocked.openFixedIncome.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.appColors.navyBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.primary
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TransferPortfolioDialog(
    transfer: PortfolioTransferState,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface,
        title = {
            Text(
                stringResource(Res.string.portfolio_settings_transfer_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    stringResource(Res.string.portfolio_settings_transfer_message, transfer.asset.name),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                if (transfer.availablePortfolios.isEmpty()) {
                    Text(
                        stringResource(Res.string.portfolio_settings_transfer_no_portfolios),
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                } else {
                    transfer.availablePortfolios.forEachIndexed { index, portfolio ->
                        Text(
                            portfolio.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirm(portfolio.id) }
                                .padding(vertical = 10.dp)
                        )
                        if (index < transfer.availablePortfolios.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.appColors.navyBorder,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.primary
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
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
