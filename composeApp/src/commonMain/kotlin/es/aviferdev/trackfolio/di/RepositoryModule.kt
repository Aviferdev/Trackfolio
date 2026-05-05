package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSource
import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.AssetLocalDataSource
import es.aviferdev.trackfolio.data.datasource.AssetLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.DebtLocalDataSource
import es.aviferdev.trackfolio.data.datasource.DebtLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.TransactionLocalDataSource
import es.aviferdev.trackfolio.data.datasource.TransactionLocalDataSourceImpl
import es.aviferdev.trackfolio.data.repository.AccountRepositoryImpl
import es.aviferdev.trackfolio.data.repository.AssetRepositoryImpl
import es.aviferdev.trackfolio.data.repository.CategoryRepositoryImpl
import es.aviferdev.trackfolio.data.repository.DebtRepositoryImpl
import es.aviferdev.trackfolio.data.repository.TransactionRepositoryImpl
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AccountLocalDataSource>     { AccountLocalDataSourceImpl(get()) }
    single<TransactionLocalDataSource> { TransactionLocalDataSourceImpl(get()) }
    single<CategoryLocalDataSource>    { CategoryLocalDataSourceImpl(get()) }
    single<DebtLocalDataSource>        { DebtLocalDataSourceImpl(get()) }
    single<AssetLocalDataSource>       { AssetLocalDataSourceImpl(get()) }

    single<AccountRepository>     { AccountRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository>    { CategoryRepositoryImpl(get()) }
    single<DebtRepository>        { DebtRepositoryImpl(get()) }
    single<AssetRepository>       { AssetRepositoryImpl(get()) }
}
