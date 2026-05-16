package es.aviferdev.n3to.platform

import android.app.Activity
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.Offering
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

actual class PurchaseManager {

    private var activityRef: WeakReference<Activity>? = null
    private var currentRCInfo: com.revenuecat.purchases.kmp.models.CustomerInfo? = null
    private var currentOffering: Offering? = null

    fun bindActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun unbindActivity() {
        activityRef?.clear()
        activityRef = null
    }

    actual fun configure(apiKey: String) {
        Purchases.configure(apiKey)
    }

    actual suspend fun getProducts(): List<ProductDetails> =
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getOfferings(
                onError = { error ->
                    continuation.resume(emptyList())
                },
                onSuccess = { offerings ->
                    currentOffering = offerings.current
                    val products = offerings.current?.availablePackages?.mapNotNull { pkg ->
                        val product = pkg.storeProduct
                        ProductDetails(
                            identifier = pkg.identifier,
                            title = product.title,
                            price = product.price.formatted,
                            currencyCode = product.price.currencyCode,
                            description = product.localizedDescription ?: ""
                        )
                    } ?: emptyList()
                    continuation.resume(products)
                }
            )
        }

    actual suspend fun purchase(productId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            val activity = activityRef?.get()
            if (activity == null) {
                continuation.resume(PurchaseResult.Error("Activity not bound"))
                return@suspendCancellableCoroutine
            }
            val pkg = currentOffering?.availablePackages?.find {
                it.identifier == productId
            }
            if (pkg == null) {
                // Si no está en caché, reintentar con getOfferings
                Purchases.sharedInstance.getOfferings(
                    onError = { error ->
                        continuation.resume(
                            PurchaseResult.Error("Producto no disponible: ${error.message}")
                        )
                    },
                    onSuccess = { offerings ->
                        val found = offerings.current?.availablePackages?.find {
                            it.identifier == productId
                        }
                        if (found == null) {
                            continuation.resume(
                                PurchaseResult.Error(
                                    "Producto no disponible. " +
                                            "Configúralo en RevenueCat Dashboard y Google Play Console"
                                )
                            )
                            return@getOfferings
                        }
                        currentOffering = offerings.current
                        doPurchase(found, continuation)
                    }
                )
                return@suspendCancellableCoroutine
            }
            doPurchase(pkg, continuation)
        }

    private fun doPurchase(
        pkg: com.revenuecat.purchases.kmp.models.Package,
        continuation: kotlin.coroutines.Continuation<PurchaseResult>
    ) {
        val activity = activityRef?.get()
        if (activity == null) {
            continuation.resume(PurchaseResult.Error("Activity not bound"))
            return
        }
        Purchases.sharedInstance.purchase(
            pkg,
            onError = { error, userCancelled ->
                val result = if (userCancelled) {
                    PurchaseResult.Cancelled
                } else {
                    PurchaseResult.Error(error.message ?: "Error al procesar la compra")
                }
                continuation.resume(result)
            },
            onSuccess = { _, customerInfo ->
                currentRCInfo = customerInfo
                continuation.resume(PurchaseResult.Success)
            }
        )
    }

    actual suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    continuation.resume(
                        PurchaseResult.Error(error.message ?: "Error al restaurar compras")
                    )
                },
                onSuccess = { customerInfo ->
                    currentRCInfo = customerInfo
                    continuation.resume(PurchaseResult.Success)
                }
            )
        }

    actual fun observeCustomerInfo(): Flow<CustomerInfo> =
        callbackFlow {
            @OptIn(com.revenuecat.purchases.kmp.ExperimentalRevenueCatApi::class)
            val delegate = object : PurchasesDelegate {
                override fun onCustomerInfoUpdated(
                    customerInfo: com.revenuecat.purchases.kmp.models.CustomerInfo
                ) {
                    currentRCInfo = customerInfo
                    trySend(customerInfo.toDomain())
                }

                override fun onPurchasePromoProduct(
                    product: com.revenuecat.purchases.kmp.models.StoreProduct,
                    startPurchase: (
                        onError: (com.revenuecat.purchases.kmp.models.PurchasesError, Boolean) -> Unit,
                        onSuccess: (com.revenuecat.purchases.kmp.models.StoreTransaction, com.revenuecat.purchases.kmp.models.CustomerInfo) -> Unit
                    ) -> Unit
                ) {
                    // Promo purchases not handled
                }
            }
            Purchases.sharedInstance.delegate = delegate
            awaitClose {
                Purchases.sharedInstance.delegate = null
            }
        }

    actual fun isPremium(): Boolean =
        currentRCInfo?.entitlements?.get("premium")?.isActive == true

    actual suspend fun getManagementUrl(): String? =
        currentRCInfo?.managementUrlString

    private fun com.revenuecat.purchases.kmp.models.CustomerInfo.toDomain(): CustomerInfo {
        val ent = entitlements["premium"]
        return CustomerInfo(
            isPremium = ent?.isActive == true,
            entitlementExpiryDate = ent?.expirationDateMillis,
            managementUrl = managementUrlString,
            isLifetime = ent?.isActive == true && ent?.expirationDateMillis == null
        )
    }
}
