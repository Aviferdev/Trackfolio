package es.aviferdev.trackfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.data.database.CategoryEntity
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class CategoryListUiState(
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val showAddSheet: Boolean                   = false,
    val editing: CategoryEntity?                = null,
    val pendingDelete: CategoryEntity?          = null,
    val error: String?                          = null
)

class CategoryViewModel(
    private val dataSource: TransactionCategoryLocalDataSource
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _editing       = MutableStateFlow<CategoryEntity?>(null)
    private val _pendingDelete = MutableStateFlow<CategoryEntity?>(null)
    private val _error         = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryListUiState> = combine(
        dataSource.getByType(TransactionType.EXPENSE.name),
        combine(_showAddSheet, _editing, _pendingDelete, _error) { s, e, p, err ->
            Quadruple(s, e, p, err)
        }
    ) { expense, q ->
        CategoryListUiState(
            expenseCategories = expense,
            showAddSheet      = q.a,
            editing           = q.b,
            pendingDelete     = q.c,
            error             = q.d
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryListUiState()
    )

    // ── Añadir ────────────────────────────────────────────────────────────────
    fun openAddSheet() {
        _showAddSheet.value = true
    }

    fun closeAddSheet() { _showAddSheet.value = false }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        if (uiState.value.expenseCategories.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = "Ya existe una categoría con ese nombre"
            return
        }

        viewModelScope.launch {
            val id = "cat_expense_${Clock.System.now().toEpochMilliseconds()}"
            dataSource.insert(
                CategoryEntity(
                    id        = id,
                    name      = trimmed,
                    type      = TransactionType.EXPENSE.name,
                    isDefault = 0L,
                    archived  = 0L
                )
            ).onFailure { _error.value = it.message }
            _showAddSheet.value = false
        }
    }

    // ── Editar nombre ─────────────────────────────────────────────────────────
    fun openEditSheet(category: CategoryEntity) { _editing.value = category }
    fun closeEditSheet()                        { _editing.value = null }

    fun renameCategory(id: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        if (uiState.value.expenseCategories.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = "Ya existe una categoría con ese nombre"
            return
        }

        viewModelScope.launch {
            dataSource.updateName(id, trimmed).onFailure { _error.value = it.message }
            _editing.value = null
        }
    }

    // ── Eliminar (soft) ───────────────────────────────────────────────────────
    fun requestDelete(category: CategoryEntity) { _pendingDelete.value = category }
    fun cancelDelete()                          { _pendingDelete.value = null }

    fun confirmDelete() {
        val cat = _pendingDelete.value ?: return
        viewModelScope.launch {
            dataSource.archive(cat.id).onFailure { _error.value = it.message }
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }

    private data class Quadruple<A, B, C, D>(
        val a: A, val b: B, val c: C, val d: D
    )
}
