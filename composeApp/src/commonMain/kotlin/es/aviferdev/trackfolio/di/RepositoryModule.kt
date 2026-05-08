package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.data.datasource.account.AccountLocalDataSource
import es.aviferdev.trackfolio.data.datasource.account.AccountLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetCategoryLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetTagLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetTagLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetTransactionLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetTransactionLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.debt.DebtLocalDataSource
import es.aviferdev.trackfolio.data.datasource.debt.DebtLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetPlatformLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetPlatformLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.asset.AssetPriceHistoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetPriceHistoryLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.trackfolio.data.datasource.assetmetadata.AssetMetadataLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.fixedincome.FixedIncomeEventLocalDataSource
import es.aviferdev.trackfolio.data.datasource.fixedincome.FixedIncomeEventLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.fixedincome.FixedIncomeLocalDataSource
import es.aviferdev.trackfolio.data.datasource.fixedincome.FixedIncomeLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.issuer.IssuerLocalDataSource
import es.aviferdev.trackfolio.data.datasource.issuer.IssuerLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.platform.PlatformCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.platform.PlatformCategoryLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.trackfolio.data.datasource.platform.PlatformLocalDataSourceImpl
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionLocalDataSource
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionLocalDataSourceImpl
import es.aviferdev.trackfolio.data.repository.account.AccountRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetCategoryRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetMetadataRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetTagRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetTransactionRepositoryImpl
import es.aviferdev.trackfolio.data.repository.category.CategoryRepositoryImpl
import es.aviferdev.trackfolio.data.repository.debt.DebtRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetPlatformRepositoryImpl
import es.aviferdev.trackfolio.data.repository.asset.AssetPriceHistoryRepositoryImpl
import es.aviferdev.trackfolio.data.repository.fixedincome.FixedIncomeEventRepositoryImpl
import es.aviferdev.trackfolio.data.repository.fixedincome.FixedIncomeRepositoryImpl
import es.aviferdev.trackfolio.data.repository.issuer.IssuerRepositoryImpl
import es.aviferdev.trackfolio.data.repository.platform.PlatformCategoryRepositoryImpl
import es.aviferdev.trackfolio.data.repository.platform.PlatformRepositoryImpl
import es.aviferdev.trackfolio.data.repository.transaction.TransactionRepositoryImpl
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import es.aviferdev.trackfolio.domain.repository.AssetCategoryRepository
import es.aviferdev.trackfolio.domain.repository.AssetMetadataRepository
import es.aviferdev.trackfolio.domain.repository.AssetRepository
import es.aviferdev.trackfolio.domain.repository.AssetTagRepository
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import es.aviferdev.trackfolio.domain.repository.IssuerRepository
import es.aviferdev.trackfolio.domain.repository.PlatformCategoryRepository
import es.aviferdev.trackfolio.domain.repository.PlatformRepository
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AccountLocalDataSource>          { AccountLocalDataSourceImpl(get()) }
    single<TransactionLocalDataSource>      { TransactionLocalDataSourceImpl(get()) }
    single<TransactionCategoryLocalDataSource>         { TransactionCategoryLocalDataSourceImpl(get()) }
    single<DebtLocalDataSource>             { DebtLocalDataSourceImpl(get()) }
    single<AssetLocalDataSource>            { AssetLocalDataSourceImpl(get()) }
    single<AssetCategoryLocalDataSource>    { AssetCategoryLocalDataSourceImpl(get()) }
    single<AssetTagLocalDataSource>         { AssetTagLocalDataSourceImpl(get()) }
    single<PlatformLocalDataSource>         { PlatformLocalDataSourceImpl(get()) }
    single<PlatformCategoryLocalDataSource>  { PlatformCategoryLocalDataSourceImpl(get()) }
    single<AssetTransactionLocalDataSource> { AssetTransactionLocalDataSourceImpl(get()) }
    single<AssetPlatformLocalDataSource>    { AssetPlatformLocalDataSourceImpl(get()) }
    single<AssetPriceHistoryLocalDataSource>  { AssetPriceHistoryLocalDataSourceImpl(get()) }
    single<AssetMetadataLocalDataSource>   { AssetMetadataLocalDataSourceImpl(get()) }
    single<IssuerLocalDataSource>           { IssuerLocalDataSourceImpl(get()) }
    single<FixedIncomeLocalDataSource>      { FixedIncomeLocalDataSourceImpl(get()) }
    single<FixedIncomeEventLocalDataSource> { FixedIncomeEventLocalDataSourceImpl(get()) }

    single<AccountRepository>          { AccountRepositoryImpl(get()) }
    single<TransactionRepository>      { TransactionRepositoryImpl(get()) }
    single<CategoryRepository>         { CategoryRepositoryImpl(get()) }
    single<DebtRepository>             { DebtRepositoryImpl(get()) }
    single<AssetRepository>            { AssetRepositoryImpl(get()) }
    single<AssetCategoryRepository>    { AssetCategoryRepositoryImpl(get()) }
    single<AssetMetadataRepository> { AssetMetadataRepositoryImpl(get()) }
    single<AssetTagRepository>         { AssetTagRepositoryImpl(get()) }
    single<PlatformRepository>         { PlatformRepositoryImpl(get()) }
    single<PlatformCategoryRepository>  { PlatformCategoryRepositoryImpl(get()) }
    single<AssetTransactionRepository> { AssetTransactionRepositoryImpl(get()) }
    single<AssetPlatformRepository>    { AssetPlatformRepositoryImpl(get()) }
    single<AssetPriceHistoryRepository>       { AssetPriceHistoryRepositoryImpl(get()) }
    single<IssuerRepository>           { IssuerRepositoryImpl(get()) }
    single<FixedIncomeRepository>      { FixedIncomeRepositoryImpl(get()) }
    single<FixedIncomeEventRepository> { FixedIncomeEventRepositoryImpl(get()) }
}
