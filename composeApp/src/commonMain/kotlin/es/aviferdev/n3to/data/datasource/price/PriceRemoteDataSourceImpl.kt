package es.aviferdev.n3to.data.datasource.price

import es.aviferdev.n3to.domain.model.IdentifierType
import es.aviferdev.n3to.domain.model.PriceQuote
import es.aviferdev.n3to.platform.nowMillis
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Implementación de [PriceRemoteDataSource] usando la API pública de Yahoo Finance.
 *
 * Yahoo Finance no tiene una API oficial, pero los endpoints que se usan aquí
 * son ampliamente utilizados por la comunidad y han sido estables durante años.
 *
 * Endpoints utilizados:
 * - Chart: https://query1.finance.yahoo.com/v8/finance/chart/{ISIN}?interval=1d&range=1d
 * - Search: https://query1.finance.yahoo.com/v1/finance/search?q={query}
 */
class PriceRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : PriceRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun fetchQuote(identifier: String, type: IdentifierType): Result<PriceQuote> {
        return try {
            val symbol = resolveSymbol(identifier, type)
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol"
            println("[PriceRefresh] 📡 GET $url")
            val response = httpClient.get(url) {
                parameter("interval", "1d")
                parameter("range", "1d")
            }
            println("[PriceRefresh] ✅ ${response.status} $identifier")
            val body = response.bodyAsText()
            val quote = parseYahooChartResponse(body, identifier, type)
            println("[PriceRefresh] 💰 $identifier → ${quote.price} ${quote.currency} (${quote.name ?: "?"})")
            Result.success(quote)
        } catch (e: Exception) {
            println("[PriceRefresh] ❌ $identifier → ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun searchIdentifier(query: String): Result<List<PriceQuote>> {
        return try {
            val url = "https://query1.finance.yahoo.com/v1/finance/search"
            val response = httpClient.get(url) {
                parameter("q", query)
            }
            val body = response.bodyAsText()
            val quotes = parseYahooSearchResponse(body, query)
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean {
        return try {
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/AAPL") {
                parameter("interval", "1d")
                parameter("range", "1d")
            }
            response.status.value in 200..299
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resuelve el símbolo a usar en la API de Yahoo Finance según el tipo de identificador.
     */
    private fun resolveSymbol(identifier: String, type: IdentifierType): String = when (type) {
        IdentifierType.ISIN -> identifier
        IdentifierType.CRYPTO_SYMBOL -> "$identifier-USD"
        IdentifierType.TICKER -> identifier
    }

    /**
     * Parsea la respuesta del endpoint /chart/ de Yahoo Finance.
     * Formato esperado:
     * {
     *   "chart": {
     *     "result": [{
     *       "meta": {
     *         "currency": "EUR",
     *         "exchangeName": "XETRA",
     *         "shortName": "Apple Inc.",
     *         "regularMarketPrice": 182.40,
     *         "previousClose": 181.90
     *       },
     *       "timestamp": [1234567890],
     *       "indicators": {
     *         "quote": [{ "close": [182.40] }]
     *       }
     *     }]
     *   }
     * }
     */
    private fun parseYahooChartResponse(
        body: String,
        identifier: String,
        type: IdentifierType
    ): PriceQuote {
        val root = json.parseToJsonElement(body).jsonObject
        val chart = root["chart"]?.jsonObject
            ?: throw IllegalStateException("Respuesta sin 'chart': $body")
        val result = chart["result"]?.jsonArray
            ?: throw IllegalStateException("Respuesta sin 'result': $body")
        if (result.isEmpty()) {
            val err = chart["error"]?.jsonObject
            val errMsg = err?.let { e ->
                e["description"]?.jsonPrimitive?.content ?: e["code"]?.jsonPrimitive?.content
            } ?: "Not Found"
            throw IllegalStateException("Yahoo Finance: $errMsg")
        }
        val meta = result[0].jsonObject["meta"]?.jsonObject
            ?: throw IllegalStateException("Respuesta sin 'meta': $body")

        val currency = meta["currency"]?.jsonPrimitive?.content ?: "USD"
        val name = meta["shortName"]?.jsonPrimitive?.content
            ?: meta["longName"]?.jsonPrimitive?.content
        val exchange = meta["exchangeName"]?.jsonPrimitive?.content
        val price = meta["regularMarketPrice"]?.jsonPrimitive?.doubleOrNull
            ?: meta["previousClose"]?.jsonPrimitive?.doubleOrNull
            ?: extractClosePrice(result[0].jsonObject)
            ?: throw IllegalStateException("No se pudo extraer el precio")

        return PriceQuote(
            identifier = identifier,
            identifierType = type,
            price = price,
            currency = currency,
            name = name,
            exchange = exchange,
            retrievedAt = nowMillis()
        )
    }

    /**
     * Extrae el precio de cierre del array de indicadores si no está en meta.
     */
    private fun extractClosePrice(resultObj: JsonObject): Double? {
        return try {
            val indicators = resultObj["indicators"]?.jsonObject ?: return null
            val quote = indicators["quote"]?.jsonArray?.getOrNull(0)?.jsonObject ?: return null
            val closes = quote["close"]?.jsonArray ?: return null
            closes.lastOrNull()?.jsonPrimitive?.doubleOrNull
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parsea la respuesta del endpoint /search/ de Yahoo Finance.
     * Formato esperado:
     * {
     *   "quotes": [{
     *     "symbol": "AAPL",
     *     "shortname": "Apple Inc.",
     *     "exchange": "NASDAQ"
     *   }]
     * }
     */
    private fun parseYahooSearchResponse(body: String, query: String): List<PriceQuote> {
        val root = json.parseToJsonElement(body).jsonObject
        val quotes = root["quotes"]?.jsonArray ?: return emptyList()
        val now = nowMillis()

        return quotes.mapNotNull { quoteObj ->
            try {
                val obj = quoteObj.jsonObject
                val symbol = obj["symbol"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["shortname"]?.jsonPrimitive?.content
                val exchange = obj["exchange"]?.jsonPrimitive?.content
                val typeDisp = obj["typeDisp"]?.jsonPrimitive?.content ?: ""

                val type = when {
                    typeDisp.contains("Cryptocurrency", ignoreCase = true) -> IdentifierType.CRYPTO_SYMBOL
                    else -> IdentifierType.ISIN
                }

                PriceQuote(
                    identifier = symbol,
                    identifierType = type,
                    price = 0.0, // Search no devuelve precio
                    currency = "",
                    name = name,
                    exchange = exchange,
                    retrievedAt = now
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
