package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.domain.usecase.account.DeleteAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.trackfolio.domain.usecase.account.SaveAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.GetActiveDebtsUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.MarkDebtAsPaidUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.SaveDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetAnnualSummaryUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.ui.home.AddTransactionViewModel
import es.aviferdev.trackfolio.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetAccountsUseCase(get()) }
    factory { SaveAccountUseCase(get()) }
    factory { DeleteAccountUseCase(get()) }

    factory { GetTransactionsByMonthUseCase(get()) }
    factory { GetMonthlyTotalsUseCase(get()) }
    factory { SaveTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetAnnualSummaryUseCase(get()) }

    factory { GetHomeBalanceUseCase(get(), get(), get()) }

    factory { GetActiveDebtsUseCase(get()) }
    factory { SaveDebtUseCase(get()) }
    factory { MarkDebtAsPaidUseCase(get()) }

    factory { GetCategoriesByTypeUseCase(get()) }

    viewModel { HomeViewModel(get()) }
    viewModel { AddTransactionViewModel(get(), get()) }
}
