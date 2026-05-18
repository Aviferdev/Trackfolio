package es.aviferdev.n3to.domain.model

enum class RentalStatus(val label: String, val emoji: String, val resourceKey: String) {
    OWN_USE("Uso propio", "\uD83C\uDFE0", "rental_status_own_use"),
    RENTED("Alquilada", "\uD83D\uDCB0", "rental_status_rented"),
    VACANT("Vacía", "\uD83D\uDD12", "rental_status_vacant");

    companion object {
        fun fromName(name: String?): RentalStatus =
            entries.firstOrNull { it.name == name } ?: OWN_USE
    }
}
