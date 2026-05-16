package es.aviferdev.n3to.domain.model

/**
 * Identifica las categorías de activo que permiten traspasos fiscalmente
 * neutros (sin generar hecho imponible en España).
 *
 * Actualmente son:
 * - Fondos de inversión (fixed_cat_funds)
 * - Planes de pensiones (fixed_cat_pensions)
 *
 * Se usa el ID fijo de [DatabaseInitializer] para evitar una migración de BBDD.
 */
object TransferableCategories {

    private val TRANSFERABLE_IDS = setOf(
        "fixed_cat_funds",
        "fixed_cat_pensions"
    )

    /** True si la categoría admite traspasos entre activos. */
    fun isTransferable(categoryId: String?): Boolean =
        categoryId in TRANSFERABLE_IDS
}
