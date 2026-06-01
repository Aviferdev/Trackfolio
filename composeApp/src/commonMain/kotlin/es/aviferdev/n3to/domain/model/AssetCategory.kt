package es.aviferdev.n3to.domain.model

/**
 * Categoría de activo del portfolio (ej. "Cryptos", "ETFs", "Bonos",
 * "Fondos indexados"). Las define el usuario desde Ajustes.
 *
 * Es una entidad totalmente independiente de [Category] (que se usa para
 * gastos/ingresos).
 *
 * @property isQuotable Indica si los activos de esta categoría pueden tener
 * precio automático (vía ISIN). Reemplaza los IDs hardcodeados en consultas SQL.
 */
data class AssetCategory(
    val id: String,
    val name: String,
    val icon: String = "📦",
    val sortOrder: Int = 0,
    val archived: Boolean = false,
    val isQuotable: Boolean = false,
    val createdAt: Long
)
