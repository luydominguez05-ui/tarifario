package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.InternetPlan
import com.example.ui.components.CommissionBadge
import com.example.ui.components.PlanSpeedBadge
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.NetTarifaViewModel

@Composable
fun CompareScreen(
    viewModel: NetTarifaViewModel,
    onQuotePlan: (InternetPlan) -> Unit,
    onBackToCatalog: () -> Unit
) {
    val comparedPlans by viewModel.comparedPlans.collectAsState()
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Comparador de Tarifas",
            subtitle = "Compara las especificaciones y ventajas de hasta 3 planes en paralelo.",
            icon = Icons.Default.Compare
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (comparedPlans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Compare,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No has seleccionado planes para comparar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ve al catálogo y marca la casilla 'Comparar' en los planes que desees.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackToCatalog) {
                        Text("Ir al Catálogo")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                comparedPlans.forEach { plan ->
                    ComparePlanColumn(
                        plan = plan,
                        onRemove = { viewModel.togglePlanComparison(plan) },
                        onSelectForQuote = {
                            viewModel.selectPlanForQuote(plan)
                            onQuotePlan(plan)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ComparePlanColumn(
    plan: InternetPlan,
    onRemove: () -> Unit,
    onSelectForQuote: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .width(260.dp)
            .testTag("compare_card_${plan.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlanSpeedBadge(speedMbps = plan.downloadSpeedMbps)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Quitar de comparación")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = plan.techType.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing comparison block
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Precio Promocional:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$${String.format("%.2f", plan.promoPriceMonthly)}/mes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Durante ${plan.promoDurationMonths} meses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Precio regular: $${String.format("%.2f", plan.standardPriceMonthly)}/mes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spec rows
            CompareSpecRow(label = "Costo Instalación", value = if (plan.installationFee == 0.0) "GRATIS" else "$${plan.installationFee}")
            CompareSpecRow(label = "Contrato Mínimo", value = "${plan.contractMonths} Meses")
            CompareSpecRow(label = "Router Mesh Incluido", value = if (plan.includesMeshRouter) "Sí (Mesh Wi-Fi 6)" else "Estándar Dual Band")
            CompareSpecRow(label = "Garantía Velocidad", value = "${plan.speedGuaranteePercent}%")

            Spacer(modifier = Modifier.height(8.dp))

            // Features list
            Text("Características:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            plan.features.forEach { feature ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = feature, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CommissionBadge(amount = plan.baseCommissionAmount)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSelectForQuote,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cotizar este Plan")
            }
        }
    }
}

@Composable
fun CompareSpecRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
