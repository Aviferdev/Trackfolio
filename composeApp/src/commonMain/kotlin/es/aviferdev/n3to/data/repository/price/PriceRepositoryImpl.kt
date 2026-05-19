package es.aviferdev.n3to.data.repository.price

import es.aviferdev.n3to.data.datasource.price.PriceRemoteDataSource
import es.aviferdev.n3to.domain.model.IdentifierType
import es.aviferdev.n3to.domain.model.PriceQuote
import es.aviferdev.n3to.domain.repository.PriceRepository

/**
 * Implementación de [PriceRepository] que delega en [PriceRemoteDataSource]
 * para obtener cotizaciones desde APIs externas.
 */
class PriceRepositoryImpl(
    private val remoteDataSource: PriceRemoteDataSource
) : PriceRepository {

    override suspend fun getQuote(identifier: String, type: IdentifierType): Result<PriceQuote> {
        return remoteDataSource.fetchQuote(identifier, type)
    }

    override suspend fun validateIdentifier(identifier: String, type: IdentifierType): Result<PriceQuote> {
        println("[PriceRepo] 🔍 validateIdentifier: $identifier (type=$type)")
        // Intenta obtener la cotización directamente
        return remoteDataSource.fetchQuote(identifier, type)
            .recoverCatching { fetchError ->
                println("[PriceRepo] ⚠️ fetchQuote falló: ${fetchError.message}, intentando search...")
                // Si la cotización directa falla, intenta buscar el identificador
                val searchResult = remoteDataSource.searchIdentifier(identifier)
                if (searchResult.isFailure) {
                    println("[PriceRepo] ❌ search también falló: ${searchResult.exceptionOrNull()?.message}")
                    throw fetchError // Propaga el error original
                }
                val searchResults = searchResult.getOrThrow()
                println("[PriceRepo] Search devolvió ${searchResults.size} resultados")
                val match = searchResults.firstOrNull { quote ->
                    quote.identifier.equals(identifier, ignoreCase = true) ||
                    quote.identifier.replace(".", "").equals(identifier, ignoreCase = true)
                }
                if (match == null) {
                    println("[PriceRepo] ❌ No se encontró coincidencia en search")
                    throw fetchError
                }
                // Si encontramos por search, intentamos obtener la cotización con el símbolo encontrado
                println("[PriceRepo] ✅ Encontrado por search: ${match.identifier}, obteniendo precio...")
                remoteDataSource.fetchQuote(match.identifier, match.identifierType).getOrElse { match }
            }
    }

    override suspend fun isServiceAvailable(): Boolean {
        return remoteDataSource.healthCheck()
    }
}
