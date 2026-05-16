package es.aviferdev.n3to.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class PurchaseManager {

    private val _customerInfo = MutableStateFlow(CustomerInfo(isPremium = false))

    actual fun configure(apiKey: String) {
        // TODO: Implementar con RevenueCat SDK cuando esté disponible
    }

    actual suspend fun purchase(productId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            continuation.resume(PurchaseResult.Error("RevenueCat no configurado"))
        }

    actual suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            continuation.resume(PurchaseResult.Error("RevenueCat no configurado"))
        }

    actual fun observeCustomerInfo(): Flow<CustomerInfo> = _customerInfo.asStateFlow()

    actual fun isPremium(): Boolean = _customerInfo.value.isPremium
}
