package es.aviferdev.n3to.ui.common.help

object HelpKeys {
    const val ACCOUNT_CONFIG = "account_config"
    const val TAX_PROFILE = "tax_profile"
    const val IRPF_SYSTEM = "irpf_system"
    const val EMERGENCY_FUND = "emergency_fund"
    const val GOAL_SETTINGS = "goal_settings"
    const val GOAL_CUSTOMIZE = "goal_customize"
    const val ASSET_CATEGORY = "asset_category"
}

data class HelpText(val title: String, val body: String)

object HelpContent {
    val texts: Map<String, HelpText> = mapOf(
        HelpKeys.ACCOUNT_CONFIG to HelpText(
            title = "¿Qué es una cuenta?",
            body = "Una cuenta es un espacio independiente con su propia configuración fiscal, categorías de gastos, activos y movimientos. Puedes tener varias cuentas completamente separadas; por ejemplo, una personal y otra de empresa."
        ),
        HelpKeys.TAX_PROFILE to HelpText(
            title = "¿Qué es el perfil fiscal?",
            body = "El perfil fiscal define los tramos IRPF y tipos de rendimiento que se aplican al calcular tu declaración. Sin un perfil configurado, los cálculos fiscales no serán precisos.\n\nSi cambias de país de residencia fiscal, añade un nuevo perfil con la fecha de inicio del cambio — el sistema aplica siempre el más reciente."
        ),
        HelpKeys.IRPF_SYSTEM to HelpText(
            title = "Sistema fiscal",
            body = "Elige el país donde declaras tus rentas habitualmente. Cada sistema define sus propios tramos IRPF y tipos impositivos para dividendos, plusvalías y rendimientos del capital.\n\nSi cambias de país de residencia, crea un nuevo perfil con la fecha exacta del cambio."
        ),
        HelpKeys.EMERGENCY_FUND to HelpText(
            title = "Fondo de emergencia",
            body = "Un colchón de dinero líquido para cubrir imprevistos sin endeudarte: pérdida de empleo, gastos médicos o reparaciones urgentes.\n\n• 3–6 meses con empleo estable\n• 6–12 meses si eres autónomo o tienes cargas familiares"
        ),
        HelpKeys.GOAL_SETTINGS to HelpText(
            title = "Objetivos mensuales",
            body = "Define cuánto quieres ahorrar e invertir cada mes. El valor base aplica a todos los meses del año. Después puedes personalizar meses concretos con ingresos o gastos excepcionales, como una paga extra o un mes de vacaciones."
        ),
        HelpKeys.GOAL_CUSTOMIZE to HelpText(
            title = "Personalizar meses",
            body = "Toca un mes futuro para ajustar su objetivo de ahorro o inversión de forma individual. Los meses pasados no son editables.\n\nUn punto azul indica que ese mes tiene un objetivo personalizado diferente del base anual."
        ),
        HelpKeys.ASSET_CATEGORY to HelpText(
            title = "Tipos de activo",
            body = "• Acciones / ETFs / Fondos — renta variable cotizada. Admite análisis por sectores y regiones.\n• Renta fija — bonos y depósitos con rendimiento acordado. Requieren fecha de vencimiento.\n• Cripto / Materias primas — activos alternativos con precio de mercado.\n• Inmuebles / Otros — activos no cotizados."
        )
    )
}
