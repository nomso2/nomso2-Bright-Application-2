package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.GoogleMapsAgentService
import com.example.model.NigeriaTransformer
import com.example.model.NigeriaTransformerRegistry
import com.example.model.OutageGridNode
import com.example.model.TransformerStatus
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

enum class MapStyleMode {
    ROADMAP,
    SATELLITE,
    DARK_SCADA
}

@Composable
fun LiveMapScreen(
    userProfile: UserProfile,
    outageNodes: List<OutageGridNode>,
    onRefreshMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var mapStyle by remember { mutableStateOf(MapStyleMode.ROADMAP) }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<TransformerStatus?>(null) }
    var selectedTransformer by remember {
        mutableStateOf<NigeriaTransformer?>(NigeriaTransformerRegistry.ALL_TRANSFORMERS.firstOrNull())
    }

    // Google Maps interactive pan and zoom
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Google Maps Agent Query State
    var agentQuery by remember { mutableStateOf("") }
    var agentResponse by remember {
        mutableStateOf(
            GoogleMapsAgentService.processAgentQuery("nearest customer care", userProfile)
        )
    }

    val context = LocalContext.current
    val allTransformers = NigeriaTransformerRegistry.ALL_TRANSFORMERS

    val filteredTransformers = remember(searchQuery, selectedCityFilter, selectedStatusFilter) {
        allTransformers.filter { tr ->
            val matchesSearch = searchQuery.isBlank() ||
                    tr.id.contains(searchQuery, ignoreCase = true) ||
                    tr.name.contains(searchQuery, ignoreCase = true) ||
                    tr.street.contains(searchQuery, ignoreCase = true) ||
                    tr.city.contains(searchQuery, ignoreCase = true) ||
                    tr.discoCode.contains(searchQuery, ignoreCase = true)

            val matchesCity = selectedCityFilter == null || tr.city.equals(selectedCityFilter, ignoreCase = true) || tr.state.contains(selectedCityFilter!!, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == null || tr.status == selectedStatusFilter

            matchesSearch && matchesCity && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NIGERIA TRANSFORMER MAP",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pinpointing all 33kV/11kV substations & distribution units in Nigeria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRefreshMap,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_outage_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Live Map",
                            tint = GoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DUAL TABS: Map Explorer vs Google Maps Assistant
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldPrimary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Google Map View (${filteredTransformers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("DisCo Care Router", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TAB 0: GOOGLE MAP PINPOINTING TRANSFORMERS ACROSS NIGERIA
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Transformer ID, Street, or City (e.g. Victoria Island, Maitama)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("transformer_map_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Quick City / Region Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCityFilter == null,
                            onClick = { selectedCityFilter = null },
                            label = { Text("All Nigeria (${allTransformers.size})", fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_all_nigeria")
                        )
                    }
                    val cities = listOf("Lagos", "Abuja", "Port Harcourt", "Ibadan", "Enugu", "Kano", "Kaduna", "Benin", "Jos", "Calabar")
                    items(cities) { city ->
                        val isSelected = selectedCityFilter == city
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCityFilter = if (isSelected) null else city },
                            label = { Text(city, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = GoldPrimary
                            )
                        )
                    }
                }

                // Interactive Map Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    // Canvas Map Render
                    GoogleStyleMapCanvas(
                        transformers = filteredTransformers,
                        selectedTransformer = selectedTransformer,
                        mapStyle = mapStyle,
                        zoomScale = zoomScale,
                        panOffsetX = panOffsetX,
                        panOffsetY = panOffsetY,
                        onPanZoomChange = { zoom, panX, panY ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                            panOffsetX += panX
                            panOffsetY += panY
                        },
                        onTransformerTapped = { tr ->
                            selectedTransformer = tr
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Floating Map Style Toggles
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xDD000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = { mapStyle = MapStyleMode.ROADMAP },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Roadmap",
                                        tint = if (mapStyle == MapStyleMode.ROADMAP) GoldPrimary else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { mapStyle = MapStyleMode.SATELLITE },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "Satellite",
                                        tint = if (mapStyle == MapStyleMode.SATELLITE) GoldPrimary else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { mapStyle = MapStyleMode.DARK_SCADA },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "SCADA",
                                        tint = if (mapStyle == MapStyleMode.DARK_SCADA) GoldPrimary else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Zoom and Recenter Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xEE1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xEE1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xEE1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                        ) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Selected Transformer Bottom Drawer
                selectedTransformer?.let { tr ->
                    TransformerInspectorCard(
                        transformer = tr,
                        onDirectionsClicked = {
                            val uri = Uri.parse("geo:${tr.latitude},${tr.longitude}?q=${tr.latitude},${tr.longitude}(${tr.name})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${tr.latitude},${tr.longitude}"))
                                context.startActivity(webIntent)
                            }
                        },
                        onReportFaultClicked = {
                            Toast.makeText(context, "Fault report initiated for ${tr.id}!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            // TAB 1: GOOGLE MAPS AGENT & ROUTING
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "DisCo Care & Substation Navigator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Find nearest DisCo district offices, pay points, and injection stations with live directions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = agentQuery,
                                onValueChange = { agentQuery = it },
                                placeholder = { Text("Ask where to find nearest fault office or substation...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    agentResponse = GoogleMapsAgentService.processAgentQuery(agentQuery, userProfile)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Find Closest Official Facilities")
                            }
                        }
                    }
                }

                items(agentResponse.places) { place ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(place.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(
                                    color = EmeraldAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${place.distanceKm} km away",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(place.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hours: ${place.operatingHours}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}(${place.name})")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(mapIntent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Google Maps Directions", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleStyleMapCanvas(
    transformers: List<NigeriaTransformer>,
    selectedTransformer: NigeriaTransformer?,
    mapStyle: MapStyleMode,
    zoomScale: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    onPanZoomChange: (Float, Float, Float) -> Unit,
    onTransformerTapped: (NigeriaTransformer) -> Unit,
    modifier: Modifier = Modifier
) {
    // Nigeria Geo bounds: Lat ~4.0 to 14.0, Long ~2.5 to 14.5
    val minLat = 4.0
    val maxLat = 14.0
    val minLng = 2.5
    val maxLng = 14.5

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onPanZoomChange(zoom, pan.x, pan.y)
                }
            }
            .pointerInput(transformers, zoomScale, panOffsetX, panOffsetY) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height

                    var closest: NigeriaTransformer? = null
                    var minDist = Float.MAX_VALUE

                    transformers.forEach { tr ->
                        val normX = ((tr.longitude - minLng) / (maxLng - minLng)).toFloat()
                        val normY = (1.0f - ((tr.latitude - minLat) / (maxLat - minLat)).toFloat())

                        val centerX = width / 2f
                        val centerY = height / 2f

                        val px = centerX + (normX * width - centerX) * zoomScale + panOffsetX
                        val py = centerY + (normY * height - centerY) * zoomScale + panOffsetY

                        val dist = kotlin.math.hypot(tapOffset.x - px, tapOffset.y - py)
                        if (dist < 44.dp.toPx() && dist < minDist) {
                            minDist = dist
                            closest = tr
                        }
                    }

                    closest?.let { onTransformerTapped(it) }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background Map Tone depending on Style
            val bgColor = when (mapStyle) {
                MapStyleMode.ROADMAP -> Color(0xFFF1F5F9) // Clean Google Map Road Tone
                MapStyleMode.SATELLITE -> Color(0xFF0F172A) // Deep satellite satellite terrain
                MapStyleMode.DARK_SCADA -> Color(0xFF090D16) // Obsidian Dark SCADA
            }
            drawRect(bgColor)

            // Grid lines (lat / long simulated Google Map coordinates)
            val gridColor = when (mapStyle) {
                MapStyleMode.ROADMAP -> Color(0xFFE2E8F0)
                MapStyleMode.SATELLITE -> Color(0xFF1E293B)
                MapStyleMode.DARK_SCADA -> Color(0xFF162032)
            }

            val stepX = width / 6
            for (i in 0..6) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * stepX, 0f),
                    end = Offset(i * stepX, height),
                    strokeWidth = 1f
                )
            }
            val stepY = height / 6
            for (i in 0..6) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, i * stepY),
                    end = Offset(width, i * stepY),
                    strokeWidth = 1f
                )
            }

            // Draw Transformers as Pinpoint Markers across Nigeria
            transformers.forEach { tr ->
                val normX = ((tr.longitude - minLng) / (maxLng - minLng)).toFloat()
                val normY = (1.0f - ((tr.latitude - minLat) / (maxLat - minLat)).toFloat())

                val centerX = width / 2f
                val centerY = height / 2f

                val px = centerX + (normX * width - centerX) * zoomScale + panOffsetX
                val py = centerY + (normY * height - centerY) * zoomScale + panOffsetY

                val isSelected = tr.id == selectedTransformer?.id
                val pinColor = Color(tr.status.colorHex)

                // Selection Halo / Pulsing circle
                if (isSelected) {
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.35f),
                        radius = 24.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = GoldPrimary,
                        radius = 16.dp.toPx(),
                        center = Offset(px, py),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }

                // Base pin marker
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 10.dp.toPx() else 7.dp.toPx(),
                    center = Offset(px, py)
                )

                // Inner core
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
private fun TransformerInspectorCard(
    transformer: NigeriaTransformer,
    onDirectionsClicked: () -> Unit,
    onReportFaultClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = transformer.id,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = GoldPrimary
                        )
                        Surface(
                            color = Color(transformer.status.colorHex).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = transformer.status.label,
                                color = Color(transformer.status.colorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = transformer.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${transformer.capacityKva} kVA",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "${transformer.street}, ${transformer.city}, ${transformer.state} (${transformer.discoCode})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Overload & Voltages Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Load: ${transformer.loadPercent}% (${transformer.connectedHouseholds} Households)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Phase A: ${transformer.phaseAVolts}V | B: ${transformer.phaseBVolts}V | C: ${transformer.phaseCVolts}V",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            LinearProgressIndicator(
                progress = { (transformer.loadPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(transformer.status.colorHex),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Recurrent Failure or Missing Parts Notice
            if (transformer.failureCountMonth >= 2 || transformer.missingPartNotice != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (transformer.failureCountMonth >= 2)
                                "⚠️ Recurrent Failure: ${transformer.failureCountMonth} breakdowns this month (NERC Review Flagged)"
                            else "Missing: ${transformer.missingPartNotice}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDirectionsClicked,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", fontSize = 11.sp)
                }

                Button(
                    onClick = onReportFaultClicked,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report Fault on Unit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
