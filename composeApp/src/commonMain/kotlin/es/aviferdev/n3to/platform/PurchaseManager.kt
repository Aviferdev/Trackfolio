package es.aviferdev.n3to.platform

import kotlinx.coroutines.flow.Flow

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

data class CustomerInfo(
    val isPremium: Boolean,
    val entitlementExpiryDate: Long? = null,
    val managementUrl: String? = null,
    val isLifetime: Boolean = false
)

/**
 * Información de un producto disponible para compra,
 * obtenida desde RevenueCat Offerings.
 */
data class ProductDetails(
    val identifier: String,
    val title: String,
    val price: String,
    val currencyCode: String,
    val description: String = ""
)

expect class PurchaseManager {
    fun configure(apiKey: String)
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restorePurchases(): PurchaseResult
    fun observeCustomerInfo(): Flow<CustomerInfo>
    fun isPremium(): Boolean
    suspend fun getProducts(): List<ProductDetails>
    suspend fun getManagementUrl(): String?
}
