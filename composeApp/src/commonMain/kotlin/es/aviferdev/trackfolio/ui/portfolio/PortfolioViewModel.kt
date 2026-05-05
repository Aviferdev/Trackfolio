package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.DeleteAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.theme.CategoryPalette
import es.aviferdev.trackfolio.ui.theme.UncategorizedColor
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
    /** Precio efectivo (current si existe, si no el de compra). */
    val currentPrice: Double,
    /** Valor actual de la posición = quantity × currentPrice. */
    val currentValue: Double,
    /** Ganancia/pérdida absoluta. 0 cuando no hay precio actual. */
    val pnlAmount: Double,
    /** Ganancia/pérdida porcentual. 0 cuando no hay precio actual. */
    val pnlPercent: Double,
    /** True si el usuario ya ha registrado un precio actual para este activo. */
    val hasCurrentPrice: Boolean
)

/** Grupo de activos pertenecientes a la misma categoría (o "Sin categoría"). */
data class CategoryGroup(
    val category: AssetCategory?,        // null = grupo "Sin categoría"
    val rows: List<AssetRow>,
    val totalInvested: Double,           // suma cantidad × precio_compra
    val totalCurrentValue: Double,       // suma cantidad × precio_actual_efectivo
    val totalPnL: Double                 // currentValue − invested
) {
    val displayName: String   get() = category?.name ?: "Sin categoría"
    val displayIcon: String   get() = category?.icon ?: "❔"
    val sortKey: Int          get() = category?.sortOrder ?: Int.MAX_VALUE
}

/**
 * Slice del donut chart de distribución por categoría.
 *
 * @param percent Porcentaje del valor actual total (0..100).
 * @param color Color asignado para pintar el arco y la leyenda.
 */
data class CategorySlice(
    val categoryId: String?,    // null = "Sin categoría"
    val name: String,
    val icon: String,
    val value: Double,
    val percent: Double,
    val color: Color
)

data class PortfolioUiState(
    val groups: List<CategoryGroup>     = emptyList(),
    val distribution: List<CategorySlice> = emptyList(),
    val totalInvested: Double           = 0.0,
    val totalCurrentValue: Double       = 0.0,
    val totalPnL: Double                = 0.0,
    val totalPnLPercent: Double         = 0.0,
    /** Código ISO de la cuenta seleccionada (EUR/USD/…). "EUR" como fallback. */
    val currencyCode: String            = "EUR",
    val isLoading: Boolean              = true,
    val error: String?                  = null,
    // Sheets / diálogos (estado controlado, en _uiState)
    val showAddSheet: Boolean           = false,
    val showEditSheet: Boolean          = false,
    val editingAsset: Asset?            = null,
    val showDeleteConfirm: Boolean      = false,
    val assetToDelete: Asset?           = null,
    // Sheet rápido para actualizar solo el precio actual
    val showUpdatePriceSheet: Boolean   = false,
    val pricingAsset: Asset?            = null
) {
    /** Acceso plano a todas las filas (para compatibilidad con código antiguo). */
    val rows: List<AssetRow> get() = groups.flatMap { it.rows }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val deleteAsset: DeleteAssetUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getAccountById: GetAccountByIdUseCase,
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
                    getAssetCategoriesIncludingArchived(),
                    getAccountById(accountId)
                ) { assets, categories, account ->
                    buildState(assets, categories, account)
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
        categories: List<AssetCategory>,
        account: Account?
    ): PortfolioUiState {
        // Cada activo: usa currentPrice si existe, si no purchasePrice (P&L = 0).
        val rows = assets.map { asset ->
            val price = asset.effectivePrice
            AssetRow(
                asset           = asset,
                currentPrice    = price,
                currentValue    = asset.quantity * price,
                pnlAmount       = asset.effectivePnL,
                pnlPercent      = asset.effectivePnLPercent,
                hasCurrentPrice = asset.hasCurrentPrice
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

        // ── Distribución por categoría (donut) ──────────────────────────────
        val distribution: List<CategorySlice> = if (totalCurrentValue <= 0.0) {
            emptyList()
        } else {
            groups
                .filter { it.totalCurrentValue > 0.0 }
                .mapIndexed { idx, g ->
                    CategorySlice(
                        categoryId = g.category?.id,
                        name       = g.displayName,
                        icon       = g.displayIcon,
                        value      = g.totalCurrentValue,
                        percent    = (g.totalCurrentValue / totalCurrentValue) * 100.0,
                        color      = colorForGroup(g, idx)
                    )
                }
                .sortedByDescending { it.percent }
        }

        return _uiState.value.copy(
            groups            = groups,
            distribution      = distribution,
            totalInvested     = totalInvested,
            totalCurrentValue = totalCurrentValue,
            totalPnL          = totalCurrentValue - totalInvested,
            totalPnLPercent   = if (totalInvested == 0.0) 0.0
                                else ((totalCurrentValue - totalInvested) / totalInvested) * 100.0,
            currencyCode      = account?.currency ?: "EUR",
            isLoading         = false
        )
    }

    /**
     * Asigna un color de la paleta a un grupo. Para "Sin categoría" devuelve
     * siempre el gris reservado. Para categorías reales, usa `sortOrder` como
     * índice (módulo paleta) para que el color sea estable entre sesiones.
     */
    private fun colorForGroup(group: CategoryGroup, fallbackIndex: Int): Color {
        val cat = group.category ?: return UncategorizedColor
        val idx = if (cat.sortOrder >= 0) cat.sortOrder else fallbackIndex
        return CategoryPalette[idx % CategoryPalette.size]
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

    /** Abre el sheet rápido para refrescar solo el precio actual del activo. */
    fun openUpdatePriceSheet(asset: Asset) {
        _uiState.value = _uiState.value.copy(showUpdatePriceSheet = true, pricingAsset = asset)
    }

    fun closeUpdatePriceSheet() {
        _uiState.value = _uiState.value.copy(showUpdatePriceSheet = false, pricingAsset = null)
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    fun addAsset(
        ticker: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        purchaseDate: Long,
        notes: String?,
        assetCategoryId: String? = null,
        currentPrice: Double? = null
    ) {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val asset = Asset(
                id              = generateId(),
                accountId       = accountId,
                ticker          = ticker.uppercase().trim(),
                name            = name.trim(),
                quantity        = quantity,
                purchasePrice   = purchasePrice,
                purchaseDate    = purchaseDate,
                notes           = notes?.ifBlank { null },
                createdAt       = now,
                assetCategoryId = assetCategoryId,
                currentPrice    = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null
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
        assetCategoryId: String? = original.assetCategoryId,
        currentPrice: Double? = original.currentPrice
    ) {
        viewModelScope.launch {
            // Si el precio actual ha cambiado respecto al original, actualizamos
            // el timestamp; si se mantiene igual, conservamos el timestamp previo;
            // si pasa de un valor a null (lo borraron), también ponemos null.
            val updatedAt = when {
                currentPrice == null                       -> null
                currentPrice == original.currentPrice      -> original.currentPriceUpdatedAt
                else -> Clock.System.now().toEpochMilliseconds()
            }
            updateAsset(
                original.copy(
                    ticker                = ticker.uppercase().trim(),
                    name                  = name.trim(),
                    quantity              = quantity,
                    purchasePrice         = purchasePrice,
                    purchaseDate          = purchaseDate,
                    notes                 = notes?.ifBlank { null },
                    assetCategoryId       = assetCategoryId,
                    currentPrice          = currentPrice,
                    currentPriceUpdatedAt = updatedAt
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

    /**
     * Actualiza únicamente el precio actual del activo (atajo desde el sheet
     * rápido en la pantalla de portfolio). Persiste el timestamp para que el
     * usuario sepa cuándo lo refrescó por última vez.
     */
    fun refreshCurrentPrice(asset: Asset, newPrice: Double) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            updateAssetCurrentPrice(asset.id, newPrice, now)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showUpdatePriceSheet = false,
                        pricingAsset         = null
                    )
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
