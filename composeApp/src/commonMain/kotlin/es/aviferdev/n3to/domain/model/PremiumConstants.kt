package es.aviferdev.n3to.domain.model

/**
 * Constantes del sistema Premium.
 * Centraliza los identificadores de productos y límites del plan gratuito.
 */
object PremiumConstants {
    /** Máximo número de cuentas en el plan gratuito */
    const val MAX_FREE_ACCOUNTS = 2

    // RevenueCat product identifiers
    const val PREMIUM_PRODUCT_MONTHLY = "premium_monthly"
    const val PREMIUM_PRODUCT_YEARLY = "premium_yearly"
    const val PREMIUM_PRODUCT_LIFETIME = "premium_lifetime"

    // RevenueCat entitlement identifier
    const val ENTITLEMENT_PREMIUM = "premium"
}
