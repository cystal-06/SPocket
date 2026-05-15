package com.example.dacs3_nguyencongduc.data.repository

import com.example.dacs3_nguyencongduc.data.dao.TransactionDao
import com.example.dacs3_nguyencongduc.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insert(transaction)
}