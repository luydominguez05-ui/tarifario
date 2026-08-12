package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_goals")
data class SalesGoalEntity(
    @PrimaryKey val id: Int = 1,
    val monthYear: String = "Agosto 2026",
    val monthlyTargetSalesCount: Int = 20,
    val tier1CountLimit: Int = 10,
    val tier1BonusPerSale: Double = 25.0,
    val tier2CountLimit: Int = 20,
    val tier2BonusPerSale: Double = 40.0,
    val tier3BonusPerSale: Double = 60.0,
    val baseMonthlySalary: Double = 400.0
)
