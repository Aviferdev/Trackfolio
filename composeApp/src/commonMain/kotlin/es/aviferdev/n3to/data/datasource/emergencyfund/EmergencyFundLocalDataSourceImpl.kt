package es.aviferdev.n3to.data.datasource.emergencyfund

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [EmergencyFundLocalDataSource] sobre [AppSettings].
 *
 * Claves usadas (por cuenta):
 * - ef_{accountId}_months    → Int (0 = no configurado)
 * - ef_{accountId}_method    → String ("MANUAL" | "AUTO")
 * - ef_{accountId}_expense   → String (Double, solo MANUAL)
 * - ef_{accountId}_excluded  → String (CSV de categoryIds)
 */
class EmergencyFundLocalDataSourceImpl(
    private val settings: AppSettings
) : EmergencyFundLocalDataSource {

    /**
     * Trigger interno para notificar cambios tras save/delete.
     * Como AppSettings no es reactivo, forzamos la reemisión manualmente.
     */
    private val refreshTrigger = MutableStateFlow(Unit)

    override fun getEmergencyFund(accountId: String): Flow<EmergencyFund?> =
        refreshTrigger.map {
            readEmergencyFund(accountId)
        }

    private fun readEmergencyFund(accountId: String): EmergencyFund? {
        val months = settings.getInt(key(accountId, KEY_MONTHS), 0)
        if (months <= 0) return null

        return EmergencyFund(
            accountId = accountId,
            targetMonths = months,
            calculationMethod = EmergencyFundMethod.valueOf(
                settings.getString(key(accountId, KEY_METHOD), "MANUAL")
            ),
            manualMonthlyExpense = settings.getString(key(accountId, KEY_EXPENSE), "0.0")
                .toDouble(),
            excludedCategoryIds = settings.getString(key(accountId, KEY_EXCLUDED), "")
                .split(SEPARATOR)
                .filter { it.isNotBlank() }
        )
    }

    override suspend fun saveEmergencyFund(fund: EmergencyFund) {
        settings.putInt(key(fund.accountId, KEY_MONTHS), fund.targetMonths)
        settings.putString(key(fund.accountId, KEY_METHOD), fund.calculationMethod.name)
        settings.putString(key(fund.accountId, KEY_EXPENSE), fund.manualMonthlyExpense.toString())
        settings.putString(
            key(fund.accountId, KEY_EXCLUDED),
            fund.excludedCategoryIds.joinToString(SEPARATOR)
        )
        refreshTrigger.value = Unit
    }

    override suspend fun deleteEmergencyFund(accountId: String) {
        settings.putInt(key(accountId, KEY_MONTHS), 0)
        refreshTrigger.value = Unit
    }

    private fun key(accountId: String, suffix: String): String =
        "ef_${accountId}_$suffix"

    private companion object {
        const val KEY_MONTHS = "months"
        const val KEY_METHOD = "method"
        const val KEY_EXPENSE = "expense"
        const val KEY_EXCLUDED = "excluded"
        const val SEPARATOR = ","
    }
}
