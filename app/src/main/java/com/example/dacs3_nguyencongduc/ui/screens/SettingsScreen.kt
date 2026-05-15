package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.dacs3_nguyencongduc.viewmodel.AuthViewModel
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple

@Composable
fun SettingsScreen(onBack: () -> Unit, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val profile by authViewModel.userProfile.collectAsState()
    
    var isStreakWidgetEnabled by remember { mutableStateOf(true) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(profile?.get("displayName") as? String ?: "") }

    // Helper functions
    val openUrl = { url: String ->
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) { }
    }

    val shareApp = {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "Tham gia Spocket cùng mình để quản lý chi tiêu cực đỉnh! Tải ngay tại: https://spocket.app")
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Chia sẻ Spocket"))
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Đổi tên hiển thị", color = Color.White) },
            containerColor = Color(0xFF1C1C1E),
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = LocketPurple,
                        focusedBorderColor = LocketPurple
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updateDisplayName(newName)
                    showEditNameDialog = false
                }) { Text("Lưu", color = LocketPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa tài khoản?", color = Color.White) },
            containerColor = Color(0xFF1C1C1E),
            text = { Text("Hành động này không thể hoàn tác. Mọi dữ liệu của bạn sẽ bị xóa vĩnh viễn.", color = Color.White.copy(0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.deleteAccount()
                    showDeleteConfirm = false
                }) { Text("Xác nhận xóa", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.1f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text("Cài đặt", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Chuỗi trên tiện ích
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Chuỗi trên tiện ích",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isStreakWidgetEnabled,
                        onCheckedChange = { isStreakWidgetEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFFB74D), // Vàng cam
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Color(0xFF333333)
                        )
                    )
                }
            }

            // TỔNG QUÁT
            item { SectionTitle("Tổng quát") }
            item { SettingsItem(Icons.Outlined.Notifications, "Thông báo", onClick = { /* Mở cài đặt hệ thống */ }) }
            item { 
                SettingsItem(
                    Icons.Outlined.Person, 
                    "Sửa tên", 
                    profile?.get("displayName") as? String ?: "Người dùng Spocket",
                    onClick = { showEditNameDialog = true }
                ) 
            }
            item { SettingsItem(Icons.Outlined.Email, "Thay đổi địa chỉ email", profile?.get("email") as? String ?: "user@gmail.com") }

            // HỖ TRỢ
            item { SectionTitle("Hỗ trợ") }
            item { SettingsItem(Icons.Outlined.HelpOutline, "Help Center", onClick = { openUrl("https://spocket.app/help") }) }
            item { SettingsItem(Icons.Outlined.FamilyRestroom, "Parent Center", onClick = { openUrl("https://spocket.app/parent") }) }
            item { SettingsItem(Icons.Outlined.Security, "Safety Center", onClick = { openUrl("https://spocket.app/safety") }) }
            item { SettingsItem(Icons.Outlined.Message, "Gửi đề xuất", onClick = { openUrl("mailto:support@spocket.app") }) }
            item { SettingsItem(Icons.Outlined.ErrorOutline, "Báo cáo sự cố", onClick = { openUrl("mailto:dev@spocket.app") }) }

            // RIÊNG TƯ & BẢO MẬT
            item { SectionTitle("Riêng tư & bảo mật") }
            item { SettingsItem(Icons.Outlined.Block, "Tài khoản bị chặn") }

            // GIỚI THIỆU
            item { SectionTitle("Giới thiệu") }
            item { SettingsItem(Icons.Outlined.PlayArrow, "TikTok", onClick = { openUrl("https://tiktok.com/@spocket") }) }
            item { SettingsItem(Icons.Outlined.CameraAlt, "Instagram", onClick = { openUrl("https://instagram.com/spocket") }) }
            item { SettingsItem(Icons.Outlined.Close, "X (Twitter)", onClick = { openUrl("https://x.com/spocket") }) }
            item { SettingsItem(Icons.Outlined.Share, "Chia sẻ Spocket", onClick = shareApp) }
            item { SettingsItem(Icons.Outlined.StarRate, "Đánh giá Spocket", onClick = { openUrl("market://details?id=" + context.packageName) }) }
            item { SettingsItem(Icons.Outlined.Description, "Điều khoản dịch vụ", onClick = { openUrl("https://spocket.app/terms") }) }
            item { SettingsItem(Icons.Outlined.PrivacyTip, "Chính sách quyền riêng tư", onClick = { openUrl("https://spocket.app/privacy") }) }

            // VÙNG NGUY HIỂM
            item { SectionTitle("Vùng nguy hiểm") }
            item {
                SettingsItem(
                    Icons.Outlined.DeleteOutline,
                    "Xóa tài khoản",
                    textColor = Color(0xFFFF5252),
                    iconColor = Color(0xFFFF5252),
                    onClick = { showDeleteConfirm = true }
                )
            }
            item {
                SettingsItem(
                    Icons.Outlined.Logout,
                    "Đăng xuất",
                    onClick = {
                        authViewModel.signOut()
                    }
                )
            }
            
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    Text("Phiên bản 1.0.0 (BETA)", color = Color.White.copy(0.2f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, top = 32.dp, bottom = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color = Color.White,
    iconColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
