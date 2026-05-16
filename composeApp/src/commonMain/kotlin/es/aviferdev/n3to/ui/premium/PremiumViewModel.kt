package es.aviferdev.n3to.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.platform.ProductDetails
import es.aviferdev.n3to.platform.PurchaseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PremiumUiState(
    val isPremium: Boolean = false,
    val isLifetime: Boolean = false,
    val purchaseInProgress: String? = null,
    val isLoading: Boolean = true,
    val products: List<ProductDetails> = emptyList(),
    val productLoadError: String? = null
)

data class ProductInfo(
    val productId: String,
    val title: String,
    val price: String,
    val period: String,
    val isBestValue: Boolean = false
)

sealed class PremiumEvent {
    data object PurchaseSuccess : PremiumEvent()
    data class PurchaseError(val message: String) : PremiumEvent()
    data object RestoreSuccess : PremiumEvent()
    data class RestoreError(val message: String) : PremiumEvent()
}

class PremiumViewModel(
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PremiumEvent>()
    val events: SharedFlow<PremiumEvent> = _events.asSharedFlow()

    init {
        observePremiumStatus()
        loadProducts()
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            premiumManager.status.collect { status ->
                _uiState.update {
                    it.copy(
                        isPremium = status.isPremium,
                        isLifetime = status.isLifetime,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val products = premiumManager.getProducts()
            if (products.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        productLoadError = "No hay productos disponibles. " +
                                "Configura los productos en RevenueCat Dashboard."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        products = products,
                        productLoadError = null
                    )
                }
            }
        }
    }

    fun purchase(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseInProgress = productId) }
            val result = premiumManager.purchase(productId)
            _uiState.update { it.copy(purchaseInProgress = null) }

            when (result) {
                is PurchaseResult.Success -> _events.emit(PremiumEvent.PurchaseSuccess)
                is PurchaseResult.Cancelled -> _events.emit(
                    PremiumEvent.PurchaseError("Compra cancelada")
                )

                is PurchaseResult.Error -> _events.emit(
                    PremiumEvent.PurchaseError(result.message)
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseInProgress = "restore") }
            val result = premiumManager.restorePurchases()
            _uiState.update { it.copy(purchaseInProgress = null) }

            when (result) {
                is PurchaseResult.Success -> _events.emit(PremiumEvent.RestoreSuccess)
                is PurchaseResult.Error -> _events.emit(
                    PremiumEvent.RestoreError(result.message)
                )

                is PurchaseResult.Cancelled -> _events.emit(
                    PremiumEvent.RestoreError("Restauración cancelada")
                )
            }
        }
    }

    fun retryLoadProducts() {
        loadProducts()
    }
}
