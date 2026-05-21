package es.aviferdev.n3to.ui.portfolio.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.PortfolioSettingsScreenData
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


sealed class PortfolioSettingsUiState {
    data object Loading : PortfolioSettingsUiState()
    data object Empty : PortfolioSettingsUiState()
    data object EmptyPortFolio : PortfolioSettingsUiState()
    data class Success(
        val data: PortfolioSettingsScreenData,
    ) : PortfolioSettingsUiState()

    data class Error(val message: String) : PortfolioSettingsUiState()
}


class PortfolioSettingsViewModel(
    private val session: AccountSession,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,

    ) : ViewModel() {

    private val _uiState =
        MutableStateFlow<PortfolioSettingsUiState>(PortfolioSettingsUiState.Loading)
    val uiState: StateFlow<PortfolioSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.selectedAccountId.collectLatest { accountId ->
                if (accountId == null) {
                    _uiState.value = PortfolioSettingsUiState.Empty
                } else {
                    _uiState.value = PortfolioSettingsUiState.Loading
                    getPortfoliosByAccount(accountId).collect { portfolios ->
                        _uiState.value = if (portfolios.isEmpty())
                            PortfolioSettingsUiState.EmptyPortFolio
                        else
                            PortfolioSettingsUiState.Success(PortfolioSettingsScreenData(portfolios))
                    }
                }
            }
        }
    }

}
