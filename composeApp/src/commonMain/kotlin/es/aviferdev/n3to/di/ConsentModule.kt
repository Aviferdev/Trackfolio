package es.aviferdev.n3to.di

import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.data.datasource.ConsentLocalDataSource
import es.aviferdev.n3to.data.datasource.ConsentLocalDataSourceImpl
import es.aviferdev.n3to.data.repository.ConsentRepositoryImpl
import es.aviferdev.n3to.domain.repository.ConsentRepository
import es.aviferdev.n3to.domain.usecase.consent.GetConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.HasUserDecidedUseCase
import es.aviferdev.n3to.domain.usecase.consent.RevokeConsentUseCase
import es.aviferdev.n3to.domain.usecase.consent.SaveConsentUseCase
import es.aviferdev.n3to.ui.consent.ConsentViewModel
import es.aviferdev.n3to.ui.settings.PrivacySettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val consentModule = module {

    // Platform trackers (expect/actual - registrados en platformModule)
    // AnalyticsTracker, CrashlyticsTracker, PurchaseManager vienen de androidModule/iosModule

    // Premium
    single {
        PremiumManager(
            purchaseManager = get()
        )
    }

    // Data
    single<ConsentLocalDataSource> {
        ConsentLocalDataSourceImpl(get()) // AppSettings
    }
    single<ConsentRepository> {
        ConsentRepositoryImpl(get())
    }

    // UseCases
    factory { SaveConsentUseCase(get()) }
    factory { GetConsentUseCase(get()) }
    factory { HasUserDecidedUseCase(get()) }
    factory { RevokeConsentUseCase(get()) }

    // ViewModels
    viewModel {
        ConsentViewModel(
            saveConsentUseCase = get(),
            analyticsTracker = get()
        )
    }
    viewModel {
        PrivacySettingsViewModel(
            getConsentUseCase = get(),
            saveConsentUseCase = get(),
            revokeConsentUseCase = get(),
            analyticsTracker = get(),
            crashlyticsTracker = get(),
            premiumManager = get()
        )
    }
}
