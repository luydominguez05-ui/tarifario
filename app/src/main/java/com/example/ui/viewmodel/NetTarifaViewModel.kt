package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.NetTarifaDatabase
import com.example.data.entity.QuoteStatus
import com.example.data.entity.SalesGoalEntity
import com.example.data.entity.SavedQuoteEntity
import com.example.data.model.AddOn
import com.example.data.model.InternetPlan
import com.example.data.model.PlanCategory
import com.example.data.model.Promotion
import com.example.data.repository.FeasibilityZone
import com.example.data.repository.PlanRepository
import com.example.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NetTarifaViewModel(application: Application) : AndroidViewModel(application) {

    private val planRepository = PlanRepository()
    private val database = NetTarifaDatabase.getDatabase(application)
    private val quoteRepository = QuoteRepository(database.quoteDao(), database.salesGoalDao())

    // --- CATALOG STATE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PlanCategory.ALL)
    val selectedCategory: StateFlow<PlanCategory> = _selectedCategory.asStateFlow()

    private val _minSpeedFilter = MutableStateFlow(0)
    val minSpeedFilter: StateFlow<Int> = _minSpeedFilter.asStateFlow()

    val filteredPlans: StateFlow<List<InternetPlan>> = combine(
        _searchQuery,
        _selectedCategory,
        _minSpeedFilter
    ) { query, category, minSpeed ->
        val categoryFiltered = planRepository.getPlansByCategory(category)
        categoryFiltered.filter { plan ->
            (query.isEmpty() || plan.name.contains(query, ignoreCase = true) || plan.features.any { it.contains(query, ignoreCase = true) }) &&
            plan.downloadSpeedMbps >= minSpeed
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), planRepository.samplePlans)

    // --- COMPARISON STATE ---
    private val _comparedPlans = MutableStateFlow<List<InternetPlan>>(emptyList())
    val comparedPlans: StateFlow<List<InternetPlan>> = _comparedPlans.asStateFlow()

    // --- CALCULATOR & QUOTE BUILDER STATE ---
    private val _selectedPlan = MutableStateFlow<InternetPlan?>(planRepository.samplePlans.find { it.isPopular } ?: planRepository.samplePlans.first())
    val selectedPlan: StateFlow<InternetPlan?> = _selectedPlan.asStateFlow()

    private val _selectedAddOns = MutableStateFlow<Set<AddOn>>(emptySet())
    val selectedAddOns: StateFlow<Set<AddOn>> = _selectedAddOns.asStateFlow()

    private val _selectedPromotions = MutableStateFlow<Set<Promotion>>(emptySet())
    val selectedPromotions: StateFlow<Set<Promotion>> = _selectedPromotions.asStateFlow()

    // Client form info
    val clientName = MutableStateFlow("")
    val clientPhone = MutableStateFlow("")
    val clientAddress = MutableStateFlow("")
    val clientNotes = MutableStateFlow("")

    val availableAddOns: List<AddOn> = planRepository.sampleAddOns
    val availablePromotions: List<Promotion> = planRepository.samplePromotions

    // --- ROOM SAVED QUOTES & GOALS STATE ---
    val allSavedQuotes: StateFlow<List<SavedQuoteEntity>> = quoteRepository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soldCount: StateFlow<Int> = quoteRepository.soldCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalEarnedCommissions: StateFlow<Double?> = quoteRepository.totalEarnedCommissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val salesGoal: StateFlow<SalesGoalEntity?> = quoteRepository.salesGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesGoalEntity())

    private val _selectedHistoryFilter = MutableStateFlow<QuoteStatus?>(null)
    val selectedHistoryFilter: StateFlow<QuoteStatus?> = _selectedHistoryFilter.asStateFlow()

    // --- FEASIBILITY STATE ---
    val feasibilitySearchQuery = MutableStateFlow("")
    val sampleFeasibilityZones = planRepository.sampleFeasibilityZones

    val filteredFeasibilityZones: StateFlow<List<FeasibilityZone>> = combine(
        feasibilitySearchQuery
    ) { (query) ->
        if (query.isBlank()) sampleFeasibilityZones
        else sampleFeasibilityZones.filter {
            it.zoneName.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true) ||
            it.primaryTech.label.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sampleFeasibilityZones)

    init {
        // Initialize default goal if null
        viewModelScope.launch {
            quoteRepository.saveSalesGoal(SalesGoalEntity())
        }
    }

    // --- USER ACTIONS ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: PlanCategory) {
        _selectedCategory.value = category
    }

    fun setMinSpeedFilter(speed: Int) {
        _minSpeedFilter.value = speed
    }

    fun togglePlanComparison(plan: InternetPlan) {
        val current = _comparedPlans.value.toMutableList()
        if (current.any { it.id == plan.id }) {
            current.removeAll { it.id == plan.id }
        } else if (current.size < 3) {
            current.add(plan)
        }
        _comparedPlans.value = current
    }

    fun selectPlanForQuote(plan: InternetPlan) {
        _selectedPlan.value = plan
    }

    fun toggleAddOn(addOn: AddOn) {
        val current = _selectedAddOns.value.toMutableSet()
        if (current.contains(addOn)) {
            current.remove(addOn)
        } else {
            current.add(addOn)
        }
        _selectedAddOns.value = current
    }

    fun togglePromotion(promotion: Promotion) {
        val current = _selectedPromotions.value.toMutableSet()
        if (current.contains(promotion)) {
            current.remove(promotion)
        } else {
            current.add(promotion)
        }
        _selectedPromotions.value = current
    }

    // --- CALCULATION LOGIC ---
    fun calculateQuoteSummary(): QuoteCalculationResult {
        val plan = _selectedPlan.value ?: return QuoteCalculationResult()
        
        val addOnsTotal = _selectedAddOns.value.sumOf { it.monthlyPrice }
        
        var basePromoPrice = plan.promoPriceMonthly
        var baseRegularPrice = plan.standardPriceMonthly
        
        // Apply promotional discounts
        var promoDiscountSum = 0.0
        _selectedPromotions.value.forEach { promo ->
            if (promo.isPercentage) {
                promoDiscountSum += basePromoPrice * promo.percentageValue
            } else {
                promoDiscountSum += promo.monthlyDiscountAmount
            }
        }

        val finalMonthlyPromo = (basePromoPrice + addOnsTotal - promoDiscountSum).coerceAtLeast(0.0)
        val finalMonthlyRegular = (baseRegularPrice + addOnsTotal).coerceAtLeast(0.0)

        val totalSavingsPromoMonths = (baseRegularPrice - basePromoPrice + promoDiscountSum) * plan.promoDurationMonths
        
        // Commission logic
        val estimatedCommission = plan.baseCommissionAmount + (addOnsTotal * 1.5)

        return QuoteCalculationResult(
            plan = plan,
            addOnsTotal = addOnsTotal,
            promotionsDiscount = promoDiscountSum,
            finalMonthlyPromo = finalMonthlyPromo,
            finalMonthlyRegular = finalMonthlyRegular,
            totalSavingsInPromo = totalSavingsPromoMonths,
            installationFee = plan.installationFee,
            estimatedCommission = estimatedCommission
        )
    }

    fun saveCurrentQuote(onSuccess: (Long) -> Unit) {
        val plan = _selectedPlan.value ?: return
        val calc = calculateQuoteSummary()

        val quote = SavedQuoteEntity(
            clientName = clientName.value.ifBlank { "Cliente Prospecto" },
            clientPhone = clientPhone.value.ifBlank { "N/A" },
            clientAddress = clientAddress.value.ifBlank { "Por Confirmar" },
            clientNotes = clientNotes.value,
            planId = plan.id,
            planName = plan.name,
            speedMbps = plan.downloadSpeedMbps,
            techTypeLabel = plan.techType.label,
            basePlanPrice = plan.standardPriceMonthly,
            promoPlanPrice = plan.promoPriceMonthly,
            promoDurationMonths = plan.promoDurationMonths,
            selectedAddOnsSummary = _selectedAddOns.value.joinToString(", ") { it.name }.ifEmpty { "Ninguno" },
            addOnsMonthlyTotal = calc.addOnsTotal,
            appliedDiscountsSummary = _selectedPromotions.value.joinToString(", ") { it.title }.ifEmpty { "Sin descuentos extra" },
            finalMonthlyPricePromo = calc.finalMonthlyPromo,
            finalMonthlyPriceRegular = calc.finalMonthlyRegular,
            installationFee = plan.installationFee,
            estimatedCommission = calc.estimatedCommission,
            status = QuoteStatus.PENDING
        )

        viewModelScope.launch {
            val id = quoteRepository.saveQuote(quote)
            onSuccess(id)
        }
    }

    fun updateQuoteStatus(quoteId: Int, newStatus: QuoteStatus) {
        viewModelScope.launch {
            quoteRepository.updateQuoteStatus(quoteId, newStatus)
        }
    }

    fun deleteQuote(quoteId: Int) {
        viewModelScope.launch {
            quoteRepository.deleteQuote(quoteId)
        }
    }

    fun setHistoryFilter(status: QuoteStatus?) {
        _selectedHistoryFilter.value = status
    }

    fun generateWhatsAppMessage(calc: QuoteCalculationResult): String {
        val plan = calc.plan ?: return ""
        val addOns = _selectedAddOns.value.joinToString("\n• ") { "${it.name} (+$${String.format("%.2f", it.monthlyPrice)}/mes)" }
        val addOnsText = if (addOns.isNotBlank()) "\n\n*Servicios Adicionales:*\n• $addOns" else ""
        val name = clientName.value.ifBlank { "Estimado/a cliente" }

        return """
            👋 ¡Hola *$name*! Le saluda su asesor de ventas de Internet.
            
            🚀 *PROPUESTA DE SERVICIO DE INTERNET*
            
            📌 *Plan Seleccionado:* ${plan.name}
            ⚡ *Velocidad:* ${plan.downloadSpeedMbps} Mbps Simétricos (${plan.techType.label})
            
            💵 *PRECIO PROMOCIONAL:* **$${String.format("%.2f", calc.finalMonthlyPromo)}/mes** (Durante ${plan.promoDurationMonths} meses)
            📊 *Precio Regular:* $$${String.format("%.2f", calc.finalMonthlyRegular)}/mes después del periodo promo.
            🛠️ *Costo de Instalación:* ${if (calc.installationFee == 0.0) "¡TOTALMENTE GRATIS!" else "$${String.format("%.2f", calc.installationFee)}"}
            $addOnsText
            
            ✨ *Beneficios Incluidos:*
            • ${plan.features.joinToString("\n• ")}
            
            📍 *Ubicación:* ${clientAddress.value.ifBlank { "Zona con cobertura confirmada" }}
            
            ¿Le gustaría programar la instalación para esta semana? ¡Quedo atento a su respuesta! 🙌
        """.trimIndent()
    }
}

data class QuoteCalculationResult(
    val plan: InternetPlan? = null,
    val addOnsTotal: Double = 0.0,
    val promotionsDiscount: Double = 0.0,
    val finalMonthlyPromo: Double = 0.0,
    val finalMonthlyRegular: Double = 0.0,
    val totalSavingsInPromo: Double = 0.0,
    val installationFee: Double = 0.0,
    val estimatedCommission: Double = 0.0
)
