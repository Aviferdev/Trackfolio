package es.aviferdev.trackfolio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.usecase.home.GetHomeBalanceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val balance: HomeBalance) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    getHomeBalance: GetHomeBalanceUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getHomeBalance()
        .map<HomeBalance, HomeUiState> { HomeUiState.Success(it) }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = HomeUiState.Loading
        )
}
