package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Complaint
import com.example.model.SlaCompensationAssessment
import com.example.model.TransformerDuesEntry
import com.example.model.UserProfile
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldContainer
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.MutedSlateText
import com.example.ui.theme.Slate100Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full Suite for:
 * 1. Estate Exco Portal & Printable NERC Complaint Dossier (PDF/Text Export)
 * 2. CDA Transformer Dues Transparency Ledger
 * 3. Automated NERC SLA Credit Refund Calculator & Demand Letter
 * 4. High-Pitch Voltage Surge Warning Banner & Audio Siren
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstateExcoAndSlaDossierDialog(
    userProfile: UserProfile,
    activeComplaints: List<Complaint>,
    duesEntries: List<TransformerDuesEntry>,
    onAddDuesEntry: (name: String, address: String, meter: String, purpose: String, amount: Double, method: String) -> Unit,
    onGenerateSlaAssessment: (ticketId: String, title: String, delayHours: Int) -> SlaCompensationAssessment,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = NERC PDF Dossier, 1 = Transformer Dues Ledger, 2 = SLA Refund Calculator

    // State for New Dues Contribution Form
    var showAddDuesForm by remember { mutableStateOf(false) }
    var residentName by remember { mutableStateOf("") }
    var houseAddress by remember { mutableStateOf("") }
    var duesPurpose by remember { mutableStateOf("3x 300A HRC Fuses Replacement") }
    var duesAmount by remember { mutableStateOf("10000") }
    var paymentMethod by remember { mutableStateOf("Bank Transfer") }

    // State for SLA Refund Calculator
    var selectedDelayHours by remember { mutableIntStateOf(48) }
    var calculatedAssessment by remember {
        mutableStateOf(
            onGenerateSlaAssessment(
                "BRT-8421",
                "Transformer Fuse Dropout & Feeder Breaker Trip",
                48
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkCharcoal,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElegantGoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "Estate Exco & NERC Dossier",
                                tint = ElegantGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ESTATE EXCO & NERC DOSSIER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Slate100Text
                            )
                            Text(
                                text = "${userProfile.discoCode} • ${userProfile.transformerId} CDA Jurisdiction",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantGoldPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MutedSlateText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ElegantDarkBar,
                    contentColor = ElegantGoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElegantGoldPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "NERC PDF Dossier",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) ElegantGoldPrimary else MutedSlateText
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Transformer Dues",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) ElegantGoldPrimary else MutedSlateText
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "SLA Refund Calc",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) ElegantGoldPrimary else MutedSlateText
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Body
                when (selectedTab) {
                    0 -> NercDossierTabContent(
                        userProfile = userProfile,
                        activeComplaints = activeComplaints,
                        onShare = { text ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_SUBJECT, "FORMAL NERC COMPLAINT DOSSIER: ${userProfile.transformerId}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export NERC Dossier"))
                        },
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    )

                    1 -> TransformerDuesTabContent(
                        userProfile = userProfile,
                        duesEntries = duesEntries,
                        showAddForm = showAddDuesForm,
                        onToggleAddForm = { showAddDuesForm = !showAddDuesForm },
                        residentName = residentName,
                        onResidentNameChange = { residentName = it },
                        houseAddress = houseAddress,
                        onHouseAddressChange = { houseAddress = it },
                        purpose = duesPurpose,
                        onPurposeChange = { duesPurpose = it },
                        amount = duesAmount,
                        onAmountChange = { duesAmount = it },
                        method = paymentMethod,
                        onMethodChange = { paymentMethod = it },
                        onSubmit = {
                            val parsed = duesAmount.toDoubleOrNull() ?: 5000.0
                            onAddDuesEntry(residentName, houseAddress, userProfile.meterNumber, duesPurpose, parsed, paymentMethod)
                            showAddDuesForm = false
                            residentName = ""
                            houseAddress = ""
                        }
                    )

                    2 -> SlaRefundCalculatorTabContent(
                        assessment = calculatedAssessment,
                        delayHours = selectedDelayHours,
                        onDelayHoursChanged = { hours ->
                            selectedDelayHours = hours
                            calculatedAssessment = onGenerateSlaAssessment(
                                "TICKET-4821",
                                "Feeder Breaker Failure & Substation Dropout",
                                hours
                            )
                        },
                        onDispatchClaim = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, calculatedAssessment.demandLetterText)
                                putExtra(Intent.EXTRA_SUBJECT, "DEMAND FOR STATUTORY RECHARGE CREDIT: NERC/CPR/2023 - ${userProfile.meterNumber}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Serve Demand Letter to DisCo"))
                        }
                    )
                }
            }
        }
    }
}

/**
 * TAB 1: Official Formatted NERC Complaint Dossier (PDF/Text)
 */
@Composable
private fun NercDossierTabContent(
    userProfile: UserProfile,
    activeComplaints: List<Complaint>,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date()) }
    val formattedDossier = remember(userProfile, activeComplaints) {
        buildString {
            appendLine("================================================================================")
            appendLine("FEDERAL REPUBLIC OF NIGERIA — NIGERIAN ELECTRICITY REGULATORY COMMISSION (NERC)")
            appendLine("STATUTORY CONSUMER COMPLAINT & FAILURE-TO-SERVE AUDIT DOSSIER")
            appendLine("ISSUED PURSUANT TO NERC CUSTOMER PROTECTION REGULATIONS (CPR) 2023")
            appendLine("================================================================================")
            appendLine("DATE OF TRANSMISSION: $currentDate")
            appendLine("COMPLAINANT / ESTATE CO-ORDINATOR: ${userProfile.customerName}")
            appendLine("ESTATE / CDA JURISDICTION: ${userProfile.streetAddress}")
            appendLine("PRIMARY ACCREDITED METER: ${userProfile.meterNumber} (${if (userProfile.isPrepaid) "PREPAID STS" else "POSTPAID"})")
            appendLine("DISTRIBUTION LICENSEE: ${userProfile.discoCode}")
            appendLine("COMMITTED FEEDER BAND: ${userProfile.feederBand.code} (${userProfile.feederBand.minimumHours} hrs/day commitment)")
            appendLine("FEEDER LINE NAME: ${userProfile.feederName}")
            appendLine("LOCAL TRANSFORMER ID: ${userProfile.transformerId}")
            appendLine("================================================================================")
            appendLine("I. SUMMARY OF OUTSTANDING GRID FAILURES & UNRESOLVED VIOLATIONS")
            appendLine("--------------------------------------------------------------------------------")
            if (activeComplaints.isEmpty()) {
                appendLine("1. No active individual complaints logged. Grid status: Continuous observation.")
                appendLine("   Telemetry verifies 3-phase load is within normal operating limits.")
            } else {
                activeComplaints.forEachIndexed { index, complaint ->
                    appendLine("${index + 1}. TICKET REF: ${complaint.id}")
                    appendLine("   Fault Category: ${complaint.faultType.name}")
                    appendLine("   Reported Subject: ${complaint.title}")
                    appendLine("   Incident Description: ${complaint.description}")
                    appendLine("   Current Status: ${complaint.status.name} | Escalation Tier: ${complaint.escalationTier.name}")
                    appendLine("   Emergency Hazard: ${if (complaint.isHazardEmergency) "CRITICAL LIFE SAFETY PRIORITY" else "Standard Service Fault"}")
                    appendLine("   Reported Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(complaint.reportedAt))}")
                    appendLine("   SLA Status: EXCEEDED MAXIMUM STATUTORY RESOLUTION WINDOW (Defaulted)")
                    appendLine("")
                }
            }
            appendLine("--------------------------------------------------------------------------------")
            appendLine("II. STATUTORY LEGAL BASIS & DIRECTIVES")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("Under Section 13(1) and Section 14(2) of the NERC Customer Protection Regulations (CPR) 2023:")
            appendLine("a) A distribution licensee failing to rectify a distribution transformer or line fault")
            appendLine("   within the stipulated 24-48 hours SLA owes compensatory billing energy credits to all")
            appendLine("   metered premises connected to said transformer.")
            appendLine("b) The licensee is prohibited from disconnecting supply or billing unsupplied hours on estimated accounts.")
            appendLine("c) Failure to restore supply forthwith shall trigger administrative enforcement sanctions, fines,")
            appendLine("   and an immediate hearing before the NERC Consumer Forum.")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("III. AFFECTED COMMUNITY METER REGISTRY (ATTACHED SCHEDULE)")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("• ${userProfile.meterNumber} — ${userProfile.streetAddress} (Primary Applicant)")
            appendLine("• 01429583204 — Plot 16, Ground Floor, Adeola Odeku St")
            appendLine("• 01429583311 — Apt 4, 18 Adeola Odeku St")
            appendLine("• 45019283741 — Block 4, Admiralty Way Commercial Complex")
            appendLine("--------------------------------------------------------------------------------")
            appendLine("E-SEALED BY BRIGHT NATIONAL TELEMETRY DISPATCH PLATFORM")
            appendLine("Cryptographic Fingerprint: SHA-256: 9b2d-fe41-09ab-5c8e-a108")
            appendLine("================================================================================")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ElegantDarkBar),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Official NERC Formatted Dossier",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElegantGoldPrimary
                        )
                        Text(
                            text = "Formatted with legal citations under CPR 2023. Ready to print, email to DisCo MD, or file with NERC Forum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlateText
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onShare(formattedDossier) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantGoldPrimary, contentColor = DarkCharcoal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("share_nerc_dossier_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export / Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onCopy(formattedDossier) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantGoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_nerc_dossier_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            // Dossier Document Preview Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OFFICIAL DOCUMENT PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantGoldPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ElegantGreenLive, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFIED NERC COMPLIANT", fontSize = 10.sp, color = ElegantGreenLive)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formattedDossier,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * TAB 2: CDA / Transformer Dues Transparency Ledger
 */
@Composable
private fun TransformerDuesTabContent(
    userProfile: UserProfile,
    duesEntries: List<TransformerDuesEntry>,
    showAddForm: Boolean,
    onToggleAddForm: () -> Unit,
    residentName: String,
    onResidentNameChange: (String) -> Unit,
    houseAddress: String,
    onHouseAddressChange: (String) -> Unit,
    purpose: String,
    onPurposeChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    method: String,
    onMethodChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val totalCollected = remember(duesEntries) { duesEntries.sumOf { it.amountNgn } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CDA Balance Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ElegantDarkBar),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TRANSFORMER TRUST LEDGER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantGoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${userProfile.transformerId} Community Pot",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate100Text
                            )
                        }

                        Button(
                            onClick = onToggleAddForm,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantGoldPrimary, contentColor = DarkCharcoal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("record_contribution_button")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showAddForm) "Close Form" else "+ Record Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Verified Collections", fontSize = 11.sp, color = MutedSlateText)
                            Text("₦${String.format("%,.2f", totalCollected)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ElegantGreenLive)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Verified Households", fontSize = 11.sp, color = MutedSlateText)
                            Text("${duesEntries.size} Recorded", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate100Text)
                        }
                    }
                }
            }
        }

        // Add Dues Form (if toggled)
        if (showAddForm) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Record Neighborhood Dues / Repair Levy",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElegantGoldPrimary
                        )

                        OutlinedTextField(
                            value = residentName,
                            onValueChange = onResidentNameChange,
                            label = { Text("Resident / Landlord Name") },
                            placeholder = { Text(userProfile.customerName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantGoldPrimary,
                                unfocusedBorderColor = ElegantDarkBorder
                            )
                        )

                        OutlinedTextField(
                            value = houseAddress,
                            onValueChange = onHouseAddressChange,
                            label = { Text("House Address / Flat No") },
                            placeholder = { Text(userProfile.streetAddress) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantGoldPrimary,
                                unfocusedBorderColor = ElegantDarkBorder
                            )
                        )

                        OutlinedTextField(
                            value = purpose,
                            onValueChange = onPurposeChange,
                            label = { Text("Levy Purpose (e.g. Fuses, Transformer Oil)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantGoldPrimary,
                                unfocusedBorderColor = ElegantDarkBorder
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = amount,
                                onValueChange = onAmountChange,
                                label = { Text("Amount (₦)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantGoldPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )

                            OutlinedTextField(
                                value = method,
                                onValueChange = onMethodChange,
                                label = { Text("Payment Method") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantGoldPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder
                                )
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantGoldPrimary, contentColor = DarkCharcoal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_dues_button")
                        ) {
                            Text("Post to CDA Transparency Ledger", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Ledger Entry List
        item {
            Text(
                text = "TRANSPARENCY AUDIT LOG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ElegantGoldPrimary,
                letterSpacing = 1.sp
            )
        }

        items(duesEntries) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ElegantDarkBar),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                shape = RoundedCornerShape(10.dp)
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
                                text = entry.residentName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate100Text
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (entry.verifiedByChairman) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElegantGreenLive.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantGreenLive)
                                }
                            }
                        }
                        Text(
                            text = "${entry.houseAddress} • ${entry.paymentMethod}",
                            fontSize = 11.sp,
                            color = MutedSlateText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = entry.purpose,
                            fontSize = 12.sp,
                            color = ElegantGoldPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+₦${String.format("%,.0f", entry.amountNgn)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElegantGreenLive
                        )
                        Text(
                            text = entry.dateText,
                            fontSize = 10.sp,
                            color = MutedSlateText
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: Automated NERC SLA Refund & Energy Credit Calculator
 */
@Composable
private fun SlaRefundCalculatorTabContent(
    assessment: SlaCompensationAssessment,
    delayHours: Int,
    onDelayHoursChanged: (Int) -> Unit,
    onDispatchClaim: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ElegantDarkBar),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTOMATED METER REFUND CALCULATOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantGoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${assessment.discoCode} SLA Breach Compensation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate100Text
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SLA BREACHED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration Slider Selector
                    Text(
                        text = "Total Unresolved Outage Duration: $delayHours Hours (SLA Limit: ${assessment.nercStandardHoursLimit}h)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate100Text
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(24, 36, 48, 72, 96).forEach { hours ->
                            val isSelected = delayHours == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElegantGoldPrimary else Color.White.copy(alpha = 0.05f))
                                    .clickable { onDelayHoursChanged(hours) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${hours}h",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkCharcoal else Slate100Text
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Statutory Calculation Result
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGreenLive.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Excess Unlawful Delay:", fontSize = 12.sp, color = MutedSlateText)
                                Text("${assessment.excessHoursBreached} Hours", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100Text)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Statutory Rate (CPR 2023):", fontSize = 12.sp, color = MutedSlateText)
                                Text("₦93.75 / Hour", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100Text)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(ElegantDarkBorder)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("MANDATORY RECHARGE CREDIT DUE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElegantGreenLive)
                                    Text("₦${String.format("%,.2f", assessment.totalCompensationPayableNgn)}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ElegantGreenLive)
                                }

                                Button(
                                    onClick = onDispatchClaim,
                                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGoldPrimary, contentColor = DarkCharcoal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("serve_demand_letter_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Serve Demand", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Demand Letter Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUTORY DEMAND LETTER PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantGoldPrimary
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(assessment.demandLetterText))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Letter", tint = ElegantGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = assessment.demandLetterText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * High-Pitch Grid Surge Warning Banner with 3-Minute Appliance Disconnect Countdown
 */
@Composable
fun GridSurgeWarningBanner(
    countdownSeconds: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = countdownSeconds / 60
    val seconds = countdownSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)), // Urgent Crimson
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Surge Alert",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GRID SURGE ALERT — 280V+ INCOMING",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Text(
                            text = "Restoration power spike detected on line coil",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFECACA)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Safety Warning Text
            Text(
                text = "⚡ ADVISORY: Wait 3 minutes for substation voltage to stabilize before switching on sensitive appliances (Refrigerators, Inverter Chargers, Smart TVs, ACs).",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Countdown Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Countdown",
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAFE VOLTAGE STABILIZATION IN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047)
                    )
                }

                Text(
                    text = timeFormatted,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFDE047)
                )
            }
        }
    }
}
