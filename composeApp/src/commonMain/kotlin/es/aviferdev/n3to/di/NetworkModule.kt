package es.aviferdev.n3to.di

import es.aviferdev.n3to.data.datasource.inflation.InflationRemoteDataSource
import es.aviferdev.n3to.data.datasource.inflation.InflationRemoteDataSourceImpl
import es.aviferdev.n3to.data.datasource.price.ExchangeRateRemoteDataSource
import es.aviferdev.n3to.data.datasource.price.ExchangeRateRemoteDataSourceImpl
import es.aviferdev.n3to.data.datasource.price.PriceRemoteDataSource
import es.aviferdev.n3to.data.datasource.price.PriceRemoteDataSourceImpl
import es.aviferdev.n3to.data.datasource.savingsrates.SavingsRatesRemoteDataSource
import es.aviferdev.n3to.data.datasource.savingsrates.SavingsRatesRemoteDataSourceImpl
import es.aviferdev.n3to.platform.httpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Módulo de red para Koin.
 * Proporciona el HttpClient y las fuentes de datos remotas.
 * Los repositorios se registran en [RepositoryModule] para mantener consistencia.
 */
val networkModule = module {
    single {
        HttpClient(httpClientEngine()) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
        }
    }

    // ── Remote DataSources ──
    single<PriceRemoteDataSource> { PriceRemoteDataSourceImpl(get()) }
    single<ExchangeRateRemoteDataSource> { ExchangeRateRemoteDataSourceImpl(get()) }
    single<SavingsRatesRemoteDataSource> { SavingsRatesRemoteDataSourceImpl(get()) }
    single<InflationRemoteDataSource> { InflationRemoteDataSourceImpl(get()) }
}
