package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.UpdatePortfolioUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PortfolioSettingsScreen(
    onBack: () -> Unit,
    onNavigateToCategoryDetail: (String) -> Unit,
    onNavigateToPlatformDetail: (Platform) -> Unit,
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
    val portfolioVM = koinInject<PortfolioViewModel>()
    val updatePortfolio = koinInject<UpdatePortfolioUseCase>()
    val deletePortfolio = koinInject<DeletePortfolioUseCase>()

    val portCoroutine = rememberCoroutineScope()
    val portfolios by portfolioVM.portfolios.collectAsState()
    var editingPortfolio by remember { mutableStateOf<Portfolio?>(null) }
    var deletingPortfolio by remember { mutableStateOf<Portfolio?>(null) }

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
        portfolios = portfolios,
        onEditPortfolio = { editingPortfolio = it },
        onDeletePortfolio = { deletingPortfolio = it }
    )

    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial   = null,
            onSave    = { name, icon, notes -> platformViewModel.addPlatform(name, icon, notes) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }
    platformState.editing?.let { platform ->
        AddEditPlatformSheet(
            initial   = platform,
            onSave    = { name, icon, notes -> platformViewModel.renamePlatform(platform.id, name, icon, notes) },
            onDismiss = { platformViewModel.closeEditSheet() }
        )
    }
    platformState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { platformViewModel.clearError() },
            containerColor   = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text  = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { platformViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
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
            containerColor = SurfaceWhite,
            title = { Text("Eliminar cartera", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("¿Eliminar \"${deletingPortfolio!!.name}\"? Los activos pasarán a \"Sin cartera\".", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                    TextButton(onClick = {
                        portCoroutine.launch {
                            deletePortfolio(deletingPortfolio!!.id)
                        }
                        deletingPortfolio = null
                    }) { Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { deletingPortfolio = null }) { Text("Cancelar", color = PrimaryDark) }
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
    onDeletePortfolio: (Portfolio) -> Unit = {}
) {
    val categories = assetCatalogState.categories
    val assetsByCategory = remember(assetCatalogState.assets) {
        assetCatalogState.assets.groupBy { it.assetCategoryId }
    }

    Column(
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        TopBarApp(title = "Ajustes de Portfolio", navigateBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "CATEGORÍAS DE ACTIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
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
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$count ${if (count == 1) "activo" else "activos"}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text("›", fontSize = 18.sp, color = TextSecondary)
                        }
                        if (index < categories.lastIndex) {
                            HorizontalDivider(
                                color = BorderGray,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 52.dp)
                            )
                        }
                    }
                }
            }

            // ── Carteras ──────────────────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text(
                    "CARTERAS",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextSecondary
                )
            }
            item {
                SettingsGroupCard {
                    if (portfolios.isEmpty()) {
                        Text(
                            "No hay carteras. Crea una desde la pantalla de Portfolio.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        portfolios.forEachIndexed { index, portfolio ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(portfolio.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    if (portfolio.description != null) {
                                        Text(portfolio.description, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onEditPortfolio(portfolio) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeletePortfolio(portfolio) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (index < portfolios.lastIndex) {
                                HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                            }
                        }
                    }
                }
            }

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
                                    onClick = { onIntervalChange(days) },
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

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label       = "PLATAFORMAS",
                    actionLabel = "Añadir",
                    onAction    = onOpenPlatformAdd
                )
            }
            item {
                SettingsGroupCard {
                    if (platformState.platforms.isEmpty()) {
                        Text(
                            "Sin plataformas. Añade brokers, exchanges o bancos para asociarlos a tus movimientos.",
                            fontSize    = 13.sp,
                            color       = TextSecondary,
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
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
                                Text(platform.icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text     = platform.name,
                                        fontSize = 15.sp,
                                        color    = TextPrimary
                                    )
                                    if (!platform.notes.isNullOrBlank()) {
                                        Text(
                                            text     = platform.notes,
                                            fontSize = 11.sp,
                                            color    = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Text("›", fontSize = 18.sp, color = TextSecondary)
                            }
                            if (index < platformState.platforms.lastIndex) {
                                HorizontalDivider(
                                    color     = BorderGray,
                                    thickness = 0.5.dp,
                                    modifier  = Modifier.padding(start = 52.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label       = "SECTORES",
                    actionLabel = "Gestionar",
                    onAction    = onOpenSectorSheet
                )
            }
            item {
                SettingsGroupCard {
                    val sectors = assetCatalogState.allSectors
                    if (sectors.isEmpty()) {
                        Text(
                            "Clasifica tus activos por sectores (Tecnologia, Salud, Energia...) para analizar tu exposicion por industria.",
                            fontSize    = 13.sp,
                            color       = TextSecondary,
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
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
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < sectors.lastIndex) {
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

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SectionHeader(
                    label       = "REGIONES",
                    actionLabel = "Gestionar",
                    onAction    = onOpenRegionSheet
                )
            }
            item {
                SettingsGroupCard {
                    val regions = assetCatalogState.allRegions
                    if (regions.isEmpty()) {
                        Text(
                            "Define la distribucion geografica de tus activos por region (EE.UU., Europa, Asia...) para analizar tu exposicion internacional.",
                            fontSize    = 13.sp,
                            color       = TextSecondary,
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
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
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < regions.lastIndex) {
                                HorizontalDivider(
                                    color = BorderGray,
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
                    AssetCategory(id = "cat1", name = "Acciones", icon = "📈", sortOrder = 0, createdAt = 0L),
                    AssetCategory(id = "cat2", name = "ETFs", icon = "📊", sortOrder = 1, createdAt = 0L)
                ),
                assets = listOf(
                    Asset(id = "a1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0)
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
                    Platform(id = "p1", name = "Interactive Brokers", icon = "🏦", sortOrder = 0, createdAt = 0L)
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
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) { Column(content = content) }
}
