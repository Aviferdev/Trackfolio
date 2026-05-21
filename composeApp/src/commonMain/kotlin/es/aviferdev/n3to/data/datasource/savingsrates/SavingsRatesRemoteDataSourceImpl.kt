package es.aviferdev.n3to.data.datasource.savingsrates

import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType
import es.aviferdev.n3to.platform.nowMillis
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SavingsRatesRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : SavingsRatesRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val baseUrl = "https://docs.google.com/spreadsheets/d/" +
            "1YXa7u38m3cFxUDpIgfhzW86BCEowFaE3yZEvS9uGAAw/gviz/tq"

    override suspend fun fetchShortTerm(): Result<List<SavingsRate>> =
        fetch("Remuneración CORTO Plazo", SavingsRateType.SHORT_TERM)

    override suspend fun fetchMediumTerm(): Result<List<SavingsRate>> =
        fetch("Remuneración MEDIO Plazo", SavingsRateType.MEDIUM_TERM)

    private suspend fun fetch(sheet: String, type: SavingsRateType): Result<List<SavingsRate>> {
        return try {
            println("[SavingsRates] 📡 GET gviz sheet='$sheet'")
            val response = httpClient.get(baseUrl) {
                parameter("tqx", "out:json")
                parameter("sheet", sheet)
            }
            println("[SavingsRates] ✅ HTTP ${response.status} sheet='$sheet'")
            val raw = response.bodyAsText()
            println("[SavingsRates] 📄 raw length=${raw.length}, starts='${raw.take(80)}'")

            val cleaned = raw
                .substringAfter("setResponse(")
                .substringBeforeLast(");")
            println("[SavingsRates] 🧹 cleaned length=${cleaned.length}")

            val tableEl = json.parseToJsonElement(cleaned).jsonObject["table"]?.jsonObject
            if (tableEl == null) {
                println(
                    "[SavingsRates] ❌ No 'table' en JSON. Keys: ${
                        json.parseToJsonElement(
                            cleaned
                        ).jsonObject.keys
                    }"
                )
                return Result.failure(IllegalStateException("No 'table' en respuesta gviz"))
            }

            val cols = tableEl["cols"]?.jsonArray ?: return Result.failure(
                IllegalStateException("No 'cols' en tabla gviz")
            )
            val colIndex = buildMap<String, Int> {
                cols.forEachIndexed { i, col ->
                    val label = col.jsonObject["label"]?.jsonPrimitive?.content?.trim()
                    if (!label.isNullOrBlank()) {
                        val key = label.lowercase()
                        put(key, i)
                        // Long header labels end with ". <real column name>" — register the short alias too
                        if (". " in key) {
                            val alias = key.substringAfterLast(". ").trim()
                            if (alias.isNotBlank() && !containsKey(alias)) put(alias, i)
                        }
                    }
                }
            }
            println("[SavingsRates] 📋 Columnas detectadas (${colIndex.size}): ${colIndex.keys}")

            // Exact match first; falls back to substring match for partial column names
            // (e.g. "máx" → "importe máx", "plazo" → "plazo (meses)", "fgd" → "fgd*")
            fun colOf(key: String): Int? =
                colIndex[key]
                    ?: if (key.length >= 3) colIndex.entries.firstOrNull { (k, _) -> k.contains(key) }?.value else null

            val rows = tableEl["rows"]?.jsonArray ?: return Result.success(emptyList())
            println("[SavingsRates] 📊 Filas totales: ${rows.size}")
            val now = nowMillis()

            val rates = rows.mapNotNull { rowEl ->
                try {
                    val cells = rowEl.jsonObject["c"]?.jsonArray ?: return@mapNotNull null
                    fun str(key: String): String? =
                        colOf(key)?.let { idx ->
                            cells.getOrNull(idx)?.let { c ->
                                if (c is JsonNull) null
                                else c.jsonObject["v"]?.let { v ->
                                    if (v is JsonNull) null else v.jsonPrimitive.content.takeIf { it.isNotBlank() }
                                }
                            }
                        }

                    fun double(key: String): Double? =
                        colOf(key)?.let { idx ->
                            cells.getOrNull(idx)?.let { c ->
                                if (c is JsonNull) null
                                else c.jsonObject["v"]?.let { v ->
                                    if (v is JsonNull) null else (v as? JsonPrimitive)?.doubleOrNull
                                }
                            }
                        }

                    fun int(key: String): Int? =
                        colOf(key)?.let { idx ->
                            cells.getOrNull(idx)?.let { c ->
                                if (c is JsonNull) null
                                else c.jsonObject["v"]?.let { v ->
                                    if (v is JsonNull) null else (v as? JsonPrimitive)?.intOrNull
                                        ?: (v as? JsonPrimitive)?.doubleOrNull?.toInt()
                                }
                            }
                        }

                    fun bool(key: String): Boolean? =
                        str(key)?.lowercase()?.let { v ->
                            when {
                                v.contains("sí") || v == "si" || v == "true" -> true
                                v.contains("no") || v == "false" -> false
                                else -> null
                            }
                        }

                    val entity = str("entidad") ?: return@mapNotNull null
                    if (entity.isBlank()) return@mapNotNull null

                    val interest = double("interés") ?: double("interes") ?: run {
                        println("[SavingsRates] ⚠️ Sin interés para entidad='$entity', omitiendo fila")
                        return@mapNotNull null
                    }

                    SavingsRate(
                        entity = entity,
                        interestRate = interest,
                        maxAmount = double("máximo") ?: double("maximo") ?: double("máx") ?: double(
                            "max"
                        ),
                        minAmount = double("mínimo") ?: double("minimo") ?: double("mín") ?: double(
                            "min"
                        ),
                        termMonths = int("plazo"),
                        conditions = str("condiciones") ?: str("notas") ?: str("información")
                        ?: str("informacion"),
                        earlyRedemption = bool("cancelación") ?: bool("cancelacion"),
                        country = str("país") ?: str("pais") ?: str("país iban"),
                        fgdGuaranteed = bool("fgd") ?: str("país fgd")?.let { it.isNotBlank() },
                        obligation720 = bool("obligación 720") ?: bool("obligacion 720")
                        ?: bool("720"),
                        type = type,
                        fetchedAt = now
                    )
                } catch (e: Exception) {
                    println("[SavingsRates] ⚠️ Error parseando fila: ${e.message}")
                    null
                }
            }
            println("[SavingsRates] ✅ Parseadas ${rates.size} entidades para '$sheet'")
            Result.success(rates)
        } catch (e: Exception) {
            println("[SavingsRates] ❌ Error en fetch '$sheet': ${e::class.simpleName} — ${e.message}")
            Result.failure(e)
        }
    }
}
