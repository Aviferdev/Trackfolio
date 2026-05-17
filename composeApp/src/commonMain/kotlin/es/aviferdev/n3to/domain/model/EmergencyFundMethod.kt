package es.aviferdev.n3to.domain.model

/**
 * Método de cálculo del fondo de emergencia.
 *
 * - MANUAL: el usuario introduce el gasto mensual estimado manualmente.
 * - AUTO:   el sistema calcula la media de gastos de los últimos 12 meses
 *           (excluyendo las categorías que el usuario haya marcado).
 */
enum class EmergencyFundMethod {
    MANUAL,
    AUTO
}
