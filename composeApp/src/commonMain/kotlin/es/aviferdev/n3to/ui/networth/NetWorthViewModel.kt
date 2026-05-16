package es.aviferdev.n3to.ui.networth

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.NetWorthData
import es.aviferdev.n3to.domain.model.NetWorthHistoryPoint
import es.aviferdev.n3to.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.n3to.domain.usecase.networth.GetNetWorthDataUseCase
import es.aviferdev.n3to.domain.usecase.networth.GetNetWorthHistoryUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.DonutSlice
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class NetWorthUiState {
    data object Loading : NetWorthUiState()
    data class Success(
        val data: NetWorthData,
        val netWorthHistory: List<NetWorthHistoryPoint> = emptyList(),
        val assetDistribution: List<DonutSlice> = emptyList()
    ) : NetWorthUiState()
    data class Error(val message: String) : NetWorthUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class NetWorthViewModel(
    private val getNetWorthData: GetNetWorthDataUseCase,
    private val getLoansByAccount: GetLoansByAccountUseCase,
    private val getNetWorthHistory: GetNetWorthHistoryUseCase,
    private val session: AccountSession,
    private val loadingManager: GlobalLoadingManager
) : ViewModel() {

    val uiState: StateFlow<NetWorthUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(NetWorthUiState.Loading)
            else {
                val dataFlow = getNetWorthData(accountId)
                val historyFlow = getNetWorthHistory(accountId)

                kotlinx.coroutines.flow.combine(dataFlow, historyFlow) { data, history ->
                    val distribution = buildAssetDistribution(data)
                    NetWorthUiState.Success(
                        data = data,
                        netWorthHistory = history,
                        assetDistribution = distribution
                    )
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = NetWorthUiState.Loading
        )

    // ── Estado para los bottom sheets ──────────────────────────────────────────
    private val _showAddLoanSheet = MutableStateFlow(false)
    val showAddLoanSheet: StateFlow<Boolean> = _showAddLoanSheet

    private val _showAddPropertySheet = MutableStateFlow(false)
    val showAddPropertySheet: StateFlow<Boolean> = _showAddPropertySheet

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                when (state) {
                    is NetWorthUiState.Loading -> loadingManager.show("Cargando patrimonio...")
                    else -> loadingManager.hide()
                }
            }
        }
    }

    fun openAddLoanSheet() { _showAddLoanSheet.value = true }
    fun closeAddLoanSheet() { _showAddLoanSheet.value = false }

    fun openAddPropertySheet() { _showAddPropertySheet.value = true }
    fun closeAddPropertySheet() { _showAddPropertySheet.value = false }

    companion object {
        private val AssetColors = listOf(
            Color(0xFF4CAF50), // Cuentas — verde
            Color(0xFF2196F3), // Inversiones — azul
            Color(0xFFFF9800), // Renta fija — ámbar
            Color(0xFF8D6E63)  // Inmuebles — marrón
        )

        private fun buildAssetDistribution(data: NetWorthData): List<DonutSlice> {
            val total = data.totalAssets
            if (total <= 0.0) return emptyList()

            val items = mutableListOf<Triple<String, String, Double>>() // icon, name, value
            if (data.totalAccountBalance > 0.0) {
                items.add(Triple("\uD83C\uDFE6", "Cuentas", data.totalAccountBalance))
            }
            if (data.totalPortfolioValue > 0.0) {
                items.add(Triple("\uD83D\uDCC8", "Inversiones", data.totalPortfolioValue))
            }
            if (data.totalFixedIncomeValue > 0.0) {
                items.add(Triple("\uD83C\uDFDB\uFE0F", "Renta fija", data.totalFixedIncomeValue))
            }
            if (data.totalRealEstateValue > 0.0) {
                items.add(Triple("\uD83C\uDFE0", "Inmuebles", data.totalRealEstateValue))
            }

            return items.mapIndexed { idx, (icon, name, value) ->
                DonutSlice(
                    name    = name,
                    icon    = icon,
                    amount  = value,
                    percent = (value / total) * 100.0,
                    color   = AssetColors[idx % AssetColors.size]
                )
            }
        }
    }
}
