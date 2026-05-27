package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.BiometricResult
import es.aviferdev.n3to.core.security.LanguageManager
import es.aviferdev.n3to.core.security.ThemeManager
import es.aviferdev.n3to.core.security.getSystemLanguage
import es.aviferdev.n3to.core.security.setPlatformLanguage
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.usecase.account.AccountOpenItems
import es.aviferdev.n3to.domain.usecase.account.ArchiveAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.CheckAccountCanBeArchivedUseCase
import es.aviferdev.n3to.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.n3to.domain.usecase.account.SaveAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.n3to.domain.usecase.account.UpdateAccountUseCase
import es.aviferdev.n3to.domain.usecase.backup.GetBackupReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.category.SeedDefaultCategoriesUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.settings.backup.BackupAction
import es.aviferdev.n3to.ui.settings.backup.BackupSheetState
import es.aviferdev.n3to.ui.settings.backup.BackupViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    val showAddSheet: Boolean = false,
    val showEditSheet: Boolean = false,
    val editingAccount: Account? = null,
    val showDeleteConfirm: Boolean = false,
    val accountToDelete: Account? = null,
    val archiveWarning: Pair<Account, AccountOpenItems>? = null,
    val biometricEnabled: Boolean = false,
    val biometricError: String? = null,
    val isDarkTheme: Boolean = true,
    val currentLanguage: String = "es",
    val isSystemDefault: Boolean = true,
    val showLanguageDialog: Boolean = false,
    val backupInterval: Int = 30,
    val backupSheetState: BackupSheetState = BackupSheetState()
)

class SettingsViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val saveAccount: SaveAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: ArchiveAccountUseCase,
    private val checkCanArchive: CheckAccountCanBeArchivedUseCase,
    private val setInitialBalance: SetInitialBalanceUseCase,
    private val seedCategories: SeedDefaultCategoriesUseCase,
    private val session: AccountSession,
    private val getBackupReminderInterval: GetBackupReminderIntervalUseCase,
    private val backupViewModel: BackupViewModel,
    private val authenticator: BiometricAuthenticator,
    private val lockManager: AppLockManager,
    private val themeManager: ThemeManager,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())
    private data class LocalState(
        val showAddSheet: Boolean = false,
        val showEditSheet: Boolean = false,
        val editingAccount: Account? = null,
        val showDeleteConfirm: Boolean = false,
        val accountToDelete: Account? = null,
        val archiveWarning: Pair<Account, AccountOpenItems>? = null,
        val biometricEnabled: Boolean = false,
        val biometricError: String? = null,
        val showLanguageDialog: Boolean = false
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            getAccounts(),
            session.selectedAccountId,
            themeManager.isDark
        ) { accounts, selectedId, isDark ->
            DataPart(accounts, selectedId, isDark)
        },
        combine(
            languageManager.languageCode,
            backupViewModel.state,
            _local
        ) { langCode, backupSheet, local ->
            ExtraPart(langCode, backupSheet, local)
        }
    ) { data, extra ->
        SettingsUiState(
            accounts = data.accounts,
            selectedAccountId = data.selectedId,
            isDarkTheme = data.isDark,
            currentLanguage = extra.langCode,
            isSystemDefault = languageManager.isSystemDefault,
            backupInterval = getBackupReminderInterval.get(),
            backupSheetState = extra.backupSheet,
            showAddSheet = extra.local.showAddSheet,
            showEditSheet = extra.local.showEditSheet,
            editingAccount = extra.local.editingAccount,
            showDeleteConfirm = extra.local.showDeleteConfirm,
            accountToDelete = extra.local.accountToDelete,
            archiveWarning = extra.local.archiveWarning,
            biometricEnabled = extra.local.biometricEnabled,
            biometricError = extra.local.biometricError,
            showLanguageDialog = extra.local.showLanguageDialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        _local.update { it.copy(biometricEnabled = lockManager.biometricEnabled) }
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                if (session.selectedAccountId.value == null && accounts.isNotEmpty()) {
                    val ready = accounts.firstOrNull { !it.needsInitialBalance } ?: accounts.firstOrNull()
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

    // ── Account ───────────────────────────────────────────────────────────────
    fun selectAccount(id: String) {
        val account = uiState.value.accounts.find { it.id == id } ?: return
        if (account.needsInitialBalance) return
        session.selectAccount(id)
    }

    fun openAddSheet() {
        _local.update { it.copy(showAddSheet = true) }
    }

    fun closeAddSheet() {
        _local.update { it.copy(showAddSheet = false) }
    }

    fun openEditSheet(account: Account) {
        _local.update { it.copy(showEditSheet = true, editingAccount = account) }
    }

    fun closeEditSheet() {
        _local.update { it.copy(showEditSheet = false, editingAccount = null) }
    }

    fun requestDelete(account: Account) {
        viewModelScope.launch {
            val openItems = checkCanArchive(account.id)
            if (openItems.isEmpty) {
                _local.update { it.copy(showDeleteConfirm = true, accountToDelete = account) }
            } else {
                _local.update { it.copy(archiveWarning = Pair(account, openItems)) }
            }
        }
    }

    fun cancelDelete() {
        _local.update { it.copy(showDeleteConfirm = false, accountToDelete = null) }
    }

    fun dismissArchiveWarning() {
        _local.update { it.copy(archiveWarning = null) }
    }

    fun confirmArchiveAnyway() {
        val account = _local.value.archiveWarning?.first ?: return
        _local.update { it.copy(archiveWarning = null, showDeleteConfirm = true, accountToDelete = account) }
    }

    fun addAccount(name: String, initialBalance: Double, currency: String = "EUR") {
        viewModelScope.launch {
            val newAccount = Account(
                id = "acc_" + (1..28).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString(""),
                name = name,
                initialBalance = 0.0,
                computedBalance = 0.0,
                createdAt = nowMillis(),
                currency = currency
            )
            saveAccount(newAccount)
                .onSuccess {
                    seedCategories(newAccount.id)
                    setInitialBalance(newAccount.id, initialBalance)
                    session.selectAccount(newAccount.id)
                    closeAddSheet()
                }
        }
    }

    fun editAccount(account: Account, newName: String) {
        viewModelScope.launch {
            updateAccount(account.copy(name = newName))
            closeEditSheet()
        }
    }

    fun confirmDelete() {
        val account = uiState.value.accountToDelete ?: return
        viewModelScope.launch {
            deleteAccount(account.id)
            _local.update { it.copy(showDeleteConfirm = false, accountToDelete = null) }
        }
    }

    // ── Biometric ─────────────────────────────────────────────────────────────
    fun toggleBiometric(
        enabled: Boolean,
        unavailableMsg: String,
        activateMsg: String,
        confirmMsg: String
    ) {
        if (enabled) {
            if (!authenticator.isAvailable()) {
                _local.update { it.copy(biometricError = unavailableMsg) }
            } else {
                authenticator.authenticate(activateMsg, confirmMsg) { result ->
                    when (result) {
                        is BiometricResult.Success -> {
                            lockManager.enableBiometric()
                            _local.update { it.copy(biometricEnabled = true) }
                        }
                        is BiometricResult.Error -> _local.update { it.copy(biometricError = result.message) }
                        else -> {}
                    }
                }
            }
        } else {
            lockManager.disableBiometric()
            _local.update { it.copy(biometricEnabled = false) }
        }
    }

    fun clearBiometricError() {
        _local.update { it.copy(biometricError = null) }
    }

    // ── Backup ────────────────────────────────────────────────────────────────
    fun setBackupInterval(days: Int) {
        getBackupReminderInterval.set(days)
    }

    fun openBackupExport() = backupViewModel.openExport()
    fun onBackupPasswordChange(value: String) = backupViewModel.onPasswordChange(value)
    fun onBackupConfirmPasswordChange(value: String) = backupViewModel.onConfirmPasswordChange(value)
    fun confirmBackupExport() = backupViewModel.confirmExport()
    fun confirmBackupImport() = backupViewModel.confirmImport()
    fun dismissBackupSheet() = backupViewModel.dismiss()
    fun clearBackupResult() = backupViewModel.clearResult()

    // ── Theme ─────────────────────────────────────────────────────────────────
    fun toggleTheme() {
        themeManager.set(!themeManager.isDark.value)
    }

    // ── Language ──────────────────────────────────────────────────────────────
    fun showLanguageDialog() {
        _local.update { it.copy(showLanguageDialog = true) }
    }

    fun dismissLanguageDialog() {
        _local.update { it.copy(showLanguageDialog = false) }
    }

    fun setLanguage(code: String) {
        val effectiveCode = code.ifEmpty { getSystemLanguage() }
        setPlatformLanguage(effectiveCode)
        languageManager.setLanguage(code)
        dismissLanguageDialog()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────
    private data class DataPart(
        val accounts: List<Account>,
        val selectedId: String?,
        val isDark: Boolean
    )

    private data class ExtraPart(
        val langCode: String,
        val backupSheet: BackupSheetState,
        val local: LocalState
    )
}
