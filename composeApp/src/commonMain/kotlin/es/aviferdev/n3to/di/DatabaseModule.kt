package es.aviferdev.n3to.di

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.data.database.DatabaseDriverFactory
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.data.database.DatabaseMigrationHelper
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.datasource.budget.CategoryBudgetLocalDataSource
import es.aviferdev.n3to.data.datasource.budget.CategoryBudgetLocalDataSourceImpl
import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSource
import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSourceImpl
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { N3toDatabase(get()) }
    single { DatabaseInitializer(get(), get(), get()) }
    single { DatabaseMigrationHelper(get<AppSettings>(), get()) }
    single<PortfolioLocalDataSource> { PortfolioLocalDataSourceImpl(get()) }
    single<CategoryBudgetLocalDataSource> { CategoryBudgetLocalDataSourceImpl(get()) }
}
