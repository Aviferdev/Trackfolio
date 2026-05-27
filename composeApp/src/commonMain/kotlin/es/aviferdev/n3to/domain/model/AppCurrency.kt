package es.aviferdev.n3to.domain.model

enum class AppCurrency(val code: String, val symbol: String, val displayName: String) {
    EUR("EUR", "€", "Euro"),
    USD("USD", "$", "Dólar"),
    GBP("GBP", "£", "Libra"),
    JPY("JPY", "¥", "Yen"),
    CHF("CHF", "Fr.", "Franco suizo"),
    CAD("CAD", "C$", "Dólar canadiense"),
    AUD("AUD", "A$", "Dólar australiano"),
    BRL("BRL", "R$", "Real"),
    MXN("MXN", "MX$", "Peso mexicano"),
    COP("COP", "COP$", "Peso colombiano");

    companion object {
        fun fromCode(code: String): AppCurrency = entries.find { it.code == code } ?: EUR
    }
}

fun String.toCurrencySymbol(): String = AppCurrency.fromCode(this).symbol
