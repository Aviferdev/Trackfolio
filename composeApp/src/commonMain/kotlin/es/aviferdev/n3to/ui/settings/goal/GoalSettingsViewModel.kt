package es.aviferdev.n3to.ui.settings.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.repository.GoalRepository
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class MonthGoalUi(
    val month: String,
    val monthLabel: String,
    val savingsText: String = "",
    val investmentText: String = "",
    val isCustomized: Boolean = false
)

data class GoalSettingsUiState(
    val year: String = currentYear(),
    val baseSavingsText: String = "",
    val baseInvestmentText: String = "",
    val months: List<MonthGoalUi> = emptyMonths(),
    val showMonthDetails: Boolean = false,
    val isLoading: Boolean = true,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
) {
    val canGoPrevious: Boolean
        get() = (year.toIntOrNull() ?: currentYear().toInt()) < currentYear().toInt()
    val customizedCount: Int get() = months.count { it.isCustomized }
}

private fun currentYear(): String =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year.toString()

private fun emptyMonths(): List<MonthGoalUi> =
    monthLabels.mapIndexed { index, label ->
        MonthGoalUi(
            month = (index + 1).toString().padStart(2, '0'),
            monthLabel = label
        )
    }

private val monthLabels = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

class GoalSettingsViewModel(
    private val goalRepository: GoalRepository,
    private val session: AccountSession
) : ViewModel() {

    private val _state = MutableStateFlow(GoalSettingsUiState())
    val uiState: StateFlow<GoalSettingsUiState> = _state.asStateFlow()

    init {
        loadYear(currentYear())
    }

    /** Carga los datos de un año desde la BD. */
    private fun loadYear(year: String) {
        val accountId = session.selectedAccountId.value ?: return
        _state.value = _state.value.copy(year = year, isLoading = true)
        viewModelScope.launch {
            try {
                val base = goalRepository.getBaseGoal(accountId, year).first()
                val overrides = goalRepository.getOverrides(accountId, year).first()
                val overridesByMonth = overrides.associateBy { it.month }

                val months = monthLabels.mapIndexed { index, label ->
                    val month = (index + 1).toString().padStart(2, '0')
                    val ov = overridesByMonth[month]
                    if (ov != null) {
                        MonthGoalUi(
                            month = month,
                            monthLabel = label,
                            savingsText = formatAmount(ov.savingsTarget),
                            investmentText = formatAmount(ov.investmentTarget),
                            isCustomized = true
                        )
                    } else {
                        MonthGoalUi(month = month, monthLabel = label)
                    }
                }

                _state.value = GoalSettingsUiState(
                    year = year,
                    baseSavingsText = formatAmount(base?.savingsTarget ?: 0.0),
                    baseInvestmentText = formatAmount(base?.investmentTarget ?: 0.0),
                    months = months,
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

    // ── Acciones base ─────────────────────────────────────────────────────────

    fun onBaseSavingsChange(value: String) {
        _state.value = _state.value.copy(baseSavingsText = value, isDirty = true)
    }

    fun onBaseInvestmentChange(value: String) {
        _state.value = _state.value.copy(baseInvestmentText = value, isDirty = true)
    }

    // ── Acciones meses ────────────────────────────────────────────────────────

    fun toggleMonthDetails() {
        _state.value = _state.value.copy(showMonthDetails = !_state.value.showMonthDetails)
    }

    private fun updateMonth(month: String, transform: (MonthGoalUi) -> MonthGoalUi) {
        val current = _state.value
        val updated = current.months.map { m ->
            if (m.month == month) transform(m) else m
        }
        _state.value = current.copy(months = updated, isDirty = true)
    }

    fun onMonthSavingsChange(month: String, value: String) {
        updateMonth(month) { it.copy(savingsText = value, isCustomized = true) }
    }

    fun onMonthInvestmentChange(month: String, value: String) {
        updateMonth(month) { it.copy(investmentText = value, isCustomized = true) }
    }

    fun resetMonth(month: String) {
        updateMonth(month) { it.copy(savingsText = "", investmentText = "", isCustomized = false) }
    }

    // ── Año ───────────────────────────────────────────────────────────────────

    fun previousYear() {
        val current = _state.value.year.toIntOrNull() ?: return
        loadYear((current - 1).toString())
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    fun save() {
        val accountId = session.selectedAccountId.value ?: return
        val state = _state.value
        _state.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val baseSavings = parseAmount(state.baseSavingsText)
                val baseInvestment = parseAmount(state.baseInvestmentText)

                val baseGoal = if (baseSavings > 0.0 || baseInvestment > 0.0) {
                    MonthlyGoal(
                        accountId = accountId,
                        year = state.year,
                        month = "00",
                        savingsTarget = baseSavings,
                        investmentTarget = baseInvestment
                    )
                } else null

                val overrides = state.months
                    .filter { it.isCustomized }
                    .mapNotNull { m ->
                        val savings = parseAmount(m.savingsText)
                        val investment = parseAmount(m.investmentText)
                        if (savings > 0.0 || investment > 0.0) {
                            MonthlyGoal(
                                accountId = accountId,
                                year = state.year,
                                month = m.month,
                                savingsTarget = savings,
                                investmentTarget = investment
                            )
                        } else null
                    }

                goalRepository.saveBaseAndOverrides(accountId, state.year, baseGoal, overrides)
                _state.value = _state.value.copy(
                    isDirty = false,
                    isSaving = false,
                    message = "Objetivos guardados"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, message = "Error: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    companion object {
        private fun formatAmount(amount: Double): String =
            if (amount == 0.0) "" else String.format("%.2f", amount).replace(".", ",")

        private fun parseAmount(text: String): Double =
            text.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }
}
