package es.aviferdev.n3to.ui.valuable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.usecase.valuable.GetAllValuablesByAccountUseCase
import es.aviferdev.n3to.domain.usecase.valuable.SaveValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.DeleteValuableUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ValuableListUiState(
    val valuables: List<Valuable> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ValuableListViewModel(
    private val getAllValuables: GetAllValuablesByAccountUseCase,
    private val deleteValuable: DeleteValuableUseCase,
    private val saveValuable: SaveValuableUseCase,
    private val session: AccountSession
) : ViewModel() {

    val uiState: StateFlow<ValuableListUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(ValuableListUiState(isLoading = false))
            else getAllValuables(accountId).map { valuables ->
                ValuableListUiState(valuables = valuables, isLoading = false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ValuableListUiState())

    fun deleteValuableAction(valuableId: String) {
        viewModelScope.launch {
            deleteValuable(valuableId)
        }
    }
}
