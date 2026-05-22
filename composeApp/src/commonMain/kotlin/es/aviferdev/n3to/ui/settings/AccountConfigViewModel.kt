package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.usecase.account.DeleteAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.account.UpdateAccountUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountConfigUiState(
    val account: Account? = null,
    val reconciliationInterval: Int = 0,
    val showEditSheet: Boolean = false,
    val deleted: Boolean = false
)

class AccountConfigViewModel(
    private val accountId: String,
    private val getAccountById: GetAccountByIdUseCase,
    private val getReminderInterval: GetReconciliationReminderIntervalUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountConfigUiState> = getAccountById(accountId)
        .map { account ->
            val interval = if (account != null) getReminderInterval.get(accountId) else 0
            AccountConfigUiState(
                account = account,
                reconciliationInterval = interval
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountConfigUiState())

    private val _showEditSheet = MutableStateFlow(false)
    private val _deleted = MutableStateFlow(false)

    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun updateReconciliationInterval(days: Int) {
        getReminderInterval.set(accountId, days)
    }

    fun openEditSheet() {
        _showEditSheet.value = true
    }

    fun closeEditSheet() {
        _showEditSheet.value = false
    }

    fun editAccount(account: Account, newName: String) {
        viewModelScope.launch {
            updateAccount(account.copy(name = newName))
            closeEditSheet()
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            deleteAccount(accountId)
            closeEditSheet()
            _deleted.value = true
        }
    }
}
