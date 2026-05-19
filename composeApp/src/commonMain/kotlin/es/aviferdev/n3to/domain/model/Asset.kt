package es.aviferdev.n3to.domain.model

/**
 * Catálogo del activo: ficha de identidad. Los datos de cuántas unidades
 * tienes y a qué precio las compraste viven en [AssetTransaction].
 *
 * `currentPrice` representa la cotización actual editable por el usuario;
 * mientras sea NULL, los cálculos de P&L caen en 0 ("Sin precio actual").
 * No aplica a renta fija (bonos/depósitos).
 *
 * `maturityDate` es la fecha de vencimiento en epoch millis. Solo aplica
 * a bonos y depósitos.
 *
 * `archived`: los activos NUNCA se borran, solo se archivan.
 *
 * `isin`: Código ISIN del activo (opcional). Se usa para consultar precio
 * automático a través de APIs externas. Se guarda normalizado (sin guiones).
 *
 * `priceSource`: Origen del precio actual (MANUAL = introducido por el usuario,
 * AUTO = obtenido de la API).
 *
 * `isinValidatedAt`: Timestamp de la última vez que el ISIN fue validado
 * exitosamente contra la API.
 *
 * `isinValidationError`: Si no es null, indica que el ISIN no se pudo validar.
 * Valores posibles: "NOT_FOUND", "API_ERROR".
 */
data class Asset(
    val id: String,
    val accountId: String,
    val portfolioId: String? = null,
    val ticker: String,
    val name: String,
    val notes: String?,
    val createdAt: Long,
    val assetCategoryId: String? = null,
    val currentPrice: Double? = null,
    val currentPriceUpdatedAt: Long? = null,
    val archived: Boolean = false,
    val maturityDate: Long? = null,
    // ── Nuevos campos para auto-precio ──
    val isin: String? = null,
    val priceSource: PriceSource = PriceSource.MANUAL,
    val isinValidatedAt: Long? = null,
    val isinValidationError: String? = null
) {
    val hasCurrentPrice: Boolean get() = currentPrice != null

    /** True si el activo tiene ISIN y puede obtener precio automático. */
    val hasIsin: Boolean get() = !isin.isNullOrBlank()

    /** True si el ISIN se ha validado recientemente (sin errores). */
    val isIsinValid: Boolean get() = isinValidatedAt != null && isinValidationError == null
}
