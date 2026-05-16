package es.aviferdev.n3to.core.premium

import es.aviferdev.n3to.platform.CustomerInfo
import es.aviferdev.n3to.platform.PurchaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PremiumStatus(
    val isPremium: Boolean = false,
    val expiryDate: Long? = null,
    val isLifetime: Boolean = false,
    val managementUrl: String? = null
)

class PremiumManager(
    private val purchaseManager: PurchaseManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _status = MutableStateFlow(PremiumStatus())
    val status: StateFlow<PremiumStatus> = _status.asStateFlow()

    fun initialize(apiKey: String) {
        purchaseManager.configure(apiKey)
        scope.launch {
            purchaseManager.observeCustomerInfo().collect { info ->
                _status.value = info.toPremiumStatus()
            }
        }
    }

    fun shouldShowAds(): Boolean = !_status.value.isPremium
}

private fun CustomerInfo.toPremiumStatus() = PremiumStatus(
    isPremium = isPremium,
    expiryDate = entitlementExpiryDate,
    isLifetime = isLifetime,
    managementUrl = managementUrl
)
