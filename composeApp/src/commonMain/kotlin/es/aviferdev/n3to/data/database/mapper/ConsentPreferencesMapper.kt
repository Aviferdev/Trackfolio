package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.ConsentPreferencesEntity
import es.aviferdev.n3to.domain.model.ConsentPreferences

fun ConsentPreferencesEntity.toDomain(): ConsentPreferences = ConsentPreferences(
    analytics = analytics != 0L,
    crashReporting = crashReporting != 0L,
    consentVersion = consentVersion.toInt(),
    consentTimestamp = consentTimestamp,
    hasDecided = hasDecided != 0L
)

fun ConsentPreferences.toEntity(): ConsentPreferencesEntity = ConsentPreferencesEntity(
    id = 1L,
    analytics = if (analytics) 1L else 0L,
    crashReporting = if (crashReporting) 1L else 0L,
    consentVersion = consentVersion.toLong(),
    consentTimestamp = consentTimestamp,
    hasDecided = if (hasDecided) 1L else 0L
)
