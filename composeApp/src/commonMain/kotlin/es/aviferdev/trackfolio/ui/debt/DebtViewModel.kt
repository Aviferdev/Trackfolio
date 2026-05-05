package es.aviferdev.trackfolio.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.usecase.debt.DeleteDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.GetActiveDebtsUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.MarkDebtAsPaidUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.SaveDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.UpdateDebtUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class DebtUiState(
    val debtsTheyOwe: List<Debt> = emptyList(),
    val debtsIOwe: List<Debt>    = emptyList(),
    val totalTheyOwe: Double     = 0.0,
    val totalIOwe: Double        = 0.0,
    val isLoading: Boolean       = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class DebtViewModel(
    getActiveDebts: GetActiveDebtsUseCase,
    private val markDebtAsPaid: MarkDebtAsPaidUseCase,
    private val saveDebt: SaveDebtUseCase,
    private val updateDebtUseCase: UpdateDebtUseCase,
    private val deleteDebtUseCase: DeleteDebtUseCase,
    private val session: AccountSession
) : ViewModel() {

    val uiState: StateFlow<DebtUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) {
                flowOf(DebtUiState(isLoading = false))
            } else {
                getActiveDebts(accountId).map { debts ->
                    val theyOwe = debts.filter { it.direction == DebtDirection.THEY_OWE }
                    val iOwe    = debts.filter { it.direction == DebtDirection.I_OWE }
                    DebtUiState(
                        debtsTheyOwe = theyOwe,
                        debtsIOwe    = iOwe,
                        totalTheyOwe = theyOwe.sumOf { it.amount },
                        totalIOwe    = iOwe.sumOf { it.amount },
                        isLoading    = false
                    )
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = DebtUiState()
        )

    fun markAsPaid(id: String) {
        viewModelScope.launch { markDebtAsPaid(id) }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch { deleteDebtUseCase(id) }
    }

    fun editDebt(original: Debt, personName: String, amount: Double, direction: DebtDirection, notes: String?) {
        viewModelScope.launch {
            updateDebtUseCase(original.copy(
                personName = personName,
                amount     = amount,
                direction  = direction,
                notes      = notes
            ))
        }
    }

    fun saveDebt(personName: String, amount: Double, direction: DebtDirection, notes: String?) {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val debt = Debt(
                id         = generateId(),
                accountId  = accountId,
                personName = personName,
                amount     = amount,
                direction  = direction,
                date       = now,
                isPaid     = false,
                notes      = notes,
                createdAt  = now
            )
            saveDebt(debt)
        }
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return "debt_" + (1..27).map { chars.random() }.joinToString("")
    }
}
