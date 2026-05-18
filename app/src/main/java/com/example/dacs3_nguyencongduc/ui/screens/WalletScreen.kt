package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import com.example.dacs3_nguyencongduc.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(transactions: List<Transaction>) {
    var selectedTab by remember { mutableStateOf("Expense") }
    
    // Tính toán số liệu
    val totalExpense = transactions.filter { it.type.equals("CHI", ignoreCase = true) }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type.equals("THU", ignoreCase = true) }.sumOf { it.amount }
    val remaining = totalIncome - totalExpense

    val currentList = if (selectedTab == "Expense") {
        transactions.filter { it.type.equals("CHI", ignoreCase = true) }
    } else {
        transactions.filter { it.type.equals("THU", ignoreCase = true) }
    }

    val totalAmount = currentList.sumOf { it.amount }
    val categoryBreakdown = currentList.groupBy { it.category }
        .map { (cat, txs) ->
            val amount = txs.sumOf { it.amount }
            CategorySummary(
                category = cat,
                amount = amount,
                percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f,
                color = getCategoryColor(cat),
                emoji = getCategoryEmoji(cat)
            )
        }.sortedByDescending { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
    ) {
        // Top Navigation: Date Range
        DateRangeSelector()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard("Expense", "-${formatCurrency(totalExpense)}", Color(0xFFFF5252), Icons.Default.ArrowOutward)
                    SummaryCard("Income", formatCurrency(totalIncome), Color(0xFF4CAF50), Icons.Default.CallReceived)
                    SummaryCard("Remaining", formatCurrency(remaining), Color(0xFF2196F3), Icons.Default.AccountBalanceWallet)
                }
            }

            // Main Content Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color(0xFF161616),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Toggle Switch
                        TabSwitcher(selectedTab) { selectedTab = it }

                        Spacer(Modifier.height(32.dp))

                        // Donut Chart
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                            DonutChart(categoryBreakdown)
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTab == "Expense") "Total Expense:" else "Total Income:",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (selectedTab == "Expense") "-${formatCurrency(totalAmount)}" else formatCurrency(totalAmount),
                                    color = if (selectedTab == "Expense") Color(0xFFFF5252) else Color(0xFF4CAF50),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Text(" 93% vs last week", color = Color(0xFF4CAF50), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))

                        // Category List Breakdown
                        categoryBreakdown.forEach { item ->
                            CategoryItem(item)
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateRangeSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White) }
        Text(
            "6 Apr 2026 - 12 Apr 2026",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        IconButton(onClick = {}) { Icon(Icons.Default.ChevronRight, null, tint = Color.White) }
    }
}

@Composable
fun SummaryCard(title: String, amount: String, color: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.width(140.dp).wrapContentHeight(),
        color = Color(0xFF161616),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(28.dp).background(color.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(2.dp))
            Text(amount, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabSwitcher(selected: String, onSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier.width(240.dp).height(48.dp),
        color = Color.Black.copy(0.3f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            TabItem("Expense", selected == "Expense", Color(0xFFFF5252), Icons.Default.ArrowOutward, Modifier.weight(1f)) { onSelected("Expense") }
            TabItem("Income", selected == "Income", Color(0xFF4CAF50), Icons.Default.CallReceived, Modifier.weight(1f)) { onSelected("Income") }
        }
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, color: Color, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable { onClick() },
        color = if (isSelected) color else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DonutChart(data: List<CategorySummary>) {
    Canvas(modifier = Modifier.size(240.dp)) {
        var startAngle = -90f
        data.forEach { item ->
            val sweepAngle = item.percentage * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
        
        // Background ring for contrast or if empty
        if (data.isEmpty()) {
            drawArc(
                color = Color.DarkGray.copy(0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 40.dp.toPx())
            )
        }
    }
}

@Composable
fun CategoryItem(item: CategorySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(item.color, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(item.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(item.category, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text("${(item.percentage * 100).toInt()}%", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
        Text("-${formatCurrency(item.amount)}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

data class CategorySummary(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val emoji: String
)

private fun getCategoryColor(cat: String): Color = when (cat) {
    "Ăn uống" -> Color(0xFFFF9800)
    "Đi lại" -> Color(0xFF2196F3)
    "Mua sắm" -> Color(0xFFE91E63)
    "Giải trí" -> Color(0xFF9C27B0)
    "Lương" -> Color(0xFF4CAF50)
    "Y tế" -> Color(0xFFF44336)
    "Giáo dục" -> Color(0xFF00BCD4)
    else -> Color(0xFF9E9E9E)
}

private fun getCategoryEmoji(cat: String): String = when (cat) {
    "Ăn uống" -> "🍜"
    "Đi lại" -> "🚗"
    "Mua sắm" -> "🛒"
    "Giải trí" -> "🎮"
    "Lương" -> "💰"
    "Y tế" -> "🏥"
    "Giáo dục" -> "🎓"
    else -> "📦"
}
