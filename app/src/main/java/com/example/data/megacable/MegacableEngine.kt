package com.example.data.megacable

import java.text.Normalizer

object MegacableEngine {

    // --- CONFIG ENGINE ---
    val tvExtraCode = "4858"

    val streamingOptions = listOf(
        StreamingOption("hbo_anuncios", "HBO", "HBO Anuncios", 80.0, "4942", "2 dispositivos", true),
        StreamingOption("hbo_max", "HBO", "HBO Max Standard", 149.0, "4943", "3 dispositivos", false),
        StreamingOption("netflix_std", "Netflix", "Netflix Estándar", 230.0, "3815", "2 dispositivos HD", false),
        StreamingOption("netflix_prem", "Netflix", "Netflix Premium", 299.0, "3816", "4 dispositivos 4K", false),
        StreamingOption("disney_ads", "Disney", "Disney+ Anuncios", 129.0, "4570", "2 dispositivos", true),
        StreamingOption("disney_std", "Disney", "Disney+ Estándar", 229.0, "4571", "2 dispositivos HD", false),
        StreamingOption("disney_prem", "Disney", "Disney+ Premium", 319.0, "4572", "4 dispositivos 4K", false)
    )

    // Code resolution lookup matrix based on (Promo, Client, IsTriple)
    fun getPackageCode(
        promo: PromotionType,
        client: ClientType,
        family: PackageFamily
    ): String {
        val isTriple = family.isTriple
        val isResidencial = client == ClientType.RESIDENCIAL

        return when (promo) {
            PromotionType.NORMAL -> {
                when {
                    !isTriple && isResidencial -> "5403"
                    !isTriple && !isResidencial -> "5412"
                    isTriple && isResidencial -> "5399"
                    else -> "5407"
                }
            }
            PromotionType.PROMO12 -> {
                when {
                    !isTriple && isResidencial -> "5456"
                    !isTriple && !isResidencial -> "5470"
                    isTriple && isResidencial -> "5453"
                    else -> "5469"
                }
            }
            PromotionType.PUEBLOS -> {
                when {
                    !isTriple && isResidencial -> "5404"
                    !isTriple && !isResidencial -> "5413"
                    isTriple && isResidencial -> "5400"
                    else -> "5408"
                }
            }
        }
    }

    // --- TARIFF ENGINE DATA ---
    val sampleBranches = listOf(
        MegacableBranch(
            number = 401,
            name = "MIGUEL HIDALGO",
            type = "EXPANSION",
            promise = "12 DÍAS",
            tvExtraPrices = listOf(50.0, 50.0, 50.0, 50.0),
            blockedColonies = listOf("SAN ANGEL RESTRENO", "SAN ANGEL ZONA RESTINGIDA", "TEPITO", "ZONA NO COBERTURA"),
            priceMatrix = createPriceMatrix(
                domiciliadoDoble = mapOf(100 to 350.0, 200 to 400.0, 300 to 450.0, 500 to 600.0, 1000 to 900.0),
                domiciliadoConecta = mapOf(100 to 420.0, 200 to 480.0, 300 to 550.0, 500 to 720.0, 1000 to 1050.0),
                domiciliadoBasico = mapOf(100 to 480.0, 200 to 550.0, 300 to 620.0, 500 to 800.0, 1000 to 1150.0),
                efectivoDoble = mapOf(100 to 380.0, 200 to 430.0, 300 to 490.0, 500 to 650.0, 1000 to 960.0),
                efectivoConecta = mapOf(100 to 460.0, 200 to 520.0, 300 to 600.0, 500 to 780.0, 1000 to 1120.0),
                efectivoBasico = mapOf(100 to 520.0, 200 to 600.0, 300 to 680.0, 500 to 870.0, 1000 to 1230.0)
            )
        ),
        MegacableBranch(
            number = 101,
            name = "GUADALAJARA CENTRO",
            type = "CONSOLIDADA",
            promise = "24 HORAS",
            tvExtraPrices = listOf(60.0, 60.0, 70.0, 80.0),
            blockedColonies = listOf("OBLATOS SECTOR B", "SAN JUAN DE DIOS RESTRICCION"),
            priceMatrix = createPriceMatrix(
                domiciliadoDoble = mapOf(100 to 330.0, 200 to 380.0, 300 to 430.0, 500 to 580.0, 1000 to 880.0),
                domiciliadoConecta = mapOf(100 to 400.0, 200 to 460.0, 300 to 530.0, 500 to 700.0, 1000 to 1000.0),
                domiciliadoBasico = mapOf(100 to 460.0, 200 to 530.0, 300 to 600.0, 500 to 780.0, 1000 to 1100.0),
                efectivoDoble = mapOf(100 to 360.0, 200 to 410.0, 300 to 470.0, 500 to 620.0, 1000 to 930.0),
                efectivoConecta = mapOf(100 to 440.0, 200 to 500.0, 300 to 570.0, 500 to 750.0, 1000 to 1080.0),
                efectivoBasico = mapOf(100 to 500.0, 200 to 570.0, 300 to 650.0, 500 to 840.0, 1000 to 1180.0)
            )
        ),
        MegacableBranch(
            number = 202,
            name = "MONTERREY NORTE",
            type = "URBANA",
            promise = "48 HORAS",
            tvExtraPrices = listOf(55.0, 65.0, 75.0, 85.0),
            blockedColonies = listOf("COLONIA INDEPENDENCIA ALTA", "CUMBRES ZONA Z"),
            priceMatrix = createPriceMatrix(
                domiciliadoDoble = mapOf(100 to 360.0, 200 to 420.0, 300 to 470.0, 500 to 620.0, 1000 to 920.0),
                domiciliadoConecta = mapOf(100 to 430.0, 200 to 490.0, 300 to 570.0, 500 to 740.0, 1000 to 1080.0),
                domiciliadoBasico = mapOf(100 to 490.0, 200 to 570.0, 300 to 640.0, 500 to 820.0, 1000 to 1180.0),
                efectivoDoble = mapOf(100 to 390.0, 200 to 450.0, 300 to 510.0, 500 to 670.0, 1000 to 980.0),
                efectivoConecta = mapOf(100 to 470.0, 200 to 530.0, 300 to 610.0, 500 to 790.0, 1000 to 1140.0),
                efectivoBasico = mapOf(100 to 530.0, 200 to 610.0, 300 to 690.0, 500 to 890.0, 1000 to 1250.0)
            )
        )
    )

    private fun createPriceMatrix(
        domiciliadoDoble: Map<Int, Double>,
        domiciliadoConecta: Map<Int, Double>,
        domiciliadoBasico: Map<Int, Double>,
        efectivoDoble: Map<Int, Double>,
        efectivoConecta: Map<Int, Double>,
        efectivoBasico: Map<Int, Double>
    ): Map<String, Map<String, Map<Int, Double>>> {
        return mapOf(
            PaymentMethod.DOMICILIADO.id to mapOf(
                PackageFamily.DOBLEPACK.id to domiciliadoDoble,
                PackageFamily.CONECTA.id to domiciliadoConecta,
                PackageFamily.BASICOPLUS.id to domiciliadoBasico
            ),
            PaymentMethod.EFECTIVO.id to mapOf(
                PackageFamily.DOBLEPACK.id to efectivoDoble,
                PackageFamily.CONECTA.id to efectivoConecta,
                PackageFamily.BASICOPLUS.id to efectivoBasico
            )
        )
    }

    // Helper to normalize string for diacritics/spaces/casing
    fun normalizeText(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .uppercase()
            .trim()
            .replace("\\s+".toRegex(), " ")
    }

    // Colony status check
    fun checkColonyStatus(branch: MegacableBranch, colony: String): Boolean {
        if (colony.isBlank()) return false
        val cleanColony = normalizeText(colony)
        return branch.blockedColonies.any { normalizeText(it).contains(cleanColony) || cleanColony.contains(normalizeText(it)) }
    }

    // --- PRICING ENGINE CALCULATOR ---
    fun calculateQuote(
        branch: MegacableBranch,
        clientType: ClientType,
        paymentMethod: PaymentMethod,
        packageFamily: PackageFamily,
        speedMbps: Int,
        promotionType: PromotionType,
        tvExtraCount: Int,
        selectedStreamingIds: List<String>,
        colony: String
    ): MegacableQuoteResult {

        // 1. Get Base Price from Branch Matrix
        val basePrice = branch.priceMatrix[paymentMethod.id]
            ?.get(packageFamily.id)
            ?.get(speedMbps) ?: 0.0

        // 2. TV Extra calculation: TV extra prices are independent per position in tvExtraPrices array!
        val tvBreakdown = mutableListOf<Double>()
        var tvExtraTotal = 0.0
        val clampedTvCount = tvExtraCount.coerceIn(0, 4)
        for (i in 0 until clampedTvCount) {
            val tvPrice = if (i < branch.tvExtraPrices.size) branch.tvExtraPrices[i] else 50.0
            tvBreakdown.add(tvPrice)
            tvExtraTotal += tvPrice
        }

        // 3. Streaming Services calculation
        val selectedStreaming = streamingOptions.filter { selectedStreamingIds.contains(it.id) }
        val streamingTotal = selectedStreaming.sumOf { it.price }

        // 4. Total Price
        val totalPrice = basePrice + tvExtraTotal + streamingTotal

        // 5. Code resolution
        val pkgCode = getPackageCode(promotionType, clientType, packageFamily)
        val streamingCodes = selectedStreaming.map { it.code }

        val isBlocked = checkColonyStatus(branch, colony)

        return MegacableQuoteResult(
            branchNumber = branch.number,
            branchName = branch.name,
            branchType = branch.type,
            branchPromise = branch.promise,
            clientType = clientType,
            paymentMethod = paymentMethod,
            packageFamily = packageFamily,
            speedMbps = speedMbps,
            promotionType = promotionType,
            basePrice = basePrice,
            tvExtraCount = clampedTvCount,
            tvExtraPricesBreakdown = tvBreakdown,
            tvExtraTotal = tvExtraTotal,
            selectedStreaming = selectedStreaming,
            streamingTotal = streamingTotal,
            totalPrice = totalPrice,
            packageCode = pkgCode,
            tvExtraCode = if (clampedTvCount > 0) tvExtraCode else "N/A",
            streamingCodes = streamingCodes,
            colony = colony,
            isColonyBlocked = isBlocked
        )
    }

    // Formatted text generator for clipboard copy
    fun generateClipboardSummary(quote: MegacableQuoteResult): String {
        val streamingCodesStr = if (quote.streamingCodes.isNotEmpty()) {
            quote.streamingCodes.joinToString("\n")
        } else {
            "N/A"
        }

        val tvExtraCodeStr = if (quote.tvExtraCount > 0) quote.tvExtraCode else "N/A"

        return """
COTIZACIÓN MEGACABLE
Sucursal: ${quote.branchNumber} - ${quote.branchName}
Tipo: ${quote.branchType}
Promesa: ${quote.branchPromise}
Cliente: ${quote.clientType.label}
Forma de pago: ${quote.paymentMethod.label}
Paquete: ${quote.packageFamily.label}
Velocidad: ${quote.speedMbps} Mbps
Promoción: ${quote.promotionType.label}
Precio base: $${quote.basePrice.toInt()}
TV adicionales: $${quote.tvExtraTotal.toInt()}
Streaming: $${quote.streamingTotal.toInt()}
TOTAL: $${quote.totalPrice.toInt()}
CÓDIGOS
Paquete: ${quote.packageCode}
TV Extra: $tvExtraCodeStr
Streaming:
$streamingCodesStr
        """.trimIndent()
    }
}
