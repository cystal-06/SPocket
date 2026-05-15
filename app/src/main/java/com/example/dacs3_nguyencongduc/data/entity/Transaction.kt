package com.example.dacs3_nguyencongduc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val imageUri: String? = null // Thêm đường dẫn ảnh cho giống Locket
)
