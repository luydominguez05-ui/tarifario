package com.example.data.model

enum class PlanCategory(val displayName: String) {
    ALL("Todos"),
    FIBER("Fibra Óptica"),
    WIRELESS_5G("5G Inalámbrico"),
    PYME("Empresas / Pyme"),
    TV_BUNDLE("Combos TV + Net"),
    GAMING("Ultra / Gaming")
}

enum class TechType(val label: String, val badgeColorHex: String) {
    FTTH("FTTH Fibra Directa", "#00F5D4"),
    HFC("HFC Cable Coaxial", "#3A86EF"),
    WIRELESS_5G("5G Fixed Wireless", "#FF007A"),
    SATELLITE("Satélite Empresarial", "#8338EC")
}

data class InternetPlan(
    val id: String,
    val name: String,
    val category: PlanCategory,
    val techType: TechType,
    val downloadSpeedMbps: Int,
    val uploadSpeedMbps: Int,
    val standardPriceMonthly: Double,
    val promoPriceMonthly: Double,
    val promoDurationMonths: Int,
    val installationFee: Double,
    val contractMonths: Int,
    val commissionRatePercent: Double,
    val baseCommissionAmount: Double,
    val features: List<String>,
    val recommendedFor: String,
    val speedGuaranteePercent: Int = 95,
    val isPopular: Boolean = false,
    val includesMeshRouter: Boolean = false
)

data class AddOn(
    val id: String,
    val name: String,
    val description: String,
    val monthlyPrice: Double,
    val iconName: String
)

data class Promotion(
    val id: String,
    val title: String,
    val discountDescription: String,
    val monthlyDiscountAmount: Double,
    val isPercentage: Boolean = false,
    val percentageValue: Double = 0.0,
    val requiresPortability: Boolean = false
)
