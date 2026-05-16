package es.aviferdev.trackfolio.domain.model

enum class RentalStatus(val label: String, val emoji: String) {
    OWN_USE("Uso propio", "\uD83C\uDFE0"),
    RENTED("Alquilada", "\uD83D\uDCB0"),
    VACANT("Vacía", "\uD83D\uDD12");

    companion object {
        fun fromName(name: String?): RentalStatus =
            entries.firstOrNull { it.name == name } ?: OWN_USE
    }
}
