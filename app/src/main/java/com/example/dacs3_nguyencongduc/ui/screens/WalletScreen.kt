package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import com.example.dacs3_nguyencongduc.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

private fun emoji(cat: String) = when (cat) {
    "Ăn uống" -> "🍜"; "Đi lại" -> "🚗"; "Mua sắm" -> "🛒"
    "Giải trí" -> "🎮"; "Lương" -> "💰"; else -> "📦"
}

/**
 * Màn hình Lịch sử giao dịch (Wallet) - Tab phải
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(transactions: List<Transaction>) {
    val grouped = transactions.sortedByDescending { it.date }.groupBy { tx ->
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(tx.date))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
    ) {
        Text(
            "Lịch sử",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 14.dp)
        )

        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, null, tint = Color.White.copy(0.15f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Chưa có giao dịch", color = Color.White.copy(0.3f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grouped.forEach { (dateStr, txs) ->
                    item {
                        Text(
                            dateStr,
                            color = Color(0xFF888888),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp, start = 4.dp)
                        )
                    }
                    items(txs) { tx -> WalletTransactionRow(tx) }
                }
                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
private fun WalletTransactionRow(tx: Transaction) {
    Surface(
        color = Color(0xFF161616),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                if (tx.imageUri != null) {
                    AsyncImage(model = tx.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text(emoji(tx.category), fontSize = 22.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = LocketPurple.copy(0.12f), shape = RoundedCornerShape(6.dp)) {
                        Text(tx.category, color = LocketPurple, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(tx.date)),
                        color = Color(0xFF666666), fontSize = 11.sp
                    )
                }
            }
            val col = if (tx.type == "CHI") Color(0xFFFF6B6B) else Color(0xFF66BB6A)
            Text(
                "${if (tx.type == "CHI") "-" else "+"}${formatCurrency(tx.amount)}đ",
                color = col, fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

// Tag component used by other screens
@Composable
fun Tag(text: String, icon: ImageVector, color: Color) {
    Surface(color = color.copy(alpha = 0.8f), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
