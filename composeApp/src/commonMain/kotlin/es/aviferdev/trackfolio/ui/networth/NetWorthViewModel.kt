package es.aviferdev.trackfolio.ui.networth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.NetWorthData
import es.aviferdev.trackfolio.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.networth.GetNetWorthDataUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class NetWorthUiState {
    data object Loading : NetWorthUiState()
    data class Success(val data: NetWorthData) : NetWorthUiState()
    data class Error(val message: String) : NetWorthUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class NetWorthViewModel(
    private val getNetWorthData: GetNetWorthDataUseCase,
    private val getLoansByAccount: GetLoansByAccountUseCase,
    private val session: AccountSession
) : ViewModel() {

    val uiState: StateFlow<NetWorthUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(NetWorthUiState.Loading)
            else getNetWorthData(accountId).map { data ->
                NetWorthUiState.Success(data)
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = NetWorthUiState.Loading
        )

    // ── Estado para el bottom sheet de crear préstamo ─────────────────────────
    private val _showAddLoanSheet = MutableStateFlow(false)
    val showAddLoanSheet: StateFlow<Boolean> = _showAddLoanSheet

    fun openAddLoanSheet() { _showAddLoanSheet.value = true }
    fun closeAddLoanSheet() { _showAddLoanSheet.value = false }
}
