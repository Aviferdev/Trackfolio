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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

// ─── Estado de cada posición enriquecida ─────────────────────────────────────
data class AssetRow(
    val asset: Asset,
    // Sin API de precios: precio actual = precio de compra (P&L = 0 hasta Sprint 12+)
    val currentPrice: Double = asset.purchasePrice,
    val currentValue: Double = asset.totalInvested,
    val pnlAmount: Double    = 0.0,
    val pnlPercent: Double   = 0.0
)

/** Grupo de activos pertenecientes a la misma categoría (o "Sin categoría"). */
data class CategoryGroup(
    val category: AssetCategory?,        // null = grupo "Sin categoría"
    val rows: List<AssetRow>,
    val totalInvested: Double,           // suma cantidad × precio_compra
    val totalCurrentValue: Double,       // suma cantidad × precio_actual
    val totalPnL: Double                 // currentValue − invested
) {
    val displayName: String   get() = category?.name ?: "Sin categoría"
    val displayIcon: String   get() = category?.icon ?: "❔"
    val sortKey: Int          get() = category?.sortOrder ?: Int.MAX_VALUE
}

data class PortfolioUiState(
    val groups: List<CategoryGroup>     = emptyList(),
    val totalInvested: Double           = 0.0,
    val totalCurrentValue: Double       = 0.0,
    val totalPnL: Double                = 0.0,
    val totalPnLPercent: Double         = 0.0,
    val isLoading: Boolean              = true,
    val error: String?                  = null,
    // Sheets / diálogos (estado controlado, en _uiState)
    val showAddSheet: Boolean           = false,
    val showEditSheet: Boolean          = false,
    val editingAsset: Asset?            = null,
    val showDeleteConfirm: Boolean      = false,
    val assetToDelete: Asset?           = null
) {
    /** Acceso plano a todas las filas (para compatibilidad con código antiguo). */
    val rows: List<AssetRow> get() = groups.flatMap { it.rows }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val deleteAsset: DeleteAssetUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    val portfolioState: StateFlow<PortfolioUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) {
                flowOf(PortfolioUiState(isLoading = false))
            } else {
                combine(
                    getAssetsByAccount(accountId),
                    getAssetCategoriesIncludingArchived()
                ) { assets, categories ->
                    buildState(assets, categories)
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = PortfolioUiState()
        )

    /**
     * Categorías activas (no archivadas) disponibles para asignar a un asset.
     * A diferencia de `portfolioState.groups`, incluye categorías sin assets.
     */
    val availableCategories: StateFlow<List<AssetCategory>> = getAssetCategoriesIncludingArchived()
        .map { list -> list.filter { !it.archived } }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private fun buildState(
        assets: List<Asset>,
        categories: List<AssetCategory>
    ): PortfolioUiState {
        // Sin precios en tiempo real: precio actual = precio de compra
        val rows = assets.map { asset ->
            AssetRow(
                asset        = asset,
                currentPrice = asset.purchasePrice,
                currentValue = asset.totalInvested,
                pnlAmount    = 0.0,
                pnlPercent   = 0.0
            )
        }

        // Mapa id → categoría (incluyendo archivadas, para no perder activos
        // ligados a categorías eliminadas)
        val byId = categories.associateBy { it.id }

        // Agrupar por assetCategoryId
        val grouped: Map<String?, List<AssetRow>> =
            rows.groupBy { it.asset.assetCategoryId }

        // Construir grupos. Activos con categoría inexistente caen en "Sin categoría".
        val groups: List<CategoryGroup> = grouped
            .map { (categoryId, groupRows) ->
                val cat = categoryId?.let { byId[it] }
                val invested  = groupRows.sumOf { it.asset.totalInvested }
                val current   = groupRows.sumOf { it.currentValue }
                CategoryGroup(
                    category          = cat,
                    rows              = groupRows.sortedByDescending { it.currentValue },
                    totalInvested     = invested,
                    totalCurrentValue = current,
                    totalPnL          = current - invested
                )
            }
            .sortedWith(
                compareBy(
                    { if (it.category == null) 1 else 0 }, // "Sin categoría" al final
                    { it.sortKey },
                    { it.displayName }
                )
            )

        val totalInvested     = groups.sumOf { it.totalInvested }
        val totalCurrentValue = groups.sumOf { it.totalCurrentValue }

        return _uiState.value.copy(
            groups            = groups,
            totalInvested     = totalInvested,
            totalCurrentValue = totalCurrentValue,
            totalPnL          = totalCurrentValue - totalInvested,
            totalPnLPercent   = if (totalInvested == 0.0) 0.0
                                else ((totalCurrentValue - totalInvested) / totalInvested) * 100.0,
            isLoading         = false
        )
    }

    // ── Sheet controls ────────────────────────────────────────────────────────
    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(showAddSheet = true)
    }

    fun closeAddSheet() {
        _uiState.value = _uiState.value.copy(showAddSheet = false)
    }

    fun openEditSheet(asset: Asset) {
        _uiState.value = _uiState.value.copy(showEditSheet = true, editingAsset = asset)
    }

    fun closeEditSheet() {
        _uiState.value = _uiState.value.copy(showEditSheet = false, editingAsset = null)
    }

    fun requestDelete(asset: Asset) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, assetToDelete = asset)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false, assetToDelete = null)
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    fun addAsset(
        ticker: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        purchaseDate: Long,
        notes: String?,
        assetCategoryId: String? = null
    ) {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            val asset = Asset(
                id              = generateId(),
                accountId       = accountId,
                ticker          = ticker.uppercase().trim(),
                name            = name.trim(),
                quantity        = quantity,
                purchasePrice   = purchasePrice,
                purchaseDate    = purchaseDate,
                notes           = notes?.ifBlank { null },
                createdAt       = Clock.System.now().toEpochMilliseconds(),
                assetCategoryId = assetCategoryId
            )
            saveAsset(asset)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(showAddSheet = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        purchaseDate: Long,
        notes: String?,
        assetCategoryId: String? = original.assetCategoryId
    ) {
        viewModelScope.launch {
            updateAsset(
                original.copy(
                    ticker          = ticker.uppercase().trim(),
                    name            = name.trim(),
                    quantity        = quantity,
                    purchasePrice   = purchasePrice,
                    purchaseDate    = purchaseDate,
                    notes           = notes?.ifBlank { null },
                    assetCategoryId = assetCategoryId
                )
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(showEditSheet = false, editingAsset = null)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun confirmDelete() {
        val asset = _uiState.value.assetToDelete ?: return
        viewModelScope.launch {
            deleteAsset(asset.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showDeleteConfirm = false, assetToDelete = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return "asset_" + (1..26).map { chars.random() }.joinToString("")
    }
}
