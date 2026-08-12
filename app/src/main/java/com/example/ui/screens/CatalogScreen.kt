package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InternetPlan
import com.example.data.model.PlanCategory
import com.example.ui.components.CommissionBadge
import com.example.ui.components.PlanSpeedBadge
import com.example.ui.viewmodel.NetTarifaViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    viewModel: NetTarifaViewModel,
    onNavigateToCalculator: () -> Unit,
    onNavigateToCompare: () -> Unit
) {
    val plans by viewModel.filteredPlans.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val minSpeed by viewModel.minSpeedFilter.collectAsState()
    val comparedPlans by viewModel.comparedPlans.collectAsState()

    var showSpeedQuizDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search & Quiz Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar planes, gigas, router...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_plans_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = { showSpeedQuizDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.testTag("recommend_quiz_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Test de Necesidades",
                        tint = Color.White
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(PlanCategory.entries.toTypedArray()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setSelectedCategory(category) },
                    label = { Text(category.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("category_chip_${category.name}")
                )
            }
        }

        // Speed Slider Filter
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Velocidad mínima: ${minSpeed} Mbps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (minSpeed > 0) {
                    TextButton(onClick = { viewModel.setMinSpeedFilter(0) }) {
                        Text("Limpiar filtro")
                    }
                }
            }
            Slider(
                value = minSpeed.toFloat(),
                onValueChange = { viewModel.setMinSpeedFilter(it.toInt()) },
                valueRange = 0f..1000f,
                steps = 9
            )
        }

        // Floating Banner for Compared Items
        AnimatedVisibility(visible = comparedPlans.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${comparedPlans.size} de 3 planes en comparación",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onNavigateToCompare,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Tabla")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Plans List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(plans, key = { it.id }) { plan ->
                PlanCatalogCard(
                    plan = plan,
                    isCompared = comparedPlans.any { it.id == plan.id },
                    onToggleCompare = { viewModel.togglePlanComparison(plan) },
                    onQuotePlan = {
                        viewModel.selectPlanForQuote(plan)
                        onNavigateToCalculator()
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showSpeedQuizDialog) {
        SpeedRecommendationQuizDialog(
            onDismiss = { showSpeedQuizDialog = false },
            onSelectPlan = { recommendedPlan ->
                viewModel.selectPlanForQuote(recommendedPlan)
                showSpeedQuizDialog = false
                onNavigateToCalculator()
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun PlanCatalogCard(
    plan: InternetPlan,
    isCompared: Boolean,
    onToggleCompare: () -> Unit,
    onQuotePlan: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Popular badge & Category + Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlanSpeedBadge(speedMbps = plan.downloadSpeedMbps)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = plan.techType.label,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (plan.isPopular) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Más Vendido",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plan Name & Price Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = plan.recommendedFor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${String.format("%.2f", plan.promoPriceMonthly)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "/mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Regular: $${String.format("%.2f", plan.standardPriceMonthly)}",
                        style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Promo ${plan.promoDurationMonths} meses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feature bullets
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                plan.features.take(3).forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions & Seller Commission
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommissionBadge(amount = plan.baseCommissionAmount)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clickable { onToggleCompare() }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isCompared,
                            onCheckedChange = { onToggleCompare() }
                        )
                        Text("Comparar", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onQuotePlan,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quote_plan_${plan.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cotizar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedRecommendationQuizDialog(
    onDismiss: () -> Unit,
    onSelectPlan: (InternetPlan) -> Unit,
    viewModel: NetTarifaViewModel
) {
    val plans by viewModel.filteredPlans.collectAsState()

    var numDevices by remember { mutableStateOf(4) }
    var uses4KStreaming by remember { mutableStateOf(true) }
    var usesOnlineGaming by remember { mutableStateOf(false) }
    var worksFromHome by remember { mutableStateOf(true) }

    // Recommendation logic
    val recommendedSpeed = when {
        usesOnlineGaming || numDevices > 8 -> 600
        numDevices in 5..8 || (uses4KStreaming && worksFromHome) -> 300
        else -> 100
    }

    val recommendedPlan = plans.find { it.downloadSpeedMbps >= recommendedSpeed } ?: plans.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asesor de Velocidad")
            }
        },
        text = {
            Column {
                Text(
                    text = "Responde para recomendar la velocidad óptima al cliente:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("¿Cuántos dispositivos se conectarán?", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = numDevices.toFloat(),
                    onValueChange = { numDevices = it.toInt() },
                    valueRange = 1f..15f,
                    steps = 13
                )
                Text(
                    "$numDevices dispositivos estimados",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { uses4KStreaming = !uses4KStreaming },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = uses4KStreaming, onCheckedChange = { uses4KStreaming = it })
                    Text("Streaming en 4K / Smart TVs", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { usesOnlineGaming = !usesOnlineGaming },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = usesOnlineGaming, onCheckedChange = { usesOnlineGaming = it })
                    Text("Juegos en línea / Consolas (PS5/Xbox)", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { worksFromHome = !worksFromHome },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = worksFromHome, onCheckedChange = { worksFromHome = it })
                    Text("Teletrabajo / Videollamadas HD", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Velocidad Recomendada: ${recommendedSpeed} Mbps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Plan sugerido: ${recommendedPlan.name} ($${recommendedPlan.promoPriceMonthly}/mes)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectPlan(recommendedPlan) }
            ) {
                Text("Usar este plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
