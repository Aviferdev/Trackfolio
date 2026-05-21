package es.aviferdev.n3to.domain.model

/**
 * Tipos de instrumentos de renta fija.
 * Cada tipo tiene duraciones típicas orientativas y frecuencias de interés permitidas.
 */
enum class FixedIncomeType(
    val label: String,
    val emoji: String,
    val typicalMinMonths: Int,
    val typicalMaxMonths: Int,
    val allowedFrequencies: Set<InterestFrequency>,
    val category: FixedIncomeCategory,
    val allowsSecondarySale: Boolean,
    val allowsEarlyCancellation: Boolean,
    val resourceKey: String
) {
    // Letras del Tesoro: 3-18 meses, solo al vencimiento
    BILL(
        label = "Letra del Tesoro",
        emoji = "📋",
        typicalMinMonths = 3,
        typicalMaxMonths = 18,
        allowedFrequencies = setOf(InterestFrequency.AT_MATURITY),
        category = FixedIncomeCategory.GOVERNMENT,
        allowsSecondarySale = true,
        allowsEarlyCancellation = false,
        resourceKey = "fixedincome_type_bill"
    ),

    // Bonos del Estado: 2-10 años, anual o semestral
    BOND(
        label = "Bono del Estado",
        emoji = "📜",
        typicalMinMonths = 24,
        typicalMaxMonths = 120,
        allowedFrequencies = setOf(InterestFrequency.ANNUAL, InterestFrequency.SEMIANNUAL),
        category = FixedIncomeCategory.GOVERNMENT,
        allowsSecondarySale = true,
        allowsEarlyCancellation = false,
        resourceKey = "fixedincome_type_bond"
    ),

    // Obligaciones del Estado: 10-50 años, anual o semestral
    GOVERNMENT_OBLIGATION(
        label = "Obligación del Estado",
        emoji = "🏛️",
        typicalMinMonths = 120,
        typicalMaxMonths = 600,
        allowedFrequencies = setOf(InterestFrequency.ANNUAL, InterestFrequency.SEMIANNUAL),
        category = FixedIncomeCategory.GOVERNMENT,
        allowsSecondarySale = true,
        allowsEarlyCancellation = false,
        resourceKey = "fixedincome_type_government_obligation"
    ),

    // Bonos corporativos: 1-15 años, trimestral, semestral o anual
    CORPORATE_BOND(
        label = "Bono corporativo",
        emoji = "🏢",
        typicalMinMonths = 12,
        typicalMaxMonths = 180,
        allowedFrequencies = setOf(
            InterestFrequency.QUARTERLY,
            InterestFrequency.SEMIANNUAL,
            InterestFrequency.ANNUAL
        ),
        category = FixedIncomeCategory.CORPORATE,
        allowsSecondarySale = true,
        allowsEarlyCancellation = false,
        resourceKey = "fixedincome_type_corporate_bond"
    ),

    // Depósito a plazo fijo: 3 meses-5 años, varias frecuencias
    DEPOSIT(
        label = "Depósito a plazo fijo",
        emoji = "🏦",
        typicalMinMonths = 3,
        typicalMaxMonths = 60,
        allowedFrequencies = setOf(
            InterestFrequency.MONTHLY,
            InterestFrequency.QUARTERLY,
            InterestFrequency.ANNUAL,
            InterestFrequency.AT_MATURITY
        ),
        category = FixedIncomeCategory.DEPOSIT,
        allowsSecondarySale = false,
        allowsEarlyCancellation = true,
        resourceKey = "fixedincome_type_deposit"
    )
}