package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.domain.portfolio.FifoBreakdown
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.DeleteAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetTransactionsByAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.UpdateAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetPlatformsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AssetHistoryUiState(
    val asset: Asset?                       = null,
    val position: AssetPosition?            = null,
    val breakdown: FifoBreakdown?           = null,
    val transactionsDesc: List<AssetTransaction> = emptyList(),
    val transactionsAsc: List<AssetTransaction>  = emptyList(),
    val platforms: List<Platform>           = emptyList(),
    val currencyCode: String                = "EUR",
    val isLoading: Boolean                  = true,
    val error: String?                      = null,
    // Sheet de añadir/editar movimiento
    val showAddSheet: Boolean               = false,
    val editing: AssetTransaction?          = null,
    // Sheet de actualizar precio actual
    val showUpdatePriceSheet: Boolean       = false,
    // Confirmación de borrado
    val pendingDelete: AssetTransaction?    = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetHistoryViewModel(
    private val assetId: String,
    private val getAssetById: es.aviferdev.trackfolio.domain.repository.AssetRepository,
    private val getTransactionsByAsset: GetTransactionsByAssetUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val getAccountById: GetAccountByIdUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val updateAssetTransaction: UpdateAssetTransactionUseCase,
    private val deleteAssetTransaction: DeleteAssetTransactionUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase
) : ViewModel() {

    private val _showAddSheet         = MutableStateFlow(false)
    private val _editing              = MutableStateFlow<AssetTransaction?>(null)
    private val _showUpdatePriceSheet = MutableStateFlow(false)
    private val _pendingDelete        = MutableStateFlow<AssetTransaction?>(null)
    private val _error                = MutableStateFlow<String?>(null)

    private data class Sheets(
        val showAdd: Boolean,
        val editing: AssetTransaction?,
        val showUpdatePrice: Boolean,
        val pendingDelete: AssetTransaction?,
        val error: String?
    )

    val uiState: StateFlow<AssetHistoryUiState> = combine(
        getAssetById.getAssetById(assetId),
        getTransactionsByAsset(assetId),
        getPlatforms(),
        combine(_showAddSheet, _editing, _showUpdatePriceSheet, _pendingDelete, _error) { a, b, c, d, e ->
            Sheets(a, b, c, d, e)
        }
    ) { asset, txs, platforms, sheets ->
        if (asset == null) {
            AssetHistoryUiState(isLoading = false, error = "Activo no encontrado")
        } else {
            val position  = PortfolioCalculator.calculate(txs, asset.currentPrice)
            val breakdown = PortfolioCalculator.breakdown(txs)
            AssetHistoryUiState(
                asset                = asset,
                position             = position,
                breakdown            = breakdown,
                transactionsDesc     = txs.sortedWith(compareByDescending<AssetTransaction> { it.date }
                    .thenByDescending { it.createdAt }),
                transactionsAsc      = txs,
                platforms            = platforms,
                currencyCode         = "EUR", // se completa abajo con la cuenta
                isLoading            = false,
                showAddSheet         = sheets.showAdd,
                editing              = sheets.editing,
                showUpdatePriceSheet = sheets.showUpdatePrice,
                pendingDelete        = sheets.pendingDelete,
                error                = sheets.error
            )
        }
    }
    .flatMapLatest { state ->
        val accId = state.asset?.accountId
        if (accId == null) flowOf(state)
        else getAccountById(accId).flatMapLatest { acc ->
            flowOf(state.copy(currencyCode = acc?.currency ?: "EUR"))
        }
    }
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetHistoryUiState()
    )

    // ── Sheet de añadir/editar movimiento ───────────────────────────────────
    fun openAddSheet() { _showAddSheet.value = true; _editing.value = null }
    fun openEditSheet(tx: AssetTransaction) { _editing.value = tx; _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false; _editing.value = null }

    fun saveTransaction(
        type: AssetTransactionType,
        quantity: Double,
        pricePerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?
    ) {
        val current = _editing.value
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val result = if (current == null) {
                val tx = AssetTransaction(
                    id           = "tx_${now}_${(0..9999).random()}",
                    assetId      = assetId,
                    type         = type,
                    quantity     = quantity,
                    pricePerUnit = pricePerUnit,
                    date         = date,
                    platformId   = platformId,
                    feeNote      = feeNote?.ifBlank { null },
                    notes        = notes?.ifBlank { null },
                    createdAt    = now
                )
                saveAssetTransaction(tx)
            } else {
                updateAssetTransaction(
                    current.copy(
                        type         = type,
                        quantity     = quantity,
                        pricePerUnit = pricePerUnit,
                        date         = date,
                        platformId   = platformId,
                        feeNote      = feeNote?.ifBlank { null },
                        notes        = notes?.ifBlank { null }
                    )
                )
            }
            result
                .onSuccess { closeAddSheet() }
                .onFailure { _error.value = it.message }
        }
    }

    // ── Borrado ─────────────────────────────────────────────────────────────
    fun requestDelete(tx: AssetTransaction) { _pendingDelete.value = tx }
    fun cancelDelete()                       { _pendingDelete.value = null }

    fun confirmDelete() {
        val tx = _pendingDelete.value ?: return
        viewModelScope.launch {
            deleteAssetTransaction(tx.id).onFailure { _error.value = it.message }
            _pendingDelete.value = null
        }
    }

    // ── Sheet de actualizar precio actual ───────────────────────────────────
    fun openUpdatePriceSheet() { _showUpdatePriceSheet.value = true }
    fun closeUpdatePriceSheet() { _showUpdatePriceSheet.value = false }

    fun refreshCurrentPrice(newPrice: Double) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            updateAssetCurrentPrice(assetId, newPrice, now)
                .onSuccess { closeUpdatePriceSheet() }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}
