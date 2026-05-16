package es.aviferdev.n3to.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.repository.PlatformCategoryRepository
import es.aviferdev.n3to.domain.repository.PlatformRepository
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AssetCategoryDetailUiState(
    val category: AssetCategory?       = null,
    val activeAssets: List<Asset>       = emptyList(),
    val archivedAssets: List<Asset>     = emptyList(),
    val activeFixedIncome: List<FixedIncomeRow> = emptyList(),
    val categoryPlatforms: List<Platform> = emptyList(),
    val allPlatforms: List<Platform>    = emptyList(),
    val allCategories: List<AssetCategory> = emptyList(),
    val showAddSheet: Boolean          = false,
    val editing: Asset?                = null,
    val editingPlatformIds: Set<String> = emptySet(),
    val editingSectorIds: Set<String>   = emptySet(),
    val editingRegionPercents: Map<String, Int> = emptyMap(),
    val editingFixedIncomePercent: Int = 0,
    val pendingArchive: Asset?         = null,
    val showLinkPlatformSheet: Boolean = false,
    val error: String?                 = null,
    val allSectors: List<es.aviferdev.n3to.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.n3to.domain.model.AssetRegion> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetCategoryDetailViewModel(
    private val categoryId: String,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val assetMetadataRepository: AssetMetadataRepository,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val platformCategoryRepository: PlatformCategoryRepository,
    private val platformRepository: PlatformRepository,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val unarchiveAsset: UnarchiveAssetUseCase,
    private val fixedIncomeRepository: FixedIncomeRepository,
    private val fixedIncomeEventRepository: FixedIncomeEventRepository,
    private val session: AccountSession
) : ViewModel() {

    private val _showAddSheet      = MutableStateFlow(false)
    private val _editing           = MutableStateFlow<Asset?>(null)
    private val _editingPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    private val _pendingArchive    = MutableStateFlow<Asset?>(null)
    private val _error             = MutableStateFlow<String?>(null)
    private val _showLinkPlatformSheet = MutableStateFlow(false)
    private val _editingSectorIds   = MutableStateFlow<Set<String>>(emptySet())
    private val _editingRegionPercents = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _editingFixedIncomePercent = MutableStateFlow(0)

    private val allSectors: StateFlow<List<es.aviferdev.n3to.domain.model.AssetSector>> =
        assetMetadataRepository.getAllSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<es.aviferdev.n3to.domain.model.AssetRegion>> =
        assetMetadataRepository.getAllRegions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState: StateFlow<AssetCategoryDetailUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(AssetCategoryDetailUiState())
            else {
                val fiPositionsFlow = fixedIncomeRepository.getByAccountAndCategory(accountId, categoryId)
                val fiEventsFlow = fixedIncomeEventRepository.getByAccount(accountId)
                val fiRowsFlow = combine(fiPositionsFlow, fiEventsFlow) { fiPositions, fiEvents ->
                    val eventsByPosition = fiEvents.groupBy { ev -> ev.positionId }
                    fiPositions.map { pos ->
                        val events = eventsByPosition[pos.id] ?: emptyList()
                        FixedIncomeCalculator.calculatePosition(pos, events)
                    }
                }

                val assetsAndCategoriesFlow = combine(
                    assetRepository.getAllByAccountIncludingArchived(accountId),
                    getAssetCategoriesIncludingArchived()
                ) { assets, categories -> assets to categories }

                val platformsFlow = combine(
                    getPlatforms(),
                    platformCategoryRepository.getByCategory(categoryId)
                ) { allPlats, catPlats -> allPlats to catPlats }

                val sheetsFlowPart1 = combine(
                    _showAddSheet,
                    _editing,
                    _editingPlatformIds
                ) { show, edit, platIds -> SheetPart1(show, edit, platIds) }

                val sheetsFlowPart2 = combine(
                    _editingSectorIds,
                    _editingRegionPercents,
                    _editingFixedIncomePercent,
                    _pendingArchive,
                    _error
                ) { sectIds, regPerc, fixedIncPct, arch, err -> SheetPart2(sectIds, regPerc, fixedIncPct, arch, err) }

                val sheetsFlow = combine(sheetsFlowPart1, sheetsFlowPart2) { part1, part2 ->
                    SheetState(part1.show, part1.edit, part1.platIds, part2.sectIds, part2.regPerc, part2.fixedIncPct, part2.arch, part2.err)
                }.combine(_showLinkPlatformSheet) { sheets, linkSheet -> sheets to linkSheet }

                val sectorsRegionsFlow = combine(allSectors, allRegions) { sectors, regions -> sectors to regions }

                combine(assetsAndCategoriesFlow, platformsFlow, fiRowsFlow, sheetsFlow, sectorsRegionsFlow) { data, platforms, fiRows, sheetsAndLink, sectorsRegions ->
                    val (allAssets, categories) = data
                    val (allPlatforms, categoryPlatforms) = platforms
                    val (sheets, linkSheet) = sheetsAndLink
                    val category = categories.firstOrNull { c -> c.id == categoryId }
                    val assetsInCategory = allAssets.filter { a -> a.assetCategoryId == categoryId }
                    AssetCategoryDetailUiState(
                        category            = category,
                        activeAssets         = assetsInCategory.filter { a -> !a.archived },
                        archivedAssets       = assetsInCategory.filter { a -> a.archived },
                        activeFixedIncome   = fiRows,
                        categoryPlatforms   = categoryPlatforms,
                        allPlatforms         = allPlatforms,
                        allCategories        = categories.filter { c -> !c.archived },
                        showAddSheet         = sheets.show,
                        editing              = sheets.edit,
                        editingPlatformIds   = sheets.platIds,
                        editingSectorIds     = sheets.sectIds,
                        editingRegionPercents = sheets.regPerc,
                        editingFixedIncomePercent = sheets.fixedIncPct,
                        pendingArchive       = sheets.arch,
                        showLinkPlatformSheet = linkSheet,
                        error                = sheets.err,
                        allSectors           = sectorsRegions.first,
                        allRegions           = sectorsRegions.second
                    )
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetCategoryDetailUiState()
        )

    fun openAddSheet()  { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }

    fun openEditSheet(asset: Asset) {
        _editing.value = asset
        viewModelScope.launch {
            val platforms = assetPlatformRepository.getPlatformsByAsset(asset.id).first()
            _editingPlatformIds.value = platforms.map { it.id }.toSet()

            val sectors = assetMetadataRepository.getSectorsByAssetId(asset.id).first()
            _editingSectorIds.value = sectors.map { it.id }.toSet()

            val regions = assetMetadataRepository.getRegionDistributionsByAssetId(asset.id).first()
            _editingRegionPercents.value = regions.associate { it.regionId to it.percent }

            val composition = assetMetadataRepository.getCompositionByAssetId(asset.id).first()
            _editingFixedIncomePercent.value = composition?.fixedIncomePercent ?: 0
        }
    }
    fun closeEditSheet() {
        _editing.value = null
        _editingPlatformIds.value = emptySet()
        _editingSectorIds.value = emptySet()
        _editingRegionPercents.value = emptyMap()
        _editingFixedIncomePercent.value = 0
    }

    fun requestArchive(asset: Asset) {
        viewModelScope.launch {
            val txs = assetTransactionRepository.getByAsset(asset.id).first()
            val position = PortfolioCalculator.calculate(txs, asset.currentPrice)
            if (position.netQuantity > 0.0) {
                _error.value = "No se puede archivar «${asset.ticker}» porque tiene posiciones abiertas. Cierra o traspasa las posiciones primero."
            } else {
                _pendingArchive.value = asset
            }
        }
    }
    fun cancelArchive() { _pendingArchive.value = null }

    fun confirmArchive() {
        val asset = _pendingArchive.value ?: return
        viewModelScope.launch {
            archiveAsset(asset.id).onFailure { _error.value = it.message }
            _pendingArchive.value = null
        }
    }

    fun restoreAsset(assetId: String) {
        viewModelScope.launch {
            unarchiveAsset(assetId).onFailure { _error.value = it.message }
        }
    }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet(),
        maturityDate: Long? = null,
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap()
    ) {
        val accountId = session.selectedAccountId.value ?: run {
            _error.value = "Selecciona primero una cuenta"
            return
        }
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim   = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = "Ticker y nombre son obligatorios"
            return
        }
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val asset = Asset(
                id              = "asset_${now}_${(0..9999).random()}",
                accountId       = accountId,
                ticker          = tickerTrim,
                name            = nameTrim,
                notes           = notes?.ifBlank { null },
                createdAt       = now,
                assetCategoryId = categoryId,
                currentPrice    = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null,
                maturityDate    = maturityDate
            )
            saveAsset(asset)
                .onSuccess {
                    if (fixedIncomePercent > 0) {
                        assetMetadataRepository.saveComposition(
                            es.aviferdev.n3to.domain.model.AssetComposition(
                                assetId = asset.id,
                                fixedIncomePercent = fixedIncomePercent,
                                createdAt = now
                            )
                        )
                    }
                    sectorIds.forEach { sectorId ->
                        assetMetadataRepository.saveSectorRelation(
                            es.aviferdev.n3to.domain.model.AssetSectorRelation(
                                assetId = asset.id,
                                sectorId = sectorId
                            )
                        )
                    }
                    regionPercents.forEach { (regionId, percent) ->
                        if (percent > 0) {
                            assetMetadataRepository.saveRegionDistribution(
                                es.aviferdev.n3to.domain.model.AssetRegionDistribution(
                                    assetId = asset.id,
                                    regionId = regionId,
                                    percent = percent
                                )
                            )
                        }
                    }
                    platformIds.forEach { platId ->
                        assetPlatformRepository.link(asset.id, platId)
                    }
                }
                .onFailure { _error.value = it.message }
            _showAddSheet.value = false
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet(),
        maturityDate: Long? = null,
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap()
    ) {
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim   = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _error.value = "Ticker y nombre son obligatorios"
            return
        }
        viewModelScope.launch {
            val updatedAt = when {
                currentPrice == null                  -> null
                currentPrice == original.currentPrice -> original.currentPriceUpdatedAt
                else -> Clock.System.now().toEpochMilliseconds()
            }
            updateAsset(
                original.copy(
                    ticker                = tickerTrim,
                    name                  = nameTrim,
                    notes                 = notes?.ifBlank { null },
                    assetCategoryId       = assetCategoryId,
                    currentPrice          = currentPrice,
                    currentPriceUpdatedAt = updatedAt,
                    maturityDate          = maturityDate
                )
            ).onSuccess {
                if (fixedIncomePercent > 0) {
                    assetMetadataRepository.saveComposition(
                        es.aviferdev.n3to.domain.model.AssetComposition(
                            assetId = original.id,
                            fixedIncomePercent = fixedIncomePercent,
                            createdAt = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                } else {
                    assetMetadataRepository.deleteComposition(original.id)
                }
                assetMetadataRepository.deleteAllSectorLinks(original.id)
                sectorIds.forEach { sectorId ->
                    assetMetadataRepository.saveSectorRelation(
                        es.aviferdev.n3to.domain.model.AssetSectorRelation(
                            assetId = original.id,
                            sectorId = sectorId
                        )
                    )
                }
                assetMetadataRepository.deleteAllRegionDistributions(original.id)
                regionPercents.forEach { (regionId, percent) ->
                    if (percent > 0) {
                        assetMetadataRepository.saveRegionDistribution(
                            es.aviferdev.n3to.domain.model.AssetRegionDistribution(
                                assetId = original.id,
                                regionId = regionId,
                                percent = percent
                            )
                        )
                    }
                }
                assetPlatformRepository.unlinkAllByAsset(original.id)
                platformIds.forEach { platId ->
                    assetPlatformRepository.link(original.id, platId)
                }
            }.onFailure { _error.value = it.message }
            _editing.value = null
            _editingPlatformIds.value = emptySet()
        }
    }

    fun clearError() { _error.value = null }

    // ── Plataformas de la categoría ───────────────────────────────────
    fun openLinkPlatformSheet()  { _showLinkPlatformSheet.value = true }
    fun closeLinkPlatformSheet() { _showLinkPlatformSheet.value = false }

    /** Vincula una plataforma existente a esta categoría. */
    fun linkPlatform(platformId: String) {
        viewModelScope.launch {
            platformCategoryRepository.link(platformId, categoryId)
                .onFailure { _error.value = it.message }
        }
    }

    /** Desvincula una plataforma de esta categoría. */
    fun unlinkPlatform(platformId: String) {
        viewModelScope.launch {
            platformCategoryRepository.unlink(platformId, categoryId)
                .onFailure { _error.value = it.message }
        }
    }

    /** Crea una plataforma nueva y la vincula a esta categoría automáticamente. */
    fun createAndLinkPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.allPlatforms.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = "Ya existe una plataforma con ese nombre"
            return
        }
        val validatedNotes = notes?.take(200)?.ifBlank { null }
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val nextOrder = (uiState.value.allPlatforms.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val platform = Platform(
                id        = "platform_$now",
                name      = trimmed,
                icon      = icon.ifBlank { "🏦" },
                sortOrder = nextOrder,
                createdAt = now,
                notes     = validatedNotes
            )
            platformRepository.save(platform)
                .onSuccess {
                    platformCategoryRepository.link(platform.id, categoryId)
                        .onFailure { _error.value = it.message }
                    _showLinkPlatformSheet.value = false
                }
                .onFailure { _error.value = it.message }
        }
    }

    private data class SheetPart1(
        val show: Boolean,
        val edit: Asset?,
        val platIds: Set<String>
    )

    private data class SheetPart2(
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?,
        val err: String?
    )

    private data class SheetState(
        val show: Boolean,
        val edit: Asset?,
        val platIds: Set<String>,
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?,
        val err: String?
    )
}
