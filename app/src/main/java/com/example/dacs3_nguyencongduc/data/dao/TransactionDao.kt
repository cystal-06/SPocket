package com.example.dacs3_nguyencongduc.data.dao

import androidx.room.*
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTransaction(): Transaction?
}