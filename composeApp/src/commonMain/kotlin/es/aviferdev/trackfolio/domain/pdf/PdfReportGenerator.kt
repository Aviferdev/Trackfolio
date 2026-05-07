package es.aviferdev.trackfolio.domain.pdf

import es.aviferdev.trackfolio.domain.model.FiscalReportData

/**
 * Genera un PDF con el informe fiscal y lanza el sistema de compartición
 * nativo (Share Sheet en iOS / Intent.ACTION_SEND en Android).
 *
 * La implementación concreta vive en cada sourceSet (androidMain / iosMain).
 * En Android requiere bindActivity / unbindActivity desde MainActivity.
 */
expect class PdfReportGenerator {

    /**
     * Genera el PDF a partir de [data] y abre el diálogo de guardar/compartir.
     * Si [password] no es null, el PDF se protege con esa contraseña.
     * [onResult] se invoca con (success, errorMessage?) cuando la operación
     * concluye (incluyendo el caso en que el usuario cancela el share sheet).
     */
    fun generate(
        data: FiscalReportData,
        password: String? = null,
        onResult: (success: Boolean, error: String?) -> Unit
    )
}
