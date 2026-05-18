package es.aviferdev.n3to.di

import es.aviferdev.n3to.data.datasource.account.AccountLocalDataSource
import es.aviferdev.n3to.data.datasource.account.AccountLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetCategoryLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetTagLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetTagLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetTransactionLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetTransactionLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.budget.CategoryBudgetLocalDataSource
import es.aviferdev.n3to.data.repository.budget.CategoryBudgetRepositoryImpl
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import es.aviferdev.n3to.data.datasource.debt.DebtLocalDataSource
import es.aviferdev.n3to.data.datasource.debt.DebtLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetPlatformLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetPlatformLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.asset.AssetPriceHistoryLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetPriceHistoryLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.n3to.data.datasource.emergencyfund.EmergencyFundLocalDataSource
import es.aviferdev.n3to.data.datasource.emergencyfund.EmergencyFundLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.goal.GoalLocalDataSource
import es.aviferdev.n3to.data.datasource.goal.GoalLocalDataSourceImpl
import es.aviferdev.n3to.data.repository.emergencyfund.EmergencyFundRepositoryImpl
import es.aviferdev.n3to.data.repository.GoalRepositoryImpl
import es.aviferdev.n3to.data.datasource.assetmetadata.AssetMetadataLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeEventLocalDataSource
import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeEventLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeLocalDataSource
import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.loan.LoanLocalDataSource
import es.aviferdev.n3to.data.datasource.loan.LoanLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.loan.LoanRateChangeLocalDataSource
import es.aviferdev.n3to.data.datasource.loan.LoanRateChangeLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.issuer.IssuerLocalDataSource
import es.aviferdev.n3to.data.datasource.issuer.IssuerLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.platform.PlatformCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.platform.PlatformCategoryLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.n3to.data.datasource.platform.PlatformLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSource
import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.taxprofile.TaxProfileSnapshotLocalDataSource
import es.aviferdev.n3to.data.datasource.taxprofile.TaxProfileSnapshotLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.transaction.TransactionLocalDataSource
import es.aviferdev.n3to.data.datasource.transaction.TransactionLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.realestate.RealEstatePropertyLocalDataSource
import es.aviferdev.n3to.data.datasource.realestate.RealEstatePropertyLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.realestate.RentalPeriodLocalDataSource
import es.aviferdev.n3to.data.datasource.realestate.RentalPeriodLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.valuable.ValuableLocalDataSource
import es.aviferdev.n3to.data.datasource.valuable.ValuableLocalDataSourceImpl
import es.aviferdev.n3to.data.repository.account.AccountRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetCategoryRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetMetadataRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetTagRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetTransactionRepositoryImpl
import es.aviferdev.n3to.data.repository.category.CategoryRepositoryImpl
import es.aviferdev.n3to.data.repository.debt.DebtRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetPlatformRepositoryImpl
import es.aviferdev.n3to.data.repository.asset.AssetPriceHistoryRepositoryImpl
import es.aviferdev.n3to.data.repository.fixedincome.FixedIncomeEventRepositoryImpl
import es.aviferdev.n3to.data.repository.fixedincome.FixedIncomeRepositoryImpl
import es.aviferdev.n3to.data.repository.loan.LoanRepositoryImpl
import es.aviferdev.n3to.data.repository.loan.LoanRateChangeRepositoryImpl
import es.aviferdev.n3to.data.repository.issuer.IssuerRepositoryImpl
import es.aviferdev.n3to.data.repository.platform.PlatformCategoryRepositoryImpl
import es.aviferdev.n3to.data.repository.platform.PlatformRepositoryImpl
import es.aviferdev.n3to.data.repository.transaction.TransactionRepositoryImpl
import es.aviferdev.n3to.data.repository.realestate.RealEstatePropertyRepositoryImpl
import es.aviferdev.n3to.data.repository.realestate.RentalPeriodRepositoryImpl
import es.aviferdev.n3to.data.repository.portfolio.PortfolioRepositoryImpl
import es.aviferdev.n3to.data.repository.taxprofile.TaxProfileSnapshotRepositoryImpl
import es.aviferdev.n3to.data.repository.valuable.ValuableRepositoryImpl
import es.aviferdev.n3to.domain.repository.AccountRepository
import es.aviferdev.n3to.domain.repository.AssetCategoryRepository
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTagRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.CategoryRepository
import es.aviferdev.n3to.domain.repository.DebtRepository
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import es.aviferdev.n3to.domain.repository.AssetPriceHistoryRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import es.aviferdev.n3to.domain.repository.EmergencyFundRepository
import es.aviferdev.n3to.domain.repository.GoalRepository
import es.aviferdev.n3to.domain.repository.LoanRepository
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import es.aviferdev.n3to.domain.repository.IssuerRepository
import es.aviferdev.n3to.domain.repository.PlatformCategoryRepository
import es.aviferdev.n3to.domain.repository.PlatformRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import es.aviferdev.n3to.domain.repository.RentalPeriodRepository
import es.aviferdev.n3to.domain.repository.PortfolioRepository
import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository
import es.aviferdev.n3to.domain.repository.ValuableRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AccountLocalDataSource>              { AccountLocalDataSourceImpl(get()) }
    single<TransactionLocalDataSource>          { TransactionLocalDataSourceImpl(get()) }
    single<TransactionCategoryLocalDataSource>  { TransactionCategoryLocalDataSourceImpl(get()) }
    single<DebtLocalDataSource>                 { DebtLocalDataSourceImpl(get()) }
    single<AssetLocalDataSource>                { AssetLocalDataSourceImpl(get()) }
    single<AssetCategoryLocalDataSource>        { AssetCategoryLocalDataSourceImpl(get()) }
    single<AssetTagLocalDataSource>             { AssetTagLocalDataSourceImpl(get()) }
    single<PlatformLocalDataSource>             { PlatformLocalDataSourceImpl(get()) }
    single<PlatformCategoryLocalDataSource>     { PlatformCategoryLocalDataSourceImpl(get()) }
    single<AssetTransactionLocalDataSource>     { AssetTransactionLocalDataSourceImpl(get()) }
    single<AssetPlatformLocalDataSource>        { AssetPlatformLocalDataSourceImpl(get()) }
    single<AssetPriceHistoryLocalDataSource>    { AssetPriceHistoryLocalDataSourceImpl(get()) }
    single<AssetMetadataLocalDataSource>        { AssetMetadataLocalDataSourceImpl(get()) }
    single<EmergencyFundLocalDataSource> { EmergencyFundLocalDataSourceImpl(get()) }
    single<GoalLocalDataSource> { GoalLocalDataSourceImpl(get()) }
    single<IssuerLocalDataSource>               { IssuerLocalDataSourceImpl(get()) }
    single<FixedIncomeLocalDataSource>          { FixedIncomeLocalDataSourceImpl(get()) }
    single<FixedIncomeEventLocalDataSource>     { FixedIncomeEventLocalDataSourceImpl(get()) }
    single<LoanLocalDataSource>                 { LoanLocalDataSourceImpl(get()) }
    single<LoanRateChangeLocalDataSource>       { LoanRateChangeLocalDataSourceImpl(get()) }

    // ── Real Estate ────────────────────────────────────────────────────────────
    single<RealEstatePropertyLocalDataSource>   { RealEstatePropertyLocalDataSourceImpl(get()) }
    single<RentalPeriodLocalDataSource>          { RentalPeriodLocalDataSourceImpl(get()) }

    // ── Valuable (Bienes) ─────────────────────────────────────────────────────
    single<ValuableLocalDataSource>              { ValuableLocalDataSourceImpl(get()) }

    // ── Tax Profile ────────────────────────────────────────────────────────────
    single<TaxProfileSnapshotLocalDataSource>   { TaxProfileSnapshotLocalDataSourceImpl(get()) }

    // ── Portfolio ────────────────────────────────────────────────────────────────
    single<PortfolioLocalDataSource>   { PortfolioLocalDataSourceImpl(get()) }

    // ── Repositories ────────────────────────────────────────────────────────────
    single<AccountRepository>                   { AccountRepositoryImpl(get()) }
    single<TransactionRepository>               { TransactionRepositoryImpl(get()) }
    single<CategoryRepository>                  { CategoryRepositoryImpl(get()) }
    single<CategoryBudgetRepository>            { CategoryBudgetRepositoryImpl(get()) }
    single<DebtRepository>                      { DebtRepositoryImpl(get()) }
    single<AssetRepository>                     { AssetRepositoryImpl(get()) }
    single<AssetCategoryRepository>             { AssetCategoryRepositoryImpl(get()) }
    single<AssetMetadataRepository>             { AssetMetadataRepositoryImpl(get()) }
    single<AssetTagRepository>                  { AssetTagRepositoryImpl(get()) }
    single<PlatformRepository>                  { PlatformRepositoryImpl(get()) }
    single<PlatformCategoryRepository>          { PlatformCategoryRepositoryImpl(get()) }
    single<AssetTransactionRepository>          { AssetTransactionRepositoryImpl(get()) }
    single<AssetPlatformRepository>             { AssetPlatformRepositoryImpl(get()) }
    single<AssetPriceHistoryRepository>         { AssetPriceHistoryRepositoryImpl(get()) }
    single<EmergencyFundRepository> { EmergencyFundRepositoryImpl(get()) }
    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<IssuerRepository>                    { IssuerRepositoryImpl(get()) }
    single<FixedIncomeRepository>               { FixedIncomeRepositoryImpl(get()) }
    single<FixedIncomeEventRepository>          { FixedIncomeEventRepositoryImpl(get()) }
    single<LoanRepository>                      { LoanRepositoryImpl(get()) }
    single<LoanRateChangeRepository>            { LoanRateChangeRepositoryImpl(get()) }

    // ── Real Estate Repositories ────────────────────────────────────────────────
    single<RealEstatePropertyRepository>        { RealEstatePropertyRepositoryImpl(get()) }
    single<RentalPeriodRepository>              { RentalPeriodRepositoryImpl(get()) }

    // ── Valuable Repositories ───────────────────────────────────────────────────
    single<ValuableRepository>                  { ValuableRepositoryImpl(get()) }

    // ── Tax Profile Repositories ────────────────────────────────────────────────
    single<TaxProfileSnapshotRepository>        { TaxProfileSnapshotRepositoryImpl(get()) }

    // ── Portfolio Repositories ───────────────────────────────────────────────────
    single<PortfolioRepository>                 { PortfolioRepositoryImpl(get()) }
}
