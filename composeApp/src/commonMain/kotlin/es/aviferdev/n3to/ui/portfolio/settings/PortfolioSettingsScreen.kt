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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.UpdatePortfolioUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.AddEditPlatformSheet
import es.aviferdev.n3to.ui.portfolio.AddEditPortfolioBottomSheet
import es.aviferdev.n3to.ui.portfolio.AssetCatalogUiState
import es.aviferdev.n3to.ui.portfolio.AssetCatalogViewModel
import es.aviferdev.n3to.ui.portfolio.PlatformError
import es.aviferdev.n3to.ui.portfolio.PlatformListUiState
import es.aviferdev.n3to.ui.portfolio.PlatformViewModel
import es.aviferdev.n3to.ui.portfolio.RegionManagementSheet
import es.aviferdev.n3to.ui.portfolio.SectorManagementSheet
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.launch
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioSettingsScreen(
    onBack: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToPlatformDetail: (Platform) -> Unit,
    portfolioSettingsViewModel: PortfolioSettingsViewModel = koinViewModel(),
    assetCatalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel()
) {
    val assetCatalogState by assetCatalogViewModel.uiState.collectAsState()
    val platformState by platformViewModel.uiState.collectAsState()

    var showSectorSheet by remember { mutableStateOf(false) }
    var showRegionSheet by remember { mutableStateOf(false) }

    val reminderIntervalUseCase = koinInject<GetPriceReminderIntervalUseCase>()
    var selectedInterval by remember { mutableIntStateOf(reminderIntervalUseCase.get()) }

    // ── Portfolio management ────────────────────────────────────────────────
    val session = koinInject<AccountSession>()
    val savePortfolio = koinInject<SavePortfolioUseCase>()
    val updatePortfolio = koinInject<UpdatePortfolioUseCase>()
    val deletePortfolio = koinInject<DeletePortfolioUseCase>()

    val portCoroutine = rememberCoroutineScope()
    val portfolios by portfolioSettingsViewModel.uiState.collectAsState()
    var editingPortfolio by remember { mutableStateOf<Portfolio?>(null) }
    var deletingPortfolio by remember { mutableStateOf<Portfolio?>(null) }
    var showAddPortfolioSheet by remember { mutableStateOf(false) }
    var noAccountError by remember { mutableStateOf(false) }

    val portfoliosList =
        (portfolios as? PortfolioSettingsUiState.Success)?.data?.portfolios ?: emptyList()

    PortfolioSettingsContent(
        assetCatalogState = assetCatalogState,
        platformState = platformState,
        onBack = onBack,
        onNavigateToCategoryDetail = onNavigateToCategoryDetail,
        onNavigateToPlatformDetail = onNavigateToPlatformDetail,
        selectedInterval = selectedInterval,
        onIntervalChange = { days ->
            selectedInterval = days
            reminderIntervalUseCase.set(days)
        },
        onOpenPlatformAdd = { platformViewModel.openAddSheet() },
        onOpenPlatformEdit = { platform -> platformViewModel.openEditSheet(platform) },
        onOpenSectorSheet = { showSectorSheet = true },
        onOpenRegionSheet = { showRegionSheet = true },
        portfolios = portfoliosList,
        onEditPortfolio = { editingPortfolio = it },
        onDeletePortfolio = { deletingPortfolio = it },
        onAddPortfolio = { showAddPortfolioSheet = true }
    )

    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> platformViewModel.addPlatform(name, icon, notes) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }
    platformState.editing?.let { platform ->
        AddEditPlatformSheet(
            initial = platform,
            onSave = { name, icon, notes ->
                platformViewModel.renamePlatform(
                    platform.id,
                    name,
                    icon,
                    notes
                )
            },
            onDismiss = { platformViewModel.closeEditSheet() }
        )
    }
    platformState.error?.let {
        val errorText = when (it) {
            is PlatformError.AlreadyExists -> stringResource(Res.string.error_platform_already_exists)
            is PlatformError.Unknown -> it.message ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { platformViewModel.clearError() },
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
                Text(
                    errorText,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { platformViewModel.clearError() }) {
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

    if (showSectorSheet) {
        SectorManagementSheet(
            onDismiss = { showSectorSheet = false }
        )
    }
    if (showRegionSheet) {
        RegionManagementSheet(
            onDismiss = { showRegionSheet = false }
        )
    }

    // ── Portfolio sheets ─────────────────────────────────────────────────────
    if (showAddPortfolioSheet) {
        AddEditPortfolioBottomSheet(
            existing = null,
            onSave = { name, desc ->
                val accountId = session.selectedAccountId.value
                if (accountId == null) {
                    showAddPortfolioSheet = false
                    noAccountError = true
                } else {
                    portCoroutine.launch {
                        savePortfolio(accountId, name, desc)
                        showAddPortfolioSheet = false
                    }
                }
            },
            onDismiss = { showAddPortfolioSheet = false }
        )
    }
    if (editingPortfolio != null) {
        AddEditPortfolioBottomSheet(
            existing = editingPortfolio,
            onSave = { name, desc ->
                portCoroutine.launch {
                    updatePortfolio(editingPortfolio!!.copy(name = name, description = desc))
                }
                editingPortfolio = null
            },
            onDismiss = { editingPortfolio = null }
        )
    }
    if (deletingPortfolio != null) {
        AlertDialog(
            onDismissRequest = { deletingPortfolio = null },
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
                    stringResource(
                        Res.string.portfolio_settings_delete_portfolio_message,
                        deletingPortfolio!!.name
                    ), fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    portCoroutine.launch {
                        deletePortfolio(deletingPortfolio!!.id)
                    }
                    deletingPortfolio = null
                }) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deletingPortfolio = null
                }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (noAccountError) {
        AlertDialog(
            onDismissRequest = { noAccountError = false },
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
                TextButton(onClick = { noAccountError = false }) {
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
    assetCatalogState: AssetCatalogUiState,
    platformState: PlatformListUiState,
    onBack: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToPlatformDetail: (Platform) -> Unit,
    selectedInterval: Int,
    onIntervalChange: (Int) -> Unit,
    onOpenPlatformAdd: () -> Unit,
    onOpenPlatformEdit: (Platform) -> Unit,
    onOpenSectorSheet: () -> Unit,
    onOpenRegionSheet: () -> Unit,
    modifier: Modifier = Modifier,
    portfolios: List<Portfolio> = emptyList(),
    onEditPortfolio: (Portfolio) -> Unit = {},
    onDeletePortfolio: (Portfolio) -> Unit = {},
    onAddPortfolio: () -> Unit = {}
) {
    val categories = assetCatalogState.categories
    val assetsByCategory = remember(assetCatalogState.assets) {
        assetCatalogState.assets.groupBy { it.assetCategoryId }
    }

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
            // ── Carteras ──────────────────────────────────────────────────────
            item {
                SectionHeader(
                    label = stringResource(Res.string.portfolio_settings_portfolio_section),
                    actionLabel = stringResource(Res.string.portfolio_settings_add),
                    onAction = onAddPortfolio
                )
            }
            item {
                SettingsGroupCard {
                    if (portfolios.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onAddPortfolio() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(Res.string.portfolio_settings_add),
                                tint = MaterialTheme.appColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(Res.string.portfolio_settings_add),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.appColors.primary
                            )
                        }
                    } else {
                        portfolios.forEachIndexed { index, portfolio ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        portfolio.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                    if (portfolio.description != null) {
                                        Text(
                                            portfolio.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.appColors.textSecondary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onEditPortfolio(portfolio) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.portfolio_settings_edit_cd),
                                        tint = MaterialTheme.appColors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeletePortfolio(portfolio) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.portfolio_settings_delete_cd),
                                        tint = MaterialTheme.appColors.expense,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (index < portfolios.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Categorías de activo ──────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text(
                    stringResource(Res.string.portfolio_settings_category_section),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
            item {
                SettingsGroupCard {
                    categories.forEachIndexed { index, category ->
                        val count = assetsByCategory[category.id]?.size ?: 0
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onNavigateToCategoryDetail(category.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                                Text(
                                    text = if (count == 1) stringResource(
                                        Res.string.portfolio_settings_category_count_one,
                                        count
                                    )
                                    else stringResource(
                                        Res.string.portfolio_settings_category_count_many,
                                        count
                                    ),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.appColors.textSecondary
                                )
                            }
                            Text(
                                "›",
                                fontSize = 18.sp,
                                color = MaterialTheme.appColors.textSecondary
                            )
                        }
                        if (index < categories.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.appColors.border,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text(
                    stringResource(Res.string.portfolio_settings_reminder_section),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary
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
                            stringResource(Res.string.portfolio_settings_reminder_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.portfolio_settings_reminder_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.appColors.textSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(7, 14, 30).forEach { days ->
                                val isSelected = selectedInterval == days
                                OutlinedButton(
                                    onClick = { onIntervalChange(days) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.appColors.primary else Color.Transparent,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.appColors.textPrimary
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${days}d",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label = stringResource(Res.string.portfolio_settings_platform_section),
                    actionLabel = stringResource(Res.string.portfolio_settings_add),
                    onAction = onOpenPlatformAdd
                )
            }
            item {
                SettingsGroupCard {
                    if (platformState.platforms.isEmpty()) {
                        Text(
                            stringResource(Res.string.portfolio_settings_platform_empty),
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                        )
                    } else {
                        platformState.platforms.forEachIndexed { index, platform ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenPlatformEdit(platform) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    platform.icon,
                                    fontSize = 18.sp,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = platform.name,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                    if (!platform.notes.isNullOrBlank()) {
                                        Text(
                                            text = platform.notes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.appColors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Text(
                                    "›",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.appColors.textSecondary
                                )
                            }
                            if (index < platformState.platforms.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label = stringResource(Res.string.portfolio_settings_sector_section),
                    actionLabel = stringResource(Res.string.portfolio_settings_manage),
                    onAction = onOpenSectorSheet
                )
            }
            item {
                SettingsGroupCard {
                    val sectors = assetCatalogState.allSectors
                    if (sectors.isEmpty()) {
                        Text(
                            stringResource(Res.string.portfolio_settings_sector_empty),
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                        )
                    } else {
                        sectors.forEachIndexed { index, sector ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sector.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = sector.name,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.appColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < sectors.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label = stringResource(Res.string.portfolio_settings_region_section),
                    actionLabel = stringResource(Res.string.portfolio_settings_manage),
                    onAction = onOpenRegionSheet
                )
            }
            item {
                SettingsGroupCard {
                    val regions = assetCatalogState.allRegions
                    if (regions.isEmpty()) {
                        Text(
                            stringResource(Res.string.portfolio_settings_region_empty),
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                        )
                    } else {
                        regions.forEachIndexed { index, region ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = region.name,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.appColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < regions.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 16.dp)
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

@Preview
@Composable
private fun PortfolioSettingsContentPreview() {
    N3toTheme {
        PortfolioSettingsContent(
            assetCatalogState = AssetCatalogUiState(
                categories = listOf(
                    AssetCategory(
                        id = "cat1",
                        name = "Acciones",
                        icon = "📈",
                        sortOrder = 0,
                        createdAt = 0L
                    ),
                    AssetCategory(
                        id = "cat2",
                        name = "ETFs",
                        icon = "📊",
                        sortOrder = 1,
                        createdAt = 0L
                    )
                ),
                assets = listOf(
                    Asset(
                        id = "a1",
                        accountId = "acc1",
                        ticker = "AAPL",
                        name = "Apple Inc.",
                        notes = null,
                        createdAt = 0L,
                        assetCategoryId = "cat1",
                        currentPrice = 150.0
                    )
                ),
                allSectors = listOf(
                    AssetSector(id = "s1", name = "Tecnología", icon = "💻", createdAt = 0L)
                ),
                allRegions = listOf(
                    AssetRegion(id = "r1", name = "EE.UU.", createdAt = 0L)
                )
            ),
            platformState = PlatformListUiState(
                platforms = listOf(
                    Platform(
                        id = "p1",
                        name = "Interactive Brokers",
                        icon = "🏦",
                        sortOrder = 0,
                        createdAt = 0L
                    )
                )
            ),
            onBack = {},
            onNavigateToCategoryDetail = {},
            onNavigateToPlatformDetail = {},
            selectedInterval = 7,
            onIntervalChange = {},
            onOpenPlatformAdd = {},
            onOpenPlatformEdit = {},
            onOpenSectorSheet = {},
            onOpenRegionSheet = {}
        )
    }
}

// ── Componentes locales ──────────────────────────────────────────────────────
// SectionHeader reemplazado por es.aviferdev.n3to.ui.common.SectionHeader

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.border),
        elevation = CardDefaults.cardElevation(0.dp)
    ) { Column(content = content) }
}
