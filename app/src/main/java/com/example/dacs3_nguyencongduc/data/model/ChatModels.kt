package com.example.dacs3_nguyencongduc.data.model

/**
 * Model cho người dùng chat
 */
data class ChatUser(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val avatarColor: Long
)

/**
 * Model cho tin nhắn
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val reaction: String? = null
)

enum class MessageType {
    TEXT, EMOJI, IMAGE
}

/**
 * Model cho cuộc trò chuyện
 */
data class Conversation(
    val id: String,
    val user: ChatUser,
    val messages: List<ChatMessage>,
    val lastActive: Long
)

// ── Dữ liệu mẫu ──

val CURRENT_USER = ChatUser("me", "Bạn", "😎", 0xFFD500F9)

private val sampleUsers = listOf(
    ChatUser("u1", "Minh Anh", "🦋", 0xFFFF6B6B),
    ChatUser("u2", "Đức Huy", "🔥", 0xFF66BB6A),
    ChatUser("u3", "Thảo Vy", "🌸", 0xFFFFB74D),
    ChatUser("u4", "Hoàng Nam", "🎮", 0xFF42A5F5),
)

val SAMPLE_CONVERSATIONS = listOf(
    Conversation(
        id = "c1",
        user = sampleUsers[0],
        messages = listOf(
            ChatMessage("m1", "u1", "💛", MessageType.EMOJI, System.currentTimeMillis() - 3600000),
            ChatMessage("m2", "u1", "💛", MessageType.EMOJI, System.currentTimeMillis() - 3500000),
            ChatMessage("m3", "u1", "💛", MessageType.EMOJI, System.currentTimeMillis() - 3400000),
            ChatMessage("m4", "me", "Hôm nay đẹp trời ghê 🌤️", MessageType.TEXT, System.currentTimeMillis() - 3000000),
            ChatMessage("m5", "u1", "Đi cafe không?", MessageType.TEXT, System.currentTimeMillis() - 2500000),
            ChatMessage("m6", "me", "💛", MessageType.EMOJI, System.currentTimeMillis() - 2000000),
            ChatMessage("m7", "me", "Đi chứ!", MessageType.TEXT, System.currentTimeMillis() - 1800000),
            ChatMessage("m8", "u1", "💛", MessageType.EMOJI, System.currentTimeMillis() - 1000000),
            ChatMessage("m9", "u1", "💛", MessageType.EMOJI, System.currentTimeMillis() - 900000),
        ),
        lastActive = System.currentTimeMillis() - 900000
    ),
    Conversation(
        id = "c2",
        user = sampleUsers[1],
        messages = listOf(
            ChatMessage("m10", "u2", "Code xong chưa bro 😂", MessageType.TEXT, System.currentTimeMillis() - 7200000),
            ChatMessage("m11", "me", "Sắp rồi 😤", MessageType.TEXT, System.currentTimeMillis() - 7000000),
            ChatMessage("m12", "u2", "💪", MessageType.EMOJI, System.currentTimeMillis() - 6800000),
            ChatMessage("m13", "me", "Deploy thành công rồi nè 🎉", MessageType.TEXT, System.currentTimeMillis() - 5000000),
            ChatMessage("m14", "u2", "🔥", MessageType.EMOJI, System.currentTimeMillis() - 4800000),
            ChatMessage("m15", "u2", "🔥", MessageType.EMOJI, System.currentTimeMillis() - 4700000),
            ChatMessage("m16", "u2", "Pro quá!", MessageType.TEXT, System.currentTimeMillis() - 4500000),
        ),
        lastActive = System.currentTimeMillis() - 4500000
    ),
    Conversation(
        id = "c3",
        user = sampleUsers[2],
        messages = listOf(
            ChatMessage("m17", "u3", "Ê ảnh mới cute ghê 😍", MessageType.TEXT, System.currentTimeMillis() - 86400000),
            ChatMessage("m18", "me", "🌸", MessageType.EMOJI, System.currentTimeMillis() - 86300000),
            ChatMessage("m19", "u3", "🌸", MessageType.EMOJI, System.currentTimeMillis() - 86200000),
            ChatMessage("m20", "me", "Cảm ơn nha 💕", MessageType.TEXT, System.currentTimeMillis() - 86100000),
        ),
        lastActive = System.currentTimeMillis() - 86100000
    ),
    Conversation(
        id = "c4",
        user = sampleUsers[3],
        messages = listOf(
            ChatMessage("m21", "u4", "Tối nay rank không?", MessageType.TEXT, System.currentTimeMillis() - 172800000),
            ChatMessage("m22", "me", "OK 9h nhé", MessageType.TEXT, System.currentTimeMillis() - 172700000),
            ChatMessage("m23", "u4", "🎮", MessageType.EMOJI, System.currentTimeMillis() - 172600000),
        ),
        lastActive = System.currentTimeMillis() - 172600000
    )
)
