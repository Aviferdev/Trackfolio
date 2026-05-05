package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Estado del catálogo de activos que se muestra en Ajustes.
 *
 * El catálogo es la lista cruda de activos del usuario (ticker, nombre,
 * categoría, precio actual). Las posiciones reales y los P&L se calculan
 * en `PortfolioViewModel` a partir de los movimientos.
 */
data class AssetCatalogUiState(
    val assets: List<Asset>             = emptyList(),
    val categories: List<AssetCategory> = emptyList(),
    val showAddSheet: Boolean           = false,
    val editing: Asset?                 = null,
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
    private val session: AccountSession
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _editing       = MutableStateFlow<Asset?>(null)
    private val _pendingDelete = MutableStateFlow<Asset?>(null)
    private val _error         = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AssetCatalogUiState> = combine(
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        },
        getAssetCategoriesIncludingArchived().map { it.filter { c -> !c.archived } },
        combine(_showAddSheet, _editing, _pendingDelete, _error) { s, e, p, err ->
            Quad(s, e, p, err)
        }
    ) { assets, categories, q ->
        AssetCatalogUiState(
            assets        = assets,
            categories    = categories,
            showAddSheet  = q.a,
            editing       = q.b,
            pendingDelete = q.c,
            error         = q.d
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetCatalogUiState()
    )

    fun openAddSheet()  { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }

    fun openEditSheet(asset: Asset) { _editing.value = asset }
    fun closeEditSheet()            { _editing.value = null }

    fun requestDelete(asset: Asset) { _pendingDelete.value = asset }
    fun cancelDelete()              { _pendingDelete.value = null }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?
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
            saveAsset(asset).onFailure { _error.value = it.message }
            _showAddSheet.value = false
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?
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
            ).onFailure { _error.value = it.message }
            _editing.value = null
        }
    }

    fun confirmDelete() {
        val asset = _pendingDelete.value ?: return
        viewModelScope.launch {
            deleteAsset(asset.id).onFailure { _error.value = it.message }
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
