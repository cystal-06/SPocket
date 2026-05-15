package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import com.example.dacs3_nguyencongduc.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

private val DEFAULT_CATEGORIES = listOf(
    "Ăn uống" to "🍜", "Đi lại" to "🚗", "Mua sắm" to "🛒",
    "Giải trí" to "🎮", "Lương" to "💰", "Chung" to "📦"
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
    var selectedCategory by remember { mutableStateOf("Chung") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }


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
            ImageCardWithToggle(imageSource, transactionType) { transactionType = it }
            Spacer(modifier = Modifier.height(16.dp))
            // Categories + Date
            CategoryAndDateRow(selectedCategory, { selectedCategory = it }, selectedDate) { showDatePicker = true }
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
private fun CategoryAndDateRow(selectedCategory: String, onCategorySelected: (String) -> Unit, selectedDate: Long, onDateClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DEFAULT_CATEGORIES) { (cat, emoji) ->
                Surface(
                    onClick = { onCategorySelected(cat) },
                    color = if (selectedCategory == cat) LocketPurple.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp), modifier = Modifier.height(38.dp),
                    border = if (selectedCategory == cat) null else BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(emoji, fontSize = 14.sp)
                        Text(cat, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Surface(onClick = onDateClick, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(38.dp), border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))) {
            Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarToday, null, tint = LocketPurple, modifier = Modifier.size(15.dp))
                Text(SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(selectedDate)), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
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
