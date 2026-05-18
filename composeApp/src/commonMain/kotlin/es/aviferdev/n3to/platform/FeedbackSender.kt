package es.aviferdev.n3to.platform

/**
 * Envía feedback del usuario a Firebase Firestore.
 * - Android: usa el SDK nativo de Firestore.
 * - iOS: stub pendiente de implementar con SPM.
 */
expect class FeedbackSender {
    suspend fun sendFeedback(
        category: String,
        title: String,
        description: String
    ): Result<Unit>
}
