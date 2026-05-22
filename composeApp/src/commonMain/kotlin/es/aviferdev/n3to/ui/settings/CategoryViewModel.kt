package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.DeleteCategoryBudgetUseCase
import es.aviferdev.n3to.domain.usecase.category.DeleteCategoryUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoryBudgetUseCase
import es.aviferdev.n3to.domain.usecase.category.RenameCategoryUseCase
import es.aviferdev.n3to.domain.usecase.category.SaveCategoryBudgetUseCase
import es.aviferdev.n3to.domain.usecase.category.SaveCategoryUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CategoryError {
    data object AlreadyExists : CategoryError()
    data object InvalidName : CategoryError()
    data class Unknown(val message: String?) : CategoryError()
}

data class CategoryListUiState(
    val expenseCategories: List<Category> = emptyList(),
    val showAddSheet: Boolean = false,
    val editing: Category? = null,
    val editingLimit: Double = 0.0,
    val editingLimitType: LimitType = LimitType.FIXED,
    val showLimitSheet: Boolean = false,
    val limitSheetCategory: Category? = null,
    val limitSheetCurrentLimit: Double = 0.0,
    val limitSheetCurrentLimitType: LimitType = LimitType.FIXED,
    val pendingDelete: Category? = null,
    val error: CategoryError? = null
)

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModel(
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val saveCategory: SaveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val renameCategory: RenameCategoryUseCase,
    private val getCategoryBudget: GetCategoryBudgetUseCase,
    private val saveCategoryBudget: SaveCategoryBudgetUseCase,
    private val deleteCategoryBudget: DeleteCategoryBudgetUseCase,
    private val session: AccountSession,
) : ViewModel() {

    private val _showAddSheet = MutableStateFlow(false)
    private val _editing = MutableStateFlow<Category?>(null)
    private val _editingLimit = MutableStateFlow(0.0)
    private val _editingLimitType = MutableStateFlow(LimitType.FIXED)
    private val _showLimitSheet = MutableStateFlow(false)
    private val _limitSheetCategory = MutableStateFlow<Category?>(null)
    private val _limitSheetCurrentLimit = MutableStateFlow(0.0)
    private val _limitSheetCurrentLimitType = MutableStateFlow(LimitType.FIXED)
    private val _pendingDelete = MutableStateFlow<Category?>(null)
    private val _error = MutableStateFlow<CategoryError?>(null)

    private val editingSheetState =
        combine(_editing, _editingLimit, _editingLimitType) { ed, lim, limType ->
            Triple(ed, lim, limType)
        }

    private val limitSheetState = combine(
        _showLimitSheet, _limitSheetCategory, _limitSheetCurrentLimit, _limitSheetCurrentLimitType
    ) { show, cat, lim, limType ->
        Quadruple(show, cat, lim, limType)
    }

    private val dialogState = combine(_showAddSheet, _pendingDelete, _error) { s, d, e ->
        Triple(s, d, e)
    }

    val uiState: StateFlow<CategoryListUiState> = combine(
        session.selectedAccountId.flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList<Category>())
            else getCategoriesByType(accountId, TransactionType.EXPENSE)
        },
        editingSheetState,
        limitSheetState,
        dialogState
    ) { expenseCategories, (editing, editLimit, editLimitType), (showLS, lsCat, lsLim, lsLimType), (showAdd, pendingDel, error) ->
        CategoryListUiState(
            expenseCategories = expenseCategories,
            showAddSheet = showAdd,
            editing = editing,
            editingLimit = editLimit,
            editingLimitType = editLimitType,
            showLimitSheet = showLS,
            limitSheetCategory = lsCat,
            limitSheetCurrentLimit = lsLim,
            limitSheetCurrentLimitType = lsLimType,
            pendingDelete = pendingDel,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryListUiState()
    )

    // ── Añadir ────────────────────────────────────────────────────────────────
    fun openAddSheet() {
        _showAddSheet.value = true
    }

    fun closeAddSheet() {
        _showAddSheet.value = false
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val accountId = session.selectedAccountId.value ?: return

        if (uiState.value.expenseCategories.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = CategoryError.AlreadyExists
            return
        }

        viewModelScope.launch {
            saveCategory(
                Category(
                    id = "cat_expense_${nowMillis()}",
                    accountId = accountId,
                    name = trimmed,
                    type = TransactionType.EXPENSE,
                    isDefault = false
                )
            ).onFailure { _error.value = CategoryError.Unknown(it.message) }
            _showAddSheet.value = false
        }
    }

    // ── Editar nombre + límite (sheet completo) ───────────────────────────────
    fun openEditSheet(category: Category) {
        _editing.value = category
        viewModelScope.launch {
            getCategoryBudget(category.id).collect { budget ->
                _editingLimit.value = budget?.annualLimit ?: 0.0
                _editingLimitType.value = try {
                    if (budget != null) LimitType.valueOf(budget.limitType) else LimitType.FIXED
                } catch (_: IllegalArgumentException) {
                    LimitType.FIXED
                }
                return@collect
            }
        }
    }

    fun closeEditSheet() {
        _editing.value = null
        _editingLimit.value = 0.0
        _editingLimitType.value = LimitType.FIXED
    }

    fun renameAndUpdateLimit(
        id: String,
        newName: String,
        annualLimit: Double,
        limitType: LimitType
    ) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        if (uiState.value.expenseCategories.any {
                it.id != id && it.name.equals(trimmed, ignoreCase = true)
            }) {
            _error.value = CategoryError.AlreadyExists
            return
        }

        viewModelScope.launch {
            renameCategory(id, trimmed)
                .onFailure { _error.value = CategoryError.Unknown(it.message) }
            saveOrDeleteBudget(id, annualLimit, limitType)
            _editing.value = null
            _editingLimit.value = 0.0
            _editingLimitType.value = LimitType.FIXED
        }
    }

    // ── Limit sheet inline (solo límite, sin nombre) ───────────────────────────
    fun openLimitSheet(category: Category) {
        _limitSheetCategory.value = category
        _showLimitSheet.value = true
        viewModelScope.launch {
            getCategoryBudget(category.id).collect { budget ->
                _limitSheetCurrentLimit.value = budget?.annualLimit ?: 0.0
                _limitSheetCurrentLimitType.value = try {
                    if (budget != null) LimitType.valueOf(budget.limitType) else LimitType.FIXED
                } catch (_: IllegalArgumentException) {
                    LimitType.FIXED
                }
                return@collect
            }
        }
    }

    fun closeLimitSheet() {
        _showLimitSheet.value = false
        _limitSheetCategory.value = null
        _limitSheetCurrentLimit.value = 0.0
        _limitSheetCurrentLimitType.value = LimitType.FIXED
    }

    fun setCategoryLimit(categoryId: String, annualLimit: Double, limitType: LimitType) {
        viewModelScope.launch {
            saveOrDeleteBudget(categoryId, annualLimit, limitType)
            closeLimitSheet()
        }
    }

    // ── Eliminar (soft) ───────────────────────────────────────────────────────
    fun requestDelete(category: Category) {
        _pendingDelete.value = category
    }

    fun cancelDelete() {
        _pendingDelete.value = null
    }

    fun confirmDelete() {
        val cat = _pendingDelete.value ?: return
        viewModelScope.launch {
            deleteCategory(cat.id)
                .onFailure { _error.value = CategoryError.Unknown(it.message) }
            deleteCategoryBudget(cat.id)
            _pendingDelete.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private suspend fun saveOrDeleteBudget(id: String, annualLimit: Double, limitType: LimitType) {
        if (annualLimit > 0.0) {
            saveCategoryBudget(id, annualLimit, limitType)
                .onFailure { _error.value = CategoryError.Unknown(it.message) }
        } else {
            deleteCategoryBudget(id)
                .onFailure { _error.value = CategoryError.Unknown(it.message) }
        }
    }
}
