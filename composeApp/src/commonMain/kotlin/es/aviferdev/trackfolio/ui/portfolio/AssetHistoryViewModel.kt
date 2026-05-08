package es.aviferdev.trackfolio.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.model.TransferableCategories
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.domain.portfolio.FifoBreakdown
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.DeleteAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.ExecuteFundTransferUseCase
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
    val allPlatforms: List<Platform>        = emptyList(),
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
    // Sheet de bono/depósito (cupones/intereses intermedios)
    val showBondDepositSheet: Boolean       = false,
    val editingBondDepositId: String?       = null,
    // Sheet de adquisición de renta fija
    val showAcquireFixedIncomeSheet: Boolean = false,
    // Sheet de liquidación/venta secundaria/cancelación anticipada
    val showCloseFixedIncomeSheet: Boolean  = false,
    // Sheet de traspaso entre fondos
    val showTransferSheet: Boolean          = false,
    // ¿Este activo admite traspasos?
    val isTransferable: Boolean             = false,
    // Fondos destino disponibles para traspaso (misma cuenta, categoría traspasable)
    val transferableDestinations: List<Asset> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetHistoryViewModel(
    private val assetId: String,
    private val getAssetById: es.aviferdev.trackfolio.domain.repository.AssetRepository,
    private val getTransactionsByAsset: GetTransactionsByAssetUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val platformCategoryRepository: es.aviferdev.trackfolio.domain.repository.PlatformCategoryRepository,
    private val assetPlatformRepository: es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository,
    private val getAccountById: GetAccountByIdUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val updateAssetTransaction: UpdateAssetTransactionUseCase,
    private val deleteAssetTransaction: DeleteAssetTransactionUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val transactionRepository: es.aviferdev.trackfolio.domain.repository.TransactionRepository,
    private val executeFundTransfer: ExecuteFundTransferUseCase,
    private val assetRepository: es.aviferdev.trackfolio.domain.repository.AssetRepository
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
    private val _showTransferSheet    = MutableStateFlow(false)
    private val _showAcquireFixedIncomeSheet = MutableStateFlow(false)
    private val _showCloseFixedIncomeSheet   = MutableStateFlow(false)

    private data class Sheets(
        val showAdd: Boolean,
        val editing: AssetTransaction?,
        val showUpdatePrice: Boolean,
        val pendingDelete: AssetTransaction?,
        val error: String?,
        val showDividend: Boolean,
        val editingDividendId: String?,
        val showBondDeposit: Boolean,
        val editingBondDepositId: String?,
        val showTransfer: Boolean,
        val showAcquireFixedIncome: Boolean,
        val showCloseFixedIncome: Boolean
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
            showDividend        = false,
            editingDividendId   = null,
            showBondDeposit     = false,
            editingBondDepositId = null,
            showTransfer        = false,
            showAcquireFixedIncome = false,
            showCloseFixedIncome   = false
        )
    }.combine(
        combine(_showDividendSheet, _editingDividendId, _showBondDepositSheet, _editingBondDepositId) { div, divId, bond, bondId ->
            object { val showDiv = div; val divId = divId; val showBond = bond; val bondId = bondId }
        }.combine(
            combine(_showTransferSheet, _showAcquireFixedIncomeSheet, _showCloseFixedIncomeSheet) { transfer, acquire, close ->
                object { val showTransfer = transfer; val showAcquire = acquire; val showClose = close }
            }
        ) { extra, fi ->
            object { val showDiv = extra.showDiv; val divId = extra.divId; val showBond = extra.showBond; val bondId = extra.bondId; val showTransfer = fi.showTransfer; val showAcquire = fi.showAcquire; val showClose = fi.showClose }
        }
    ) { base, extra ->
        base.copy(
            showDividend         = extra.showDiv,
            editingDividendId    = extra.divId,
            showBondDeposit      = extra.showBond,
            editingBondDepositId = extra.bondId,
            showTransfer         = extra.showTransfer,
            showAcquireFixedIncome = extra.showAcquire,
            showCloseFixedIncome   = extra.showClose
        )
    }

    // Combinar datos del activo + transacciones + plataformas de la categoría
    private val coreDataFlow = getAssetById.getAssetById(assetId)
        .flatMapLatest { asset ->
            if (asset == null) {
                flowOf(CoreData(null, emptyList(), emptyList(), emptyList(), emptyList()))
            } else {
                val categoryPlatformsFlow = if (asset.assetCategoryId != null)
                    platformCategoryRepository.getByCategory(asset.assetCategoryId)
                else
                    getPlatforms()
                combine(
                    flowOf(asset),
                    getTransactionsByAsset(assetId),
                    assetPlatformRepository.getPlatformsByAsset(assetId),
                    transactionRepository.getDividendsByAsset(assetId),
                    categoryPlatformsFlow
                ) { a, txs, assetPlatforms, dividends, catPlatforms ->
                    CoreData(a, txs, assetPlatforms, catPlatforms, dividends)
                }
            }
        }

    private data class CoreData(
        val asset: Asset?,
        val txs: List<AssetTransaction>,
        val assetPlatforms: List<Platform>,
        val globalPlatforms: List<Platform>,
        val dividends: List<Transaction>
    )

    val uiState: StateFlow<AssetHistoryUiState> = combine(
        coreDataFlow,
        sheetsFlow
    ) { core, sheets ->
        val asset = core.asset
        if (asset == null) {
            AssetHistoryUiState(isLoading = false, error = "Activo no encontrado")
        } else {
            val dividendIncome = core.dividends.sumOf { it.amount }
            val position  = PortfolioCalculator.calculate(core.txs, asset.currentPrice, dividendIncome)
            val breakdown = PortfolioCalculator.breakdown(core.txs)
            val isTransferable = TransferableCategories.isTransferable(asset.assetCategoryId)
            AssetHistoryUiState(
                asset                = asset,
                position             = position,
                breakdown            = breakdown,
                transactionsDesc     = core.txs.sortedWith(compareByDescending<AssetTransaction> { it.date }
                    .thenByDescending { it.createdAt }),
                transactionsAsc      = core.txs,
                dividends            = core.dividends.sortedByDescending { it.date },
                platforms            = core.assetPlatforms,
                allPlatforms         = core.globalPlatforms,
                currencyCode         = "EUR",
                isLoading            = false,
                showAddSheet         = sheets.showAdd,
                editing              = sheets.editing,
                showUpdatePriceSheet = sheets.showUpdatePrice,
                pendingDelete        = sheets.pendingDelete,
                error                = sheets.error,
                showDividendSheet    = sheets.showDividend,
                editingDividendId    = sheets.editingDividendId,
                showBondDepositSheet = sheets.showBondDeposit,
                editingBondDepositId = sheets.editingBondDepositId,
                showTransferSheet    = sheets.showTransfer,
                showAcquireFixedIncomeSheet = sheets.showAcquireFixedIncome,
                showCloseFixedIncomeSheet   = sheets.showCloseFixedIncome,
                isTransferable       = isTransferable
            )
        }
    }
    .flatMapLatest { state ->
        val accId = state.asset?.accountId
        if (accId == null) flowOf(state)
        else getAccountById(accId).flatMapLatest { acc ->
            if (state.isTransferable) {
                assetRepository.getAssetsByAccount(accId).flatMapLatest { allAssets ->
                    val destinations = allAssets.filter { a ->
                        a.id != assetId && TransferableCategories.isTransferable(a.assetCategoryId)
                    }
                    flowOf(state.copy(
                        currencyCode = acc?.currency ?: "EUR",
                        transferableDestinations = destinations
                    ))
                }
            } else {
                flowOf(state.copy(currencyCode = acc?.currency ?: "EUR"))
            }
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
                    if (r.isSuccess) {
                        syncToLedger.sync(
                            assetTx   = tx,
                            accountId = uiState.value.asset!!.accountId,
                            assetName = uiState.value.asset!!.name
                        )
                        if (type == AssetTransactionType.BUY && isSameDay(date, now)) {
                            updateAssetCurrentPrice(assetId, pricePerUnit, now)
                        }
                    }
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
            if (tx.isTransfer) {
                // Un traspaso tiene dos patas: OUT + IN. Borrar ambas.
                val groupId = tx.transferGroupId
                if (groupId != null) {
                    deleteAssetTransaction("txout_$groupId").onFailure { _error.value = it.message }
                    deleteAssetTransaction("txin_$groupId").onFailure { _error.value = it.message }
                } else {
                    // Fallback: borrar solo esta
                    deleteAssetTransaction(tx.id).onFailure { _error.value = it.message }
                }
            } else {
                syncToLedger.remove(tx.id)
                deleteAssetTransaction(tx.id).onFailure { _error.value = it.message }
            }
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

    // ── Traspaso entre fondos ───────────────────────────────────────────
    fun openTransferSheet()  { _showTransferSheet.value = true }
    fun closeTransferSheet() { _showTransferSheet.value = false }

    // ── Adquisición de renta fija ───────────────────────────────────────
    fun openAcquireFixedIncomeSheet()  { _showAcquireFixedIncomeSheet.value = true }
    fun closeAcquireFixedIncomeSheet() { _showAcquireFixedIncomeSheet.value = false }

    fun saveFixedIncomeAcquisition(
        quantity: Double,
        nominalPerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val tx = AssetTransaction(
                id           = "tx_${now}_${(0..9999).random()}",
                assetId      = assetId,
                type         = AssetTransactionType.BUY,
                quantity     = quantity,
                pricePerUnit = nominalPerUnit,
                date         = date,
                platformId   = platformId,
                feeNote      = feeNote,
                notes        = notes,
                createdAt    = now
            )
            saveAssetTransaction(tx)
                .onSuccess {
                    syncToLedger.sync(
                        assetTx   = tx,
                        accountId = uiState.value.asset!!.accountId,
                        assetName = uiState.value.asset!!.name
                    )
                    closeAcquireFixedIncomeSheet()
                }
                .onFailure { _error.value = it.message }
        }
    }

    // ── Liquidación / venta secundaria / cancelación anticipada ──────────
    fun openCloseFixedIncomeSheet()  { _showCloseFixedIncomeSheet.value = true }
    fun closeCloseFixedIncomeSheet() { _showCloseFixedIncomeSheet.value = false }

    fun saveFixedIncomeClose(
        closeType: FixedIncomeCloseType,
        quantity: Double,
        salePrice: Double,
        grossInterest: Double,
        irpfPercent: Double,
        commissionAmount: Double,
        date: Long,
        platformId: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val asset = uiState.value.asset ?: return@launch
            val now = Clock.System.now().toEpochMilliseconds()

            // 1) Registrar la venta/liquidación como SELL en AssetTransaction
            val closeNote = when (closeType) {
                FixedIncomeCloseType.MATURITY -> "Vencimiento"
                FixedIncomeCloseType.SECONDARY_SALE -> "Venta secundario"
                FixedIncomeCloseType.EARLY_CANCELLATION -> "Cancelación anticipada"
            }
            val fullNote = listOfNotNull(closeNote, notes).joinToString(" · ")

            val sellTx = AssetTransaction(
                id           = "tx_${now}_${(0..9999).random()}",
                assetId      = assetId,
                type         = AssetTransactionType.SELL,
                quantity     = quantity,
                pricePerUnit = salePrice,
                date         = date,
                platformId   = platformId,
                feeNote      = null,
                notes        = fullNote,
                createdAt    = now
            )

            val sellResult = saveAssetTransaction(sellTx)
            if (sellResult.isFailure) {
                _error.value = sellResult.exceptionOrNull()?.message
                return@launch
            }

            // Sync la venta al ledger (devuelve capital a la cuenta)
            syncToLedger.sync(
                assetTx   = sellTx,
                accountId = asset.accountId,
                assetName = asset.name
            )

            // 2) Si hay intereses, registrarlos como rendimiento de bono/depósito
            if (grossInterest > 0.0) {
                val interestId = "close_interest_${now}_${(0..9999).random()}"
                syncToLedger.syncBondDeposit(
                    bondDepositId    = interestId,
                    accountId        = asset.accountId,
                    assetName        = asset.name,
                    grossAmount      = grossInterest,
                    irpfPercent      = irpfPercent,
                    commissionAmount = commissionAmount,
                    date             = date
                ).onFailure { _error.value = it.message; return@launch }
            }

            closeCloseFixedIncomeSheet()
        }
    }

    /**
     * Ejecuta un traspaso del fondo actual a otro fondo destino.
     *
     * @param destinationAssetId ID del fondo destino.
     * @param quantity           participaciones a traspasar del fondo actual.
     * @param sourcePlatformId   plataforma de las participaciones origen.
     * @param destinationPlatformId plataforma donde se suscribirán las nuevas participaciones.
     * @param destinationPricePerUnit VL del fondo destino a fecha del traspaso.
     * @param date               fecha del traspaso en epoch millis.
     */
    fun executeTransfer(
        destinationAssetId: String,
        quantity: Double,
        sourcePlatformId: String,
        destinationPlatformId: String,
        destinationPricePerUnit: Double,
        date: Long
    ) {
        viewModelScope.launch {
            val result = executeFundTransfer(
                sourceAssetId           = assetId,
                destinationAssetId      = destinationAssetId,
                quantity                = quantity,
                sourcePlatformId        = sourcePlatformId,
                destinationPlatformId   = destinationPlatformId,
                destinationPricePerUnit = destinationPricePerUnit,
                date                    = date
            )
            result
                .onSuccess { closeTransferSheet() }
                .onFailure { _error.value = it.message }
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = timestamp1 / (24 * 60 * 60 * 1000)
        val day2 = timestamp2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }
}
