package es.aviferdev.n3to.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── UI State ──────────────────────────────────────────────────────────────────

sealed class TransactionDetailUiState {
    data object Loading : TransactionDetailUiState()
    data class Success(
        val transaction: Transaction,
        val categoryName: String,
        val accountName: String,
        val incomeTypeLabel: String?,
        val incomeTypeEmoji: String?,
    ) : TransactionDetailUiState()

    data class Error(val message: String) : TransactionDetailUiState()
    data object Deleted : TransactionDetailUiState()
}

// ─── ViewModel ─────────────────────────────────────────────────────────────────

class TransactionDetailViewModel(
    private val transactionId: String,
    private val getTransactionById: GetTransactionByIdUseCase,
    private val getAccountById: GetAccountByIdUseCase,
    private val getAllCategoriesIncludingArchived: GetAllCategoriesIncludingArchivedUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadTransaction() {
        viewModelScope.launch {
            getTransactionById(transactionId)
                .flatMapLatest { tx ->
                    if (tx == null) flowOf(null to emptyList<Category>())
                    else getAllCategoriesIncludingArchived(tx.accountId)
                        .map { categories -> tx to categories }
                }
                .onStart { _uiState.value = TransactionDetailUiState.Loading }
                .catch { e ->
                    _uiState.value = TransactionDetailUiState.Error(
                        e.message ?: "Error al cargar la transacción"
                    )
                }
                .collect { (tx, categories) ->
                    if (tx == null) {
                        _uiState.value = TransactionDetailUiState.Error("Transacción no encontrada")
                        return@collect
                    }
                    resolveDetails(tx, categories)
                }
        }
    }

    private suspend fun resolveDetails(tx: Transaction, categories: List<Category>) {
        // Resolver nombre de cuenta
        val accountName = getAccountById(tx.accountId)
            .map { account -> account?.name ?: "Cuenta desconocida" }
            .first()

        // Resolver nombre de categoría (para gastos)
        val categoryName = if (tx.isExpense) {
            tx.categoryId?.let { id ->
                categories.find { it.id == id }?.name
            } ?: "Gasto"
        } else if (tx.isIncome) {
            tx.incomeType?.label ?: "Ingreso"
        } else if (tx.isAdjustment) {
            "Ajuste de saldo"
        } else {
            tx.notes ?: "Movimiento"
        }

        val incomeTypeLabel = tx.incomeType?.label
        val incomeTypeEmoji = tx.incomeType?.emoji

        _uiState.value = TransactionDetailUiState.Success(
            transaction = tx,
            categoryName = categoryName,
            accountName = accountName,
            incomeTypeLabel = incomeTypeLabel,
            incomeTypeEmoji = incomeTypeEmoji,
        )
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            deleteTransactionUseCase(transactionId)
                .onSuccess { _uiState.value = TransactionDetailUiState.Deleted }
                .onFailure { e ->
                    val current = _uiState.value
                    if (current is TransactionDetailUiState.Success) {
                        _uiState.value = current // keep success but show error elsewhere
                    }
                }
        }
    }

    fun refresh() {
        loadTransaction()
    }
}
