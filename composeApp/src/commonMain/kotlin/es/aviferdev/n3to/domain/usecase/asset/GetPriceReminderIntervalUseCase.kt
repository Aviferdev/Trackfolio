package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.model.ValidationError

/**
 * Lee y escribe el intervalo (en días) del recordatorio de precios.
 * Opciones válidas: 7, 14, 30. Cualquier otro valor se trata como 14.
 */
class GetPriceReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    fun get(): Int {
        val value = appSettings.getInt(
            GetOutdatedAssetsUseCase.KEY_REMINDER_INTERVAL,
            GetOutdatedAssetsUseCase.DEFAULT_INTERVAL
        )
        return if (value in VALID_INTERVALS) value else GetOutdatedAssetsUseCase.DEFAULT_INTERVAL
    }

    fun set(days: Int): Result<Unit> {
        if (days !in VALID_INTERVALS) return Result.failure(ValidationError.InvalidInterval(days))
        appSettings.putInt(GetOutdatedAssetsUseCase.KEY_REMINDER_INTERVAL, days)
        return Result.success(Unit)
    }

    companion object {
        val VALID_INTERVALS = listOf(7, 14, 30)
    }
}
