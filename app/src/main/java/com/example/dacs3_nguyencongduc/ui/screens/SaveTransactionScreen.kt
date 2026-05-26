package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import com.example.dacs3_nguyencongduc.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

/**
 * VisualTransformation để thêm dấu chấm ngăn cách hàng nghìn (chuẩn VN) khi nhập tiền
 */
class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val out = StringBuilder()
        for (i in originalText.indices) {
            out.append(originalText[i])
            val remainingDigits = originalText.length - 1 - i
            if (remainingDigits > 0 && remainingDigits % 3 == 0) {
                out.append('.')
            }
        }

        val transformedText = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val realOffset = offset.coerceAtMost(originalText.length)
                val remainingDigits = originalText.length - realOffset
                val dotsAfter = if (remainingDigits > 0) (remainingDigits + 2) / 3 - 1 else 0
                val totalDots = (originalText.length - 1) / 3
                val dotsBefore = totalDots - dotsAfter
                return realOffset + dotsBefore
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val realOffset = offset.coerceAtMost(transformedText.length)
                val dotsBefore = transformedText.substring(0, realOffset).count { it == '.' }
                return realOffset - dotsBefore
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

// Định nghĩa Cấu Trúc Nhóm Danh Mục Chi Tiêu chuẩn theo screenshot của USER
data class CategoryItemInfo(val name: String, val emoji: String)
data class CategoryGroup(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val items: List<CategoryItemInfo>)

val CATEGORY_GROUPS = listOf(
    CategoryGroup("Cố định", Icons.Default.Home, listOf(
        CategoryItemInfo("Thuê nhà", "🏠"),
        CategoryItemInfo("Hiếu hỉ", "❤️")
    )),
    CategoryGroup("Nhu cầu thiết yếu", Icons.Default.ShoppingCart, listOf(
        CategoryItemInfo("Cafe", "☕"),
        CategoryItemInfo("Ăn uống", "🥐"),
        CategoryItemInfo("Đi chợ", "🥦"),
        CategoryItemInfo("Điện/ Nước", "💡"),
        CategoryItemInfo("Di chuyển", "🛵"),
        CategoryItemInfo("Xăng", "⛽")
    )),
    CategoryGroup("Hưởng thụ & Giải trí", Icons.Default.Waves, listOf(
        CategoryItemInfo("Mua sắm", "🛍️"),
        CategoryItemInfo("Shoppee/ Tiktok", "🛵"),
        CategoryItemInfo("Du lịch", "🛫")
    )),
    CategoryGroup("Giáo dục", Icons.Default.MenuBook, listOf(
        CategoryItemInfo("Học tập", "📚")
    ))
)

// Danh mục dành riêng cho Thu Nhập (Thu)
val INCOME_CATEGORIES = listOf(
    CategoryItemInfo("Lương", "💰"),
    CategoryItemInfo("Khác", "💵")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveTransactionScreen(
    imageSource: Any,
    onSave: (String, Double, String, String, Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("CHI") }
    var selectedCategory by remember { mutableStateOf("Ăn uống") }
    var selectedEmoji by remember { mutableStateOf("🥐") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(imageSource) {
        val scanner = com.example.dacs3_nguyencongduc.utils.BillScanner
        if (imageSource is android.graphics.Bitmap) {
            scanner.scanBill(imageSource) { detectedAmount ->
                if (detectedAmount != null) amount = detectedAmount.toLong().toString()
            }
        } else if (imageSource is android.net.Uri) {
            scanner.scanBill(context, imageSource) { detectedAmount ->
                if (detectedAmount != null) amount = detectedAmount.toLong().toString()
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = state.selectedDateMillis ?: selectedDate
                    showDatePicker = false
                }) { Text("Xong") }
            }
        ) { DatePicker(state) }
    }

    // Hiển thị Dialog chọn danh mục chuẩn 100% của USER
    if (showCategoryDialog) {
        CategorySelectionDialog(
            currentSelected = selectedCategory,
            currentEmoji = selectedEmoji,
            transactionType = transactionType,
            onDismiss = { showCategoryDialog = false },
            onConfirmed = { cat, emoji ->
                selectedCategory = cat
                selectedEmoji = emoji
                showCategoryDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            SaveScreenTopBar(onCancel = onCancel)
            // Image + Toggle
            ImageCardWithToggle(imageSource, transactionType) { type ->
                transactionType = type
                // Tự chọn mặc định tương ứng với loại GD khi đổi toggle
                if (type == "CHI") {
                    selectedCategory = "Ăn uống"
                    selectedEmoji = "🥐"
                } else {
                    selectedCategory = "Lương"
                    selectedEmoji = "💰"
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Categories + Date (Mới: Bấm chọn danh mục sẽ mở cửa sổ popup)
            CategoryAndDateRow(
                selectedCategory = selectedCategory,
                selectedEmoji = selectedEmoji,
                onCategoryClick = { showCategoryDialog = true },
                selectedDate = selectedDate,
                onDateClick = { showDatePicker = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Amount
            AmountInput(amount, { if (it.all { c -> c.isDigit() }) amount = it }, transactionType)
            Spacer(modifier = Modifier.height(16.dp))
            // Note
            NoteInput(title) { title = it }
            Spacer(modifier = Modifier.weight(1f))
            // Save
            SaveButton(transactionType) {
                onSave(title.ifEmpty { selectedCategory }, amount.toDoubleOrNull() ?: 0.0, transactionType, selectedCategory, selectedDate)
            }
        }
    }
}

@Composable
private fun SaveScreenTopBar(onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCancel) {
            Box(Modifier.size(40.dp).background(Color.White.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Text("Thêm giao dịch", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun ImageCardWithToggle(imageSource: Any, transactionType: String, onTypeChanged: (String) -> Unit) {
    val context = LocalContext.current
    Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(28.dp)).background(Color(0xFF1C1C1E))) {
        val imageRequest = remember(imageSource) { ImageRequest.Builder(context).data(imageSource).crossfade(200).build() }
        AsyncImage(model = imageRequest, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
        TransactionTypeToggle(transactionType, onTypeChanged, Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
    }
}

@Composable
private fun TransactionTypeToggle(transactionType: String, onTypeChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(28.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(onClick = { onTypeChanged("CHI") }, color = if (transactionType == "CHI") Color(0xFFFF6B6B) else Color.Transparent, shape = RoundedCornerShape(24.dp), modifier = Modifier.height(40.dp)) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Chi tiêu", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Surface(onClick = { onTypeChanged("THU") }, color = if (transactionType == "THU") LocketPurple else Color.Transparent, shape = RoundedCornerShape(24.dp), modifier = Modifier.height(40.dp)) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.ArrowDownward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Thu nhập", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CategoryAndDateRow(
    selectedCategory: String,
    selectedEmoji: String,
    onCategoryClick: () -> Unit,
    selectedDate: Long,
    onDateClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        // Pill Button hiển thị danh mục đã chọn & cho phép click để mở popup
        Surface(
            onClick = onCategoryClick,
            color = LocketPurple,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(selectedEmoji, fontSize = 15.sp)
                Text(selectedCategory, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Surface(
            onClick = onDateClick,
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(40.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = LocketPurple, modifier = Modifier.size(14.dp))
                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate)), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    currentSelected: String,
    currentEmoji: String,
    transactionType: String,
    onDismiss: () -> Unit,
    onConfirmed: (String, String) -> Unit
) {
    var tempSelectedCategory by remember { mutableStateOf(currentSelected) }
    var tempSelectedEmoji by remember { mutableStateOf(currentEmoji) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F11))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                // ── Top Bar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Text(
                        "Danh mục chi tiêu",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onConfirmed(tempSelectedCategory, tempSelectedEmoji) }
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(LocketPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // ── Danh sách các Hạng mục chuẩn ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (transactionType == "CHI") {
                        // Hiện toàn bộ nhóm chi tiêu
                        CATEGORY_GROUPS.forEach { group ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = group.icon,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = group.name,
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Layout thủ công cực kỳ ổn định, khớp 100% với ảnh mẫu của USER mà không sợ crash FlowRow ở runtime
                                val rows = when (group.name) {
                                    "Cố định" -> group.items.chunked(2)
                                    "Nhu cầu thiết yếu" -> listOf(
                                        group.items.take(3),
                                        group.items.drop(3).take(2),
                                        group.items.drop(5)
                                    ).filter { it.isNotEmpty() }
                                    "Hưởng thụ & Giải trí" -> listOf(
                                        group.items.take(2),
                                        group.items.drop(2)
                                    ).filter { it.isNotEmpty() }
                                    else -> group.items.chunked(2)
                                }

                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            val isSel = tempSelectedCategory == item.name
                                            CategoryChip(
                                                name = item.name,
                                                emoji = item.emoji,
                                                isSelected = isSel,
                                                onClick = {
                                                    tempSelectedCategory = item.name
                                                    tempSelectedEmoji = item.emoji
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Chỉ hiện nhóm thu nhập (Khoản thu)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Nguồn thu nhập",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val rows = INCOME_CATEGORIES.chunked(2)
                            rows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val isSel = tempSelectedCategory == item.name
                                        CategoryChip(
                                            name = item.name,
                                            emoji = item.emoji,
                                            isSelected = isSel,
                                            onClick = {
                                                tempSelectedCategory = item.name
                                                tempSelectedEmoji = item.emoji
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Nút tạo mới ở phía dưới ──
                val context = LocalContext.current
                Button(
                    onClick = {
                        Toast.makeText(context, "Tính năng tạo danh mục mới đang được phát triển!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocketPurple.copy(alpha = 0.12f),
                        contentColor = LocketPurple
                    )
                ) {
                    Text("+ Tạo mới", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) LocketPurple.copy(alpha = 0.25f) else Color(0xFF1E1E22),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) BorderStroke(1.dp, LocketPurple) else null,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountInput(amount: String, onAmountChange: (String) -> Unit, transactionType: String) {
    val displayAmount = amount.toDoubleOrNull() ?: 0.0
    val amountColor = if (transactionType == "CHI") Color(0xFFFF6B6B) else Color(0xFF66BB6A)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.05f)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            TextField(
                value = amount, onValueChange = onAmountChange,
                textStyle = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = amountColor, textAlign = TextAlign.Center),
                placeholder = { Text("0đ", color = Color.White.copy(0.2f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold) },
                suffix = { if (amount.isNotEmpty()) Text("đ", color = amountColor, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = amountColor),
                visualTransformation = ThousandSeparatorTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (displayAmount > 0) {
            Text("${if (transactionType == "CHI") "-" else "+"} ${formatCurrency(displayAmount)}đ", color = amountColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteInput(title: String, onTitleChange: (String) -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(20.dp), border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = title, onValueChange = onTitleChange,
            placeholder = { Text("Nhập mô tả...", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp) },
            leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = LocketPurple)
        )
    }
}

@Composable
private fun SaveButton(transactionType: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(58.dp).padding(bottom = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (transactionType == "CHI") Color(0xFFFF6B6B) else LocketPurple),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Default.Check, null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text("Lưu giao dịch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
