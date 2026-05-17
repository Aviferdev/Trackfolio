package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountConfigUiState(
    val account: Account? = null,
    val reconciliationInterval: Int = 0,
    val showEditSheet: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountConfigViewModel(
    private val accountId: String,
    private val accountRepository: AccountRepository,
    private val getReminderInterval: GetReconciliationReminderIntervalUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountConfigUiState> = accountRepository.getAccountById(accountId)
        .map { account ->
            val interval = if (account != null) getReminderInterval.get(accountId) else 0
            AccountConfigUiState(
                account = account,
                reconciliationInterval = interval
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountConfigUiState())

    private val _showEditSheet = MutableStateFlow(false)
    private val _showDeleteConfirm = MutableStateFlow(false)

    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    fun updateReconciliationInterval(days: Int) {
        getReminderInterval.set(accountId, days)
    }

    fun openEditSheet() { _showEditSheet.value = true }
    fun closeEditSheet() { _showEditSheet.value = false }

    fun requestDelete() { _showDeleteConfirm.value = true }
    fun cancelDelete() { _showDeleteConfirm.value = false }
    fun confirmDelete() {
        viewModelScope.launch {
            accountRepository.deleteAccount(accountId)
            _showDeleteConfirm.value = false
        }
    }
}
