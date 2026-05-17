package es.aviferdev.n3to.domain.model

/**
 * Tipos de entidad emisora. Cada [IncomeType] apunta a uno de estos tipos,
 * que determina en qué tabla se persiste el emisor.
 */
enum class IssuerType(val label: String) {
    EMPLOYER("Empresa / Cliente"),
    BANK("Entidad bancaria"),
    BOND_ISSUER("Emisor del bono / depósito"),
    DIVIDEND_SOURCE("Acción"),
    PROMOTION_PLATFORM("Plataforma / sorteo"),
    EXEMPT_SOURCE("Fuente de ingreso")
}
