package com.example.dacs3_nguyencongduc.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Format số tiền với dấu chấm (.) phân cách hàng nghìn (chuẩn VN)
 */
fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale("vi", "VN"))
    symbols.groupingSeparator = '.'
    val decimalFormat = DecimalFormat("#,###", symbols)
    return decimalFormat.format(amount)
}
