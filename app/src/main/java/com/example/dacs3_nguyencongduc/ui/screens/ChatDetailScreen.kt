package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.data.model.*
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Quick-send emojis
private val QUICK_EMOJIS = listOf("💛", "❤️", "😂", "😍", "🔥", "😘")

/**
 * Màn hình chat chi tiết
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var messages by remember { mutableStateOf(conversation.messages.toMutableList()) }
    var inputText by remember { mutableStateOf("") }
    var showContextMenu by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Top Bar ──
            ChatTopBar(user = conversation.user, onBack = onBack)

            // ── Messages ──
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Time separator at top
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color(0xFF1C1C1E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Hôm nay",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == CURRENT_USER.id

                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showContextMenu = msg
                        }
                    )
                }
            }

            // ── Input Bar ──
            ChatInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        messages = (messages + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            senderId = CURRENT_USER.id,
                            content = inputText.trim(),
                            type = MessageType.TEXT,
                            timestamp = System.currentTimeMillis()
                        )).toMutableList()
                        inputText = ""
                    }
                },
                onEmojiSend = { emoji ->
                    messages = (messages + ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = CURRENT_USER.id,
                        content = emoji,
                        type = MessageType.EMOJI,
                        timestamp = System.currentTimeMillis()
                    )).toMutableList()
                }
            )
        }

        // ── Context Menu Overlay ──
        if (showContextMenu != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6f))
                    .clickable { showContextMenu = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(Modifier.width(220.dp)) {
                        // Show the message
                        if (showContextMenu?.type == MessageType.TEXT) {
                            Text(
                                showContextMenu?.content ?: "",
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider(color = Color.White.copy(0.08f))
                        }

                        // Copy text option
                        if (showContextMenu?.type == MessageType.TEXT) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { showContextMenu = null }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("Sao chép", color = Color.White, fontSize = 15.sp)
                            }
                            HorizontalDivider(color = Color.White.copy(0.08f))
                        }

                        // Undo send (only own messages)
                        if (showContextMenu?.senderId == CURRENT_USER.id) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showContextMenu?.let { msg ->
                                            messages = messages
                                                .filter { it.id != msg.id }
                                                .toMutableList()
                                        }
                                        showContextMenu = null
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Undo, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                                Text("Thu hồi", color = Color(0xFFFF6B6B), fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(user: ChatUser, onBack: () -> Unit) {
    Surface(
        color = Color(0xFF111111),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            // Avatar
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color(user.avatarColor).copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.avatarEmoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Đang hoạt động", color = Color(0xFF66BB6A), fontSize = 12.sp)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onLongPress: () -> Unit
) {
    val alignment = if (isMe) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        if (message.type == MessageType.EMOJI) {
            // Emoji - large, no bubble
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val emojiScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Text(
                message.content,
                fontSize = 44.sp,
                modifier = Modifier
                    .scale(emojiScale)
                    .padding(vertical = 2.dp, horizontal = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    }
            )
        } else {
            // Text bubble
            val bubbleColor = if (isMe) {
                Brush.horizontalGradient(listOf(LocketPurple.copy(0.9f), LocketPurple.copy(0.7f)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF252525)))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMe) 20.dp else 6.dp,
                            bottomEnd = if (isMe) 6.dp else 20.dp
                        )
                    )
                    .background(bubbleColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        message.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        color = Color.White.copy(0.5f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onEmojiSend: (String) -> Unit
) {
    Surface(
        color = Color(0xFF111111),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Quick emoji row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QUICK_EMOJIS.forEach { emoji ->
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onEmojiSend(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Input field
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        placeholder = {
                            Text("Gửi tin nhắn...", color = Color(0xFF666666), fontSize = 15.sp)
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() })
                    )
                }

                // Send button
                if (inputText.isNotBlank()) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(LocketPurple, Color(0xFF9C27B0))),
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { onSend() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

private val EaseInOutSine: Easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
