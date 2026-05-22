package es.aviferdev.n3to.domain.model

/**
 * Representa el desglose de activos por tipo para el gráfico donut de patrimonio.
 * No contiene colores ni dependencias de Compose; los colores se asignan en la capa UI.
 */
data class AssetBreakdown(
    val icon: String,
    val name: String,
    val amount: Double,
    val percent: Double
)
