package com.example.dacs3_nguyencongduc.data.repository

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Repository - xử lý tương tác với Firebase backend
 * - Auth (Phone OTP)
 * - Firestore (dữ liệu giao dịch, bạn bè)
 * - Ảnh: dùng Cloudinary (xem CloudinaryRepository)
 */
class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── Collections ──
    private val usersCollection = firestore.collection("users")
    private val transactionsCollection = firestore.collection("transactions")
    private val friendsCollection = firestore.collection("friends")

    // ── Current user ──
    val currentUserId: String get() = auth.currentUser?.uid ?: ""
    val isLoggedIn: Boolean get() = auth.currentUser != null

    // ══════════════════════════════════════════════
    // AUTH - Đăng ký & Đăng nhập bằng Email và Password
    // ══════════════════════════════════════════════

    /**
     * Đăng ký tài khoản mới bằng Email và Mật khẩu
     */
    suspend fun signUpWithEmailAndPassword(email: String, password: String, displayName: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Không tạo được tài khoản")
            // Cập nhật Profile trong Firestore sau khi tạo xong tài khoản
            createOrUpdateUser(displayName = displayName)
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đăng nhập bằng Email và Mật khẩu
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Sai thông tin đăng nhập")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════
    // AUTH - Đăng nhập bằng số điện thoại (Phone OTP)
    // ══════════════════════════════════════════════

    /**
     * Xác thực OTP và đăng nhập
     */
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<String> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: throw Exception("Không lấy được UID")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── SOCIAL AUTH (Google, Facebook, Twitter) ──
    
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: throw Exception("Lỗi đăng nhập Google"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithFacebook(accessToken: String): Result<String> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: throw Exception("Lỗi đăng nhập Facebook"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithTwitter(accessToken: String, secret: String): Result<String> {
        return try {
            // Twitter sử dụng OAuthProvider trong Firebase mới
            val credential = OAuthProvider.newCredentialBuilder("twitter.com")
                .build() // Thực tế Twitter cần provider/activity nhưng ta có thể truyền credential nếu có token/secret
            // Tạm thời Firebase Auth cho Twitter đã bị deprecate token/secret trực tiếp
            // Thông thường sẽ gọi OAuthProvider từ Activity, đoạn này setup sẵn
            Result.failure(Exception("Twitter Sign-In requires Activity Context. Implement via OAuthProvider in Activity."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tạo/cập nhật profile user trong Firestore
     */
    suspend fun createOrUpdateUser(
        displayName: String,
        avatarEmoji: String = "😎",
        avatarColor: Long = 0xFFD500F9
    ): Result<Unit> {
        return try {
            val uid = currentUserId ?: throw Exception("Chưa đăng nhập")
            val phoneNumber = auth.currentUser?.phoneNumber ?: ""

            val userData = hashMapOf(
                "displayName" to displayName,
                "phoneNumber" to phoneNumber,
                "avatarEmoji" to avatarEmoji,
                "avatarColor" to avatarColor,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            usersCollection.document(uid).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy hồ sơ thông tin người dùng từ Firestore
     */
    suspend fun getUserProfile(): Result<Map<String, Any>?> {
        return try {
            val uid = currentUserId
            if (uid.isEmpty()) return Result.success(null)
            val doc = usersCollection.document(uid).get().await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Đăng xuất
     */
    fun signOut() {
        auth.signOut()
    }

    // ══════════════════════════════════════════════
    // FIRESTORE - Giao dịch (Transactions)
    // ══════════════════════════════════════════════

    /**
     * Thêm giao dịch mới vào Firestore
     */
    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        imageUrl: String? = null
    ): Result<String> {
        return try {
            val uid = currentUserId
            val data = hashMapOf(
                "userId" to uid,
                "title" to title,
                "amount" to amount,
                "type" to type,
                "category" to category,
                "date" to date,
                "imageUrl" to imageUrl,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            val docRef = transactionsCollection.add(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy toàn bộ giao dịch (Real-time) của user hiện tại
     */
    fun getTransactionsFlow(): Flow<List<FirestoreTransaction>> = callbackFlow {
        val uid = currentUserId
        if (uid.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Query theo UID thực + fallback uid cũ (bypass test) để không mất data cũ
        val uidsToQuery = listOf(uid, "user_bypass_test_123")

        val listener = transactionsCollection
            .whereIn("userId", uidsToQuery)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val txList = snapshot?.documents?.mapNotNull { doc ->
                    FirestoreTransaction(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        type = doc.getString("type") ?: "CHI",
                        category = doc.getString("category") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        imageUrl = doc.getString("imageUrl")
                    )
                }?.sortedByDescending { it.date } ?: emptyList()
                trySend(txList)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Xóa giao dịch
     */
    suspend fun deleteTransaction(docId: String): Result<Unit> {
        return try {
            transactionsCollection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════
    // FIRESTORE - Bạn bè (Friends)
    // ══════════════════════════════════════════════

    /**
     * Thêm bạn bè
     */
    suspend fun addFriend(friendId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: throw Exception("Chưa đăng nhập")
            friendsCollection.document(uid).update(
                "friendIds", com.google.firebase.firestore.FieldValue.arrayUnion(friendId)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Nếu document chưa tồn tại, tạo mới
            try {
                val uid = currentUserId ?: throw Exception("Chưa đăng nhập")
                friendsCollection.document(uid).set(
                    hashMapOf("friendIds" to listOf(friendId))
                ).await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    /**
     * Xóa bạn bè
     */
    suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: throw Exception("Chưa đăng nhập")
            friendsCollection.document(uid).update(
                "friendIds", com.google.firebase.firestore.FieldValue.arrayRemove(friendId)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách bạn bè real-time
     */
    fun getFriendsFlow(): Flow<List<String>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = friendsCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                @Suppress("UNCHECKED_CAST")
                val ids = snapshot?.get("friendIds") as? List<String> ?: emptyList()
                trySend(ids)
            }

        awaitClose { listener.remove() }
    }
}

/**
 * Data class cho giao dịch từ Firestore
 */
data class FirestoreTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val imageUrl: String?
)
