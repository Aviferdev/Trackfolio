package es.aviferdev.n3to.ui.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.usecase.reconciliation.BalanceAlreadyMatchesException
import es.aviferdev.n3to.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.ReconcileBalanceUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.ShouldShowReconciliationReminderUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.theme.formatAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReconciliationError {
    data object InvalidAmount : ReconciliationError()
    data object AlreadyBalanced : ReconciliationError()
    data class Generic(val message: String?) : ReconciliationError()
    data class Success(val formattedMessage: String) : ReconciliationError()
}

data class ReconciliationUiState(
    val showBanner: Boolean = false,
    val showBottomSheet: Boolean = false,
    val computedBalance: Double = 0.0,
    val realBalanceInput: String = "",
    val isProcessing: Boolean = false,
    val resultMessage: ReconciliationError? = null,
    val isSuccess: Boolean = false
)

class ReconciliationViewModel(
    private val reconcileBalance: ReconcileBalanceUseCase,
    private val shouldShowReminder: ShouldShowReconciliationReminderUseCase,
    private val getReminderInterval: GetReconciliationReminderIntervalUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReconciliationUiState())
    val uiState: StateFlow<ReconciliationUiState> = _uiState.asStateFlow()

    fun checkReminder(accountId: String) {
        _uiState.value = _uiState.value.copy(
            showBanner = shouldShowReminder(accountId)
        )
    }

    fun openBottomSheet(computedBalance: Double) {
        _uiState.value = _uiState.value.copy(
            showBottomSheet = true,
            computedBalance = computedBalance,
            realBalanceInput = "",
            resultMessage = null,
            isSuccess = false,
            isProcessing = false
        )
    }

    fun closeBottomSheet() {
        _uiState.value = _uiState.value.copy(
            showBottomSheet = false,
            resultMessage = null
        )
    }

    fun dismissBanner() {
        _uiState.value = _uiState.value.copy(showBanner = false)
    }

    fun updateRealBalance(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
        _uiState.value = _uiState.value.copy(realBalanceInput = filtered)
    }

    fun reconcile() {
        val accountId = session.selectedAccountId.value ?: return
        val current = _uiState.value

        val realBalance = current.realBalanceInput
            .replace(',', '.')
            .toDoubleOrNull()

        if (realBalance == null) {
            _uiState.value = current.copy(
                resultMessage = ReconciliationError.InvalidAmount,
                isSuccess = false
            )
            return
        }

        _uiState.value = current.copy(isProcessing = true)

        viewModelScope.launch {
            val result = reconcileBalance(
                accountId = accountId,
                computedBalance = current.computedBalance,
                realBalance = realBalance
            )

            result.fold(
                onSuccess = { tx ->
                    val diff = realBalance - current.computedBalance
                    val sign = if (diff > 0) "+" else ""
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        resultMessage = ReconciliationError.Success("Ajuste de ${sign}${formatAmount(diff)}€ registrado"),
                        isSuccess = true,
                        showBanner = false
                    )
                },
                onFailure = { error ->
                    val resultError = when (error) {
                        is BalanceAlreadyMatchesException -> ReconciliationError.AlreadyBalanced
                        else -> ReconciliationError.Generic(error.message)
                    }
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        resultMessage = resultError,
                        isSuccess = error is BalanceAlreadyMatchesException
                    )
                }
            )
        }
    }

    fun getReminderIntervalDays(accountId: String): Int = getReminderInterval.get(accountId)

    fun setReminderIntervalDays(accountId: String, days: Int) {
        getReminderInterval.set(accountId, days)
        checkReminder(accountId)
    }
}
