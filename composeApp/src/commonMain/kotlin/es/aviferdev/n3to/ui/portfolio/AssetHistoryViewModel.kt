package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.TransferableCategories
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.domain.portfolio.FifoBreakdown
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.DeleteAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.ExecuteFundTransferUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.UpdateAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import es.aviferdev.n3to.domain.model.Transaction

data class AssetHistoryUiState(
    val asset: Asset?                       = null,
    val position: AssetPosition?            = null,
    val breakdown: FifoBreakdown?           = null,
    val transactionsDesc: List<AssetTransaction> = emptyList(),
    val transactionsAsc: List<AssetTransaction>  = emptyList(),
    val dividends: List<Transaction>        = emptyList(),
    val platforms: List<Platform>           = emptyList(),
    val allPlatforms: List<Platform>        = emptyList(),
    val platformsByAsset: Map<String, List<Platform>> = emptyMap(),
    val categories: List<AssetCategory>    = emptyList(),
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
    private val getAssetById: es.aviferdev.n3to.domain.repository.AssetRepository,
    private val getTransactionsByAsset: GetTransactionsByAssetUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val platformCategoryRepository: es.aviferdev.n3to.domain.repository.PlatformCategoryRepository,
    private val assetPlatformRepository: es.aviferdev.n3to.domain.repository.AssetPlatformRepository,
    private val getAccountById: GetAccountByIdUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val updateAssetTransaction: UpdateAssetTransactionUseCase,
    private val deleteAssetTransaction: DeleteAssetTransactionUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val assetPriceHistoryRepository: AssetPriceHistoryRepository,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val transactionRepository: es.aviferdev.n3to.domain.repository.TransactionRepository,
    private val executeFundTransfer: ExecuteFundTransferUseCase,
    private val assetRepository: es.aviferdev.n3to.domain.repository.AssetRepository
) : ViewModel() {

    private val _showAddSheet         = MutableStateFlow(false)
    private val _editing              = MutableStateFlow<AssetTransaction?>(null)
    private val _showUpdatePriceSheet = MutableStateFlow(false)
    private val _pendingDelete        = MutableStateFlow<AssetTransaction?>(null)
    private val _error                = MutableStateFlow<String?>(null)
    private val _showDividendSheet    = MutableStateFlow(false)
    private val _editingDividendId   = MutableStateFlow<String?>(null)
    private val _showTransferSheet    = MutableStateFlow(false)

    private data class Sheets(
        val showAdd: Boolean,
        val editing: AssetTransaction?,
        val showUpdatePrice: Boolean,
        val pendingDelete: AssetTransaction?,
        val error: String?,
        val showDividend: Boolean,
        val editingDividendId: String?,
        val showTransfer: Boolean
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
            showTransfer        = false
        )
    }.combine(
        combine(_showDividendSheet, _editingDividendId) { div, divId ->
            object { val showDiv = div; val divId = divId }
        }.combine(
            _showTransferSheet
        ) { extra, transfer ->
            object { val showDiv = extra.showDiv; val divId = extra.divId; val showTransfer = transfer }
        }
    ) { base, extra ->
        base.copy(
            showDividend         = extra.showDiv,
            editingDividendId    = extra.divId,
            showTransfer         = extra.showTransfer
        )
    }

    // Combinar datos del activo + transacciones + plataformas de la categoría
    private val coreDataFlow = getAssetById.getAssetById(assetId)
        .flatMapLatest { asset ->
            if (asset == null) {
                flowOf(CoreData(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyMap(), emptyList()))
            } else {
                val categoryPlatformsFlow = if (asset.assetCategoryId != null)
                    platformCategoryRepository.getByCategory(asset.assetCategoryId)
                else
                    getPlatforms()
                // combine solo acepta hasta 5 flows, así que anidamos
                val firstPart = combine(
                    flowOf(asset),
                    getTransactionsByAsset(assetId),
                    assetPlatformRepository.getPlatformsByAsset(assetId),
                    transactionRepository.getDividendsByAsset(assetId),
                    categoryPlatformsFlow
                ) { a, txs, assetPlatforms, dividends, catPlatforms ->
                    object { val a = a; val txs = txs; val assetPlatforms = assetPlatforms; val dividends = dividends; val catPlatforms = catPlatforms }
                }
                val secondPart = combine(
                    assetPlatformRepository.getPlatformsByAssets(listOf(assetId)),
                    getAssetCategoriesIncludingArchived()
                ) { platformsByAsset, categories ->
                    object { val platformsByAsset = platformsByAsset; val categories = categories }
                }
                combine(firstPart, secondPart) { first, second ->
                    CoreData(
                        first.a,
                        first.txs,
                        first.assetPlatforms,
                        first.catPlatforms,
                        first.dividends,
                        second.platformsByAsset,
                        second.categories
                    )
                }
            }
        }

    private data class CoreData(
        val asset: Asset?,
        val txs: List<AssetTransaction>,
        val assetPlatforms: List<Platform>,
        val globalPlatforms: List<Platform>,
        val dividends: List<Transaction>,
        val platformsByAsset: Map<String, List<Platform>>,
        val categories: List<AssetCategory>
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
                platformsByAsset     = core.platformsByAsset,
                categories           = core.categories,
                isLoading            = false,
                showAddSheet         = sheets.showAdd,
                editing              = sheets.editing,
                showUpdatePriceSheet = sheets.showUpdatePrice,
                pendingDelete        = sheets.pendingDelete,
                error                = sheets.error,
                showDividendSheet    = sheets.showDividend,
                editingDividendId    = sheets.editingDividendId,
                showTransferSheet    = sheets.showTransfer,
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
                        transferableDestinations = destinations
                    ))
                }
            } else {
                flowOf(state)
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
            val now = nowMillis()
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
                        if (type == AssetTransactionType.BUY) {
                            assetPriceHistoryRepository.insert(
                                AssetPriceHistory(
                                    id         = uuid4().toString(),
                                    assetId    = assetId,
                                    price      = pricePerUnit,
                                    recordedAt = date
                                )
                            ).onFailure { err ->
                                _error.value = "Error al registrar precio histórico: ${err.message}"
                            }
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
            val now = nowMillis()
            updateAssetCurrentPrice(assetId, newPrice, now, uiState.value.asset?.assetCategoryId)
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
            val dividendId = _editingDividendId.value ?: "div_${asset.id}_${nowMillis()}_${(0..9999).random()}"

            val result = syncToLedger.syncDividend(
                dividendId  = dividendId,
                accountId   = asset.accountId,
                assetName   = asset.name,
                grossAmount = grossAmount,
                withholdingPercent = irpfPercent,
                date               = date
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

    // ── Traspaso entre fondos ───────────────────────────────────────────
    fun openTransferSheet()  { _showTransferSheet.value = true }
    fun closeTransferSheet() { _showTransferSheet.value = false }

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
