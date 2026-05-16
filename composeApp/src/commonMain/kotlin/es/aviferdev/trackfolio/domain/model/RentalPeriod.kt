package es.aviferdev.trackfolio.domain.model

/**
 * Período concreto de actividad de alquiler de una propiedad.
 * Se crea al activar RENTED y se cierra al cambiar de estado.
 * El período activo tiene endDate = null.
 */
data class RentalPeriod(
    val id: String,
    val propertyId: String,
    val startDate: Long,
    val endDate: Long?,            // null = período activo
    val monthlyRent: Double,
    val notes: String?
) {
    val isActive: Boolean get() = endDate == null
}
