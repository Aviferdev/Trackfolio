package es.aviferdev.n3to.platform

/**
 * Stub de FeedbackSender para iOS.
 * PENDIENTE: Implementar con Firebase Firestore vía SPM.
 * Cuando se configure SPM en el proyecto, reemplazar por el SDK nativo.
 */
actual class FeedbackSender {
    actual suspend fun sendFeedback(
        category: String,
        title: String,
        description: String
    ): Result<Unit> {
        println("[FeedbackSender] iOS stub — feedback recibido pero no enviado a Firestore (pendiente SPM)")
        println("[FeedbackSender] category=$category, title=$title")
        // Devolvemos éxito para no bloquear el flujo de UI mientras tanto.
        return Result.success(Unit)
    }
}
