package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.QuoteStatus
import com.example.data.entity.SavedQuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM saved_quotes ORDER BY createdAtTimestamp DESC")
    fun getAllQuotes(): Flow<List<SavedQuoteEntity>>

    @Query("SELECT * FROM saved_quotes WHERE id = :quoteId")
    suspend fun getQuoteById(quoteId: Int): SavedQuoteEntity?

    @Query("SELECT * FROM saved_quotes WHERE status = :status ORDER BY createdAtTimestamp DESC")
    fun getQuotesByStatus(status: QuoteStatus): Flow<List<SavedQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SavedQuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: SavedQuoteEntity)

    @Query("UPDATE saved_quotes SET status = :newStatus WHERE id = :quoteId")
    suspend fun updateQuoteStatus(quoteId: Int, newStatus: QuoteStatus)

    @Query("DELETE FROM saved_quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: Int)

    @Query("SELECT COUNT(*) FROM saved_quotes WHERE status = 'SOLD'")
    fun getSoldCount(): Flow<Int>

    @Query("SELECT SUM(estimatedCommission) FROM saved_quotes WHERE status = 'SOLD'")
    fun getTotalEarnedCommissions(): Flow<Double?>
}
