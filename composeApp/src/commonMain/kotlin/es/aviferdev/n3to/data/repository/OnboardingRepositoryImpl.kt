package es.aviferdev.n3to.data.repository

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.repository.OnboardingRepository

class OnboardingRepositoryImpl(
    private val settings: AppSettings
) : OnboardingRepository {

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override suspend fun isCompleted(): Boolean =
        settings.getBool(KEY_ONBOARDING_COMPLETED, false)

    override suspend fun markCompleted() {
        settings.putBool(KEY_ONBOARDING_COMPLETED, true)
    }

    override suspend fun reset() {
        settings.putBool(KEY_ONBOARDING_COMPLETED, false)
    }
}
