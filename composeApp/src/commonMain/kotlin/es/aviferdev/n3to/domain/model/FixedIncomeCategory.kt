package es.aviferdev.n3to.domain.model

/**
 * Categoría de agrupación para renta fija.
 * Se usa para filtrar emisores y determinar comportamiento de la UI.
 */
enum class FixedIncomeCategory(val label: String, val resourceKey: String) {
    GOVERNMENT("Deuda pública", "fixedincome_category_government"),
    CORPORATE("Deuda corporativa", "fixedincome_category_corporate"),
    DEPOSIT("Depósitos", "fixedincome_category_deposit")
}