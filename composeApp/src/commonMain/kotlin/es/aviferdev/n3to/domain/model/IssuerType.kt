package es.aviferdev.n3to.domain.model

/**
 * Tipos de entidad emisora. Cada [IncomeType] apunta a uno de estos tipos,
 * que determina en qué tabla se persiste el emisor.
 */
enum class IssuerType(val label: String, val resourceKey: String) {
    EMPLOYER("Empresa / Cliente", "issuer_type_employer"),
    BANK("Entidad bancaria", "issuer_type_bank"),
    BOND_ISSUER("Emisor del bono / depósito", "issuer_type_bond_issuer"),
    DIVIDEND_SOURCE("Acción", "issuer_type_dividend_source"),
    PROMOTION_PLATFORM("Plataforma / sorteo", "issuer_type_promotion_platform"),
    EXEMPT_SOURCE("Fuente de ingreso", "issuer_type_exempt_source")
}
