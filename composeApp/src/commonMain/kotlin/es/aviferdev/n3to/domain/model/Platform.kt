package es.aviferdev.n3to.domain.model

/**
 * Plataforma donde se ejecutó un movimiento de portfolio (broker, exchange,
 * banco). Gestionada desde Ajustes. Soporta archivado lógico para preservar
 * la integridad referencial con movimientos antiguos.
 */
data class Platform(
    val id: String,
    val name: String,
    val icon: String      = "🏦",
    val sortOrder: Int    = 0,
    val archived: Boolean = false,
    val createdAt: Long,
    val notes: String?    = null
)
