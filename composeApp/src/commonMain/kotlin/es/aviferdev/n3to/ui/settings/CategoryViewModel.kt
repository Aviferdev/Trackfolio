package es.aviferdev.n3to.ui.settings

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.data.database.CategoryEntity
import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CategoryError {
    data object AlreadyExists : CategoryError()
    data object InvalidName : CategoryError()
    data class Unknown(val message: String?) : CategoryError()
}

data class CategoryListUiState(
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val showAddSheet: Boolean                   = false,
    val editing: CategoryEntity?                = null,
    val editingLimit: Double                    = 0.0,
    val editingLimitType: LimitType             = LimitType.FIXED,
    val pendingDelete: CategoryEntity?          = null,
    val error: CategoryError?                   = null
)

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModel(
    private val dataSource: TransactionCategoryLocalDataSource,
    private val session: AccountSession,
    private val budgetRepository: CategoryBudgetRepository
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _editing       = MutableStateFlow<CategoryEntity?>(null)
    private val _editingLimit  = MutableStateFlow(0.0)
    private val _editingLimitType = MutableStateFlow(LimitType.FIXED)
    private val _pendingDelete = MutableStateFlow<CategoryEntity?>(null)
    private val _error         = MutableStateFlow<CategoryError?>(null)

    private val editingSheetState = combine(_editing, _editingLimit, _editingLimitType) { ed, lim, limType ->
        Triple(ed, lim, limType)
    }

    private val dialogState = combine(_showAddSheet, _pendingDelete, _error) { s, d, e ->
        Triple(s, d, e)
    }

    val uiState: StateFlow<CategoryListUiState> = combine(
        session.selectedAccountId.flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList<CategoryEntity>())
            else dataSource.getByTypeAndAccount(accountId, TransactionType.EXPENSE.name)
        },
        editingSheetState,
        dialogState
    ) { expenseCategories, (editing, editLimit, editLimitType), (showAdd, pendingDel, error) ->
        CategoryListUiState(
            expenseCategories = expenseCategories,
            showAddSheet      = showAdd,
            editing           = editing,
            editingLimit      = editLimit,
            editingLimitType  = editLimitType,
            pendingDelete     = pendingDel,
            error             = error
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

        val accountId = session.selectedAccountId.value ?: return

        if (uiState.value.expenseCategories.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = CategoryError.AlreadyExists
            return
        }

        viewModelScope.launch {
            val id = "cat_expense_${nowMillis()}"
            dataSource.insert(
                CategoryEntity(
                    id        = id,
                    accountId = accountId,
                    name      = trimmed,
                    type      = TransactionType.EXPENSE.name,
                    isDefault = 0L,
                    archived  = 0L
                )
            ).onFailure { _error.value = CategoryError.Unknown(it.message) }
            _showAddSheet.value = false
        }
    }

    // ── Editar nombre + límite ─────────────────────────────────────────────────
    fun openEditSheet(category: CategoryEntity) {
        _editing.value = category
        // Cargar límite existente si lo hay (una sola vez)
        viewModelScope.launch {
            budgetRepository.getBudgetByCategoryId(category.id).collect { budget ->
                _editingLimit.value = budget?.annualLimit ?: 0.0
                _editingLimitType.value = try {
                    if (budget != null) LimitType.valueOf(budget.limitType) else LimitType.FIXED
                } catch (_: IllegalArgumentException) {
                    LimitType.FIXED
                }
                // Dejar de recolectar tras obtener el primer valor
                if (budget != null || true) return@collect
            }
        }
    }

    fun closeEditSheet() {
        _editing.value = null
        _editingLimit.value = 0.0
        _editingLimitType.value = LimitType.FIXED
    }

    fun renameAndUpdateLimit(id: String, newName: String, annualLimit: Double, limitType: LimitType) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        if (uiState.value.expenseCategories.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = CategoryError.AlreadyExists
            return
        }

        viewModelScope.launch {
            // Actualizar nombre
            dataSource.updateName(id, trimmed).onFailure { _error.value = CategoryError.Unknown(it.message) }

            // Guardar o eliminar límite
            if (annualLimit > 0.0) {
                budgetRepository.saveBudget(id, annualLimit, limitType)
                    .onFailure { _error.value = CategoryError.Unknown(it.message) }
            } else {
                budgetRepository.deleteBudget(id)
                    .onFailure { _error.value = CategoryError.Unknown(it.message) }
            }

            _editing.value = null
            _editingLimit.value = 0.0
            _editingLimitType.value = LimitType.FIXED
        }
    }

    // ── Eliminar (soft) ───────────────────────────────────────────────────────
    fun requestDelete(category: CategoryEntity) { _pendingDelete.value = category }
    fun cancelDelete()                          { _pendingDelete.value = null }

    fun confirmDelete() {
        val cat = _pendingDelete.value ?: return
        viewModelScope.launch {
            dataSource.archive(cat.id).onFailure { _error.value = CategoryError.Unknown(it.message) }
            budgetRepository.deleteBudget(cat.id)
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }
}
