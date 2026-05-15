package com.example.dacs3_nguyencongduc.utils

import java.text.DecimalFormat

/**
 * Format số tiền với dấu phẩy phân cách hàng nghìn
 */
fun formatCurrency(amount: Double): String = DecimalFormat("#,###").format(amount)
