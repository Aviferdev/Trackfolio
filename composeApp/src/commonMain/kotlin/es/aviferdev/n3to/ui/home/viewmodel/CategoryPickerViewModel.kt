package es.aviferdev.n3to.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

// ─── UI State ──────────────────────────────────────────────────────────────────

data class CategoryPickerUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val frequentCategories: List<Category> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val incomeTypes: List<IncomeType> = emptyList(),
    val searchQuery: String = "",
    val filteredCategoryIndices: List<Int> = emptyList(), // indices into allCategories
    val isLoading: Boolean = true,
)

// ─── ViewModel ─────────────────────────────────────────────────────────────────

class CategoryPickerViewModel(
    private val initialTypeName: String,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val initialType: TransactionType =
        try {
            TransactionType.valueOf(initialTypeName)
        } catch (_: IllegalArgumentException) {
            TransactionType.EXPENSE
        }

    private val _uiState = MutableStateFlow(CategoryPickerUiState())
    val uiState: StateFlow<CategoryPickerUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = CategoryPickerUiState(type = initialType)
        loadCategories(initialType)
    }

    private fun loadCategories(type: TransactionType) {
        if (type == TransactionType.INCOME) {
            val types = IncomeType.entries.filter {
                it != IncomeType.DIVIDEND &&
                        it != IncomeType.BOND_DEPOSIT &&
                        it != IncomeType.BONUS_PRIZE &&
                        it != IncomeType.RENTAL_INCOME
            }
            _uiState.value = CategoryPickerUiState(
                type = type,
                incomeTypes = types,
                isLoading = false,
            )
        } else {
            val accountId = session.selectedAccountId.value ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return
            }
            viewModelScope.launch {
                getCategoriesByType(accountId, type)
                    .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                    .collect { categories ->
                        val freq = categories.take(4)
                        val all = categories
                        _uiState.value = CategoryPickerUiState(
                            type = type,
                            frequentCategories = freq,
                            allCategories = all,
                            filteredCategoryIndices = all.indices.toList(),
                            isLoading = false,
                        )
                    }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val state = _uiState.value
        val filtered = if (query.isBlank()) {
            state.allCategories.indices.toList()
        } else {
            state.allCategories.indices.filter { i ->
                state.allCategories[i].name.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = state.copy(
            searchQuery = query,
            filteredCategoryIndices = filtered
        )
    }
}
