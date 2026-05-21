package es.aviferdev.n3to.di

import es.aviferdev.n3to.data.repository.OnboardingRepositoryImpl
import es.aviferdev.n3to.domain.repository.OnboardingRepository
import es.aviferdev.n3to.domain.usecase.onboarding.IsOnboardingCompletedUseCase
import es.aviferdev.n3to.domain.usecase.onboarding.MarkOnboardingCompletedUseCase
import es.aviferdev.n3to.domain.usecase.onboarding.ResetOnboardingUseCase
import es.aviferdev.n3to.ui.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single<OnboardingRepository> {
        OnboardingRepositoryImpl(get()) // AppSettings
    }
    factory { IsOnboardingCompletedUseCase(get()) }
    factory { MarkOnboardingCompletedUseCase(get()) }
    factory { ResetOnboardingUseCase(get()) }
    viewModel { OnboardingViewModel(get()) }
}
