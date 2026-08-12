package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.NetTarifaViewModel

@Composable
fun CommissionsScreen(
    viewModel: NetTarifaViewModel
) {
    val scrollState = rememberScrollState()
    val soldCount by viewModel.soldCount.collectAsState()
    val totalEarnedCommissions by viewModel.totalEarnedCommissions.collectAsState()
    val salesGoal by viewModel.salesGoal.collectAsState()

    val goal = salesGoal ?: return
    val progressFraction = (soldCount.toFloat() / goal.monthlyTargetSalesCount).coerceIn(0f, 1f)

    var simulatedSalesCount by remember { mutableStateOf(soldCount.coerceAtLeast(10)) }

    // Tier bonuses calculation
    val currentTier = when {
        soldCount >= goal.tier2CountLimit -> "Nivel 3 Pro (+$${goal.tier3BonusPerSale}/venta)"
        soldCount >= goal.tier1CountLimit -> "Nivel 2 Medio (+$${goal.tier2BonusPerSale}/venta)"
        else -> "Nivel 1 Inicial ($${goal.tier1BonusPerSale}/venta)"
    }

    // Simulated Earnings Calculation
    val simulatedTierBonusPerSale = when {
        simulatedSalesCount >= goal.tier2CountLimit -> goal.tier3BonusPerSale
        simulatedSalesCount >= goal.tier1CountLimit -> goal.tier2BonusPerSale
        else -> goal.tier1BonusPerSale
    }
    val simulatedCommissions = simulatedSalesCount * simulatedTierBonusPerSale
    val simulatedTotalIncome = goal.baseMonthlySalary + simulatedCommissions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SectionHeader(
            title = "Control de Comisiones y Metas",
            subtitle = "Escalas de incentivos, bono por volumen de ventas y proyección de ingresos.",
            icon = Icons.Default.Payments
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Toward Goal Card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth().testTag("goal_progress_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "META DEL MES (${goal.monthYear})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$soldCount de ${goal.monthlyTargetSalesCount} Ventas Realizadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF06D6A0),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Escala actual: $currentTier",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06D6A0)
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% Completado",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tiers Info Card
        SectionHeader(title = "Tabla de Niveles de Comisión", icon = Icons.Default.Stars)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CommissionTierRow(
                tierTitle = "Nivel 1 (1 a 10 Ventas)",
                payoutText = "$${goal.tier1BonusPerSale} por cada instalación completada.",
                isActive = soldCount in 1..10
            )
            CommissionTierRow(
                tierTitle = "Nivel 2 (11 a 20 Ventas)",
                payoutText = "$${goal.tier2BonusPerSale} por cada instalación completada.",
                isActive = soldCount in 11..20
            )
            CommissionTierRow(
                tierTitle = "Nivel 3 (21+ Ventas) - ¡Nivel VIP!",
                payoutText = "$${goal.tier3BonusPerSale} por venta + Bono de Aceleración.",
                isActive = soldCount >= 21
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Earnings Projection Simulator Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag("commission_simulator_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulador de Ingresos Mensuales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ajusta la cantidad de ventas estimadas para calcular tu pago total:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ventas proyectadas: $simulatedSalesCount contratos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = simulatedSalesCount.toFloat(),
                    onValueChange = { simulatedSalesCount = it.toInt() },
                    valueRange = 1f..40f,
                    steps = 38
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sueldo Base Asesor:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format("%.2f", goal.baseMonthlySalary)}", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Comisiones Estimadas ($simulatedSalesCount x $${simulatedTierBonusPerSale}):", style = MaterialTheme.typography.bodyMedium)
                    Text("+$${String.format("%.2f", simulatedCommissions)}", fontWeight = FontWeight.Bold, color = Color(0xFF06D6A0))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAGO TOTAL ESTIMADO",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%.2f", simulatedTotalIncome)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CommissionTierRow(tierTitle: String, payoutText: String, isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFF06D6A0).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFF06D6A0) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tierTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color(0xFF06D6A0) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = payoutText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF06D6A0)
                ) {
                    Text(
                        text = "ACTIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
