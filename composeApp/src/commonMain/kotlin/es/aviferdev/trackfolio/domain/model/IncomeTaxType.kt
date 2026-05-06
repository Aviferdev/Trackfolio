package es.aviferdev.trackfolio.domain.model

/**
 * Tipos de rendimiento del IRPF español.
 * Determina la casilla de la declaración y, orientativamente,
 * el porcentaje de retención habitual.
 */
enum class IncomeTaxType(
    val label: String,
    val defaultIrpfPercent: Double?,   // % orientativo; el usuario puede cambiarlo
    val emoji: String
) {
    RENDIMIENTOS_TRABAJO(
        label              = "Rendimientos del trabajo",
        defaultIrpfPercent = null,     // varía según el tramo; el usuario lo introduce
        emoji              = "💼"
    ),
    CAPITAL_MOBILIARIO(
        label              = "Capital mobiliario",
        defaultIrpfPercent = 19.0,     // dividendos, intereses, seguros de vida
        emoji              = "📈"
    ),
    CAPITAL_INMOBILIARIO(
        label              = "Capital inmobiliario",
        defaultIrpfPercent = 19.0,     // arrendamientos
        emoji              = "🏠"
    ),
    ACTIVIDADES_ECONOMICAS(
        label              = "Actividades económicas",
        defaultIrpfPercent = 15.0,     // autónomos / profesionales
        emoji              = "🧾"
    ),
    GANANCIAS_PATRIMONIALES(
        label              = "Ganancias patrimoniales",
        defaultIrpfPercent = 19.0,     // transmisión de activos, premios
        emoji              = "💰"
    ),
    SIN_RETENCION(
        label              = "Sin retención / Otro",
        defaultIrpfPercent = 0.0,
        emoji              = "📋"
    );

    companion object {
        fun fromName(name: String?): IncomeTaxType? =
            name?.let { entries.firstOrNull { e -> e.name == it } }
    }
}
