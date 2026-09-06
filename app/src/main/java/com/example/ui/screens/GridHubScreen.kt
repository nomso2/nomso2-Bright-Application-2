package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DisCo
import com.example.model.FeederBand
import com.example.model.MaintenanceAlert
import com.example.model.UserProfile
import com.example.ui.AppLanguage
import com.example.ui.components.Solutions30ComprehensiveHub

enum class HubSection {
    SOLUTIONS_30,
    ALL,
    TARIFFS,
    BILLING_METHODOLOGIES,
    NERC_POLICIES,
    DISCO_DIRECTORY
}

data class PolicyGuide(
    val title: String,
    val regulationCode: String,
    val summary: String,
    val keyPoints: List<String>,
    val officialUrl: String
)

@Composable
fun GridHubScreen(
    userProfile: UserProfile,
    maintenanceAlerts: List<MaintenanceAlert>,
    currentLanguage: AppLanguage,
    isLowDataMode: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleLowData: () -> Unit,
    onSubmitBillingDispute: (disputeType: String, amount: Double, month: String, desc: String) -> Unit,
    isBatSignalMode: Boolean = false,
    onToggleBatSignalMode: (Boolean) -> Unit = {},
    onOpenRedDangerSOS: () -> Unit = {},
    onOpenForum: () -> Unit = {},
    onPlaySirenAlarm: () -> Unit = {},
    onOpenEstateExco: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(HubSection.SOLUTIONS_30) }
    var selectedPolicyGuide by remember { mutableStateOf<PolicyGuide?>(null) }

    // Tariff Calculator State
    var calcAmountText by remember { mutableStateOf("10000") }
    var calcBand by remember { mutableStateOf(userProfile.feederBand) }

    // Dispute Form State
    var showDisputeForm by remember { mutableStateOf(false) }
    var disputeType by remember { mutableStateOf("Estimated Bill Above NERC Capped Order") }
    var disputedAmountText by remember { mutableStateOf("25000") }
    var billingMonth by remember { mutableStateOf("September 2026") }
    var disputeDescription by remember { mutableStateOf("") }

    val context = LocalContext.current

    val officialPolicies = remember {
        listOf(
            PolicyGuide(
                title = "NERC Customer Protection Regulations (CPR) 2023",
                regulationCode = "NERC/REG/2023/CPR-01",
                summary = "The primary statutory framework establishing consumer rights, billing standards, disconnection limits, and DisCo obligations across Nigeria.",
                keyPoints = listOf(
                    "Strict 48-Hour Written Notice before any disconnection for non-payment.",
                    "Disconnection during an active, unresolved billing dispute is strictly ILLEGAL.",
                    "DisCos must resolve all customer complaints within 15 calendar days.",
                    "Right to compensation for household appliances damaged by DisCo power surges or voltage fluctuations.",
                    "Free access to NERC Consumer Forum Offices for arbitration if DisCo refuses settlement."
                ),
                officialUrl = "https://nerc.gov.ng/index.php/regulations/regulations-order"
            ),
            PolicyGuide(
                title = "Prohibition on Consumer Infrastructure Financing",
                regulationCode = "NERC Investment in Network Regulations",
                summary = "Legal protections barring DisCos from demanding that landlords or communities purchase transformers, electric poles, cables, or meters.",
                keyPoints = listOf(
                    "Consumers CANNOT be mandated or coerced to buy electrical equipment.",
                    "The DisCo holds sole statutory responsibility for funding network expansion & transformer replacements.",
                    "If a community willingly co-finances equipment to expedite restoration, the DisCo MUST enter a formal refund agreement crediting their electricity accounts."
                ),
                officialUrl = "https://nerc.gov.ng/index.php/home/operators/consumer-protection"
            ),
            PolicyGuide(
                title = "NERC Capped Estimated Billing Order",
                regulationCode = "Order NERC/REG/2020/004",
                summary = "Strict ceiling limits on monthly charges for unmetered customers to eradicate extortionate estimated billing.",
                keyPoints = listOf(
                    "DisCos are legally prohibited from billing unmetered customers more than the capped kWh approved for their transformer cluster.",
                    "Charges must reflect the actual consumption of metered neighbors on the identical feeder.",
                    "Unmetered customers experiencing blackouts are entitled to pro-rata reductions in their capped bill."
                ),
                officialUrl = "https://nerc.gov.ng/index.php/regulations/orders"
            ),
            PolicyGuide(
                title = "Meter Asset Provider (MAP) Regulations",
                regulationCode = "NERC/REG/MAP-REV-2024",
                summary = "Rules governing prepaid meter procurement, installation timelines, and token vending transparency.",
                keyPoints = listOf(
                    "DisCos must install prepaid meters within 10 working days of verified MAP payment.",
                    "Customers who pay upfront for meters are entitled to full reimbursement via electricity energy tokens over time.",
                    "Prepaid meters must support STS token entry via standard Customer Interface Unit (CIU) keypads."
                ),
                officialUrl = "https://nerc.gov.ng/index.php/regulations/regulations-order"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "INFORMATION & CONSUMER RESOURCE HUB",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tariffs, billing methodologies (prepaid vs estimated) & NERC policy guidelines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Estate Exco Portal & NERC Dossier CTA Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenEstateExco)
                    .testTag("hub_estate_exco_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAB308))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ESTATE EXCO PORTAL & NERC DOSSIER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFEAB308)
                            )
                            Text(
                                text = "PDF Export • Dues Ledger • ₦ SLA Refund Calculator",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = onOpenEstateExco,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HubSection.entries) { section ->
                    val isSelected = selectedSection == section
                    val label = when (section) {
                        HubSection.SOLUTIONS_30 -> "30 Grid Solutions"
                        HubSection.ALL -> "All Resources"
                        HubSection.TARIFFS -> "Electricity Tariffs"
                        HubSection.BILLING_METHODOLOGIES -> "Billing (Prepaid vs Estimated)"
                        HubSection.NERC_POLICIES -> "NERC Consumer Policies"
                        HubSection.DISCO_DIRECTORY -> "DisCo Directory"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        label = { Text(label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // ==========================================
        // 30 POWER SECTOR SOLUTIONS COMPREHENSIVE HUB
        // ==========================================
        if (selectedSection == HubSection.SOLUTIONS_30 || selectedSection == HubSection.ALL) {
            item {
                Solutions30ComprehensiveHub(
                    userProfile = userProfile,
                    isBatSignalMode = isBatSignalMode,
                    onToggleBatSignalMode = onToggleBatSignalMode,
                    onOpenRedDangerSOS = onOpenRedDangerSOS,
                    onOpenForum = onOpenForum,
                    onPlaySirenAlarm = onPlaySirenAlarm
                )
            }
        }

        // ==========================================
        // PILLAR 1: CURRENT ELECTRICITY TARIFFS
        // ==========================================
        if (selectedSection == HubSection.ALL || selectedSection == HubSection.TARIFFS) {
            item {
                Text(
                    text = "1. CURRENT ELECTRICITY TARIFFS (MYTO 2026)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Tariffs Breakdown Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tariff_schedule_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriceCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "NERC Approved Multi-Year Tariff Schedule",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Under the current NERC MYTO tariff order, electricity rates are tied directly to guaranteed daily supply hours across five service bands:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Bands Table
                        TariffBandRow(
                            bandName = "Band A",
                            hoursText = "Min 20 hrs/day",
                            rateText = "₦206.80 / kWh",
                            description = "High-priority express feeders. Zero subsidy.",
                            badgeColor = Color(0xFF10B981)
                        )
                        TariffBandRow(
                            bandName = "Band B",
                            hoursText = "16 – 20 hrs/day",
                            rateText = "₦63.00 / kWh",
                            description = "Subsidized standard commercial/residential.",
                            badgeColor = Color(0xFF38BDF8)
                        )
                        TariffBandRow(
                            bandName = "Band C",
                            hoursText = "12 – 16 hrs/day",
                            rateText = "₦50.00 / kWh",
                            description = "Subsidized urban residential corridors.",
                            badgeColor = Color(0xFFF59E0B)
                        )
                        TariffBandRow(
                            bandName = "Band D",
                            hoursText = "8 – 12 hrs/day",
                            rateText = "₦33.00 / kWh",
                            description = "Subsidized sub-urban feeder lines.",
                            badgeColor = Color(0xFF94A3B8)
                        )
                        TariffBandRow(
                            bandName = "Band E",
                            hoursText = "4 – 8 hrs/day",
                            rateText = "₦32.00 / kWh",
                            description = "Rural/industrial frontier supply.",
                            badgeColor = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Interactive Tariff & kWh Unit Calculator
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tariff_calculator_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Interactive Tariff & Token Unit Calculator",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Calculate exact electricity units (kWh) receivable for any payment amount after 7.5% VAT.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Band selection row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FeederBand.entries.forEach { band ->
                                val isSel = calcBand == band
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSel) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { calcBand = band }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = band.code,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = calcAmountText,
                            onValueChange = { calcAmountText = it },
                            label = { Text("Recharge Amount (NGN ₦)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val enteredAmt = calcAmountText.toDoubleOrNull() ?: 10000.0
                        val vat = enteredAmt * 0.075
                        val energyNet = (enteredAmt - vat).coerceAtLeast(0.0)
                        val rate = when (calcBand) {
                            FeederBand.BAND_A -> 206.80
                            FeederBand.BAND_B -> 63.00
                            FeederBand.BAND_C -> 50.00
                            FeederBand.BAND_D -> 33.00
                            FeederBand.BAND_E -> 32.00
                        }
                        val kwhUnits = energyNet / rate

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gross Payment:", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    Text("₦%,.2f".format(enteredAmt), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Statutory 7.5% VAT:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                    Text("₦%,.2f".format(vat), style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Energy Credit:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                    Text("₦%,.2f".format(energyNet), style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Units Receivable (${calcBand.code}):",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "%.2f kWh".format(kwhUnits),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // PILLAR 2: BILLING METHODOLOGIES (PREPAID VS ESTIMATED)
        // ==========================================
        if (selectedSection == HubSection.ALL || selectedSection == HubSection.BILLING_METHODOLOGIES) {
            item {
                Text(
                    text = "2. BILLING METHODOLOGIES: PREPAID VS ESTIMATED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Comparative Card: Prepaid vs Estimated
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("billing_methodology_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Prepaid Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricMeter,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Prepaid Metering (STS Class)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "• Pay-as-you-use: Purchase 20-digit STS tokens via app, USSD, or banking channels.\n• Absolute transparency: Users only consume what they purchase; zero estimation risk.\n• Real-time CIU Display: View current voltage, instantaneous load (kW), and remaining credit kWh.\n• Automatic cutoff when units expire; restored instantly upon token entry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Estimated Billing Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Estimated Billing Methodology & Capping Order",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "• Applied strictly to unmetered customers awaiting MAP meter rollout.\n• Statutory NERC Capping (Order 2020/004): DisCos are legally prohibited from billing unmetered consumers above the approved energy ceiling for their transformer cluster.\n• Pro-rata outage deductions: Bills must be reduced when transformer/feeder faults cause protracted outages.\n• Illegal Estimation: Any estimated bill exceeding the NERC cap can be contested immediately without threat of disconnection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Maximum Demand (MD) vs Non-MD
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "MD (Maximum Demand) vs Non-MD Customers",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "• Non-MD: Residential and small retail premises with consumption under 45 kVA. Billed purely on active kWh energy.\n• MD (Maximum Demand): Factories, plazas, and large estates with 45+ kVA capacity. Billed on dual components: capacity kVA demand charge plus energy kWh consumed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Estimated Billing Dispute Tool Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Have an Outrageous Estimated Bill?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Button(
                                onClick = { showDisputeForm = !showDisputeForm },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (showDisputeForm) "Close" else "Lodge Dispute", fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = "Under NERC rules, you have the right to lodge a formal dispute against any bill that exceeds your capped quota before making payment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (showDisputeForm) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = disputedAmountText,
                                onValueChange = { disputedAmountText = it },
                                label = { Text("Disputed Excess Amount (₦)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = billingMonth,
                                onValueChange = { billingMonth = it },
                                label = { Text("Billing Month") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = disputeDescription,
                                onValueChange = { disputeDescription = it },
                                label = { Text("Dispute Reason") },
                                placeholder = { Text("e.g. Capped at 150 kWh but billed 800 kWh with 14 days blackout") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    val amt = disputedAmountText.toDoubleOrNull() ?: 15000.0
                                    onSubmitBillingDispute(
                                        disputeType,
                                        amt,
                                        billingMonth,
                                        disputeDescription.ifBlank { "Unverified tariff surge beyond NERC capped order." }
                                    )
                                    showDisputeForm = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("TRANSMIT DISPUTE AUDIT TO NERC FORUM", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // PILLAR 3: NERC CONSUMER PROTECTION POLICIES & GUIDELINES
        // ==========================================
        if (selectedSection == HubSection.ALL || selectedSection == HubSection.NERC_POLICIES) {
            item {
                Text(
                    text = "3. NERC CONSUMER PROTECTION POLICIES & STATUTES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(officialPolicies, key = { it.regulationCode }) { policy ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_item_${policy.regulationCode.replace("/", "_")}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = policy.regulationCode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policy.officialUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        selectedPolicyGuide = policy
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open Official Document",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = policy.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = policy.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            policy.keyPoints.forEach { point ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = { selectedPolicyGuide = policy },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Full Regulatory Framework & Rights", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // DISCO DIRECTORY & SCORECARDS
        // ==========================================
        if (selectedSection == HubSection.ALL || selectedSection == HubSection.DISCO_DIRECTORY) {
            item {
                Text(
                    text = "ALL 11 NIGERIAN DISCOS DIRECTORY & NERC RATINGS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(DisCo.entries.toList(), key = { it.code }) { disco ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = disco.code,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = disco.fullName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "HQ: ${disco.headOffice} • Coverage: ${disco.statesCovered}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NERC Rating",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${disco.customerRating}/5.0",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (disco.customerRating >= 4.0) Color(0xFF10B981) else MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Policy Detail Modal Dialog
    selectedPolicyGuide?.let { guide ->
        Dialog(onDismissRequest = { selectedPolicyGuide = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = guide.regulationCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedPolicyGuide = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = guide.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "ENFORCEABLE STATUTORY PROVISIONS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    guide.keyPoints.forEach { point ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.officialUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // fallback
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Official NERC Document Online")
                    }
                }
            }
        }
    }
}

@Composable
fun TariffBandRow(
    bandName: String,
    hoursText: String,
    rateText: String,
    description: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bandName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            fontSize = 10.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = hoursText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = rateText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}
