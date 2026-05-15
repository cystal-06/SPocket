package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.data.model.Conversation
import com.example.dacs3_nguyencongduc.data.model.CURRENT_USER
import com.example.dacs3_nguyencongduc.data.model.MessageType
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple

/**
 * Màn hình danh sách tin nhắn
 */
@Composable
fun ChatListScreen(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Tin nhắn",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Edit, null, tint = LocketPurple, modifier = Modifier.size(22.dp))
            }
        }

        // ── Online friends strip ──
        Surface(
            color = Color(0xFF141414),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                conversations.take(4).forEach { conv ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(Color(conv.user.avatarColor).copy(0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(conv.user.avatarEmoji, fontSize = 22.sp)
                            }
                            // Online dot
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(Color(0xFF0A0A0A), CircleShape)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF66BB6A), CircleShape)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            conv.user.name.split(" ").last(),
                            color = Color.White.copy(0.7f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Conversation List ──
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(conversations) { conv ->
                ConversationRow(
                    conversation = conv,
                    onClick = { onConversationClick(conv) }
                )
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val lastMsg = conversation.messages.lastOrNull()
    val timeText = lastMsg?.let { formatTimeAgo(it.timestamp) } ?: ""
    val previewText = when {
        lastMsg == null -> ""
        lastMsg.type == MessageType.EMOJI -> lastMsg.content
        lastMsg.senderId == CURRENT_USER.id -> "Bạn: ${lastMsg.content}"
        else -> lastMsg.content
    }

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color(conversation.user.avatarColor).copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(conversation.user.avatarEmoji, fontSize = 26.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conversation.user.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        timeText,
                        color = Color(0xFF666666),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    previewText,
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color(0xFF444444),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "vừa xong"
        minutes < 60 -> "${minutes}ph"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}ng"
        else -> "${days / 7}t"
    }
}
