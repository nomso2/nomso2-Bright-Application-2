package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.service.GoogleMapsAgentService
import com.example.data.service.PlaceResult
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

/**
 * Launches real-time Google Maps Navigation Directions directly to the Transformer,
 * clearly pinpointing the Transformer ID as the destination.
 */
fun launchGoogleMapsDirections(context: Context, transformer: NigeriaTransformer) {
    val lat = transformer.latitude
    val lng = transformer.longitude
    val id = transformer.id
    val name = transformer.name
    val queryLabel = "$id - $name"
    val encoded = Uri.encode(queryLabel)

    // 1. Primary Google Maps Navigation Universal Intent with destination pinpoint & label
    val gmapsDirUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&destination_place_name=$encoded")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmapsDirUri).apply {
        setPackage("com.google.android.apps.maps")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encoded)&z=17")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}

/**
 * Drops a precise pinpoint marker in Google Maps at the Transformer's GPS coordinates,
 * labeled with the Transformer ID and Substation Name.
 */
fun launchGoogleMapsPinpoint(context: Context, transformer: NigeriaTransformer) {
    val lat = transformer.latitude
    val lng = transformer.longitude
    val id = transformer.id
    val name = transformer.name
    val queryLabel = "$id - $name"
    val encoded = Uri.encode(queryLabel)

    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encoded)&z=17")
    val gmapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(gmapsIntent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
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

    // Map tab search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<TransformerStatus?>(null) }

    // DisCo Care Router tab search and filter state
    var discoSearchQuery by remember { mutableStateOf("") }
    var selectedDiscoFilter by remember { mutableStateOf<String?>(null) }

    val allTransformers = NigeriaTransformerRegistry.ALL_TRANSFORMERS
    var selectedTransformer by remember {
        mutableStateOf<NigeriaTransformer?>(allTransformers.firstOrNull())
    }

    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val filteredTransformers = remember(searchQuery, selectedCityFilter, selectedStatusFilter) {
        allTransformers.filter { tr ->
            val matchesSearch = searchQuery.isBlank() ||
                    tr.id.contains(searchQuery, ignoreCase = true) ||
                    tr.name.contains(searchQuery, ignoreCase = true) ||
                    tr.street.contains(searchQuery, ignoreCase = true) ||
                    tr.city.contains(searchQuery, ignoreCase = true) ||
                    tr.discoCode.contains(searchQuery, ignoreCase = true)

            val matchesCity = selectedCityFilter == null ||
                    tr.city.equals(selectedCityFilter, ignoreCase = true) ||
                    tr.state.contains(selectedCityFilter!!, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null || tr.status == selectedStatusFilter

            matchesSearch && matchesCity && matchesStatus
        }
    }

    // Filtered DisCos for DisCo Care Router
    val discoResults = remember(discoSearchQuery, selectedDiscoFilter) {
        val query = discoSearchQuery.trim()
        val basePlaces = if (query.isBlank()) {
            GoogleMapsAgentService.searchPlaces("", userProfile)
        } else {
            GoogleMapsAgentService.searchPlaces(query, userProfile)
        }

        if (selectedDiscoFilter == null) {
            basePlaces
        } else {
            basePlaces.filter { it.discoAffiliation.equals(selectedDiscoFilter, ignoreCase = true) }
        }
    }

    // When selectedTransformer updates, center the real-time map smoothly
    LaunchedEffect(selectedTransformer) {
        selectedTransformer?.let { tr ->
            webViewRef?.evaluateJavascript(
                "if (window.focusTransformer) { window.focusTransformer('${tr.id}', ${tr.latitude}, ${tr.longitude}); }",
                null
            )
        }
    }

    // When map style mode changes, switch live tile layers
    LaunchedEffect(mapStyle) {
        val tileUrl = when (mapStyle) {
            MapStyleMode.SATELLITE -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            MapStyleMode.DARK_SCADA -> "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            MapStyleMode.ROADMAP -> "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        }
        webViewRef?.evaluateJavascript(
            "if (window.setTileLayer) { window.setTileLayer('$tileUrl'); }",
            null
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP HEADER: Title & SCADA Refresh
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NIGERIA GRID & DISCO NAVIGATOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pinpointing 33kV/11kV substations and official DisCo offices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

            Spacer(modifier = Modifier.height(6.dp))

            // TAB ROW: Google Map View vs DisCo Care Router
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Google Map View (${filteredTransformers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("DisCo Care Router", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TAB 0: REAL-TIME INTERACTIVE GOOGLE MAP PINPOINTING TRANSFORMERS
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar: Search by Transformer ID, Street, or City
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        val match = filteredTransformers.firstOrNull()
                        if (match != null) {
                            selectedTransformer = match
                        }
                    },
                    placeholder = { Text("Search by Transformer ID, Street, or City...", fontSize = 12.sp) },
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
                        .padding(horizontal = 16.dp, vertical = 2.dp)
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
                            onClick = {
                                selectedCityFilter = null
                                selectedTransformer = allTransformers.firstOrNull()
                                webViewRef?.evaluateJavascript("if (window.recenterMap) { window.recenterMap(9.0820, 8.6753, 6); }", null)
                            },
                            label = { Text("All Nigeria (${allTransformers.size})", fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_all_nigeria")
                        )
                    }
                    val cities = listOf("Benin", "Lagos", "Abuja", "Port Harcourt", "Ibadan", "Enugu", "Kano", "Kaduna", "Jos", "Calabar")
                    items(cities) { city ->
                        val isSelected = selectedCityFilter == city
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCityFilter = if (isSelected) null else city
                                val firstInCity = allTransformers.firstOrNull { it.city.equals(city, ignoreCase = true) }
                                if (firstInCity != null) {
                                    selectedTransformer = firstInCity
                                }
                            },
                            label = { Text(city, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = GoldPrimary
                            )
                        )
                    }
                }

                // REAL-TIME INTERACTIVE MAP CONTAINER
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    RealtimeGoogleMapView(
                        transformers = filteredTransformers,
                        selectedTransformer = selectedTransformer,
                        mapStyle = mapStyle,
                        onWebViewReady = { webViewRef = it },
                        onTransformerSelected = { tr -> selectedTransformer = tr },
                        onDirectionsClicked = { tr -> launchGoogleMapsDirections(context, tr) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Floating Map Style Toggles: Roadmap, Satellite, Dark SCADA
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
                                onClick = { webViewRef?.evaluateJavascript("if (window.zoomIn) { window.zoomIn(); }", null) },
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
                                onClick = { webViewRef?.evaluateJavascript("if (window.zoomOut) { window.zoomOut(); }", null) },
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
                                    selectedTransformer?.let { tr ->
                                        webViewRef?.evaluateJavascript(
                                            "if (window.focusTransformer) { window.focusTransformer('${tr.id}', ${tr.latitude}, ${tr.longitude}); }",
                                            null
                                        )
                                    } ?: run {
                                        webViewRef?.evaluateJavascript("if (window.recenterMap) { window.recenterMap(9.0820, 8.6753, 6); }", null)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Selected Transformer Detail Card
                selectedTransformer?.let { tr ->
                    TransformerInspectorCard(
                        transformer = tr,
                        onGoogleMapsDirections = { launchGoogleMapsDirections(context, tr) },
                        onGoogleMapsPinpoint = { launchGoogleMapsPinpoint(context, tr) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            // TAB 1: DISCO CARE ROUTER - Clean layout, zero empty space, highly visible open hours & official contacts
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Search Bar for DisCos
                OutlinedTextField(
                    value = discoSearchQuery,
                    onValueChange = { discoSearchQuery = it },
                    placeholder = { Text("Search Abuja (AEDC), Eko, Ikeja, Benin...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (discoSearchQuery.isNotBlank()) {
                            IconButton(onClick = { discoSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Filter Chips for all 11 DisCos
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDiscoFilter == null,
                            onClick = { selectedDiscoFilter = null },
                            label = { Text("All DisCos", fontSize = 11.sp) }
                        )
                    }
                    val discoChips = listOf(
                        "AEDC" to "Abuja (AEDC)",
                        "EKEDC" to "Eko (EKEDC)",
                        "IE" to "Ikeja (IE)",
                        "BEDC" to "Benin (BEDC)",
                        "IBEDC" to "Ibadan (IBEDC)",
                        "EEDC" to "Enugu (EEDC)",
                        "PHED" to "Port Harcourt (PHED)",
                        "KEDCO" to "Kano (KEDCO)",
                        "KAEDC" to "Kaduna (KAEDC)",
                        "JED" to "Jos (JED)",
                        "YEDC" to "Yola (YEDC)"
                    )
                    items(discoChips) { (code, label) ->
                        val isSelected = selectedDiscoFilter == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDiscoFilter = if (isSelected) null else code },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = GoldPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // List of DisCo Headquarters & Facilities - Starts IMMEDIATELY without empty space
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(discoResults) { place ->
                        DisCoFacilityCard(
                            place = place,
                            onCallDisCo = {
                                val clean = place.phoneNumber.replace(" ", "").replace("-", "")
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(dialIntent)
                            },
                            onOpenDirections = {
                                GoogleMapsAgentService.launchGoogleMapsNavigation(
                                    context = context,
                                    destinationLat = place.latitude,
                                    destinationLng = place.longitude,
                                    destinationName = place.name
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * DisCo Facility Card: Clean, tightly packed layout with ZERO empty space above,
 * displaying prominent, highly visible Open Hours, official citizen contact numbers, and Google Maps Navigation.
 */
@Composable
private fun DisCoFacilityCard(
    place: PlaceResult,
    onCallDisCo: () -> Unit,
    onOpenDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: DisCo Name & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = place.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                place.discoAffiliation?.let { aff ->
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = aff,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Address Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = place.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // HIGHLY VISIBLE OPEN HOURS BADGE
            Surface(
                color = EmeraldAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "WORKING & FAULT DESK HOURS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldAccent.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = place.operatingHours,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EmeraldAccent
                        )
                    }
                }
            }

            // DISCO OFFICIAL PHONE NUMBER FOR CITIZENS
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Official Citizen Contact Line",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = place.phoneNumber,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = onCallDisCo,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call DisCo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Google Maps Direction Button
            Button(
                onClick = onOpenDirections,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = GoldPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Google Maps Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Embedded Real-Time Map component leveraging high-performance Leaflet WebGL/Canvas rendering
 * with real tile sets (Roadmap, Satellite Imagery, Dark SCADA) pinpointing transformer IDs.
 */
@Composable
fun RealtimeGoogleMapView(
    transformers: List<NigeriaTransformer>,
    selectedTransformer: NigeriaTransformer?,
    mapStyle: MapStyleMode,
    onWebViewReady: (WebView) -> Unit,
    onTransformerSelected: (NigeriaTransformer) -> Unit,
    onDirectionsClicked: (NigeriaTransformer) -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(transformers) {
        buildLeafletMapHtml(transformers, selectedTransformer?.id, mapStyle)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        selectedTransformer?.let { tr ->
                            view?.evaluateJavascript(
                                "if (window.focusTransformer) { window.focusTransformer('${tr.id}', ${tr.latitude}, ${tr.longitude}); }",
                                null
                            )
                        }
                    }
                }
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onTransformerSelected(id: String) {
                            Handler(Looper.getMainLooper()).post {
                                val match = transformers.firstOrNull { it.id == id }
                                if (match != null) {
                                    onTransformerSelected(match)
                                }
                            }
                        }

                        @JavascriptInterface
                        fun onDirections(id: String) {
                            Handler(Looper.getMainLooper()).post {
                                val match = transformers.firstOrNull { it.id == id }
                                if (match != null) {
                                    onDirectionsClicked(match)
                                }
                            }
                        }
                    },
                    "AndroidBridge"
                )
                loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
                onWebViewReady(this)
            }
        },
        update = { webView ->
            onWebViewReady(webView)
        }
    )
}

private fun buildLeafletMapHtml(
    transformers: List<NigeriaTransformer>,
    selectedId: String?,
    mapStyle: MapStyleMode
): String {
    val sb = StringBuilder()
    transformers.forEach { tr ->
        val colorHex = when (tr.status) {
            TransformerStatus.HEALTHY -> "#22C55E"
            TransformerStatus.HEAVY_LOAD -> "#F59E0B"
            TransformerStatus.OVERLOADED, TransformerStatus.FAULT_TRIPPED -> "#EF4444"
            TransformerStatus.LOAD_SHEDDING -> "#8B5CF6"
        }
        val safeName = tr.name.replace("'", "\\'")
        val safeStreet = tr.street.replace("'", "\\'")
        val safeCity = tr.city.replace("'", "\\'")
        sb.append(
            """{ id: '${tr.id}', name: '$safeName', street: '$safeStreet', city: '$safeCity', lat: ${tr.latitude}, lng: ${tr.longitude}, capacity: ${tr.capacityKva}, load: ${tr.loadPercent}, color: '$colorHex' },"""
        )
    }

    val defaultLat = transformers.firstOrNull()?.latitude ?: 9.0820
    val defaultLng = transformers.firstOrNull()?.longitude ?: 8.6753
    val initialZoom = if (transformers.size == 1) 15 else 6

    val tileUrl = when (mapStyle) {
        MapStyleMode.SATELLITE -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
        MapStyleMode.DARK_SCADA -> "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        MapStyleMode.ROADMAP -> "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
          <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #0F172A; }
            .leaflet-popup-content-wrapper {
              background: #1E293B;
              color: #F8FAFC;
              border-radius: 12px;
              border: 1.5px solid #E5B869;
              box-shadow: 0 4px 16px rgba(0,0,0,0.6);
              padding: 4px;
            }
            .leaflet-popup-tip { background: #1E293B; }
            .popup-id { font-size: 13px; font-weight: 900; color: #E5B869; margin: 0 0 2px 0; }
            .popup-name { font-size: 11px; font-weight: 600; color: #F1F5F9; margin: 0 0 4px 0; }
            .popup-desc { font-size: 10px; color: #94A3B8; margin: 0 0 8px 0; }
            .popup-btn {
              background: #E5B869;
              color: #000000;
              font-size: 11px;
              font-weight: 800;
              border: none;
              border-radius: 6px;
              padding: 6px 12px;
              width: 100%;
              box-sizing: border-box;
              cursor: pointer;
              display: block;
              text-align: center;
              text-decoration: none;
            }
            .pin-container {
              display: flex;
              flex-direction: column;
              align-items: center;
            }
            .pin-badge {
              background: rgba(15, 23, 42, 0.92);
              color: #E5B869;
              border: 1px solid rgba(229,184,105,0.7);
              border-radius: 4px;
              font-size: 8.5px;
              font-weight: 800;
              padding: 1px 4px;
              white-space: nowrap;
              margin-bottom: 2px;
              box-shadow: 0 2px 4px rgba(0,0,0,0.6);
            }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
          <script>
            var rawData = [$sb];
            var map = L.map('map', { zoomControl: false, attributionControl: false }).setView([$defaultLat, $defaultLng], $initialZoom);

            var currentTile = L.tileLayer('$tileUrl', { maxZoom: 19 }).addTo(map);

            var markers = {};

            function createPinIcon(tr, isSelected) {
              var scale = isSelected ? 1.25 : 1.0;
              var svg = '<svg width="' + (22 * scale) + '" height="' + (30 * scale) + '" viewBox="0 0 24 32" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M12 0C5.37258 0 0 5.37258 0 12C0 19.5 12 32 12 32C12 32 24 19.5 24 12C24 5.37258 18.6274 0 12 0Z" fill="' + tr.color + '" stroke="#FFFFFF" stroke-width="1.5"/>' +
                '<circle cx="12" cy="12" r="5" fill="#FFFFFF"/>' +
                (isSelected ? '<circle cx="12" cy="12" r="2.5" fill="' + tr.color + '"/>' : '') +
                '</svg>';
              var html = '<div class="pin-container">' +
                '<div class="pin-badge">' + tr.id + '</div>' +
                svg +
                '</div>';
              return L.divIcon({
                className: 'custom-pin',
                html: html,
                iconSize: [60, 48],
                iconAnchor: [30, 46],
                popupAnchor: [0, -42]
              });
            }

            rawData.forEach(function(tr) {
              var isSel = (tr.id === '$selectedId');
              var icon = createPinIcon(tr, isSel);
              var marker = L.marker([tr.lat, tr.lng], { icon: icon }).addTo(map);

              var popupContent = '<div class="popup-id">' + tr.id + '</div>' +
                '<div class="popup-name">' + tr.name + '</div>' +
                '<div class="popup-desc">' + tr.street + ', ' + tr.city + '<br><b>Capacity:</b> ' + tr.capacity + ' kVA | <b>Load:</b> ' + tr.load + '%</div>' +
                '<button class="popup-btn" onclick="openDirections(\'' + tr.id + '\')">📍 Google Maps Directions</button>';

              marker.bindPopup(popupContent);

              marker.on('click', function() {
                if (window.AndroidBridge && window.AndroidBridge.onTransformerSelected) {
                  window.AndroidBridge.onTransformerSelected(tr.id);
                }
              });

              markers[tr.id] = marker;
            });

            function openDirections(id) {
              if (window.AndroidBridge && window.AndroidBridge.onDirections) {
                window.AndroidBridge.onDirections(id);
              }
            }

            window.focusTransformer = function(id, lat, lng) {
              if (map) {
                map.flyTo([lat, lng], 15, { animate: true, duration: 0.8 });
                if (markers[id]) {
                  markers[id].openPopup();
                }
              }
            };

            window.setTileLayer = function(url) {
              if (currentTile) map.removeLayer(currentTile);
              currentTile = L.tileLayer(url, { maxZoom: 19 }).addTo(map);
            };

            window.zoomIn = function() { if (map) map.zoomIn(); };
            window.zoomOut = function() { if (map) map.zoomOut(); };
            window.recenterMap = function(lat, lng, zoom) { if (map) map.flyTo([lat, lng], zoom); };
          </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Inspector Card for the selected Transformer Unit.
 * Clean, production-grade layout displaying real grid metrics, with zero demo reports,
 * and a prominent Google Maps Direction feature that takes you to Google Maps pinpointing the Transformer ID.
 */
@Composable
private fun TransformerInspectorCard(
    transformer: NigeriaTransformer,
    onGoogleMapsDirections: () -> Unit,
    onGoogleMapsPinpoint: () -> Unit,
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
            // Top Header: Transformer ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transformer.id,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
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
            }

            // Transformer Name
            Text(
                text = transformer.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Horizontally placed Capacity pill directly under the transformer name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${transformer.capacityKva} kVA",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Distribution Substation Unit",
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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

            // Real-Time Google Maps Actions (Directions & Pinpoint labeled with Transformer ID)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGoogleMapsDirections,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Google Maps Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Pinpointing ${transformer.id}", fontSize = 10.sp, fontWeight = FontWeight.Normal)
                    }
                }

                OutlinedButton(
                    onClick = onGoogleMapsPinpoint,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pinpoint Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
