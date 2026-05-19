package es.aviferdev.n3to.domain.model

/**
 * Origen del precio actual de un activo.
 * - [MANUAL]: Precio introducido por el usuario.
 * - [AUTO]: Precio obtenido automáticamente de una API externa.
 */
enum class PriceSource(val label: String) {
    MANUAL("Manual"),
    AUTO("Automático")
}
