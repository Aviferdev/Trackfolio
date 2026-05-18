package es.aviferdev.n3to.domain.model

/** 
 * Tipo de propiedad inmobiliaria.
 * Diseñado para extensibilidad futura (GARAGE, COMMERCIAL, LAND, STORAGE_ROOM).
 */
enum class PropertyType(val label: String, val emoji: String, val resourceKey: String) {
    PRIMARY_HOME("Vivienda habitual", "\uD83C\uDFE0", "property_type_primary_home"),
    SECONDARY_HOME("Segunda residencia", "\uD83C\uDFD6\uFE0F", "property_type_secondary_home"),
    INVESTMENT("Inversión", "\uD83C\uDFE2", "property_type_investment");

    companion object {
        fun fromName(name: String?): PropertyType =
            entries.firstOrNull { it.name == name } ?: PRIMARY_HOME
    }
}
