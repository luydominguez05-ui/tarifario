package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.QuoteStatus
import com.example.data.entity.SavedQuoteEntity
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.NetTarifaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuotesHistoryScreen(
    viewModel: NetTarifaViewModel
) {
    val context = LocalContext.current
    val allQuotes by viewModel.allSavedQuotes.collectAsState()
    val soldCount by viewModel.soldCount.collectAsState()
    val totalEarnedCommissions by viewModel.totalEarnedCommissions.collectAsState()
    val selectedFilter by viewModel.selectedHistoryFilter.collectAsState()

    val filteredQuotes = if (selectedFilter == null) {
        allQuotes
    } else {
        allQuotes.filter { it.status == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SectionHeader(
            title = "Mis Cotizaciones y Ventas",
            subtitle = "Historial de clientes cotizados, seguimiento y comisiones ganadas.",
            icon = Icons.Default.History
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Metrics Bar Card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag("history_metrics_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cotizaciones", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${allQuotes.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ventas Cerradas", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$soldCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06D6A0)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Comisión Acumulada", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$${String.format("%.2f", totalEarnedCommissions ?: 0.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF06D6A0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setHistoryFilter(null) },
                    label = { Text("Todas (${allQuotes.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_all_quotes")
                )
            }
            items(QuoteStatus.entries.toTypedArray()) { status ->
                val count = allQuotes.count { it.status == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { viewModel.setHistoryFilter(status) },
                    label = { Text("${status.label} ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_status_${status.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredQuotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay cotizaciones registradas en esta categoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredQuotes, key = { it.id }) { quote ->
                    SavedQuoteItemCard(
                        quote = quote,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateQuoteStatus(quote.id, newStatus)
                        },
                        onDelete = {
                            viewModel.deleteQuote(quote.id)
                        },
                        onShare = {
                            val shareText = """
                                🚀 Cotización para ${quote.clientName}:
                                Plan: ${quote.planName} (${quote.speedMbps} Mbps)
                                Pago mensual promo: $${String.format("%.2f", quote.finalMonthlyPricePromo)}/mes
                                Instalación: $${String.format("%.2f", quote.installationFee)}
                                Contacto asesor.
                            """.trimIndent()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Re-enviar propuesta"))
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SavedQuoteItemCard(
    quote: SavedQuoteEntity,
    onUpdateStatus: (QuoteStatus) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = remember(quote.createdAtTimestamp) { dateFormat.format(Date(quote.createdAtTimestamp)) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_quote_card_${quote.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Client Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tel: ${quote.clientPhone} | $dateString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = quote.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plan Summary Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = quote.planName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", quote.finalMonthlyPricePromo)}/mes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Servicios Extra: ${quote.selectedAddOnsSummary}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Dirección: ${quote.clientAddress}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Commission info for rep
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comisión Asesor: +$${String.format("%.2f", quote.estimatedCommission)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06D6A0)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = { showStatusDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("change_status_button_${quote.id}")
                    ) {
                        Text("Cambiar Estado", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Actualizar Estado de Venta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuoteStatus.entries.forEach { status ->
                        Surface(
                            onClick = {
                                onUpdateStatus(status)
                                showStatusDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (quote.status == status) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = status)
                                if (quote.status == status) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
