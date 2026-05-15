package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import com.example.dacs3_nguyencongduc.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

private fun catEmoji(cat: String) = when (cat) {
    "Ăn uống" -> "🍜"; "Đi lại" -> "🚗"; "Mua sắm" -> "🛒"
    "Giải trí" -> "🎮"; "Lương" -> "💰"; else -> "📦"
}

/**
 * Data wrapper cho 1 ngày chứa nhiều giao dịch
 */
data class DayGroup(
    val day: Int,
    val month: Int,
    val year: Int,
    val transactions: List<Transaction>
)

/**
 * Màn hình xem chi tiết giao dịch full-screen
 * - Lướt dọc: chuyển ngày
 * - Lướt ngang: chuyển ảnh/giao dịch trong cùng ngày
 */
@Composable
fun TransactionDetailScreen(
    dayGroups: List<DayGroup>,
    initialDayIndex: Int,
    onClose: () -> Unit
) {
    if (dayGroups.isEmpty()) return

    val verticalPagerState = rememberPagerState(
        initialPage = initialDayIndex.coerceIn(0, dayGroups.lastIndex)
    ) { dayGroups.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Vertical pager: mỗi page = 1 ngày
        VerticalPager(
            state = verticalPagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { dayIndex ->
            val dayGroup = dayGroups[dayIndex]
            DayDetailPage(
                dayGroup = dayGroup,
                onClose = onClose
            )
        }

        // Vertical dot indicator bên phải
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dayGroups.forEachIndexed { i, _ ->
                val isActive = i == verticalPagerState.currentPage
                val size by animateDpAsState(if (isActive) 8.dp else 5.dp, label = "")
                Box(
                    Modifier
                        .size(width = size, height = size)
                        .clip(CircleShape)
                        .background(
                            if (isActive) LocketPurple else Color.White.copy(0.2f)
                        )
                )
            }
        }
    }
}

@Composable
private fun DayDetailPage(
    dayGroup: DayGroup,
    onClose: () -> Unit
) {
    val txs = dayGroup.transactions
    if (txs.isEmpty()) return

    val horizontalPagerState = rememberPagerState { txs.size }
    val currentTx = txs.getOrNull(horizontalPagerState.currentPage) ?: txs.first()

    val dateStr = remember(dayGroup) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, dayGroup.year)
            set(Calendar.MONTH, dayGroup.month)
            set(Calendar.DAY_OF_MONTH, dayGroup.day)
        }
        SimpleDateFormat("'Ngày' d 'Thg' M, yyyy", Locale.getDefault()).format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top Bar: Close + Date + Edit ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.08f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Surface(
                color = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    dateStr,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }

            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.08f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // ── Image Pager (swipe horizontal for images in same day) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tx = txs[page]
                Box(Modifier.fillMaxSize()) {
                    if (tx.imageUri != null) {
                        AsyncImage(
                            model = tx.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // No image – show emoji
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(catEmoji(tx.category), fontSize = 72.sp)
                        }
                    }

                    // Gradient bottom
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.75f))
                                )
                            )
                    )

                    // Type toggle overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isChi = tx.type == "CHI"
                        Surface(
                            color = if (isChi) Color(0xFFFF6B6B) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Chi tiêu", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Surface(
                            color = if (!isChi) LocketPurple else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ArrowDownward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Thu nhập", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Image counter badge
            if (txs.size > 1) {
                Surface(
                    color = Color.Black.copy(0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Text(
                        "${horizontalPagerState.currentPage + 1}/${txs.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Fullscreen icon top-left
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.4f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FullscreenExit, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Category + Date chips ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(catEmoji(currentTx.category), fontSize = 16.sp)
                    Text(currentTx.category, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Surface(
                color = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    dateStr,
                    color = Color.White.copy(0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── Amount & Description Card ──
        Surface(
            color = Color(0xFF0A0A0A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val amtColor = if (currentTx.type == "CHI") Color.White else Color(0xFF66BB6A)
                Text(
                    "${formatCurrency(currentTx.amount)} đ",
                    color = amtColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    currentTx.title,
                    color = Color.White.copy(0.7f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Horizontal page dots ──
        if (txs.size > 1) {
            Row(
                Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(txs.size) { i ->
                    val isCurrent = i == horizontalPagerState.currentPage
                    val w by animateDpAsState(if (isCurrent) 20.dp else 6.dp, label = "")
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(w)
                            .clip(CircleShape)
                            .background(if (isCurrent) LocketPurple else Color.White.copy(0.2f))
                    )
                }
            }
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}
