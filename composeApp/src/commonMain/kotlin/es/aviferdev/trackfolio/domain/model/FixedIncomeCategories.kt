package es.aviferdev.trackfolio.domain.model

/**
 * Identifica las categorías de activo de renta fija (bonos y depósitos).
 *
 * Estas categorías tienen un comportamiento especial:
 * - No tienen precio actual cotizado (no aplica currentPrice).
 * - Tienen fecha de vencimiento (maturityDate).
 * - Los bonos pueden generar cupones periódicos.
 * - Los depósitos generan intereses al vencimiento.
 * - Ambos están sujetos a retención de IRPF.
 */
object FixedIncomeCategories {

    private const val BONDS_ID    = "fixed_cat_bonds"
    private const val DEPOSITS_ID = "fixed_cat_deposits"

    private val FIXED_INCOME_IDS = setOf(BONDS_ID, DEPOSITS_ID)

    /** True si la categoría es de renta fija (bonos o depósitos). */
    fun isFixedIncome(categoryId: String?): Boolean =
        categoryId in FIXED_INCOME_IDS

    /** True si la categoría es de bonos / deuda pública. */
    fun isBond(categoryId: String?): Boolean =
        categoryId == BONDS_ID

    /** True si la categoría es de depósitos bancarios. */
    fun isDeposit(categoryId: String?): Boolean =
        categoryId == DEPOSITS_ID
}
