package es.aviferdev.n3to.domain.model

/**
 * Datos consolidados para el informe fiscal.
 * Se recopilan de todos los repositorios en [GetFiscalReportDataUseCase].
 */
data class FiscalReportData(
    val accountName: String,
    val year: String,
    val generatedAt: Long,
    val currencySymbol: String = AppCurrency.EUR.symbol,
    val annualSummary: AnnualSummary?,
    val monthlyBreakdown: List<MonthlyTotals>,
    val activeDebts: List<Debt>,
    val assetPositions: List<AssetPosition>,
    /** Desglose de ingresos por tipo de rendimiento IRPF (vacío si no hay datos fiscales). */
    val incomeTaxBreakdown: List<FiscalIncomeTaxBreakdown> = emptyList(),
    /** Indica si hay ingresos registrados solo con neto (sin desglose fiscal). */
    val hasNetOnlyIncomes: Boolean = false,
    /** Transacciones de ingreso individuales del año para el desglose detallado en el PDF. */
    val yearlyIncomes: List<Transaction> = emptyList()
)

/**
 * Posición consolidada de un activo para el informe fiscal.
 */
data class AssetPosition(
    val ticker: String,
    val name: String,
    val categoryName: String?,
    val netQuantity: Double,
    val avgCostBasis: Double,
    val totalCost: Double,
    val currentPrice: Double?,
    val currentValue: Double?,
    val unrealizedPnl: Double?,
    val realizedPnl: Double,
    val totalBought: Double,
    val totalSold: Double,
    val yearTransactions: List<AssetTransaction>
)
