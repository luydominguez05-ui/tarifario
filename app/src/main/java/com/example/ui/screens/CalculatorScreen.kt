package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AddOn
import com.example.data.model.Promotion
import com.example.ui.components.CommissionBadge
import com.example.ui.components.PlanSpeedBadge
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.NetTarifaViewModel

@Composable
fun CalculatorScreen(
    viewModel: NetTarifaViewModel,
    onQuoteSavedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val selectedAddOns by viewModel.selectedAddOns.collectAsState()
    val selectedPromotions by viewModel.selectedPromotions.collectAsState()

    val clientName by viewModel.clientName.collectAsState()
    val clientPhone by viewModel.clientPhone.collectAsState()
    val clientAddress by viewModel.clientAddress.collectAsState()
    val clientNotes by viewModel.clientNotes.collectAsState()

    val calcResult = viewModel.calculateQuoteSummary()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Hero Banner Graphic
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.sales_hero_banner_1786501259218),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Cotizador de Tarifas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Calcula planes, extras, descuentos y tu comisión",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Plan Principal Selected
        SectionHeader(title = "1. Plan Base Seleccionado", icon = Icons.Default.Speed)
        
        if (selectedPlan != null) {
            val plan = selectedPlan!!
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        PlanSpeedBadge(speedMbps = plan.downloadSpeedMbps)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Precio base promo: $${String.format("%.2f", plan.promoPriceMonthly)}/mes (${plan.promoDurationMonths} meses)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Precio regular: $${String.format("%.2f", plan.standardPriceMonthly)}/mes | Instalación: ${if (plan.installationFee == 0.0) "GRATIS" else "$${plan.installationFee}"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Servicios Adicionales (Add-Ons)
        SectionHeader(title = "2. Servicios Adicionales (Opcional)", icon = Icons.Default.AddCircle)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.availableAddOns.forEach { addOn ->
                val isSelected = selectedAddOns.contains(addOn)
                Surface(
                    onClick = { viewModel.toggleAddOn(addOn) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("addon_item_${addOn.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = addOn.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = addOn.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+$${String.format("%.2f", addOn.monthlyPrice)}/m",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Promociones & Descuentos
        SectionHeader(title = "3. Descuentos Aplicables", icon = Icons.Default.Discount)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.availablePromotions.forEach { promo ->
                val isSelected = selectedPromotions.contains(promo)
                Surface(
                    onClick = { viewModel.togglePromotion(promo) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("promo_item_${promo.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = promo.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = promo.discountDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (promo.isPercentage) "-${(promo.percentageValue * 100).toInt()}%" else "-$${String.format("%.2f", promo.monthlyDiscountAmount)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Client Info Form
        SectionHeader(title = "4. Datos del Cliente / Prospecto", icon = Icons.Default.Person)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = clientName,
                onValueChange = { viewModel.clientName.value = it },
                label = { Text("Nombre Completo del Cliente") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { viewModel.clientPhone.value = it },
                    label = { Text("Teléfono de Contacto") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.weight(1f).testTag("client_phone_input"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = clientAddress,
                onValueChange = { viewModel.clientAddress.value = it },
                label = { Text("Dirección / Sector de Instalación") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("client_address_input"),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Total Price & Commission Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag("quote_summary_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RESUMEN DE COTIZACIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Plan Base (Promo):", style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format("%.2f", calcResult.plan?.promoPriceMonthly ?: 0.0)}/mes", fontWeight = FontWeight.Bold)
                }

                if (calcResult.addOnsTotal > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Servicios Extra:", style = MaterialTheme.typography.bodyMedium)
                        Text("+$${String.format("%.2f", calcResult.addOnsTotal)}/mes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (calcResult.promotionsDiscount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Descuentos Promo:", style = MaterialTheme.typography.bodyMedium)
                        Text("-$${String.format("%.2f", calcResult.promotionsDiscount)}/mes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Final Monthly Promo Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PAGO MENSUAL PROMO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Primeros ${calcResult.plan?.promoDurationMonths ?: 6} meses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", calcResult.finalMonthlyPromo)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Regular monthly price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Precio regular posterior:", style = MaterialTheme.typography.bodySmall)
                    Text("$${String.format("%.2f", calcResult.finalMonthlyRegular)}/mes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estimated Seller Commission Highlight
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF06D6A0).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06D6A0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TU COMISIÓN ESTIMADA (ASESOR)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF06D6A0)
                            )
                            Text(
                                text = "Pago por venta de esta línea",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "+$${String.format("%.2f", calcResult.estimatedCommission)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF06D6A0)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions: Save Quote & Share via WhatsApp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.saveCurrentQuote { quoteId ->
                        Toast.makeText(context, "Cotización guardada exitosamente (#$quoteId)", Toast.LENGTH_SHORT).show()
                        onQuoteSavedSuccess()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("save_quote_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar")
            }

            Button(
                onClick = {
                    val message = viewModel.generateWhatsAppMessage(calcResult)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Enviar propuesta a cliente por...")
                    context.startActivity(shareIntent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366) // WhatsApp Green
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("share_whatsapp_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartir", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
