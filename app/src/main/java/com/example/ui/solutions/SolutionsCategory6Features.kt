package com.example.ui.solutions

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

// ==========================================
// SOLUTION 26: SURGE RETURN WARNING
// ==========================================
@Composable
fun SurgeReturnWarningFeature(
    onPlaySiren: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFridgeUnplugged by remember { mutableStateOf(true) }
    var isTvUnplugged by remember { mutableStateOf(false) }
    var isAcUnplugged by remember { mutableStateOf(true) }
    var isConfirmedSafe by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "26. High-Voltage Surge Return Safety Countdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "When grid power restores after hours in darkness, an initial 280V+ spike often blows televisions, compressors, and inverter boards. Pre-warning alerts protect appliances.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                        Column {
                            Text("5-MINUTE PRE-RESTORATION WARNING", fontWeight = FontWeight.Black, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            Text("Lines energizing shortly. Unplug delicate electronics now.", fontSize = 10.sp)
                        }
                    }
                }

                Text("Appliance Safety Isolation Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFridgeUnplugged, onCheckedChange = { isFridgeUnplugged = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                    Text("Refrigerator / Deep Freezer Compressor Isolated", fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTvUnplugged, onCheckedChange = { isTvUnplugged = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                    Text("Smart TV & Home Theater System Unplugged", fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAcUnplugged, onCheckedChange = { isAcUnplugged = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                    Text("Inverter AC / Split Units Switched Off", fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlaySiren,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Surge Siren", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isConfirmedSafe = true
                            Toast.makeText(context, "Appliances confirmed isolated from voltage spikes!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isConfirmedSafe) "Confirmed Safe ✓" else "Appliances Safe", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 27: "GRID IS BACK" AUDIO SIREN
// ==========================================
@Composable
fun GridIsBackAudioSirenFeature(
    onPlaySiren: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fuelBurnLitersPerHour by remember { mutableStateOf(2.5f) }
    val petrolPricePerLiter = 1150
    val hourlyFuelCost = (fuelBurnLitersPerHour * petrolPricePerLiter).toInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "27. 'Grid is Back' Audio Siren & Fuel Saver",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Plays a loud audible tone the instant power returns to your transformer. Prevents wasting expensive petrol or diesel on running generators unnoticed.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Generator Consumption:", fontSize = 11.sp)
                    Text("${String.format("%.1f", fuelBurnLitersPerHour)} Liters / Hour", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
                }

                Slider(
                    value = fuelBurnLitersPerHour,
                    onValueChange = { fuelBurnLitersPerHour = it },
                    valueRange = 0.8f..8f,
                    colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                )

                Surface(
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fuel Money Saved When Turned Off:", fontSize = 11.sp, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                            Text("₦$hourlyFuelCost / Hour", fontWeight = FontWeight.Black, fontSize = 13.sp, color = EmeraldAccent)
                        }
                        Text("Every 15 minutes you turn off the gen earlier saves ₦${hourlyFuelCost / 4}.", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = {
                        onPlaySiren()
                        Toast.makeText(context, "Playing generator shut-off chime!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test 'Grid is Back' Audio Siren Chime", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 28: APPLIANCE LOAD BUDGETER
// ==========================================
@Composable
fun ApplianceLoadBudgeterFeature(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasFridge by remember { mutableStateOf(true) }      // 300W
    var hasAc by remember { mutableStateOf(false) }          // 1200W
    var hasFans by remember { mutableStateOf(true) }         // 210W
    var hasTv by remember { mutableStateOf(true) }           // 120W
    var hasLights by remember { mutableStateOf(true) }       // 60W

    val totalWatts = (if (hasFridge) 300 else 0) +
            (if (hasAc) 1200 else 0) +
            (if (hasFans) 210 else 0) +
            (if (hasTv) 120 else 0) +
            (if (hasLights) 60 else 0)

    val recommendedInverterKva = when {
        totalWatts > 1500 -> "3.5 kVA (48V)"
        totalWatts > 800 -> "2.0 kVA (24V)"
        else -> "1.0 kVA (12V)"
    }

    val batteryRuntimeHours = if (totalWatts > 0) String.format("%.1f", (2400f * 0.8f) / totalWatts) else "0.0"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "28. Appliance Load Budgeter & Inverter Sizing Tool",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Select your household appliances to calculate real-time running watts, required inverter capacity, and estimated battery backup duration.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Deep Freezer (300W)", fontSize = 11.sp)
                    Checkbox(checked = hasFridge, onCheckedChange = { hasFridge = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("1.5HP Inverter AC (1,200W)", fontSize = 11.sp)
                    Checkbox(checked = hasAc, onCheckedChange = { hasAc = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("3x Standing Fans (210W)", fontSize = 11.sp)
                    Checkbox(checked = hasFans, onCheckedChange = { hasFans = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Smart TV & Soundbar (120W)", fontSize = 11.sp)
                    Checkbox(checked = hasTv, onCheckedChange = { hasTv = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Continuous Load:", fontSize = 11.sp, color = Color.Gray)
                            Text("$totalWatts Watts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recommended Inverter Size:", fontSize = 11.sp, color = Color.Gray)
                            Text(recommendedInverterKva, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Est. 2x200Ah Battery Runtime:", fontSize = 11.sp, color = Color.Gray)
                            Text("$batteryRuntimeHours Hours", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 29: HYBRID ENERGY OPTIMIZER
// ==========================================
@Composable
fun HybridEnergyOptimizerFeature(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isScheduleApplied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "29. Hybrid Solar-Grid AI Energy Optimizer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Correlates local weather forecasts with historical feeder tripping patterns to advise when to charge batteries and when to pump water.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Today's Weather:", fontSize = 11.sp)
                    Text("33°C Sunny (85% Solar Irradiance)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Feeder Tripping Risk (Afternoon):", fontSize = 11.sp)
                    Text("72% Likelihood at 2:00 PM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }

                Surface(
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("AI ACTIONABLE STRATEGY:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = EmeraldAccent)
                        Text("1. Top up inverter batteries from the grid before 1:00 PM.", fontSize = 10.sp)
                        Text("2. Run water pumping machine on free solar between 11 AM - 1 PM.", fontSize = 10.sp)
                        Text("3. Switch heavy ACs to eco mode by 2 PM before grid cut.", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = {
                        isScheduleApplied = true
                        Toast.makeText(context, "Applied hybrid solar-grid optimization profile!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SolarPower, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isScheduleApplied) "Schedule Active on Inverter ✓" else "Apply Recommended Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 30: TARIFF FLASH NEWS
// ==========================================
@Composable
fun TariffFlashNewsFeature(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val newsItems = listOf(
        Pair("Band A Tariff Demystified", "Consumers on Band A paying ₦209/kWh have statutory right to 20hrs daily minimum or receive automatic band demotion to Band B."),
        Pair("DisCo Extortion is Illegal", "Under NERC Customer Protection Regulations 2023, DisCos are strictly prohibited from demanding residents contribute money for transformer replacement, oil, or poles."),
        Pair("Capped Billing Safeguard", "Unmetered customers cannot be billed above the NERC cap order formula even if DisCo claims commercial losses on feeder.")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "30. Jargon-Free NERC Regulatory News Bulletin",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Plain English, easy-to-understand explanations of your legal electricity rights and latest tariff orders from the Nigerian Electricity Regulatory Commission.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                newsItems.forEach { (title, summary) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GoldPrimary)
                                Icon(Icons.Default.Newspaper, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text(summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Shared NERC Rights Bulletin to WhatsApp Estate Group!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Rights Bulletin to Estate WhatsApp Group", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
