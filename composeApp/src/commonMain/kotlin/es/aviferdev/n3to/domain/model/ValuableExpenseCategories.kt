package es.aviferdev.n3to.domain.model

/**
 * IDs de categorías por defecto para gastos de bienes (valuables).
 * Estas categorías se insertan en [es.aviferdev.n3to.data.database.DatabaseInitializer].
 */
object ValuableExpenseCategories {
    const val TRANSPORT = "cat_exp_val_transport"
    const val REPAIR = "cat_exp_val_repair"
    const val RESTORATION = "cat_exp_val_restoration"
    const val COMMISSION = "cat_exp_val_commission"
    const val STORAGE = "cat_exp_val_storage"
    const val INSURANCE = "cat_exp_val_insurance"
    const val MARKETING = "cat_exp_val_marketing"
    const val OTHER = "cat_exp_val_other"

    val allIds =
        setOf(TRANSPORT, REPAIR, RESTORATION, COMMISSION, STORAGE, INSURANCE, MARKETING, OTHER)
}
