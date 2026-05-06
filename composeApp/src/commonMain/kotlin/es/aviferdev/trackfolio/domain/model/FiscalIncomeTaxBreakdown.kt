package es.aviferdev.trackfolio.domain.model

/**
 * Desglose fiscal de los ingresos agrupado por tipo de rendimiento IRPF.
 * Alimenta la sección fiscal del informe y el PDF.
 */
data class FiscalIncomeTaxBreakdown(
    val taxType: IncomeTaxType,
    /** Número de ingresos clasificados con este tipo. */
    val count: Int,
    /** Suma de importes brutos (grossAmount). Puede ser 0 si ninguno tenía bruto introducido. */
    val grossTotal: Double,
    /** Suma de importes netos (amount). */
    val netTotal: Double,
    /** Suma de IRPF retenido (grossAmount - amount). */
    val irpfTotal: Double,
    /** Retención media ponderada efectiva (irpfTotal / grossTotal × 100). */
    val avgIrpfPercent: Double
)
