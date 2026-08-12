package com.example.data.repository

import com.example.data.model.AddOn
import com.example.data.model.InternetPlan
import com.example.data.model.PlanCategory
import com.example.data.model.Promotion
import com.example.data.model.TechType

data class FeasibilityZone(
    val id: String,
    val zoneName: String,
    val primaryTech: TechType,
    val maxSpeedMbps: Int,
    val coverageQuality: String,
    val estimatedInstallationDays: String,
    val notes: String
)

class PlanRepository {

    val samplePlans = listOf(
        InternetPlan(
            id = "plan_fibra_100",
            name = "Fibra Inicial 100M",
            category = PlanCategory.FIBER,
            techType = TechType.FTTH,
            downloadSpeedMbps = 100,
            uploadSpeedMbps = 100,
            standardPriceMonthly = 29.99,
            promoPriceMonthly = 19.99,
            promoDurationMonths = 6,
            installationFee = 0.0,
            contractMonths = 12,
            commissionRatePercent = 100.0,
            baseCommissionAmount = 25.00,
            features = listOf(
                "100 Mbps Simétricos",
                "Router Wi-Fi 5 Dual Band",
                "Instalación Gratis en 24h",
                "Soporte Estándar 24/7"
            ),
            recommendedFor = "Hogares pequeños (1-3 personas), navegación y teletrabajo básico.",
            isPopular = false,
            includesMeshRouter = false
        ),
        InternetPlan(
            id = "plan_fibra_300",
            name = "Fibra Hogar Plus 300M",
            category = PlanCategory.FIBER,
            techType = TechType.FTTH,
            downloadSpeedMbps = 300,
            uploadSpeedMbps = 300,
            standardPriceMonthly = 39.99,
            promoPriceMonthly = 27.99,
            promoDurationMonths = 6,
            installationFee = 0.0,
            contractMonths = 12,
            commissionRatePercent = 120.0,
            baseCommissionAmount = 35.00,
            features = listOf(
                "300 Mbps Simétricos",
                "Router Wi-Fi 6 de Última Generación",
                "Instalación Gratis Express",
                "1 Extensor Mesh Incluido",
                "Streaming 4K Multipantalla"
            ),
            recommendedFor = "Familias (3-5 usuarios), streaming 4K simultáneo y clases en línea.",
            isPopular = true,
            includesMeshRouter = true
        ),
        InternetPlan(
            id = "plan_fibra_600",
            name = "Fibra Ultra 600M",
            category = PlanCategory.FIBER,
            techType = TechType.FTTH,
            downloadSpeedMbps = 600,
            uploadSpeedMbps = 600,
            standardPriceMonthly = 54.99,
            promoPriceMonthly = 38.99,
            promoDurationMonths = 12,
            installationFee = 0.0,
            contractMonths = 12,
            commissionRatePercent = 150.0,
            baseCommissionAmount = 50.00,
            features = listOf(
                "600 Mbps Simétricos",
                "Router Wi-Fi 6E Profesional",
                "2 Extensores Mesh Wi-Fi Incluidos",
                "Prioridad de Tráfico Gaming",
                "Soporte Técnico VIP"
            ),
            recommendedFor = "Gamers, creadores de contenido, casas inteligentes y teletrabajo pesado.",
            isPopular = false,
            includesMeshRouter = true
        ),
        InternetPlan(
            id = "plan_fibra_1g",
            name = "Fibra GigaPro 1 Gbps",
            category = PlanCategory.GAMING,
            techType = TechType.FTTH,
            downloadSpeedMbps = 1000,
            uploadSpeedMbps = 1000,
            standardPriceMonthly = 79.99,
            promoPriceMonthly = 59.99,
            promoDurationMonths = 12,
            installationFee = 0.0,
            contractMonths = 12,
            commissionRatePercent = 200.0,
            baseCommissionAmount = 75.00,
            features = listOf(
                "1,000 Mbps (1 Gbps Simétrico)",
                "Router Wi-Fi 6E Tribanda Pro",
                "Red MESH Integral (2 Nodos)",
                "IP Dinámica de Baja Latencia",
                "Garantía Ancho de Banda 99%"
            ),
            recommendedFor = "Casas muy grandes, usuarios entusiastas, servidores caseros y streaming 8K.",
            isPopular = true,
            includesMeshRouter = true
        ),
        InternetPlan(
            id = "plan_5g_home",
            name = "NetMobile 5G Home",
            category = PlanCategory.WIRELESS_5G,
            techType = TechType.WIRELESS_5G,
            downloadSpeedMbps = 250,
            uploadSpeedMbps = 50,
            standardPriceMonthly = 34.99,
            promoPriceMonthly = 24.99,
            promoDurationMonths = 3,
            installationFee = 0.0,
            contractMonths = 6,
            commissionRatePercent = 100.0,
            baseCommissionAmount = 30.00,
            features = listOf(
                "Sin Cables ni Obras",
                "Módem 5G Auto-Instalable",
                "Llévalo donde quieras",
                "Conexión Inmediata al Enchufar"
            ),
            recommendedFor = "Apartamentos en alquiler, zonas rurales con buena señal 5G o clientes sin fibra.",
            isPopular = false,
            includesMeshRouter = false
        ),
        InternetPlan(
            id = "plan_pyme_pro",
            name = "Pyme Connect 500M Pro",
            category = PlanCategory.PYME,
            techType = TechType.FTTH,
            downloadSpeedMbps = 500,
            uploadSpeedMbps = 500,
            standardPriceMonthly = 89.99,
            promoPriceMonthly = 69.99,
            promoDurationMonths = 6,
            installationFee = 25.0,
            contractMonths = 18,
            commissionRatePercent = 180.0,
            baseCommissionAmount = 90.00,
            features = listOf(
                "500 Mbps Simétricos Garantizados",
                "1 IP Estática Pública Incluida",
                "Router Empresarial con Firewall",
                "SLA de Atención < 4 Horas",
                "Facturación Fiscal Pyme"
            ),
            recommendedFor = "Oficinas, comercios, agencias, restaurantes y clínicas pequeñas.",
            isPopular = false,
            includesMeshRouter = false
        ),
        InternetPlan(
            id = "plan_tv_combo_300",
            name = "Fibra 300M + TV Max HD",
            category = PlanCategory.TV_BUNDLE,
            techType = TechType.FTTH,
            downloadSpeedMbps = 300,
            uploadSpeedMbps = 300,
            standardPriceMonthly = 59.99,
            promoPriceMonthly = 44.99,
            promoDurationMonths = 6,
            installationFee = 0.0,
            contractMonths = 12,
            commissionRatePercent = 140.0,
            baseCommissionAmount = 55.00,
            features = listOf(
                "300 Mbps Fibra Óptica",
                "120+ Canales HD + 4K",
                "2 Decodificadores Android TV",
                "App Movilidad Multipantalla Gratis",
                "Rebobinado de TV 7 Días"
            ),
            recommendedFor = "Hogares familiares que buscan entretenimiento total con TV en vivo y cine.",
            isPopular = true,
            includesMeshRouter = true
        )
    )

    val sampleAddOns = listOf(
        AddOn("addon_mesh", "Extensor Mesh Wi-Fi 6 Extra", "Aumenta la cobertura a habitaciones lejanas", 4.99, "Wifi"),
        AddOn("addon_tv_pack", "Pack Deportes Premium & Cines", "30 Canales adicionales de fútbol y películas", 9.99, "Tv"),
        AddOn("addon_static_ip", "IP Estática Dedicada", "Ideal para cámaras CCTV, servidores y VPNs", 12.00, "Dns"),
        AddOn("addon_cyber", "Escudo Ciberseguridad Hogar", "Protección antivirus en router para todos los dispositivos", 3.50, "ShieldCheck"),
        AddOn("addon_mobile_sim", "Línea Móvil 5G 50GB", "Chip SIM con llamadas ilimitadas y datos 5G", 9.99, "Smartphone")
    )

    val samplePromotions = listOf(
        Promotion("promo_portability", "Bono Portabilidad Móvil / Internet", "Descuento de $5.00/mes durante 12 meses", 5.00, isPercentage = false, requiresPortability = true),
        Promotion("promo_autopay", "Descuento Pago Automático", "Ahorra $3.00/mes al afiliar tarjeta de débito o crédito", 3.00, isPercentage = false),
        Promotion("promo_special_5", "Descuento Especial Vendedor", "Descuento promocional autorizado del 10%", 0.10, isPercentage = true)
    )

    val sampleFeasibilityZones = listOf(
        FeasibilityZone("z1", "Centro Urbano / Sector Comercial", TechType.FTTH, 1000, "Excelente (100% Fibra Subterránea)", "24 a 48 horas", "Cajas NAP con puertos libres. Red de alta disponibilidad."),
        FeasibilityZone("z2", "Residencial Las Acacias / Novedad", TechType.FTTH, 600, "Muy Buena (Fibra Aérea FTTH)", "24 a 48 horas", "Acceso rápido por postería convencional."),
        FeasibilityZone("z3", "Urbanización Los Pinos / Periferia", TechType.WIRELESS_5G, 250, "Buena Cobertura 5G Home", "Entrega Inmediata", "Ideal solución sin cables. Antena 5G a 400 metros."),
        FeasibilityZone("z4", "Zona Industrial Norte / Parque Pyme", TechType.FTTH, 1000, "Acometida Dedicada Pyme", "48 a 72 horas", "Soporta IPs estáticas y enlaces simétricos empresariales."),
        FeasibilityZone("z5", "Sector Altos del Valle / Rural Proxi", TechType.SATELLITE, 150, "Factibilidad Coaxial / Satelital", "3 a 5 días hábiles", "Fibra en despliegue. Disponible HFC y Radioenlace.")
    )

    fun getPlansByCategory(category: PlanCategory): List<InternetPlan> {
        return if (category == PlanCategory.ALL) samplePlans else samplePlans.filter { it.category == category }
    }

    fun searchPlans(query: String, minSpeed: Int): List<InternetPlan> {
        return samplePlans.filter { plan ->
            (query.isEmpty() || plan.name.contains(query, ignoreCase = true) || plan.features.any { it.contains(query, ignoreCase = true) }) &&
            plan.downloadSpeedMbps >= minSpeed
        }
    }
}
