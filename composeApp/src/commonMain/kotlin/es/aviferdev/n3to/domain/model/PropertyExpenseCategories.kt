package es.aviferdev.n3to.domain.model

/**
 * IDs de categorías por defecto para gastos inmobiliarios.
 * Estas categorías se insertan en [es.aviferdev.n3to.data.database.DatabaseInitializer].
 */
object PropertyExpenseCategories {
    const val NOTARY = "cat_exp_prop_notary"
    const val REGISTRY = "cat_exp_prop_registry"
    const val TAX = "cat_exp_prop_tax"
    const val AGENCY = "cat_exp_prop_agency"
    const val APPRAISAL = "cat_exp_prop_appraisal"
    const val GESTORIA = "cat_exp_prop_gestoria"
    const val MUNICIPAL = "cat_exp_prop_municipal"

    val allIds = setOf(NOTARY, REGISTRY, TAX, AGENCY, APPRAISAL, GESTORIA, MUNICIPAL)
}
