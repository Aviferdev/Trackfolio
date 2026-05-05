package es.aviferdev.trackfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.data.database.CategoryEntity
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class CategoryListUiState(
    val incomeCategories: List<CategoryEntity>  = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val showAddSheet: Boolean                   = false,
    val addType: TransactionType                = TransactionType.EXPENSE,
    val editing: CategoryEntity?                = null,
    val pendingDelete: CategoryEntity?          = null,
    val error: String?                          = null
)

class CategoryViewModel(
    private val dataSource: CategoryLocalDataSource
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _addType       = MutableStateFlow(TransactionType.EXPENSE)
    private val _editing       = MutableStateFlow<CategoryEntity?>(null)
    private val _pendingDelete = MutableStateFlow<CategoryEntity?>(null)
    private val _error         = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryListUiState> = combine(
        // Listado mostrado en Ajustes: solo activas
        dataSource.getByType(TransactionType.INCOME.name),
        dataSource.getByType(TransactionType.EXPENSE.name),
        combine(_showAddSheet, _addType, _editing, _pendingDelete, _error) { s, t, e, p, err ->
            Quintuple(s, t, e, p, err)
        }
    ) { income, expense, q ->
        CategoryListUiState(
            incomeCategories  = income,
            expenseCategories = expense,
            showAddSheet      = q.a,
            addType           = q.b,
            editing           = q.c,
            pendingDelete     = q.d,
            error             = q.e
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryListUiState()
    )

    // ── Añadir ────────────────────────────────────────────────────────────────
    fun openAddSheet(type: TransactionType) {
        _addType.value      = type
        _showAddSheet.value = true
    }

    fun closeAddSheet() { _showAddSheet.value = false }

    fun addCategory(name: String, type: TransactionType) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        // Evitar duplicados (case-insensitive) entre activas del mismo tipo
        val existing = if (type == TransactionType.INCOME)
            uiState.value.incomeCategories else uiState.value.expenseCategories
        if (existing.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = "Ya existe una categoría con ese nombre"
            return
        }

        viewModelScope.launch {
            val id = "cat_${type.name.lowercase()}_${Clock.System.now().toEpochMilliseconds()}"
            dataSource.insert(
                CategoryEntity(
                    id        = id,
                    name      = trimmed,
                    type      = type.name,
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

        val type = _editing.value?.type ?: return
        val list = if (type == TransactionType.INCOME.name)
            uiState.value.incomeCategories else uiState.value.expenseCategories
        if (list.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
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

    // Helper privado para combinar 5 flows
    private data class Quintuple<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )
}
