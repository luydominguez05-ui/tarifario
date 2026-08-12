package com.example.data.repository

import com.example.data.dao.QuoteDao
import com.example.data.dao.SalesGoalDao
import com.example.data.entity.QuoteStatus
import com.example.data.entity.SalesGoalEntity
import com.example.data.entity.SavedQuoteEntity
import kotlinx.coroutines.flow.Flow

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val salesGoalDao: SalesGoalDao
) {
    val allQuotes: Flow<List<SavedQuoteEntity>> = quoteDao.getAllQuotes()
    val soldCount: Flow<Int> = quoteDao.getSoldCount()
    val totalEarnedCommissions: Flow<Double?> = quoteDao.getTotalEarnedCommissions()
    val salesGoal: Flow<SalesGoalEntity?> = salesGoalDao.getSalesGoal()

    fun getQuotesByStatus(status: QuoteStatus): Flow<List<SavedQuoteEntity>> {
        return quoteDao.getQuotesByStatus(status)
    }

    suspend fun saveQuote(quote: SavedQuoteEntity): Long {
        return quoteDao.insertQuote(quote)
    }

    suspend fun updateQuoteStatus(quoteId: Int, newStatus: QuoteStatus) {
        quoteDao.updateQuoteStatus(quoteId, newStatus)
    }

    suspend fun deleteQuote(quoteId: Int) {
        quoteDao.deleteQuoteById(quoteId)
    }

    suspend fun saveSalesGoal(goal: SalesGoalEntity) {
        salesGoalDao.insertOrUpdateGoal(goal)
    }
}
