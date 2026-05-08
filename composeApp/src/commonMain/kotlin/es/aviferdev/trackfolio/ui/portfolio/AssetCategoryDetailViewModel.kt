package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.FixedIncomeRow
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.repository.PlatformCategoryRepository
import es.aviferdev.trackfolio.domain.repository.PlatformRepository
import es.aviferdev.trackfolio.domain.repository.AssetMetadataRepository
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import es.aviferdev.trackfolio.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
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
    val currencyCode: String           = "EUR",
    val showAddSheet: Boolean          = false,
    val editing: Asset?                = null,
    val editingPlatformIds: Set<String> = emptySet(),
    val pendingArchive: Asset?         = null,
    val showLinkPlatformSheet: Boolean = false,
    val error: String?                 = null
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

                val sheetsFlow = combine(
                    _showAddSheet,
                    _editing,
                    _editingPlatformIds,
                    _pendingArchive,
                    _error
                ) { show, edit, platIds, arch, err -> SheetState(show, edit, platIds, arch, err) }
                    .combine(_showLinkPlatformSheet) { sheets, linkSheet -> sheets to linkSheet }

                combine(assetsAndCategoriesFlow, platformsFlow, fiRowsFlow, sheetsFlow) { data, platforms, fiRows, sheetsAndLink ->
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
                        currencyCode         = "EUR",
                        showAddSheet         = sheets.show,
                        editing              = sheets.edit,
                        editingPlatformIds   = sheets.platIds,
                        pendingArchive       = sheets.arch,
                        showLinkPlatformSheet = linkSheet,
                        error                = sheets.err
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
        }
    }
    fun closeEditSheet() { _editing.value = null; _editingPlatformIds.value = emptySet() }

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
        fixedIncomePercent: Int = 0
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
                            es.aviferdev.trackfolio.domain.model.AssetComposition(
                                assetId = asset.id,
                                fixedIncomePercent = fixedIncomePercent,
                                createdAt = now
                            )
                        )
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
        fixedIncomePercent: Int = 0
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
                        es.aviferdev.trackfolio.domain.model.AssetComposition(
                            assetId = original.id,
                            fixedIncomePercent = fixedIncomePercent,
                            createdAt = Clock.System.now().toEpochMilliseconds()
                        )
                    )
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

    private data class SheetState(
        val show: Boolean,
        val edit: Asset?,
        val platIds: Set<String>,
        val arch: Asset?,
        val err: String?
    )
}
