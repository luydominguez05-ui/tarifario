package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.SalesGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesGoalDao {
    @Query("SELECT * FROM sales_goals WHERE id = 1 LIMIT 1")
    fun getSalesGoal(): Flow<SalesGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: SalesGoalEntity)
}
