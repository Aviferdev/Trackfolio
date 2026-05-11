package es.aviferdev.trackfolio.domain.model

/**
 * Categoría de agrupación para renta fija.
 * Se usa para filtrar emisores y determinar comportamiento de la UI.
 */
enum class FixedIncomeCategory(val label: String) {
    GOVERNMENT("Deuda pública"),
    CORPORATE("Deuda corporativa"),
    DEPOSIT("Depósitos")
}