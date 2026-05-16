package es.aviferdev.n3to.di

import es.aviferdev.n3to.data.database.DatabaseDriverFactory
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.data.database.N3toDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { N3toDatabase(get()) }
    single { DatabaseInitializer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
