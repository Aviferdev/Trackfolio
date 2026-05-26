package es.aviferdev.n3to.data.datasource.inflation

import es.aviferdev.n3to.domain.model.InflationDataPoint
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val BASE_URL =
    "https://api.worldbank.org/v2/country/{code}/indicator/FP.CPI.TOTL.ZG"

class InflationRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : InflationRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun fetchHistory(
        countryCode: String,
        years: Int
    ): Result<List<InflationDataPoint>> = try {
        val url = BASE_URL.replace("{code}", countryCode)
        val response = httpClient.get(url) {
            parameter("format", "json")
            parameter("mrv", years)
            parameter("per_page", years)
        }
        val raw = response.bodyAsText()
        val root = json.parseToJsonElement(raw).jsonArray
        val dataArray = root.getOrNull(1)?.jsonArray
            ?: return Result.failure(IllegalStateException("Unexpected API response for $countryCode"))

        val points = dataArray.mapNotNull { el ->
            val obj = el.jsonObject
            val yearStr = obj["date"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val year = yearStr.toIntOrNull() ?: return@mapNotNull null
            val valueEl = obj["value"]
            val rate = if (valueEl == null || valueEl is JsonNull) return@mapNotNull null
            else valueEl.jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            InflationDataPoint(countryCode = countryCode, year = year, rate = rate)
        }.sortedBy { it.year }

        Result.success(points)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
