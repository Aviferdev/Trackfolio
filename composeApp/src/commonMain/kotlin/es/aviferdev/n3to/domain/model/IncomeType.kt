package es.aviferdev.n3to.domain.model

enum class IncomeType(
    val label: String,
    val emoji: String,
    /** Cómo llamar al emisor de este tipo de ingreso en la UI. */
    val issuerLabel: String,
    /** Si este tipo puede llevar retención sobre la renta (IRPF, Income Tax...). */
    val hasWithholdingTax: Boolean,
    /** Si este tipo puede llevar cotización social (SS, NI, FICA...). */
    val hasSocialContribution: Boolean,
    /** Si este tipo tiene campo de comisiones. */
    val hasCommission: Boolean,
    /** Tipo de entidad emisora asociada. */
    val issuerType: IssuerType,
    val resourceKey: String
) {
    SALARY(
        label                = "Salario",
        emoji                = "💼",
        issuerLabel          = "Empresa empleadora",
        hasWithholdingTax    = true,
        hasSocialContribution = true,
        hasCommission        = false,
        issuerType           = IssuerType.EMPLOYER,
        resourceKey          = "income_type_salary"
    ),
    FREELANCE(
        label                = "Autónomo / Freelance",
        emoji                = "🧾",
        issuerLabel          = "Cliente",
        hasWithholdingTax    = true,
        hasSocialContribution = true,
        hasCommission        = false,
        issuerType           = IssuerType.EMPLOYER,
        resourceKey          = "income_type_freelance"
    ),
    BANK_INTEREST(
        label                = "Intereses bancarios",
        emoji                = "🏦",
        issuerLabel          = "Entidad bancaria",
        hasWithholdingTax    = true,
        hasSocialContribution = false,
        hasCommission        = false,
        issuerType           = IssuerType.BANK,
        resourceKey          = "income_type_bank_interest"
    ),
    BOND_DEPOSIT(
        label                = "Bonos o depósitos",
        emoji                = "📜",
        issuerLabel          = "Emisor",
        hasWithholdingTax    = true,
        hasSocialContribution = false,
        hasCommission        = true,
        issuerType           = IssuerType.BOND_ISSUER,
        resourceKey          = "income_type_bond_deposit"
    ),
    DIVIDEND(
        label                = "Dividendos",
        emoji                = "📈",
        issuerLabel          = "Empresa",
        hasWithholdingTax    = true,
        hasSocialContribution = false,
        hasCommission        = false,
        issuerType           = IssuerType.DIVIDEND_SOURCE,
        resourceKey          = "income_type_dividend"
    ),
    BONUS_PRIZE(
        label                = "Bonus laboral",
        emoji                = "🎁",
        issuerLabel          = "Empresa empleadora",
        hasWithholdingTax    = true,
        hasSocialContribution = true,
        hasCommission        = false,
        issuerType           = IssuerType.EMPLOYER,
        resourceKey          = "income_type_bonus_prize"
    ),
    PRIZE_LOTTERY(
        label                = "Premio / Lotería",
        emoji                = "🏆",
        issuerLabel          = "Organizador",
        hasWithholdingTax    = true,
        hasSocialContribution = false,
        hasCommission        = false,
        issuerType           = IssuerType.PROMOTION_PLATFORM,
        resourceKey          = "income_type_prize_lottery"
    ),
    RENTAL_INCOME(
        label                = "Alquiler",
        emoji                = "🏠",
        issuerLabel          = "Arrendatario",
        hasWithholdingTax    = true,
        hasSocialContribution = false,
        hasCommission        = false,
        issuerType           = IssuerType.EXEMPT_SOURCE,
        resourceKey          = "income_type_rental_income"
    ),
    EXEMPT_INCOME(
        label                = "Ingreso exento",
        emoji                = "📋",
        issuerLabel          = "Fuente de ingreso",
        hasWithholdingTax    = false,
        hasSocialContribution = false,
        hasCommission        = false,
        issuerType           = IssuerType.EXEMPT_SOURCE,
        resourceKey          = "income_type_exempt_income"
    );

    companion object {
        fun fromName(name: String?): IncomeType? =
            name?.let { n -> entries.firstOrNull { it.name == n } }
    }
}
