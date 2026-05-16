package es.aviferdev.n3to.domain.usecase.onboarding

import es.aviferdev.n3to.domain.repository.OnboardingRepository

class MarkOnboardingCompletedUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() = repository.markCompleted()
}
