package es.aviferdev.n3to.domain.model

data class TaxProfile(
    val countryCode: String?,
    val currency: String,
    /** Líneas fiscales por defecto para cada tipo de ingreso. */
    val templates: Map<IncomeType, List<TaxLineTemplate>>
) {
    fun templatesFor(incomeType: IncomeType): List<TaxLineTemplate> =
        templates[incomeType] ?: emptyList()

    companion object {
        val SPAIN = TaxProfile(
            countryCode = "ES",
            currency = "EUR",
            templates = mapOf(
                IncomeType.SALARY to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("Seg. Social", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.BANK_INTEREST to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, 19.0)
                ),
                IncomeType.BOND_DEPOSIT to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, 19.0)
                ),
                IncomeType.DIVIDEND to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, 19.0)
                ),
                IncomeType.BONUS_PRIZE to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("Seg. Social", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.PRIZE_LOTTERY to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, 20.0)
                ),
                IncomeType.RENTAL_INCOME to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.FREELANCE to listOf(
                    TaxLineTemplate("IRPF", TaxRole.INCOME_TAX, 15.0),
                    TaxLineTemplate("Seg. Social (Autónomos)", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.EXEMPT_INCOME to emptyList()
            )
        )

        val UK = TaxProfile(
            countryCode = "GB",
            currency = "GBP",
            templates = mapOf(
                IncomeType.SALARY to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("National Insurance", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.BANK_INTEREST to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, 20.0)
                ),
                IncomeType.BOND_DEPOSIT to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, 20.0)
                ),
                IncomeType.DIVIDEND to listOf(
                    TaxLineTemplate("Dividend Tax", TaxRole.INCOME_TAX, 8.75)
                ),
                IncomeType.BONUS_PRIZE to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("National Insurance", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.PRIZE_LOTTERY to emptyList(),
                IncomeType.RENTAL_INCOME to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.FREELANCE to listOf(
                    TaxLineTemplate("Income Tax", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("NI Class 4", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.EXEMPT_INCOME to emptyList()
            )
        )

        val USA = TaxProfile(
            countryCode = "US",
            currency = "USD",
            templates = mapOf(
                IncomeType.SALARY to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("State Tax", TaxRole.REGIONAL_TAX, null),
                    TaxLineTemplate("FICA", TaxRole.SOCIAL_CONTRIBUTION, 7.65)
                ),
                IncomeType.BANK_INTEREST to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.BOND_DEPOSIT to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.DIVIDEND to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, 15.0)
                ),
                IncomeType.BONUS_PRIZE to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("State Tax", TaxRole.REGIONAL_TAX, null),
                    TaxLineTemplate("FICA", TaxRole.SOCIAL_CONTRIBUTION, 7.65)
                ),
                IncomeType.PRIZE_LOTTERY to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, 24.0)
                ),
                IncomeType.RENTAL_INCOME to listOf(
                    TaxLineTemplate("Federal Tax", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.FREELANCE to listOf(
                    TaxLineTemplate("SE Tax", TaxRole.SOCIAL_CONTRIBUTION, 15.3)
                ),
                IncomeType.EXEMPT_INCOME to emptyList()
            )
        )

        val GERMANY = TaxProfile(
            countryCode = "DE",
            currency = "EUR",
            templates = mapOf(
                IncomeType.SALARY to listOf(
                    TaxLineTemplate("Lohnsteuer", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("Solidaritätszuschlag", TaxRole.REGIONAL_TAX, 5.5),
                    TaxLineTemplate("Krankenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 7.3),
                    TaxLineTemplate("Rentenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 9.3),
                    TaxLineTemplate("Arbeitslosenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 1.3),
                    TaxLineTemplate("Pflegeversicherung", TaxRole.SOCIAL_CONTRIBUTION, 1.525)
                ),
                IncomeType.BANK_INTEREST to listOf(
                    TaxLineTemplate("Kapitalertragsteuer", TaxRole.INCOME_TAX, 25.0)
                ),
                IncomeType.BOND_DEPOSIT to listOf(
                    TaxLineTemplate("Kapitalertragsteuer", TaxRole.INCOME_TAX, 25.0)
                ),
                IncomeType.DIVIDEND to listOf(
                    TaxLineTemplate("Kapitalertragsteuer", TaxRole.INCOME_TAX, 25.0)
                ),
                IncomeType.BONUS_PRIZE to listOf(
                    TaxLineTemplate("Lohnsteuer", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("Solidaritätszuschlag", TaxRole.REGIONAL_TAX, 5.5),
                    TaxLineTemplate("Krankenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 7.3),
                    TaxLineTemplate("Rentenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 9.3),
                    TaxLineTemplate("Arbeitslosenversicherung", TaxRole.SOCIAL_CONTRIBUTION, 1.3),
                    TaxLineTemplate("Pflegeversicherung", TaxRole.SOCIAL_CONTRIBUTION, 1.525)
                ),
                IncomeType.PRIZE_LOTTERY to emptyList(),
                IncomeType.RENTAL_INCOME to listOf(
                    TaxLineTemplate("Einkommensteuer", TaxRole.INCOME_TAX, null)
                ),
                IncomeType.FREELANCE to listOf(
                    TaxLineTemplate("Einkommensteuer", TaxRole.INCOME_TAX, null),
                    TaxLineTemplate("Krankenversicherung", TaxRole.SOCIAL_CONTRIBUTION, null)
                ),
                IncomeType.EXEMPT_INCOME to emptyList()
            )
        )

        val CUSTOM = TaxProfile(
            countryCode = null,
            currency = "EUR",
            templates = emptyMap()
        )

        val ALL = listOf(SPAIN, UK, USA, GERMANY, CUSTOM)
    }
}
