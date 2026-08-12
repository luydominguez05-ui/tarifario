package com.example.data.megacable

enum class ClientType(val id: String, val label: String) {
    RESIDENCIAL("residencial", "Residencial"),
    NEGOCIO("negocio", "Negocio")
}

enum class PaymentMethod(val id: String, val label: String) {
    DOMICILIADO("domiciliado", "Domiciliado"),
    EFECTIVO("efectivo", "Efectivo")
}

enum class PackageFamily(val id: String, val label: String, val isTriple: Boolean) {
    DOBLEPACK("doblepack", "Doble Pack", false),
    CONECTA("conecta", "Conecta (Triple)", true),
    BASICOPLUS("basicoplus", "Básico Plus (Triple)", true)
}

enum class PromotionType(val id: String, val label: String) {
    NORMAL("normal", "Normal"),
    PROMO12("promo12", "Promo 12 Meses"),
    PUEBLOS("pueblos", "Pueblos")
}

data class StreamingOption(
    val id: String,
    val service: String,
    val plan: String,
    val price: Double,
    val code: String,
    val devices: String,
    val ads: Boolean
)

data class MegacableBranch(
    val number: Int,
    val name: String,
    val type: String,
    val promise: String,
    val tvExtraPrices: List<Double>, // [tv1, tv2, tv3, tv4]
    val blockedColonies: List<String>,
    // Map<PaymentMethod, Map<PackageFamily, Map<SpeedMbps, Price>>>
    val priceMatrix: Map<String, Map<String, Map<Int, Double>>>
)

data class MegacableQuoteResult(
    val branchNumber: Int,
    val branchName: String,
    val branchType: String,
    val branchPromise: String,
    val clientType: ClientType,
    val paymentMethod: PaymentMethod,
    val packageFamily: PackageFamily,
    val speedMbps: Int,
    val promotionType: PromotionType,
    val basePrice: Double,
    val tvExtraCount: Int,
    val tvExtraPricesBreakdown: List<Double>,
    val tvExtraTotal: Double,
    val selectedStreaming: List<StreamingOption>,
    val streamingTotal: Double,
    val totalPrice: Double,
    val packageCode: String,
    val tvExtraCode: String,
    val streamingCodes: List<String>,
    val colony: String,
    val isColonyBlocked: Boolean
)
