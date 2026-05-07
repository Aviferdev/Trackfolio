package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.usecase.asset.DeleteAssetUseCase
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
    val pendingDelete: Asset?           = null,
    val error: String?                  = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetCatalogViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val deleteAsset: DeleteAssetUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val session: AccountSession
) : ViewModel() {

    private val _showAddSheet      = MutableStateFlow(false)
    private val _addForCategoryId  = MutableStateFlow<String?>(null)
    private val _editing           = MutableStateFlow<Asset?>(null)
    private val _editingPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    private val _pendingDelete     = MutableStateFlow<Asset?>(null)
    private val _error             = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AssetCatalogUiState> = combine(
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        },
        getAssetCategoriesIncludingArchived().map { it.filter { c -> !c.archived } },
        combine(_showAddSheet, _addForCategoryId, _editing, _editingPlatformIds, _pendingDelete) { show, catId, edit, platIds, del ->
            SheetState(show, catId, edit, platIds, del)
        },
        _error
    ) { assets, categories, sheets, error ->
        AssetCatalogUiState(
            assets             = assets,
            categories         = categories,
            showAddSheet       = sheets.show,
            addForCategoryId   = sheets.catId,
            editing            = sheets.edit,
            editingPlatformIds = sheets.platIds,
            pendingDelete      = sheets.del,
            error              = error
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
        // Cargar las plataformas vinculadas
        viewModelScope.launch {
            val platforms = assetPlatformRepository.getPlatformsByAsset(asset.id).first()
            _editingPlatformIds.value = platforms.map { it.id }.toSet()
        }
    }
    fun closeEditSheet() { _editing.value = null; _editingPlatformIds.value = emptySet() }

    fun requestDelete(asset: Asset) { _pendingDelete.value = asset }
    fun cancelDelete()              { _pendingDelete.value = null }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String> = emptySet()
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
        platformIds: Set<String> = emptySet()
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

    fun confirmDelete() {
        val asset = _pendingDelete.value ?: return
        viewModelScope.launch {
            assetPlatformRepository.unlinkAllByAsset(asset.id)
            deleteAsset(asset.id).onFailure { _error.value = it.message }
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }

    private data class SheetState(
        val show: Boolean,
        val catId: String?,
        val edit: Asset?,
        val platIds: Set<String>,
        val del: Asset?
    )
}
