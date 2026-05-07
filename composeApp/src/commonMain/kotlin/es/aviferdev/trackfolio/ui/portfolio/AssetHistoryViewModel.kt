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
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
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

import es.aviferdev.trackfolio.domain.model.Transaction

data class AssetHistoryUiState(
    val asset: Asset?                       = null,
    val position: AssetPosition?            = null,
    val breakdown: FifoBreakdown?           = null,
    val transactionsDesc: List<AssetTransaction> = emptyList(),
    val transactionsAsc: List<AssetTransaction>  = emptyList(),
    val dividends: List<Transaction>        = emptyList(),
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
    val pendingDelete: AssetTransaction?    = null,
    // Sheet de dividendo
    val showDividendSheet: Boolean          = false,
    val editingDividendId: String?          = null,
    // Sheet de bono/depósito
    val showBondDepositSheet: Boolean       = false,
    val editingBondDepositId: String?       = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetHistoryViewModel(
    private val assetId: String,
    private val getAssetById: es.aviferdev.trackfolio.domain.repository.AssetRepository,
    private val getTransactionsByAsset: GetTransactionsByAssetUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val assetPlatformRepository: es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository,
    private val getAccountById: GetAccountByIdUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val updateAssetTransaction: UpdateAssetTransactionUseCase,
    private val deleteAssetTransaction: DeleteAssetTransactionUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val transactionRepository: es.aviferdev.trackfolio.domain.repository.TransactionRepository
) : ViewModel() {

    private val _showAddSheet         = MutableStateFlow(false)
    private val _editing              = MutableStateFlow<AssetTransaction?>(null)
    private val _showUpdatePriceSheet = MutableStateFlow(false)
    private val _pendingDelete        = MutableStateFlow<AssetTransaction?>(null)
    private val _error                = MutableStateFlow<String?>(null)
    private val _showDividendSheet    = MutableStateFlow(false)
    private val _editingDividendId   = MutableStateFlow<String?>(null)
    private val _showBondDepositSheet = MutableStateFlow(false)
    private val _editingBondDepositId = MutableStateFlow<String?>(null)

    private data class Sheets(
        val showAdd: Boolean,
        val editing: AssetTransaction?,
        val showUpdatePrice: Boolean,
        val pendingDelete: AssetTransaction?,
        val error: String?,
        val showDividend: Boolean,
        val editingDividendId: String?,
        val showBondDeposit: Boolean,
        val editingBondDepositId: String?
    )

    private val sheetsFlow = combine(
        _showAddSheet,
        _editing,
        _showUpdatePriceSheet,
        _pendingDelete,
        _error
    ) { showAdd, editing, showPrice, pendingDel, err ->
        Sheets(
            showAdd  = showAdd,
            editing  = editing,
            showUpdatePrice = showPrice,
            pendingDelete   = pendingDel,
            error           = err,
            showDividend        = false, // placeholder
            editingDividendId   = null,
            showBondDeposit     = false,
            editingBondDepositId = null
        )
    }.combine(
        combine(_showDividendSheet, _editingDividendId, _showBondDepositSheet, _editingBondDepositId) { div, divId, bond, bondId ->
            object { val showDiv = div; val divId = divId; val showBond = bond; val bondId = bondId }
        }
    ) { base, extra ->
        base.copy(
            showDividend         = extra.showDiv,
            editingDividendId    = extra.divId,
            showBondDeposit      = extra.showBond,
            editingBondDepositId = extra.bondId
        )
    }

    val uiState: StateFlow<AssetHistoryUiState> = combine(
        getAssetById.getAssetById(assetId),
        getTransactionsByAsset(assetId),
        assetPlatformRepository.getPlatformsByAsset(assetId),
        transactionRepository.getDividendsByAsset(assetId),
        sheetsFlow
    ) { asset, txs, platforms, dividends, sheets ->
        if (asset == null) {
            AssetHistoryUiState(isLoading = false, error = "Activo no encontrado")
        } else {
            val dividendIncome = dividends.sumOf { it.amount }
            val position  = PortfolioCalculator.calculate(txs, asset.currentPrice, dividendIncome)
            val breakdown = PortfolioCalculator.breakdown(txs)
            AssetHistoryUiState(
                asset                = asset,
                position             = position,
                breakdown            = breakdown,
                transactionsDesc     = txs.sortedWith(compareByDescending<AssetTransaction> { it.date }
                    .thenByDescending { it.createdAt }),
                transactionsAsc      = txs,
                dividends            = dividends.sortedByDescending { it.date },
                platforms            = platforms,
                currencyCode         = "EUR", // se completa abajo con la cuenta
                isLoading            = false,
                showAddSheet         = sheets.showAdd,
                editing              = sheets.editing,
                showUpdatePriceSheet = sheets.showUpdatePrice,
                pendingDelete        = sheets.pendingDelete,
                error                = sheets.error,
                showDividendSheet    = sheets.showDividend,
                editingDividendId    = sheets.editingDividendId,
                showBondDepositSheet = sheets.showBondDeposit,
                editingBondDepositId = sheets.editingBondDepositId
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
                saveAssetTransaction(tx).also { r ->
                    if (r.isSuccess) syncToLedger.sync(
                        assetTx   = tx,
                        accountId = uiState.value.asset!!.accountId,
                        assetName = uiState.value.asset!!.name
                    )
                }
            } else {
                val updated = current.copy(
                    type         = type,
                    quantity     = quantity,
                    pricePerUnit = pricePerUnit,
                    date         = date,
                    platformId   = platformId,
                    feeNote      = feeNote?.ifBlank { null },
                    notes        = notes?.ifBlank { null }
                )
                updateAssetTransaction(updated).also { r ->
                    if (r.isSuccess) syncToLedger.sync(
                        assetTx   = updated,
                        accountId = uiState.value.asset!!.accountId,
                        assetName = uiState.value.asset!!.name
                    )
                }
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
            syncToLedger.remove(tx.id)
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

    // ── Dividendos ─────────────────────────────────────────────────────
    fun openDividendSheet()  { _showDividendSheet.value = true; _editingDividendId.value = null }
    fun openEditDividendSheet(dividendId: String) { _editingDividendId.value = dividendId; _showDividendSheet.value = true }
    fun closeDividendSheet() { _showDividendSheet.value = false; _editingDividendId.value = null }

    fun saveDividend(
        grossAmount: Double,
        irpfPercent: Double,
        date: Long
    ) {
        viewModelScope.launch {
            val asset = uiState.value.asset ?: return@launch
            val dividendId = _editingDividendId.value ?: "div_${asset.id}_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"

            val result = syncToLedger.syncDividend(
                dividendId  = dividendId,
                accountId   = asset.accountId,
                assetName   = asset.name,
                grossAmount = grossAmount,
                irpfPercent = irpfPercent,
                date        = date
            )
            result
                .onSuccess { closeDividendSheet() }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteDividend(dividendId: String) {
        viewModelScope.launch {
            syncToLedger.remove(dividendId)
        }
    }

    // ── Bonos / Depósitos ───────────────────────────────────────────
    fun openBondDepositSheet()  { _showBondDepositSheet.value = true; _editingBondDepositId.value = null }
    fun closeBondDepositSheet() { _showBondDepositSheet.value = false; _editingBondDepositId.value = null }

    fun saveBondDeposit(
        grossAmount: Double,
        irpfPercent: Double,
        commissionAmount: Double,
        date: Long
    ) {
        viewModelScope.launch {
            val asset = uiState.value.asset ?: return@launch
            val bondDepositId = _editingBondDepositId.value ?: "bond_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"

            val result = syncToLedger.syncBondDeposit(
                bondDepositId    = bondDepositId,
                accountId        = asset.accountId,
                assetName        = asset.name,
                grossAmount      = grossAmount,
                irpfPercent      = irpfPercent,
                commissionAmount = commissionAmount,
                date             = date
            )
            result
                .onSuccess { closeBondDepositSheet() }
                .onFailure { _error.value = it.message }
        }
    }
}
