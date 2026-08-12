package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.QuoteDao
import com.example.data.dao.SalesGoalDao
import com.example.data.entity.SalesGoalEntity
import com.example.data.entity.SavedQuoteEntity

@Database(
    entities = [SavedQuoteEntity::class, SalesGoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NetTarifaDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun salesGoalDao(): SalesGoalDao

    companion object {
        @Volatile
        private var INSTANCE: NetTarifaDatabase? = null

        fun getDatabase(context: Context): NetTarifaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetTarifaDatabase::class.java,
                    "nettarifa_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
