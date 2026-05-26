package com.example.dacs3_nguyencongduc.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import java.text.DecimalFormat

@Composable
fun TransactionItem(item: Transaction) {
    ListItem(
        headlineContent = { Text(item.title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(item.category) },
        trailingContent = {
            Text(
                text = "${if (item.type == "THU") "+" else "-"} ${DecimalFormat("#,###").format(item.amount)} đ",
                color = if (item.type == "THU") Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    )
}
