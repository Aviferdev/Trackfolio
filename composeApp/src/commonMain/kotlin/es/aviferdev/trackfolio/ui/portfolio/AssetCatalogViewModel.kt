package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.repository.AssetMetadataRepository
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AssetCatalogUiState(
    val assets: List<Asset>             = emptyList(),
    val categories: List<AssetCategory> = emptyList(),
    val showAddSheet: Boolean           = false,
    val addForCategoryId: String?       = null,
    val editing: Asset?                 = null,
    val editingPlatformIds: Set<String> = emptySet(),
    val editingSectorIds: Set<String>   = emptySet(),
    val editingRegionPercents: Map<String, Int> = emptyMap(),
    val editingFixedIncomePercent: Int = 0,
    val pendingArchive: Asset?          = null,
    val error: String?                  = null,
    val allSectors: List<es.aviferdev.trackfolio.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.trackfolio.domain.model.AssetRegion> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetCatalogViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val unarchiveAsset: UnarchiveAssetUseCase,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val assetMetadataRepository: AssetMetadataRepository,
    private val session: AccountSession
) : ViewModel() {

    private val _showAddSheet      = MutableStateFlow(false)
    private val _addForCategoryId  = MutableStateFlow<String?>(null)
    private val _editing           = MutableStateFlow<Asset?>(null)
    private val _editingPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    private val _pendingArchive     = MutableStateFlow<Asset?>(null)
    private val _error              = MutableStateFlow<String?>(null)
    private val _editingSectorIds   = MutableStateFlow<Set<String>>(emptySet())
    private val _editingRegionPercents = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _editingFixedIncomePercent = MutableStateFlow(0)

    private val allSectors: StateFlow<List<es.aviferdev.trackfolio.domain.model.AssetSector>> =
        assetMetadataRepository.getAllSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<es.aviferdev.trackfolio.domain.model.AssetRegion>> =
        assetMetadataRepository.getAllRegions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val sheetStateFlowPart1 = combine(
        _showAddSheet,
        _addForCategoryId,
        _editing,
        _editingPlatformIds
    ) { show, catId, edit, platIds -> SheetPart1(show, catId, edit, platIds) }

    private val sheetStateFlowPart2 = combine(
        _editingSectorIds,
        _editingRegionPercents,
        _editingFixedIncomePercent,
        _pendingArchive
    ) { sectIds, regPerc, fixedIncPct, arch -> SheetPart2(sectIds, regPerc, fixedIncPct, arch) }

    private val sheetStateFlow = combine(sheetStateFlowPart1, sheetStateFlowPart2) { part1, part2 ->
        SheetState(part1.show, part1.catId, part1.edit, part1.platIds, part2.sectIds, part2.regPerc, part2.fixedIncPct, part2.arch)
    }

    private val metadataFlow = combine(allSectors, allRegions) { sectors, regions -> sectors to regions }

    val uiState: StateFlow<AssetCatalogUiState> = combine(
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        },
        getAssetCategoriesIncludingArchived().map { it.filter { c -> !c.archived } },
        sheetStateFlow,
        _error,
        metadataFlow
    ) { assets, categories, sheets, error, metadata ->
        AssetCatalogUiState(
            assets             = assets,
            categories         = categories,
            showAddSheet       = sheets.show,
            addForCategoryId   = sheets.catId,
            editing            = sheets.edit,
            editingPlatformIds = sheets.platIds,
            editingSectorIds   = sheets.sectIds,
            editingRegionPercents = sheets.regPerc,
            editingFixedIncomePercent = sheets.fixedIncPct,
            pendingArchive     = sheets.arch,
            error              = error,
            allSectors         = metadata.first,
            allRegions         = metadata.second
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetCatalogUiState()
    )

    fun openAddSheet()  { _showAddSheet.value = true; _addForCategoryId.value = null }
    fun openAddSheetForCategory(categoryId: String) {
        _addForCategoryId.value = categoryId
        _showAddSheet.value = true
    }
    fun closeAddSheet() { _showAddSheet.value = false; _addForCategoryId.value = null }

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
                _error.value = "No se puede archivar «${asset.ticker}» porque tiene posiciones abiertas (${formatQtySimple(position.netQuantity)} uds.). Cierra o traspasa las posiciones primero."
            } else {
                _pendingArchive.value = asset
            }
        }
    }
    fun cancelArchive()              { _pendingArchive.value = null }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet(),
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
        if (uiState.value.assets.any {
            it.ticker.equals(tickerTrim, ignoreCase = true) && it.accountId == accountId
        }) {
            _error.value = "Ya existe un activo con el ticker $tickerTrim en esta cuenta"
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
                assetCategoryId = assetCategoryId,
                currentPrice    = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null
            )
            saveAsset(asset)
                .onSuccess {
                    // Guardar composición RF/RV
                    if (fixedIncomePercent > 0) {
                        assetMetadataRepository.saveComposition(
                            es.aviferdev.trackfolio.domain.model.AssetComposition(
                                assetId = asset.id,
                                fixedIncomePercent = fixedIncomePercent,
                                createdAt = now
                            )
                        )
                    }
                    // Vincular sectores
                    sectorIds.forEach { sectorId ->
                        assetMetadataRepository.saveSectorRelation(
                            es.aviferdev.trackfolio.domain.model.AssetSectorRelation(
                                assetId = asset.id,
                                sectorId = sectorId
                            )
                        )
                    }
                    // Guardar distribución regional
                    regionPercents.forEach { (regionId, percent) ->
                        if (percent > 0) {
                            assetMetadataRepository.saveRegionDistribution(
                                es.aviferdev.trackfolio.domain.model.AssetRegionDistribution(
                                    assetId = asset.id,
                                    regionId = regionId,
                                    percent = percent
                                )
                            )
                        }
                    }
                    // Vincular plataformas
                    platformIds.forEach { platId ->
                        assetPlatformRepository.link(asset.id, platId)
                    }
                }
                .onFailure { _error.value = it.message }
            _showAddSheet.value = false
            _addForCategoryId.value = null
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
                    currentPriceUpdatedAt = updatedAt
                )
            ).onSuccess {
                // Guardar composición RF/RV
                if (fixedIncomePercent > 0) {
                    assetMetadataRepository.saveComposition(
                        es.aviferdev.trackfolio.domain.model.AssetComposition(
                            assetId = original.id,
                            fixedIncomePercent = fixedIncomePercent,
                            createdAt = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                } else {
                    assetMetadataRepository.deleteComposition(original.id)
                }
                // Actualizar sectores: borrar todos y recrear
                assetMetadataRepository.deleteAllSectorLinks(original.id)
                sectorIds.forEach { sectorId ->
                    assetMetadataRepository.saveSectorRelation(
                        es.aviferdev.trackfolio.domain.model.AssetSectorRelation(
                            assetId = original.id,
                            sectorId = sectorId
                        )
                    )
                }
                // Actualizar distribución regional
                assetMetadataRepository.deleteAllRegionDistributions(original.id)
                regionPercents.forEach { (regionId, percent) ->
                    if (percent > 0) {
                        assetMetadataRepository.saveRegionDistribution(
                            es.aviferdev.trackfolio.domain.model.AssetRegionDistribution(
                                assetId = original.id,
                                regionId = regionId,
                                percent = percent
                            )
                        )
                    }
                }
                // Actualizar plataformas: borrar todas y recrear
                assetPlatformRepository.unlinkAllByAsset(original.id)
                platformIds.forEach { platId ->
                    assetPlatformRepository.link(original.id, platId)
                }
            }.onFailure { _error.value = it.message }
            _editing.value = null
            _editingPlatformIds.value = emptySet()
        }
    }

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

    fun clearError() { _error.value = null }

    private fun formatQtySimple(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString()
        else v.toString().replace('.', ',')
    }

    private data class SheetPart1(
        val show: Boolean,
        val catId: String?,
        val edit: Asset?,
        val platIds: Set<String>
    )

    private data class SheetPart2(
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?
    )

    private data class SheetState(
        val show: Boolean,
        val catId: String?,
        val edit: Asset?,
        val platIds: Set<String>,
        val sectIds: Set<String>,
        val regPerc: Map<String, Int>,
        val fixedIncPct: Int,
        val arch: Asset?
    )
}
