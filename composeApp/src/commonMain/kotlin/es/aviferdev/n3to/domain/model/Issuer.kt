package es.aviferdev.n3to.domain.model

/**
 * Entidad emisora genérica de un ingreso.
 * En BD se persiste en tablas separadas por [IssuerType],
 * pero en dominio se usa este modelo unificado.
 */
data class Issuer(
    val id: String,
    val accountId: String,
    val name: String,
    val type: IssuerType,
    val icon: String = defaultIconFor(type),
    val archived: Boolean = false,
    val createdAt: Long
)

private fun defaultIconFor(type: IssuerType): String = when (type) {
    IssuerType.EMPLOYER           -> "🏢"
    IssuerType.BANK               -> "🏦"
    IssuerType.BOND_ISSUER        -> "📜"
    IssuerType.DIVIDEND_SOURCE    -> "📈"
    IssuerType.PROMOTION_PLATFORM -> "🎁"
    IssuerType.EXEMPT_SOURCE      -> "📋"
}
