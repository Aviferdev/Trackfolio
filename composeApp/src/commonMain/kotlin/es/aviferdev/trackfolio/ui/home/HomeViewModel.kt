package es.aviferdev.trackfolio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.home.GetHomeBalanceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

class HomeViewModel(
    getHomeBalance: GetHomeBalanceUseCase,
    getCategoriesByType: GetCategoriesByTypeUseCase,
    private val setInitialBalance: SetInitialBalanceUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getHomeBalance(),
        getCategoriesByType(TransactionType.INCOME),
        getCategoriesByType(TransactionType.EXPENSE)
    ) { balance, incomeCategories, expenseCategories ->
        val categoryNames = (incomeCategories + expenseCategories)
            .associate { it.id to it.name }
        HomeUiState.Success(
            balance = balance,
            categoryNames = categoryNames,
            showInitialBalancePrompt = balance.totalCash == 0.0 &&
                balance.recentTransactions.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun setInitialBalance(amount: Double) {
        viewModelScope.launch {
            setInitialBalance.invoke(amount)
        }
    }
}
