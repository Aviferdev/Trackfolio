package es.aviferdev.n3to.domain.model

/**
 * Clasifica las categorías de activo según el comportamiento que deben tener
 * en la aplicación.
 *
 * Esta clasificación se usa para mostrar/ocultar campos y funcionalidades
 * específicas según el tipo de activo:
 *
 * - [isInvestment]: Activos con precio de mercado (soportan currentPrice, priceHistory, buy/sell)
 * - [isAnalyzable]: Activos analizables (soportan sectores, regiones, composición RF/RV, dividendos)
 * - [isFixedIncome]: Activos de renta fija (soportan maturityDate, NO priceHistory ni buy/sell)
 *
 * Se usa el ID fijo de [es.aviferdev.n3to.data.database.DatabaseInitializer]
 * para evitar una migración de base de datos.
 */
object AssetCategoryType {

    /**
     * Categorías de activos de inversión con precio de mercado.
     * Estos activos pueden tener precio actual, historial de precios y transacciones de compra/venta.
     *
     * Excluye:
     * - fixed_cat_fixedincome (usa el sistema FixedIncomePosition)
     */
    private val INVESTMENT_IDS = setOf(
        "fixed_cat_stocks",    // Acciones
        "fixed_cat_etfs",      // ETFs
        "fixed_cat_funds",     // Fondos de inversión
        "fixed_cat_crypto",    // Criptomonedas
        "fixed_cat_pensions",  // Planes de pensiones
        "fixed_cat_commodities", // Materias primas
        "fixed_cat_crowdlending" // Crowdlending
    )

    /**
     * Categorías de activos analizables con sectores, regiones y composición RF/RV.
     * Solo los activos tradicionales de mercado tienen sentido con estas características.
     */
    private val ANALYZABLE_IDS = setOf(
        "fixed_cat_stocks",    // Acciones
        "fixed_cat_etfs",      // ETFs
        "fixed_cat_funds"      // Fondos de inversión
    )

    /**
     * Categoría de renta fija (bonos y depósitos).
     * Estos activos usan el sistema [FixedIncomePosition] y no tienen precio de mercado.
     */
    private val FIXED_INCOME_IDS = setOf(
        "fixed_cat_fixedincome" // Renta fija
    )

    /**
     * True si la categoría admite precio de mercado, historial de precios y transacciones.
     * False para renta fija (que usa el sistema FixedIncomePosition).
     */
    fun isInvestment(categoryId: String?): Boolean =
        categoryId in INVESTMENT_IDS

    /**
     * True si la categoría admite sectores, regiones, composición RF/RV y dividendos.
     * Solo aplica a Acciones, ETFs y Fondos de inversión.
     */
    fun isAnalyzable(categoryId: String?): Boolean =
        categoryId in ANALYZABLE_IDS

    /**
     * True si la categoría es de renta fija (bonos/depósitos).
     * Estos activos usan el sistema [FixedIncomePosition] en lugar del sistema de activos genéricos.
     */
    fun isFixedIncome(categoryId: String?): Boolean =
        categoryId in FIXED_INCOME_IDS
}