package es.aviferdev.n3to.ui.settings.emergencyfund

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.GetEmergencyFundUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.SaveEmergencyFundUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EmergencyFundSettingsUiState(
    val monthsText: String = "",
    val method: EmergencyFundMethod = EmergencyFundMethod.MANUAL,
    val manualExpenseText: String = "",
    val expenseCategories: List<Category> = emptyList(),
    val excludedCategoryIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val message: String? = null
) {
    val isValid: Boolean
        get() {
            val months = monthsText.trim().toIntOrNull() ?: 0
            if (months <= 0) return false
            return when (method) {
                EmergencyFundMethod.MANUAL -> {
                    val expense = manualExpenseText.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                    expense > 0.0
                }
                EmergencyFundMethod.AUTO -> true
            }
        }
}

class EmergencyFundSettingsViewModel(
    private val getEmergencyFund: GetEmergencyFundUseCase,
    private val saveEmergencyFund: SaveEmergencyFundUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _state = MutableStateFlow(EmergencyFundSettingsUiState())
    val uiState: StateFlow<EmergencyFundSettingsUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val accountId = session.selectedAccountId.value ?: return
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Cargar configuración actual
                val fund = getEmergencyFund(accountId).first()

                // Cargar categorías de gasto para la exclusión
                val categories = getCategoriesByType(accountId, TransactionType.EXPENSE).first()

                _state.value = _state.value.copy(
                    monthsText = if (fund != null) fund.targetMonths.toString() else "",
                    method = fund?.calculationMethod ?: EmergencyFundMethod.MANUAL,
                    manualExpenseText = if (fund != null && fund.manualMonthlyExpense > 0.0)
                        formatAmount(fund.manualMonthlyExpense) else "",
                    excludedCategoryIds = fund?.excludedCategoryIds?.toSet() ?: emptySet(),
                    expenseCategories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Error al cargar: ${e.message}"
                )
            }
        }
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    fun onMonthsChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _state.value = _state.value.copy(monthsText = filtered, isDirty = true)
    }

    fun onMethodChange(method: EmergencyFundMethod) {
        _state.value = _state.value.copy(method = method, isDirty = true)
    }

    fun onManualExpenseChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == ',' || it == '.' }
        _state.value = _state.value.copy(manualExpenseText = filtered, isDirty = true)
    }

    fun toggleCategoryExclusion(categoryId: String) {
        val current = _state.value.excludedCategoryIds
        val updated = if (categoryId in current) current - categoryId else current + categoryId
        _state.value = _state.value.copy(excludedCategoryIds = updated, isDirty = true)
    }

    fun save() {
        val accountId = session.selectedAccountId.value ?: return
        val state = _state.value
        if (!state.isValid) return

        _state.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val months = state.monthsText.toInt()
                val manualExpense = if (state.method == EmergencyFundMethod.MANUAL) {
                    state.manualExpenseText.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                } else 0.0

                val fund = EmergencyFund(
                    accountId = accountId,
                    targetMonths = months,
                    calculationMethod = state.method,
                    manualMonthlyExpense = manualExpense,
                    excludedCategoryIds = state.excludedCategoryIds.toList()
                )

                saveEmergencyFund(fund)
                _state.value = _state.value.copy(
                    isDirty = false,
                    isSaving = false,
                    message = "Fondo de emergencia guardado"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    message = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun delete() {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            try {
                saveEmergencyFund(accountId) // sobrecarga que borra
                _state.value = EmergencyFundSettingsUiState(
                    expenseCategories = _state.value.expenseCategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Error: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private companion object {
        private fun formatAmount(amount: Double): String =
            if (amount == 0.0) "" else String.format("%.2f", amount).replace(".", ",")
    }
}
