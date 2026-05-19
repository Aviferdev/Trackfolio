package es.aviferdev.n3to.data.repository.savingsrates

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.data.datasource.savingsrates.SavingsRatesRemoteDataSource
import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType
import es.aviferdev.n3to.domain.repository.SavingsRatesRepository
import es.aviferdev.n3to.platform.nowMillis
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CACHE_TTL_MS = 24L * 60 * 60 * 1000

class SavingsRatesRepositoryImpl(
    private val remote: SavingsRatesRemoteDataSource,
    private val settings: AppSettings
) : SavingsRatesRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getShortTerm(forceRefresh: Boolean): Result<List<SavingsRate>> =
        get(SavingsRateType.SHORT_TERM, forceRefresh) { remote.fetchShortTerm() }

    override suspend fun getMediumTerm(forceRefresh: Boolean): Result<List<SavingsRate>> =
        get(SavingsRateType.MEDIUM_TERM, forceRefresh) { remote.fetchMediumTerm() }

    override fun getLastFetchedAt(type: SavingsRateType): Long? {
        val ts = settings.getLong(tsKey(type), 0L)
        return if (ts == 0L) null else ts
    }

    private suspend fun get(
        type: SavingsRateType,
        forceRefresh: Boolean,
        fetch: suspend () -> Result<List<SavingsRate>>
    ): Result<List<SavingsRate>> {
        val cached = readCache(type)
        val ts = settings.getLong(tsKey(type), 0L)
        val isFresh = ts > 0L && (nowMillis() - ts) < CACHE_TTL_MS
        println("[SavingsRatesRepo] $type — cached=${cached?.size} ts=$ts isFresh=$isFresh forceRefresh=$forceRefresh")

        if (!forceRefresh && isFresh && cached != null) {
            println("[SavingsRatesRepo] $type ✅ Devolviendo cache (${cached.size} items)")
            return Result.success(cached)
        }

        println("[SavingsRatesRepo] $type 🌐 Llamando remoto...")
        val remoteResult = fetch()
        return if (remoteResult.isSuccess) {
            val rates = remoteResult.getOrThrow()
            println("[SavingsRatesRepo] $type ✅ Remoto OK (${rates.size} items), guardando cache")
            writeCache(type, rates)
            Result.success(rates)
        } else {
            println("[SavingsRatesRepo] $type ❌ Remoto falló: ${remoteResult.exceptionOrNull()?.message}")
            if (cached != null) {
                println("[SavingsRatesRepo] $type ⚠️ Devolviendo cache expirada (${cached.size} items)")
                Result.success(cached)
            } else {
                remoteResult
            }
        }
    }

    private fun readCache(type: SavingsRateType): List<SavingsRate>? {
        val raw = settings.getString(dataKey(type), "")
        if (raw.isBlank()) return null
        return try {
            val arr = json.parseToJsonElement(raw).jsonArray
            arr.mapNotNull { el -> parseSavingsRate(el.jsonObject, type) }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeCache(type: SavingsRateType, rates: List<SavingsRate>) {
        val arr = JsonArray(rates.map { it.toJson() })
        settings.putString(dataKey(type), arr.toString())
        settings.putLong(tsKey(type), nowMillis())
    }

    private fun dataKey(type: SavingsRateType) =
        if (type == SavingsRateType.SHORT_TERM) "savings_short_data" else "savings_medium_data"

    private fun tsKey(type: SavingsRateType) =
        if (type == SavingsRateType.SHORT_TERM) "savings_short_ts" else "savings_medium_ts"

    private fun SavingsRate.toJson(): JsonObject = JsonObject(buildMap {
        put("entity", JsonPrimitive(entity))
        put("interestRate", JsonPrimitive(interestRate))
        put("maxAmount", maxAmount?.let { JsonPrimitive(it) } ?: JsonNull)
        put("minAmount", minAmount?.let { JsonPrimitive(it) } ?: JsonNull)
        put("termMonths", termMonths?.let { JsonPrimitive(it) } ?: JsonNull)
        put("conditions", conditions?.let { JsonPrimitive(it) } ?: JsonNull)
        put("earlyRedemption", earlyRedemption?.let { JsonPrimitive(it) } ?: JsonNull)
        put("country", country?.let { JsonPrimitive(it) } ?: JsonNull)
        put("fgdGuaranteed", fgdGuaranteed?.let { JsonPrimitive(it) } ?: JsonNull)
        put("obligation720", obligation720?.let { JsonPrimitive(it) } ?: JsonNull)
        put("fetchedAt", JsonPrimitive(fetchedAt))
    })

    private fun parseSavingsRate(obj: JsonObject, type: SavingsRateType): SavingsRate? {
        return try {
            SavingsRate(
                entity = obj["entity"]?.jsonPrimitive?.content ?: return null,
                interestRate = obj["interestRate"]?.jsonPrimitive?.doubleOrNull ?: return null,
                maxAmount = obj["maxAmount"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                minAmount = obj["minAmount"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.doubleOrNull,
                termMonths = obj["termMonths"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.intOrNull,
                conditions = obj["conditions"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.content,
                earlyRedemption = obj["earlyRedemption"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull,
                country = obj["country"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.content,
                fgdGuaranteed = obj["fgdGuaranteed"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull,
                obligation720 = obj["obligation720"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.booleanOrNull,
                type = type,
                fetchedAt = obj["fetchedAt"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            )
        } catch (_: Exception) {
            null
        }
    }
}
