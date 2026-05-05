package es.aviferdev.trackfolio.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed class AddTransactionUiState {
    data object Idle    : AddTransactionUiState()
    data object Loading : AddTransactionUiState()
    data object Success : AddTransactionUiState()
    data class Error(val message: String) : AddTransactionUiState()
}

class AddTransactionViewModel(
    private val saveTransaction: SaveTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getCategoriesByType: GetCategoriesByTypeUseCase,
    private val session: AccountSession
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    // Transacción en edición (null = modo creación)
    private var editingTransaction: Transaction? = null
    val isEditing: Boolean get() = editingTransaction != null

    var amount by mutableStateOf("")
        private set
    var type by mutableStateOf(TransactionType.EXPENSE)
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var selectedCategoryId by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set

    val isValid: Boolean
        get() = amount.isNotEmpty()
            && amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
            && selectedCategoryId.isNotEmpty()

    init { loadCategories() }

    /** Llama esto antes de mostrar el sheet en modo edición */
    fun loadForEdit(transaction: Transaction) {
        editingTransaction = transaction
        amount = transaction.amount.toString().replace('.', ',')
        type   = transaction.type
        notes  = transaction.notes ?: ""
        loadCategoriesAndSelect(transaction.type, transaction.categoryId)
    }

    fun resetForCreate() {
        editingTransaction = null
        amount = ""
        type   = TransactionType.EXPENSE
        notes  = ""
        loadCategories()
    }

    fun onAmountChange(value: String) {
        amount = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onTypeChange(newType: TransactionType) {
        type = newType
        loadCategories()
    }

    fun onCategoryChange(categoryId: String) { selectedCategoryId = categoryId }
    fun onNotesChange(value: String) { notes = value }

    private fun loadCategories() {
        loadCategoriesAndSelect(type, null)
    }

    private fun loadCategoriesAndSelect(forType: TransactionType, selectId: String?) {
        getCategoriesByType(forType)
            .onEach { list ->
                categories = list
                selectedCategoryId = if (selectId != null && list.any { it.id == selectId })
                    selectId
                else
                    list.firstOrNull()?.id ?: ""
            }
            .launchIn(viewModelScope)
    }

    fun save() {
        if (!isValid) return
        _uiState.value = AddTransactionUiState.Loading
        viewModelScope.launch {
            val amountValue = amount.replace(',', '.').toDoubleOrNull() ?: return@launch
            val accountId   = session.selectedAccountId.value ?: run {
                _uiState.value = AddTransactionUiState.Error("No hay cuenta seleccionada")
                return@launch
            }
            val now = Clock.System.now().toEpochMilliseconds()

            val existing = editingTransaction
            if (existing != null) {
                // Modo edición
                val updated = existing.copy(
                    amount     = amountValue,
                    type       = type,
                    categoryId = selectedCategoryId,
                    notes      = notes.ifBlank { null }
                )
                updateTransaction(updated)
                    .onSuccess { _uiState.value = AddTransactionUiState.Success }
                    .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
            } else {
                // Modo creación
                val transaction = Transaction(
                    id         = generateId(),
                    accountId  = accountId,
                    amount     = amountValue,
                    type       = type,
                    categoryId = selectedCategoryId,
                    date       = now,
                    notes      = notes.ifBlank { null },
                    createdAt  = now
                )
                saveTransaction(transaction)
                    .onSuccess { _uiState.value = AddTransactionUiState.Success }
                    .onFailure { _uiState.value = AddTransactionUiState.Error(it.message ?: "Error") }
            }
        }
    }

    private fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return "tx_" + (1..29).map { chars.random() }.joinToString("")
    }
}
