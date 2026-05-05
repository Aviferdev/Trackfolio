package es.aviferdev.trackfolio.domain.model

/**
 * Etiqueta libre para activos (ej. "Tecnología", "Salud", "USA", "Europa").
 *
 * Una etiqueta puede estar ligada a una [AssetCategory] (solo aplica a los
 * activos de esa categoría — útil para "sectores de ETF" o "regiones de bonos")
 * o ser global (categoryId == null) y aplicar a cualquier activo.
 */
data class AssetTag(
    val id: String,
    val name: String,
    val categoryId: String? = null,
    val color: String       = "#3D6EAD",
    val archived: Boolean   = false,
    val createdAt: Long
)

/**
 * Asignación de una etiqueta a un activo concreto, con un peso porcentual.
 * Por ejemplo, un ETF puede tener 60% Tecnología + 40% Salud.
 *
 * El cliente valida que la suma de pesos de un mismo asset sea ≤ 100.
 */
data class AssetTagAssignment(
    val assetId: String,
    val tag: AssetTag,
    val weight: Double      // 0..100 (porcentaje de exposición)
)
