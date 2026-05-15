package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple

// ── Data models ──

data class FriendUser(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Long,
    val status: FriendStatus
)

enum class FriendStatus { FRIEND, SUGGESTED, PENDING }

// ── Dữ liệu mẫu ──

private val MY_FRIENDS = listOf(
    FriendUser("f1", "Minh Anh", "🦋", 0xFFFF6B6B, FriendStatus.FRIEND),
    FriendUser("f2", "Đức Huy", "🔥", 0xFF66BB6A, FriendStatus.FRIEND),
    FriendUser("f3", "Thảo Vy", "🌸", 0xFFFFB74D, FriendStatus.FRIEND),
    FriendUser("f4", "Hoàng Nam", "🎮", 0xFF42A5F5, FriendStatus.FRIEND),
)

private val SUGGESTIONS = listOf(
    FriendUser("s1", "Phương Linh", "🌺", 0xFFE040FB, FriendStatus.SUGGESTED),
    FriendUser("s2", "Quốc Bảo", "⚡", 0xFFFFD54F, FriendStatus.SUGGESTED),
    FriendUser("s3", "Thu Hà", "🌙", 0xFF80DEEA, FriendStatus.SUGGESTED),
    FriendUser("s4", "Trung Kiên", "🎸", 0xFFFF8A65, FriendStatus.SUGGESTED),
    FriendUser("s5", "Ngọc Trinh", "💎", 0xFFCE93D8, FriendStatus.SUGGESTED),
    FriendUser("s6", "Văn Toàn", "⚽", 0xFFA5D6A7, FriendStatus.SUGGESTED),
)

private val SHARE_OPTIONS = listOf(
    Triple("Facebook", Icons.Default.Facebook, 0xFF1877F2),
    Triple("Zalo", Icons.Default.Chat, 0xFF0068FF),
    Triple("Tin nhắn", Icons.Default.Sms, 0xFF66BB6A),
)

/**
 * Màn hình Bạn bè - Friends Screen
 */
@Composable
fun FriendsScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf(MY_FRIENDS.toList()) }
    var suggestions by remember { mutableStateOf(SUGGESTIONS.toList()) }
    var showAllSuggestions by remember { mutableStateOf(false) }

    val displayedSuggestions = if (showAllSuggestions) suggestions else suggestions.take(3)

    val filteredFriends = remember(searchQuery, friends) {
        if (searchQuery.isBlank()) friends
        else friends.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val filteredSuggestions = remember(searchQuery, displayedSuggestions) {
        if (searchQuery.isBlank()) displayedSuggestions
        else displayedSuggestions.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top Bar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.08f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text(
                "Bạn bè",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LocketPurple.copy(0.15f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = LocketPurple, modifier = Modifier.size(20.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Header Stats ──
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Friend count with accent
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${friends.size}",
                            color = LocketPurple,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            " trên ",
                            color = Color.White.copy(0.5f),
                            fontSize = 16.sp
                        )
                        Text(
                            "10",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            " bạn bè",
                            color = Color.White.copy(0.5f),
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Thêm bạn thân để chia sẻ khoảnh khắc",
                        color = LocketPurple.copy(0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // ── Search Bar ──
            item {
                Surface(
                    color = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search, null,
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Tìm kiếm bạn bè...", color = Color(0xFF666666), fontSize = 15.sp)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = LocketPurple
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── My Friends Section ──
            item {
                SectionHeader(
                    title = "Bạn bè của bạn",
                    count = friends.size,
                    icon = Icons.Default.People
                )
            }

            // Friend avatars horizontal strip
            item {
                if (friends.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(friends, key = { it.id }) { friend ->
                            FriendAvatarChip(friend)
                        }
                    }
                }
            }

            // Friend list
            items(filteredFriends, key = { "friend_${it.id}" }) { friend ->
                FriendRow(
                    user = friend,
                    actionType = ActionType.REMOVE,
                    onAction = {
                        friends = friends.filter { it.id != friend.id }
                        suggestions = suggestions + friend.copy(status = FriendStatus.SUGGESTED)
                    }
                )
            }

            // ── Gợi ý kết bạn Section ──
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(
                    title = "Gợi ý cho bạn",
                    count = suggestions.size,
                    icon = Icons.Default.PersonAdd
                )
            }

            items(filteredSuggestions, key = { "sug_${it.id}" }) { sug ->
                FriendRow(
                    user = sug,
                    actionType = ActionType.ADD,
                    onAction = {
                        suggestions = suggestions.filter { it.id != sug.id }
                        friends = friends + sug.copy(status = FriendStatus.FRIEND)
                    }
                )
            }

            // Show more button
            if (!showAllSuggestions && suggestions.size > 3) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = { showAllSuggestions = true },
                            color = Color(0xFF1C1C1E),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "Xem thêm",
                                color = Color.White.copy(0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // ── Chia sẻ Section ──
            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Share, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(18.dp))
                    Text(
                        "Chia sẻ liên kết của bạn",
                        color = Color.White.copy(0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            items(SHARE_OPTIONS) { (name, icon, color) ->
                ShareOptionRow(name = name, icon = icon, color = Color(color))
            }

            // ── Continue Button ──
            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(LocketPurple, Color(0xFF9C27B0))
                                ),
                                RoundedCornerShape(28.dp)
                            ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Tiếp tục",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ──

@Composable
private fun SectionHeader(title: String, count: Int, icon: ImageVector) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = LocketPurple, modifier = Modifier.size(18.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Surface(color = LocketPurple.copy(0.15f), shape = CircleShape) {
            Text(
                "$count",
                color = LocketPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FriendAvatarChip(friend: FriendUser) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier
                    .size(56.dp)
                    .border(2.dp, Color(friend.color).copy(0.4f), CircleShape)
                    .padding(3.dp)
                    .background(Color(friend.color).copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(friend.emoji, fontSize = 26.sp)
            }
            Box(
                Modifier
                    .size(16.dp)
                    .background(Color(0xFF0A0A0A), CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().background(Color(0xFF66BB6A), CircleShape)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            friend.name.split(" ").last(),
            color = Color.White.copy(0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private enum class ActionType { ADD, REMOVE }

@Composable
private fun FriendRow(
    user: FriendUser,
    actionType: ActionType,
    onAction: () -> Unit
) {
    var actionDone by remember { mutableStateOf(false) }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141414))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                Modifier
                    .size(48.dp)
                    .background(Color(user.color).copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(14.dp))

            // Name + status
            Column(Modifier.weight(1f)) {
                Text(
                    user.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when (user.status) {
                        FriendStatus.FRIEND -> "Bạn bè"
                        FriendStatus.SUGGESTED -> "Gợi ý cho bạn"
                        FriendStatus.PENDING -> "Đang chờ"
                    },
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )
            }

            // Action button
            AnimatedContent(
                targetState = actionDone,
                transitionSpec = {
                    scaleIn(tween(150)) + fadeIn() togetherWith
                            scaleOut(tween(150)) + fadeOut()
                },
                label = ""
            ) { done ->
                if (done) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(Color(0xFF66BB6A).copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF66BB6A), modifier = Modifier.size(18.dp))
                    }
                } else {
                    when (actionType) {
                        ActionType.ADD -> {
                            Surface(
                                onClick = {
                                    actionDone = true
                                    onAction()
                                },
                                color = LocketPurple,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Thêm", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        ActionType.REMOVE -> {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.06f))
                                    .clickable {
                                        actionDone = true
                                        onAction()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MoreHoriz, null, tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareOptionRow(name: String, icon: ImageVector, color: Color) {
    Surface(
        onClick = { },
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141414))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(color.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF444444), modifier = Modifier.size(20.dp))
        }
    }
}
