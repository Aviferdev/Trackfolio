package es.aviferdev.n3to.domain.model

/**
 * Categoría de activo del portfolio (ej. "Cryptos", "ETFs", "Bonos",
 * "Fondos indexados"). Las define el usuario desde Ajustes.
 *
 * Es una entidad totalmente independiente de [Category] (que se usa para
 * gastos/ingresos).
 */
data class AssetCategory(
    val id: String,
    val name: String,
    val icon: String = "📦",
    val sortOrder: Int = 0,
    val archived: Boolean = false,
    val createdAt: Long
)
