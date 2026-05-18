package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.usecase.assetcategory.ArchiveAssetCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAssetCategoriesUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.RenameAssetCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.SaveAssetCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AssetCategoryError {
    data object AlreadyExists : AssetCategoryError()
    data class Unknown(val message: String?) : AssetCategoryError()
}

data class AssetCategoryListUiState(
    val categories: List<AssetCategory>     = emptyList(),
    val showAddSheet: Boolean               = false,
    val editing: AssetCategory?             = null,
    val pendingDelete: AssetCategory?       = null,
    val error: AssetCategoryError?          = null
)

class AssetCategoryViewModel(
    private val getCategories: GetAssetCategoriesUseCase,
    private val saveCategory: SaveAssetCategoryUseCase,
    private val renameCategory: RenameAssetCategoryUseCase,
    private val archiveCategory: ArchiveAssetCategoryUseCase
) : ViewModel() {

    private val _showAddSheet  = MutableStateFlow(false)
    private val _editing       = MutableStateFlow<AssetCategory?>(null)
    private val _pendingDelete = MutableStateFlow<AssetCategory?>(null)
    private val _error         = MutableStateFlow<AssetCategoryError?>(null)

    val uiState: StateFlow<AssetCategoryListUiState> = combine(
        getCategories(),
        combine(_showAddSheet, _editing, _pendingDelete, _error) { s, e, p, err ->
            Quad(s, e, p, err)
        }
    ) { categories, q ->
        AssetCategoryListUiState(
            categories    = categories,
            showAddSheet  = q.a,
            editing       = q.b,
            pendingDelete = q.c,
            error         = q.d
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetCategoryListUiState()
    )

    // ── Crear ─────────────────────────────────────────────────────────────────
    fun openAddSheet()  { _showAddSheet.value = true }
    fun closeAddSheet() { _showAddSheet.value = false }

    fun addCategory(name: String, icon: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.categories.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = AssetCategoryError.AlreadyExists
            return
        }
        viewModelScope.launch {
            val now = nowMillis()
            val nextOrder = (uiState.value.categories.maxOfOrNull { it.sortOrder } ?: -1) + 1
            saveCategory(
                AssetCategory(
                    id        = "asset_cat_$now",
                    name      = trimmed,
                    icon      = icon.ifBlank { "📦" },
                    sortOrder = nextOrder,
                    createdAt = now
                )
            ).onFailure { _error.value = AssetCategoryError.Unknown(it.message) }
            _showAddSheet.value = false
        }
    }

    // ── Editar ────────────────────────────────────────────────────────────────
    fun openEditSheet(category: AssetCategory) { _editing.value = category }
    fun closeEditSheet()                       { _editing.value = null }

    fun renameCategory(id: String, newName: String, newIcon: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        if (uiState.value.categories.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            _error.value = AssetCategoryError.AlreadyExists
            return
        }
        viewModelScope.launch {
            renameCategory.invoke(id, trimmed, newIcon.ifBlank { "📦" })
                .onFailure { _error.value = AssetCategoryError.Unknown(it.message) }
            _editing.value = null
        }
    }

    // ── Eliminar (soft) ───────────────────────────────────────────────────────
    fun requestDelete(category: AssetCategory) { _pendingDelete.value = category }
    fun cancelDelete()                         { _pendingDelete.value = null }

    fun confirmDelete() {
        val cat = _pendingDelete.value ?: return
        viewModelScope.launch {
            archiveCategory(cat.id).onFailure { _error.value = AssetCategoryError.Unknown(it.message) }
            _pendingDelete.value = null
        }
    }

    fun clearError() { _error.value = null }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
