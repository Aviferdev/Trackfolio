package es.aviferdev.n3to.domain.model

/**
 * Tipos de ingreso predefinidos e inmutables.
 * Cada tipo determina:
 * - Qué campos fiscales se muestran en el formulario
 * - Qué tipo de entidad emisora se asocia (empresa, banco, acción…)
 * - La clasificación fiscal a efectos de IRPF
 */
enum class IncomeType(
    val label: String,
    val emoji: String,
    /** Porcentaje de retención orientativo; el usuario puede cambiarlo. */
    val defaultIrpfPercent: Double?,
    /** Si este tipo requiere campos de retención IRPF. */
    val hasIrpf: Boolean,
    /** Si este tipo tiene campo de cotizaciones a la Seguridad Social. */
    val hasSocialSecurity: Boolean,
    /** Si este tipo tiene campo de comisiones. */
    val hasCommission: Boolean,
    /** Tipo de entidad emisora asociada. */
    val issuerType: IssuerType
) {
    SALARY(
        label              = "Salario",
        emoji              = "💼",
        defaultIrpfPercent = null,
        hasIrpf            = true,
        hasSocialSecurity  = true,
        hasCommission      = false,
        issuerType         = IssuerType.EMPLOYER
    ),
    BANK_INTEREST(
        label              = "Intereses bancarios",
        emoji              = "🏦",
        defaultIrpfPercent = 19.0,
        hasIrpf            = true,
        hasSocialSecurity  = false,
        hasCommission      = false,
        issuerType         = IssuerType.BANK
    ),
    BOND_DEPOSIT(
        label              = "Bonos o depósitos",
        emoji              = "📜",
        defaultIrpfPercent = 19.0,
        hasIrpf            = true,
        hasSocialSecurity  = false,
        hasCommission      = true,
        issuerType         = IssuerType.BOND_ISSUER
    ),
    DIVIDEND(
        label              = "Dividendos",
        emoji              = "📈",
        defaultIrpfPercent = 19.0,
        hasIrpf            = true,
        hasSocialSecurity  = false,
        hasCommission      = false,
        issuerType         = IssuerType.DIVIDEND_SOURCE
    ),
    BONUS_PRIZE(
        label              = "Bonificaciones / Premios",
        emoji              = "🎁",
        defaultIrpfPercent = 19.0,
        hasIrpf            = true,
        hasSocialSecurity  = false,
        hasCommission      = false,
        issuerType         = IssuerType.PROMOTION_PLATFORM
    ),
    EXEMPT_INCOME(
        label              = "Ingreso exento",
        emoji              = "📋",
        defaultIrpfPercent = 0.0,
        hasIrpf            = false,
        hasSocialSecurity  = false,
        hasCommission      = false,
        issuerType         = IssuerType.EXEMPT_SOURCE
    );

    companion object {
        fun fromName(name: String?): IncomeType? =
            name?.let { n -> entries.firstOrNull { it.name == n } }
    }
}
