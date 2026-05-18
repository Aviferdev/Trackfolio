package es.aviferdev.n3to.platform

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class FeedbackSender(
    private val appVersion: String
) {
    private val firestore: FirebaseFirestore = Firebase.firestore

    actual suspend fun sendFeedback(
        category: String,
        title: String,
        description: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val data = hashMapOf(
            "category" to category,
            "title" to title,
            "description" to description,
            "appVersion" to appVersion,
            "platform" to "android",
            "createdAt" to Timestamp.now()
        )
        firestore.collection("feedback")
            .add(data)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                continuation.resume(Result.failure(error))
            }
    }
}
