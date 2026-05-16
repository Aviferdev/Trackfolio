package es.aviferdev.n3to.domain.repository

interface OnboardingRepository {
    suspend fun isCompleted(): Boolean
    suspend fun markCompleted()
    suspend fun reset()
}
