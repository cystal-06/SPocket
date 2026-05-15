package com.example.dacs3_nguyencongduc.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.dacs3_nguyencongduc.data.repository.CloudinaryRepository
import com.example.dacs3_nguyencongduc.data.repository.FirebaseRepository
import com.example.dacs3_nguyencongduc.data.repository.TransactionRepository
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * FinanceViewModel - quản lý giao dịch
 * Dual-mode: Room (offline cache) + Firebase Firestore (cloud sync)
 * Ảnh: upload lên Cloudinary → lưu URL vào Firestore
 */
class FinanceViewModel(private val localRepository: TransactionRepository) : ViewModel() {

    private val firebaseRepo = FirebaseRepository()
    private val cloudinaryRepo = CloudinaryRepository()

    // Dữ liệu lấy trực tiếp từ Firebase (Real-time sync) nếu có mạng
    val transactions = firebaseRepo.getTransactionsFlow().map { cloudList ->
        cloudList.map { fTx ->
            Transaction(
                id = fTx.id.hashCode(), // Chuyển đổi String ID sang Int cho Room entity
                title = fTx.title,
                amount = fTx.amount,
                type = fTx.type,
                category = fTx.category,
                date = fTx.date,
                imageUri = fTx.imageUrl
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    /**
     * Thêm giao dịch - lưu cả local (Room) và cloud (Firebase + Cloudinary)
     */
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        imageUri: String? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            // 1. Lưu local ngay (Room) - nhanh + offline support
            localRepository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    imageUri = imageUri
                )
            )

            // Phát tín hiệu cập nhật Widget ra màn hình chính
            if (context != null) {
                val intent = android.content.Intent(context, com.example.dacs3_nguyencongduc.widget.SpocketWidgetProvider::class.java)
                intent.action = "com.example.dacs3_nguyencongduc.UPDATE_WIDGET"
                context.sendBroadcast(intent)
            }

            // 2. Upload ảnh lên Cloudinary nếu có (chạy nền)
            if (firebaseRepo.isLoggedIn) {
                var cloudImageUrl: String? = null
                if (imageUri != null && context != null) {
                    try {
                        val uri = Uri.parse(imageUri)
                        val result = cloudinaryRepo.uploadImage(context, uri)
                        cloudImageUrl = result.getOrNull()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Không block nếu upload lỗi - vẫn lưu giao dịch
                    }
                }

                // 3. Sync lên Firestore (lưu URL từ Cloudinary)
                firebaseRepo.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    imageUrl = cloudImageUrl
                )
            }
        }
    }
}

class FinanceViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
