package com.offlineledger.utils

import java.text.NumberFormat
import java.util.*

private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

fun formatCurrency(amount: Double): String {
    return inrFormat.format(amount)
        .replace("₹", "")   // symbol added by caller for sign control
        .trim()
        .let { "₹$it" }
}
