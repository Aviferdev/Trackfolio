package es.aviferdev.trackfolio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val balance: HomeBalance,
        val categoryNames: Map<String, String>,
        val showInitialBalancePrompt: Boolean
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getHomeBalance: GetHomeBalanceUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val setInitialBalance: SetInitialBalanceUseCase,
    private val session: AccountSession
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            combine(
                getHomeBalance(accountId),
                getCategoriesByType(TransactionType.INCOME),
                getCategoriesByType(TransactionType.EXPENSE)
            ) { balance, incomeCategories, expenseCategories ->
                val categoryNames = (incomeCategories + expenseCategories)
                    .associate { it.id to it.name }
                HomeUiState.Success(
                    balance = balance,
                    categoryNames = categoryNames,
                    showInitialBalancePrompt = balance.selectedAccount != null
                        && balance.selectedAccountBalance == 0.0
                        && balance.recentTransactions.isEmpty()
                )
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun setInitialBalance(amount: Double) {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            setInitialBalance.invoke(accountId, amount)
        }
    }
}
