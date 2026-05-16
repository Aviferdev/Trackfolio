package es.aviferdev.n3to.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.AccountType
import es.aviferdev.n3to.domain.model.PremiumConstants
import es.aviferdev.n3to.domain.usecase.account.DeleteAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.n3to.domain.usecase.account.SaveAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.n3to.domain.usecase.account.UpdateAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class AccountUiState(
    val accounts: List<Account>     = emptyList(),
    val isLoading: Boolean          = false,
    val error: String?              = null,
    val showAddSheet: Boolean       = false,
    val showEditSheet: Boolean      = false,
    val editingAccount: Account?    = null,
    val showDeleteConfirm: Boolean  = false,
    val accountToDelete: Account?   = null,
    val pendingInitialBalanceAccount: Account? = null,
    val showPremiumLimitWarning: Boolean = false
)

class AccountViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val saveAccount: SaveAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val setInitialBalance: SetInitialBalanceUseCase,
    private val session: AccountSession,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    val selectedAccountId: StateFlow<String?> = session.selectedAccountId

    init {
        observeAccounts()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
                if (session.selectedAccountId.value == null && accounts.isNotEmpty()) {
                    val ready = accounts.firstOrNull { !it.needsInitialBalance }
                    ready?.let { session.selectAccount(it.id) }
                }
                val currentId = session.selectedAccountId.value
                if (currentId != null && accounts.none { it.id == currentId }) {
                    val ready = accounts.firstOrNull { !it.needsInitialBalance }
                    ready?.let { session.selectAccount(it.id) } ?: session.clearSelection()
                }
            }
        }
    }

    fun selectAccount(id: String) {
        val account = _uiState.value.accounts.find { it.id == id } ?: return
        if (account.needsInitialBalance) {
            _uiState.value = _uiState.value.copy(pendingInitialBalanceAccount = account)
        } else {
            session.selectAccount(id)
        }
    }

    fun selectAccount() {
        val account = _uiState.value.accounts.find { it.id == session.selectedAccountId.value } ?: return
        if (account.needsInitialBalance) {
            _uiState.value = _uiState.value.copy(pendingInitialBalanceAccount = account)
        } else {
            session.selectedAccountId.value?.let {
                session.selectAccount(it)
            }
        }
    }

    fun openAddSheet() {
        if (!premiumManager.status.value.isPremium &&
            _uiState.value.accounts.size >= PremiumConstants.MAX_FREE_ACCOUNTS
        ) {
            _uiState.value = _uiState.value.copy(showPremiumLimitWarning = true)
        } else {
            _uiState.value = _uiState.value.copy(showAddSheet = true)
        }
    }

    fun closeAddSheet() {
        _uiState.value = _uiState.value.copy(showAddSheet = false)
    }

    fun dismissPremiumLimitWarning() {
        _uiState.value = _uiState.value.copy(showPremiumLimitWarning = false)
    }

    fun openEditSheet(account: Account) {
        _uiState.value = _uiState.value.copy(showEditSheet = true, editingAccount = account)
    }

    fun closeEditSheet() {
        _uiState.value = _uiState.value.copy(showEditSheet = false, editingAccount = null)
    }

    fun requestDelete(account: Account) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, accountToDelete = account)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false, accountToDelete = null)
    }

    fun addAccount(name: String, accountType: AccountType = AccountType.GENERAL) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val newAccount = Account(
                id              = generateId(),
                name            = name,
                initialBalance  = 0.0,
                computedBalance = 0.0,
                createdAt       = Clock.System.now().toEpochMilliseconds(),
                accountType     = accountType
            )
            saveAccount(newAccount)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        showAddSheet = false,
                        pendingInitialBalanceAccount = newAccount
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun confirmInitialBalance(amount: Double) {
        val account = _uiState.value.pendingInitialBalanceAccount ?: return
        viewModelScope.launch {
            setInitialBalance(account.id, amount)
            session.selectAccount(account.id)
            _uiState.value = _uiState.value.copy(pendingInitialBalanceAccount = null)
        }
    }

    fun editAccount(account: Account, newName: String, accountType: AccountType? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val updated = account.copy(
                name        = newName,
                accountType = accountType ?: account.accountType
            )
            updateAccount(updated)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, showEditSheet = false, editingAccount = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun confirmDelete() {
        val account = _uiState.value.accountToDelete ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            deleteAccount(account.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, showDeleteConfirm = false, accountToDelete = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return "acc_" + (1..28).map { chars.random() }.joinToString("")
    }
}
