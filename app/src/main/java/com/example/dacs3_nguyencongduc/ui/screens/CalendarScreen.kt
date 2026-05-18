package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import java.util.*

private val FILTER_LIST = listOf(
    "Tất cả" to Icons.Default.GridView,
    "Ăn uống" to Icons.Default.Restaurant,
    "Đi lại" to Icons.Default.DirectionsCar,
    "Mua sắm" to Icons.Default.ShoppingBag,
    "Lương" to Icons.Default.AccountBalanceWallet,
    "Giải trí" to Icons.Default.SportsEsports
)

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

private val MONTH_NAMES = listOf(
    "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
)

private fun getCategoryEmoji(cat: String) = when (cat) {
    "Ăn uống" -> "🍜"
    "Đi lại" -> "🚗"
    "Mua sắm" -> "🛒"
    "Giải trí" -> "🎮"
    "Lương" -> "💰"
    else -> "📦"
}

/**
 * Màn hình Lịch chi tiêu - Tab trái
 */
@Composable
fun CalendarScreen(
    transactions: List<Transaction>,
    onDayClick: (List<DayGroup>, Int) -> Unit = { _, _ -> }
) {
    val now = Calendar.getInstance()
    var currentMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val filtered = remember(transactions, currentMonth, currentYear, selectedFilter) {
        transactions.filter { tx ->
            val c = Calendar.getInstance().apply { timeInMillis = tx.date }
            c.get(Calendar.MONTH) == currentMonth &&
                    c.get(Calendar.YEAR) == currentYear &&
                    (selectedFilter == "Tất cả" || tx.category == selectedFilter)
        }
    }

    val byDay = remember(filtered) {
        filtered.groupBy { tx ->
            Calendar.getInstance().apply { timeInMillis = tx.date }.get(Calendar.DAY_OF_MONTH)
        }
    }

    val totalChi = remember(filtered) { filtered.filter { it.type == "CHI" }.sumOf { it.amount } }
    val totalThu = remember(filtered) { filtered.filter { it.type == "THU" }.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
    ) {
        // ── Header ──
        Text(
            "Lịch chi tiêu",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 10.dp)
        )

        // ── Filter Chips ──
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FILTER_LIST) { (label, icon) ->
                val sel = selectedFilter == label
                Surface(
                    onClick = { selectedFilter = label },
                    color = if (sel) LocketPurple else Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(24.dp),
                    border = if (!sel) BorderStroke(0.5.dp, Color.White.copy(0.06f)) else null,
                    tonalElevation = if (sel) 4.dp else 0.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Month Nav ──
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth--
                selectedDay = null
            }) {
                Box(
                    Modifier.size(36.dp).background(Color(0xFF1E1E1E), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            }

            Surface(color = Color(0xFF1E1E1E), shape = RoundedCornerShape(20.dp)) {
                Row(
                    Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = LocketPurple, modifier = Modifier.size(20.dp))
                    Text(
                        "${MONTH_NAMES[currentMonth]} $currentYear",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = {
                if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++
                selectedDay = null
            }) {
                Box(
                    Modifier.size(36.dp).background(Color(0xFF1E1E1E), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Summary ──
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassSummaryCard(Modifier.weight(1f), "Chi tiêu", totalChi, Color(0xFFFF6B6B), Icons.Default.TrendingDown)
            GlassSummaryCard(Modifier.weight(1f), "Thu nhập", totalThu, Color(0xFF66BB6A), Icons.Default.TrendingUp)
        }

        Spacer(Modifier.height(14.dp))

        // ── Calendar body (scrollable) ──
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp)
        ) {
            // Weekday headers
            Row(Modifier.fillMaxWidth()) {
                WEEKDAY_LABELS.forEach { label ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            color = if (label == "CN") Color(0xFFFF6B6B) else Color(0xFF888888),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Calendar grid – 5-column-width cells
            val cal = remember(currentMonth, currentYear) {
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDow = cal.get(Calendar.DAY_OF_WEEK)
            val offset = if (firstDow == Calendar.SUNDAY) 6 else firstDow - 2

            val todayCal = Calendar.getInstance()
            val isThisMonth = todayCal.get(Calendar.MONTH) == currentMonth && todayCal.get(Calendar.YEAR) == currentYear
            val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    for (c in 0..6) {
                        val idx = r * 7 + c
                        val day = idx - offset + 1
                        Box(Modifier.weight(1f).padding(horizontal = 2.dp)) {
                            if (day in 1..daysInMonth) {
                                val txs = byDay[day]
                                val hasTx = txs != null && txs.isNotEmpty()
                                val isToday = isThisMonth && day == todayDay
                                val isSel = day == selectedDay

                                BigCalendarDayCell(
                                    day = day,
                                    transactions = txs ?: emptyList(),
                                    hasTx = hasTx,
                                    isToday = isToday,
                                    isSelected = isSel,
                                    onClick = {
                                        selectedDay = day
                                        if (hasTx) {
                                            // Build sorted day groups for detail screen
                                            val sortedDays = byDay.keys.sorted()
                                            val groups = sortedDays.map { d ->
                                                DayGroup(d, currentMonth, currentYear, byDay[d]!!)
                                            }
                                            val clickedIdx = sortedDays.indexOf(day)
                                            onDayClick(groups, clickedIdx.coerceAtLeast(0))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Selected day detail ──
            AnimatedVisibility(
                visible = selectedDay != null && byDay.containsKey(selectedDay),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedDay?.let { d ->
                    DayDetailCard(d, currentMonth + 1, byDay[d] ?: emptyList())
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

// ── Sub-composables ──

@Composable
private fun GlassSummaryCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    accent: Color,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF161616),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(0.5.dp, accent.copy(alpha = 0.2f)),
        tonalElevation = 2.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(accent.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, color = Color(0xFF999999), fontSize = 12.sp)
                Text(
                    "${if (label == "Chi tiêu") "-" else "+"}${formatCurrency(amount)}đ",
                    color = accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatShortCurrency(amount: Double): String {
    val absAmt = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else if (amount > 0) "+" else ""
    return when {
        absAmt >= 1_000_000 -> "${sign}${String.format(Locale.US, "%.1f", absAmt / 1_000_000).replace(".0", "")}tr"
        absAmt >= 1_000 -> "${sign}${String.format(Locale.US, "%.0f", absAmt / 1_000)}k"
        else -> "${sign}${absAmt.toInt()}"
    }
}

@Composable
private fun BigCalendarDayCell(
    day: Int,
    transactions: List<Transaction>,
    hasTx: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Color.White.copy(0.05f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasTx) {
            val imgList = transactions.filter { it.imageUri != null }.take(2)
            
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f)
            ) {
                if (imgList.isNotEmpty()) {
                    // Back image (3D stacking)
                    if (imgList.size > 1) {
                        AsyncImage(
                            model = imgList[1].imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 6.dp, bottom = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    // Front image
                    AsyncImage(
                        model = imgList[0].imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                end = if (imgList.size > 1) 6.dp else 0.dp,
                                top = if (imgList.size > 1) 6.dp else 0.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .border(if (isToday) 2.dp else 0.dp, if (isToday) Color(0xFF66BB6A) else Color.Transparent, RoundedCornerShape(12.dp))
                    )
                } else {
                    // Fallback to Emoji if no images
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(end = if (transactions.size > 1) 6.dp else 0.dp, top = if (transactions.size > 1) 6.dp else 0.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(if (isToday) 2.dp else 0.dp, if (isToday) Color(0xFF66BB6A) else Color.Transparent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryEmoji(transactions.first().category), fontSize = 20.sp)
                    }
                }
                
                // Count Badge
                if (transactions.size > 1) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .background(Color(0xFFD500F9), CircleShape)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${transactions.size}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            // Date below image
            Text(
                day.toString(),
                color = if (isToday) Color(0xFF66BB6A) else Color.White,
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
            )
            
            // Amount below date
            val sum = transactions.sumOf { if (it.type == "CHI") -it.amount else it.amount }
            Text(
                formatShortCurrency(sum),
                color = if (sum < 0) Color(0xFFFF5252) else Color(0xFF4CAF50),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            
        } else {
            // Empty State
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131313))
                    .border(if (isToday) 2.dp else 0.dp, if (isToday) Color(0xFF66BB6A) else Color.Transparent, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White.copy(0.05f), modifier = Modifier.size(24.dp))
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                day.toString(),
                color = if (isToday) Color(0xFF66BB6A) else Color.White.copy(0.3f),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
            )
            
            // Invisible text to maintain height
            Text(" ", fontSize = 10.sp)
        }
    }
}

@Composable
private fun DayDetailCard(day: Int, month: Int, transactions: List<Transaction>) {
    Surface(
        color = Color(0xFF161616),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).background(LocketPurple.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day.toString(), color = LocketPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Ngày $day/$month", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("${transactions.size} giao dịch", color = Color(0xFF888888), fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                val dayTotal = transactions.sumOf { if (it.type == "CHI") -it.amount else it.amount }
                val c = if (dayTotal < 0) Color(0xFFFF6B6B) else Color(0xFF66BB6A)
                Surface(color = c.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "${if (dayTotal < 0) "-" else "+"}${formatCurrency(kotlin.math.abs(dayTotal))}đ",
                        color = c, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            transactions.forEachIndexed { i, tx ->
                TxRow(tx)
                if (i < transactions.lastIndex) {
                    HorizontalDivider(
                        color = Color.White.copy(0.05f),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: Transaction) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Thumbnail
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF222222)),
            contentAlignment = Alignment.Center
        ) {
            if (tx.imageUri != null) {
                AsyncImage(model = tx.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(getCategoryEmoji(tx.category), fontSize = 22.sp)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = LocketPurple.copy(0.12f), shape = RoundedCornerShape(6.dp)) {
                    Text(tx.category, color = LocketPurple, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text(
                    if (tx.type == "CHI") "Chi tiêu" else "Thu nhập",
                    color = Color(0xFF666666),
                    fontSize = 11.sp
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
