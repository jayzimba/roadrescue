package com.jayjaycode.miniproject.util

import java.util.Locale

object CurrencyFormatter {

    const val CODE = "ZMW"

    fun format(amount: Double, decimals: Int = 2): String {
        val pattern = "%,.${decimals}f"
        return "$CODE ${String.format(Locale.US, pattern, amount)}"
    }

    fun formatCompact(amount: Double): String = format(amount, decimals = 0)
}
