package com.example.dacs3_nguyencongduc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dacs3_nguyencongduc.data.dao.TransactionDao
import com.example.dacs3_nguyencongduc.data.entity.Transaction

@Database(entities = [Transaction::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, 
                    AppDatabase::class.java, 
                    "finance_db"
                )
                .fallbackToDestructiveMigration() // Tự động xóa database cũ khi đổi schema
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
