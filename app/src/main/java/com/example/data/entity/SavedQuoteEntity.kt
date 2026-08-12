package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QuoteStatus(val label: String, val colorHex: String) {
    PENDING("Pendiente", "#FFB703"),
    SOLD("Vendido / Instalado", "#06D6A0"),
    FOLLOW_UP("En Seguimiento", "#118AB2"),
    CANCELLED("Cancelado", "#EF476F")
}

@Entity(tableName = "saved_quotes")
data class SavedQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val clientNotes: String,
    val planId: String,
    val planName: String,
    val speedMbps: Int,
    val techTypeLabel: String,
    val basePlanPrice: Double,
    val promoPlanPrice: Double,
    val promoDurationMonths: Int,
    val selectedAddOnsSummary: String,
    val addOnsMonthlyTotal: Double,
    val appliedDiscountsSummary: String,
    val finalMonthlyPricePromo: Double,
    val finalMonthlyPriceRegular: Double,
    val installationFee: Double,
    val estimatedCommission: Double,
    val status: QuoteStatus = QuoteStatus.PENDING,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
