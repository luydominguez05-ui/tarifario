package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.sp
import com.example.data.megacable.ClientType
import com.example.data.megacable.GasDeliverables
import com.example.data.megacable.MegacableBranch
import com.example.data.megacable.MegacableEngine
import com.example.data.megacable.PackageFamily
import com.example.data.megacable.PaymentMethod
import com.example.data.megacable.PromotionType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MegacableScreen(
    onOpenGasExporter: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- STATE ---
    var selectedBranch by remember { mutableStateOf(MegacableEngine.sampleBranches.first { it.number == 401 }) }
    var branchSearchQuery by remember { mutableStateOf("401 - MIGUEL HIDALGO") }
    var showBranchDropdown by remember { mutableStateOf(false) }

    var colonyInput by remember { mutableStateOf("SAN ANGEL RESTRENO") }

    var clientType by remember { mutableStateOf(ClientType.RESIDENCIAL) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.DOMICILIADO) }
    var packageFamily by remember { mutableStateOf(PackageFamily.DOBLEPACK) }
    var selectedSpeed by remember { mutableIntStateOf(300) }
    var promotionType by remember { mutableStateOf(PromotionType.NORMAL) }

    var tvExtraCount by remember { mutableIntStateOf(3) }

    // Test case initial streaming selection: HBO Anuncios, Netflix Estándar, Disney Premium
    val selectedStreamingIds = remember {
        mutableStateListOf("hbo_anuncios", "netflix_std", "disney_prem")
    }

    // Dynamic Calculation
    val quoteResult = remember(
        selectedBranch, clientType, paymentMethod, packageFamily,
        selectedSpeed, promotionType, tvExtraCount,
        selectedStreamingIds.toList(), colonyInput
    ) {
        MegacableEngine.calculateQuote(
            branch = selectedBranch,
            clientType = clientType,
            paymentMethod = paymentMethod,
            packageFamily = packageFamily,
            speedMbps = selectedSpeed,
            promotionType = promotionType,
            tvExtraCount = tvExtraCount,
            selectedStreamingIds = selectedStreamingIds,
            colony = colonyInput
        )
    }

    val filteredBranches = remember(branchSearchQuery) {
        val q = branchSearchQuery.lowercase().trim()
        if (q.isBlank()) MegacableEngine.sampleBranches
        else MegacableEngine.sampleBranches.filter {
            it.number.toString().contains(q) || it.name.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Title Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TARIFARIO MEGACABLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cotizador Comercial para Agentes de Ventas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onOpenGasExporter,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_gas_code_view")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GAS Code", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. SUCURSAL
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SUCURSAL",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = branchSearchQuery,
                        onValueChange = {
                            branchSearchQuery = it
                            showBranchDropdown = true
                        },
                        label = { Text("Buscar sucursal...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { showBranchDropdown = !showBranchDropdown }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_branch"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = showBranchDropdown && filteredBranches.isNotEmpty(),
                        onDismissRequest = { showBranchDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredBranches.forEach { branch ->
                            DropdownMenuItem(
                                text = { Text("${branch.number} - ${branch.name}") },
                                onClick = {
                                    selectedBranch = branch
                                    branchSearchQuery = "${branch.number} - ${branch.name}"
                                    showBranchDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selected Branch Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBranch.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = selectedBranch.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sucursal ${selectedBranch.number}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Instalación: ${selectedBranch.promise}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. DISPONIBILIDAD DE COLONIA
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DISPONIBILIDAD DE COLONIA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = colonyInput,
                    onValueChange = { colonyInput = it },
                    label = { Text("Buscar colonia...") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_colony_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (colonyInput.isNotBlank()) {
                    if (quoteResult.isColonyBlocked) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚠️ Esta colonia presenta restricción para la sucursal seleccionada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓ Colonia disponible.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. COTIZACIÓN CONFIG
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "COTIZACIÓN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cliente
                Text("Tipo de Cliente", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClientType.entries.forEach { type ->
                        FilterChip(
                            selected = clientType == type,
                            onClick = { clientType = type },
                            label = { Text(type.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_client_${type.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Forma de pago
                Text("Forma de Pago", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { pm ->
                        FilterChip(
                            selected = paymentMethod == pm,
                            onClick = { paymentMethod = pm },
                            label = { Text(pm.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_payment_${pm.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Paquete
                Text("Paquete", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PackageFamily.entries.forEach { pkg ->
                        FilterChip(
                            selected = packageFamily == pkg,
                            onClick = { packageFamily = pkg },
                            label = { Text(pkg.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_pkg_${pkg.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Velocidad
                Text("Velocidad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(100, 200, 300, 500, 1000).forEach { speed ->
                        FilterChip(
                            selected = selectedSpeed == speed,
                            onClick = { selectedSpeed = speed },
                            label = { Text("$speed Mbps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_speed_$speed")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Promoción
                Text("Promoción", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PromotionType.entries.forEach { promo ->
                        FilterChip(
                            selected = promotionType == promo,
                            onClick = { promotionType = promo },
                            label = { Text(promo.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_promo_${promo.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TV Adicionales
                Text("TV Adicionales", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..4).forEach { count ->
                        Surface(
                            onClick = { tvExtraCount = count },
                            shape = RoundedCornerShape(8.dp),
                            color = if (tvExtraCount == count) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).testTag("tv_btn_$count")
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tvExtraCount == count) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (tvExtraCount > 0) {
                    Column(modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
                        quoteResult.tvExtraPricesBreakdown.forEachIndexed { index, price ->
                            Text(
                                text = "TV adicional ${index + 1}:  $${price.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Streaming Services
                Text("Servicios de Streaming", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MegacableEngine.streamingOptions.forEach { streaming ->
                        val isChecked = selectedStreamingIds.contains(streaming.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedStreamingIds.remove(streaming.id)
                                    else selectedStreamingIds.add(streaming.id)
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) selectedStreamingIds.add(streaming.id)
                                        else selectedStreamingIds.remove(streaming.id)
                                    }
                                )
                                Text(
                                    text = "${streaming.service} - ${streaming.plan}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = "$${streaming.price.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. RESUMEN Y TOTAL
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().testTag("summary_card_megacable")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RESUMEN DE COTIZACIÓN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Precio base:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${quoteResult.basePrice.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TV adicionales:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${quoteResult.tvExtraTotal.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Streaming:", style = MaterialTheme.typography.bodyMedium)
                    Text("$${quoteResult.streamingTotal.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "$${quoteResult.totalPrice.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CÓDIGOS DE CONTRATACIÓN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CÓDIGOS DE CONTRATACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Paquete: ${quoteResult.packageCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TV Extra: ${quoteResult.tvExtraCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Streaming:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (quoteResult.streamingCodes.isNotEmpty()) {
                            quoteResult.streamingCodes.forEach { code ->
                                Text(
                                    text = " • $code",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(" • N/A", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val text = MegacableEngine.generateClipboardSummary(quoteResult)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Cotizacion Megacable", text))
                            Toast.makeText(context, "Cotización copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("btn_copy_quote")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPIAR COTIZACIÓN", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            colonyInput = ""
                            clientType = ClientType.RESIDENCIAL
                            paymentMethod = PaymentMethod.DOMICILIADO
                            packageFamily = PackageFamily.DOBLEPACK
                            selectedSpeed = 300
                            promotionType = PromotionType.NORMAL
                            tvExtraCount = 0
                            selectedStreamingIds.clear()
                            Toast.makeText(context, "Formulario reiniciado", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("btn_new_quote")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NUEVA COTIZACIÓN", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
