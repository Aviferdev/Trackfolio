package es.aviferdev.n3to.domain.model

/**
 * Interfaz sellada que unifica el acceso polimórfico a los campos comunes
 * de los instrumentos financieros ([Asset] y [FixedIncomePosition]).
 *
 * Las tablas SQL permanecen separadas porque sus campos específicos son
 * significativamente diferentes (precio actual vs. tasa de interés, etc.).
 * Esta interfaz permite tratar ambos tipos de forma polimórfica en la
 * capa de dominio donde sea útil (filtros, listados combinados, net worth).
 *
 * @see Asset para renta variable (acciones, ETFs, fondos, cripto, etc.)
 * @see FixedIncomePosition para renta fija (bonos, depósitos, pagarés)
 */
sealed interface FinancialInstrument {
    val id: String
    val accountId: String
    val portfolioId: String?
    val assetCategoryId: String?
    val name: String
    val ticker: String
    val notes: String?
    val createdAt: Long
    val archived: Boolean

    /** Tipo de instrumento para discriminar en `when`. */
    val instrumentType: InstrumentType
}

/**
 * Discriminador para [FinancialInstrument].
 */
enum class InstrumentType {
    /** Renta variable: acciones, ETFs, fondos, cripto, materias primas, etc. */
    VARIABLE_INCOME,
    /** Renta fija: bonos, depósitos bancarios, pagarés. */
    FIXED_INCOME
}
