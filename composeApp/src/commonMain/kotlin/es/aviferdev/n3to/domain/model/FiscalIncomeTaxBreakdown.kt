package es.aviferdev.n3to.domain.model

/**
 * Desglose fiscal de los ingresos agrupado por tipo de ingreso.
 * Alimenta la sección fiscal del informe y el PDF.
 */
data class FiscalIncomeTaxBreakdown(
    val incomeType: IncomeType,
    /** Número de ingresos clasificados con este tipo. */
    val count: Int,
    /** Suma de importes brutos (grossAmount). */
    val grossTotal: Double,
    /** Suma de importes netos (amount). */
    val netTotal: Double,
    /** Suma de IRPF retenido. */
    val irpfTotal: Double,
    /** Suma de cotizaciones a la Seguridad Social (solo SALARY). */
    val socialSecurityTotal: Double = 0.0,
    /** Suma de comisiones (solo BOND_DEPOSIT). */
    val commissionTotal: Double = 0.0,
    /** Retención media ponderada efectiva (irpfTotal / grossTotal × 100). */
    val avgIrpfPercent: Double
)
