package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.EmergencyFundEntity
import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.model.EmergencyFundMethod

/**
 * Convierte una [EmergencyFundEntity] a [EmergencyFund].
 * Las categorías excluidas se pasan por separado desde [EmergencyFundExcludedCategoryEntity].
 */
fun EmergencyFundEntity.toDomain(excludedCategoryIds: List<String>): EmergencyFund = EmergencyFund(
    accountId = accountId,
    targetMonths = targetMonths.toInt(),
    calculationMethod = EmergencyFundMethod.valueOf(calculationMethod),
    manualMonthlyExpense = manualMonthlyExpense,
    excludedCategoryIds = excludedCategoryIds
)

/**
 * Convierte un [EmergencyFund] a [EmergencyFundEntity].
 * Las categorías excluidas se persisten en [EmergencyFundExcludedCategoryEntity]
 * de forma separada (relación N:M normalizada).
 */
fun EmergencyFund.toEntity(): EmergencyFundEntity = EmergencyFundEntity(
    accountId = accountId,
    targetMonths = targetMonths.toLong(),
    calculationMethod = calculationMethod.name,
    manualMonthlyExpense = manualMonthlyExpense
)
