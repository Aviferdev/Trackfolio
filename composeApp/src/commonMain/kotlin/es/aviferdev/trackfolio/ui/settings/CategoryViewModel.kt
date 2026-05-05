package es.aviferdev.trackfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.data.database.CategoryEntity
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class CategoryListUiState(
    val incomeCategories: List<CategoryEntity>  = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val showAddSheet: Boolean                   = false,
    val addType: TransactionType                = TransactionType.EXPENSE,
    val error: String?                          = null
)

class CategoryViewModel(
    private val dataSource: CategoryLocalDataSource
) : ViewModel() {

    private val _showAddSheet = MutableStateFlow(false)
    private val _addType      = MutableStateFlow(TransactionType.EXPENSE)
    private val _error        = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryListUiState> = combine(
        dataSource.getByType(TransactionType.INCOME.name),
        dataSource.getByType(TransactionType.EXPENSE.name),
        _showAddSheet,
        _addType,
        _error
    ) { income, expense, showAdd, addType, error ->
        CategoryListUiState(
            incomeCategories  = income,
            expenseCategories = expense,
            showAddSheet      = showAdd,
            addType           = addType,
            error             = error
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryListUiState()
    )

    fun openAddSheet(type: TransactionType) {
        _addType.value      = type
        _showAddSheet.value = true
    }

    fun closeAddSheet() { _showAddSheet.value = false }

    fun addCategory(name: String, type: TransactionType) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = "cat_${type.name.lowercase()}_${Clock.System.now().toEpochMilliseconds()}"
            dataSource.insert(
                CategoryEntity(
                    id        = id,
                    name      = name.trim(),
                    type      = type.name,
                    isDefault = 0L
                )
            ).onFailure { _error.value = it.message }
            _showAddSheet.value = false
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            dataSource.delete(id).onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}
