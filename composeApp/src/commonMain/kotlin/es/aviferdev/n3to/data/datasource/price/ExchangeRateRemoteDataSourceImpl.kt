package es.aviferdev.n3to.data.datasource.price

import es.aviferdev.n3to.domain.model.ExchangeRate
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
 * Implementación de [ExchangeRateRemoteDataSource] usando Yahoo Finance para
 * obtener el tipo de cambio EUR/USD.
 *
 * Endpoint: https://query1.finance.yahoo.com/v8/finance/chart/EURUSD=X?interval=1d&range=1d
 *
 * La respuesta es idéntica a la del endpoint /chart/ de cotizaciones, donde el
 * precio de EUR/USD representa cuántos USD vale 1 EUR.
 */
class ExchangeRateRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : ExchangeRateRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun fetchEurUsdRate(): Result<ExchangeRate> {
        return try {
            println("[PriceRefresh] 💱 GET EUR/USD")
            val response = httpClient.get("https://query1.finance.yahoo.com/v8/finance/chart/EURUSD=X") {
                parameter("interval", "1d")
                parameter("range", "1d")
            }
            println("[PriceRefresh] ✅ ${response.status} EUR/USD")
            val body = response.bodyAsText()
            val rate = parseEurUsdRateResponse(body)
            println("[PriceRefresh] 💱 EUR/USD = ${rate.usdPerEur} (1€ = ${rate.usdPerEur}$)")
            Result.success(rate)
        } catch (e: Exception) {
            println("[PriceRefresh] ❌ EUR/USD → ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Parsea la respuesta del chart de EUR/USD que tiene el mismo formato que
     * cualquier otra cotización. Extrae el precio de cierre (regularMarketPrice).
     *
     * El precio obtenido es la cotización EUR/USD (cuántos USD por 1 EUR).
     * Ej: 1.09 significa 1 EUR = 1.09 USD.
     */
    private fun parseEurUsdRateResponse(body: String): ExchangeRate {
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
            throw IllegalStateException("Yahoo Finance (EUR/USD): $errMsg")
        }
        val meta = result[0].jsonObject["meta"]?.jsonObject
            ?: throw IllegalStateException("Respuesta sin 'meta': $body")

        val eurUsdPrice = meta["regularMarketPrice"]?.jsonPrimitive?.doubleOrNull
            ?: meta["previousClose"]?.jsonPrimitive?.doubleOrNull
            ?: extractClosePrice(result[0].jsonObject)
            ?: throw IllegalStateException("No se pudo extraer el tipo de cambio EUR/USD")

        val now = nowMillis()
        return ExchangeRate.fromEurUsdPair(eurUsdPrice, now)
    }

    /**
     * Extrae el precio de cierre del array de indicadores como fallback.
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
}
