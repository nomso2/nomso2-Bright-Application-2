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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.service.GoogleMapsAgentService
import com.example.data.service.PlaceResult
import com.example.model.NigeriaTransformer
import com.example.model.OutageGridNode
import com.example.data.outage.OutagePin
import com.example.data.outage.StateCentroids
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.model.UserProfile

enum class MapStyleMode {
    ROADMAP,
    SATELLITE,
    DARK_SCADA
}

/**
 * Launches real-time Google Maps Navigation Directions directly to the Transformer,
 * clearly pinpointing the Transformer ID as the destination.
 */
fun launchGoogleMapsDirections(context: Context, transformer: NigeriaTransformer) =
    launchGoogleMapsDirections(context, transformer.latitude, transformer.longitude, "${transformer.id} - ${transformer.name}")

/** Opens Google Maps directions to [lat],[lng], labelled with [queryLabel]. */
fun launchGoogleMapsDirections(context: Context, lat: Double, lng: Double, queryLabel: String) {
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
fun launchGoogleMapsPinpoint(context: Context, transformer: NigeriaTransformer) =
    launchGoogleMapsPinpoint(context, transformer.latitude, transformer.longitude, "${transformer.id} - ${transformer.name}")

/** Drops a labelled pin in Google Maps at [lat],[lng]. */
fun launchGoogleMapsPinpoint(context: Context, lat: Double, lng: Double, queryLabel: String) {
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
    modifier: Modifier = Modifier,
    /** Real pins only: the user's own located complaints + last-48h community reports. */
    outagePins: List<OutagePin> = emptyList(),
    /** When community reports were last fetched (epoch ms), or null if never. */
    communityFetchedAt: Long? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var mapStyle by remember { mutableStateOf(MapStyleMode.ROADMAP) }

    // Map tab search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf<String?>(null) }

    // DisCo Care Router tab search and filter state
    var discoSearchQuery by remember { mutableStateOf("") }
    var selectedDiscoFilter by remember { mutableStateOf<String?>(null) }

    var selectedPinId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val filteredPins = remember(outagePins, searchQuery, selectedCityFilter) {
        outagePins.filter { pin ->
            val matchesSearch = searchQuery.isBlank() ||
                    pin.title.contains(searchQuery, ignoreCase = true) ||
                    pin.place.contains(searchQuery, ignoreCase = true) ||
                    pin.state.contains(searchQuery, ignoreCase = true) ||
                    pin.discoCode.contains(searchQuery, ignoreCase = true) ||
                    pin.sourceLabel.contains(searchQuery, ignoreCase = true)

            val cityState = selectedCityFilter?.let { MAP_CITY_STATES[it] ?: it }
            val matchesCity = cityState == null || pin.state.equals(cityState, ignoreCase = true)

            matchesSearch && matchesCity
        }
    }
    val selectedPin = filteredPins.firstOrNull { it.id == selectedPinId }

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

    // When the selected pin changes, center the map on it
    LaunchedEffect(selectedPinId) {
        selectedPin?.let { pin ->
            webViewRef?.evaluateJavascript(
                "if (window.focusPin) { window.focusPin('${pin.id}', ${pin.latitude}, ${pin.longitude}, ${if (pin.isApproximate) 9 else 14}); }",
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
        // TOP HEADER: Title & Refresh
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NIGERIA GRID & DISCO NAVIGATOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Outages reported by you and other Bright users, plus official DisCo offices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onRefreshMap,
                    modifier = Modifier
                        .testTag("refresh_outage_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Live Map",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TAB ROW: Google Map View vs DisCo Care Router
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
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
                            Text("Google Map View (${filteredPins.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("DisCo Care Router", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TAB 0: OUTAGE MAP - real reports only (never seeded or simulated pins)
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar: search the reported outages by area, state or DisCo
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                    },
                    placeholder = { Text("Search outages by area, state or DisCo...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                                selectedPinId = null
                                webViewRef?.evaluateJavascript("if (window.recenterMap) { window.recenterMap(9.0820, 8.6753, 6); }", null)
                            },
                            label = { Text("All Nigeria (${outagePins.size})", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                                selectedPinId = null
                                val cap = if (isSelected) null else StateCentroids.capitalOf(MAP_CITY_STATES[city] ?: city)
                                val js = if (cap != null) "window.recenterMap(${cap.second}, ${cap.third}, 9)" else "window.recenterMap(9.0820, 8.6753, 6)"
                                webViewRef?.evaluateJavascript("if (window.recenterMap) { $js; }", null)
                            },
                            label = { Text(city, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Where the pins come from, and how fresh the community reports are
                OutageSourceStrip(
                    communityFetchedAt = communityFetchedAt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                )

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
                        pins = filteredPins,
                        selectedPin = selectedPin,
                        mapStyle = mapStyle,
                        onWebViewReady = { webViewRef = it },
                        onPinSelected = { pin -> selectedPinId = pin.id },
                        onDirectionsClicked = { pin -> launchGoogleMapsDirections(context, pin.latitude, pin.longitude, pin.title) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Honest empty state: no reports means no pins (never invented ones)
                    if (filteredPins.isEmpty()) {
                        OutageMapEmptyState(
                            filtered = outagePins.isNotEmpty(),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

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
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Roadmap",
                                        tint = if (mapStyle == MapStyleMode.ROADMAP) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { mapStyle = MapStyleMode.SATELLITE },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "Satellite",
                                        tint = if (mapStyle == MapStyleMode.SATELLITE) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { mapStyle = MapStyleMode.DARK_SCADA },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Grid operator map style",
                                        tint = if (mapStyle == MapStyleMode.DARK_SCADA) MaterialTheme.colorScheme.primary else Color.LightGray,
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
                                modifier = Modifier
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
                                modifier = Modifier
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xEE1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedPin?.let { pin ->
                                        webViewRef?.evaluateJavascript(
                                            "if (window.focusPin) { window.focusPin('${pin.id}', ${pin.latitude}, ${pin.longitude}, ${if (pin.isApproximate) 9 else 14}); }",
                                            null
                                        )
                                    } ?: run {
                                        webViewRef?.evaluateJavascript("if (window.recenterMap) { window.recenterMap(9.0820, 8.6753, 6); }", null)
                                    }
                                },
                                modifier = Modifier
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Selected outage pin detail card
                selectedPin?.let { pin ->
                    OutagePinInspectorCard(
                        pin = pin,
                        onGoogleMapsDirections = { launchGoogleMapsDirections(context, pin.latitude, pin.longitude, pin.title) },
                        onGoogleMapsPinpoint = { launchGoogleMapsPinpoint(context, pin.latitude, pin.longitude, pin.title) },
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                            label = { Text("All DisCos", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
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
                            label = { Text(label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                place.discoAffiliation?.let { aff ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = aff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
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
                    tint = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
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
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "WORKING & FAULT DESK HOURS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = place.operatingHours,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
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
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Official Citizen Contact Line",
                                fontSize = 12.sp,
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call DisCo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Google Maps Direction Button
            Button(
                onClick = onOpenDirections,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Google Maps Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


/** City chips on the map filter by the state the city is in. */
private val MAP_CITY_STATES: Map<String, String> = mapOf(
    "Benin" to "Edo",
    "Lagos" to "Lagos",
    "Abuja" to "FCT",
    "Port Harcourt" to "Rivers",
    "Ibadan" to "Oyo",
    "Enugu" to "Enugu",
    "Kano" to "Kano",
    "Kaduna" to "Kaduna",
    "Jos" to "Plateau",
    "Calabar" to "Cross River"
)

private fun formatMapTime(ms: Long): String =
    SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(ms))

private fun timeAgo(ms: Long, now: Long = System.currentTimeMillis()): String {
    val mins = ((now - ms) / 60_000L).coerceAtLeast(0)
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 48 * 60 -> "${mins / 60} hr ago"
        else -> formatMapTime(ms)
    }
}

/** One line saying where the pins come from and how fresh the community reports are. */
@Composable
private fun OutageSourceStrip(communityFetchedAt: Long?, modifier: Modifier = Modifier) {
    val text = if (communityFetchedAt == null) {
        "Your own reports with GPS. Community reports appear once Bright can reach its server."
    } else {
        "Your reports with GPS + Bright users' reports from the last 48 hours. Updated ${formatMapTime(communityFetchedAt)}."
    }
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Shown over the map when there is nothing real to pin. */
@Composable
private fun OutageMapEmptyState(filtered: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.testTag("outage_map_empty_state"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = if (filtered) "No outages reported here yet" else "No outages reported near you yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (filtered) {
                    "Nothing matches this search or area. Clear the filter to see all reports."
                } else {
                    "Pins appear when you report a fault with your location, or when other Bright users report one in the last 48 hours."
                },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Embedded map (Leaflet in a WebView, real OpenStreetMap / Esri / Carto tiles) showing real
 * outage pins only.
 */
@Composable
fun RealtimeGoogleMapView(
    pins: List<OutagePin>,
    selectedPin: OutagePin?,
    mapStyle: MapStyleMode,
    onWebViewReady: (WebView) -> Unit,
    onPinSelected: (OutagePin) -> Unit,
    onDirectionsClicked: (OutagePin) -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(pins) {
        buildLeafletMapHtml(pins, selectedPin?.id, mapStyle)
    }
    // The JS bridge is created once; read the latest pins and callbacks through these.
    val currentPins by rememberUpdatedState(pins)
    val currentOnSelected by rememberUpdatedState(onPinSelected)
    val currentOnDirections by rememberUpdatedState(onDirectionsClicked)

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
                webViewClient = WebViewClient()
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onPinSelected(id: String) {
                            Handler(Looper.getMainLooper()).post {
                                currentPins.firstOrNull { it.id == id }?.let { currentOnSelected(it) }
                            }
                        }

                        @JavascriptInterface
                        fun onDirections(id: String) {
                            Handler(Looper.getMainLooper()).post {
                                currentPins.firstOrNull { it.id == id }?.let { currentOnDirections(it) }
                            }
                        }
                    },
                    "AndroidBridge"
                )
                tag = htmlContent.hashCode()
                loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
                onWebViewReady(this)
            }
        },
        update = { webView ->
            // Pins changed (new report, community refresh): reload the map with the new set.
            if (webView.tag != htmlContent.hashCode()) {
                webView.tag = htmlContent.hashCode()
                webView.loadDataWithBaseURL("https://maps.google.com", htmlContent, "text/html", "UTF-8", null)
            }
            onWebViewReady(webView)
        }
    )
}

private fun jsSafe(s: String): String =
    s.replace("\\", "\\\\").replace("'", "\\'").replace("<", "&lt;").replace(">", "&gt;").replace("\n", " ")

private fun buildLeafletMapHtml(
    pins: List<OutagePin>,
    selectedId: String?,
    mapStyle: MapStyleMode
): String {
    val sb = StringBuilder()
    pins.forEach { p ->
        val desc = buildString {
            append(p.statusLabel)
            append(" · ")
            append(p.place)
            append("<br>Latest: ")
            append(timeAgo(p.latestAt))
            if (p.discoCode.isNotBlank()) append(" · ").append(p.discoCode)
        }
        sb.append(
            """{ id: '${jsSafe(p.id)}', badge: '${jsSafe(p.badge)}', title: '${jsSafe(p.title)}', desc: '${jsSafe(desc).replace("&lt;br&gt;", "<br>")}', lat: ${p.latitude}, lng: ${p.longitude}, color: '${p.colorHex}' },"""
        )
    }

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
            .popup-id { font-size: 15px; font-weight: 900; color: #E5B869; margin: 0 0 4px 0; }
            .popup-desc { font-size: 13px; color: #CBD5E1; margin: 0 0 8px 0; line-height: 1.35; }
            .popup-btn {
              background: #E5B869;
              color: #000000;
              font-size: 13px;
              font-weight: 800;
              border: none;
              border-radius: 6px;
              padding: 8px 12px;
              width: 100%;
              box-sizing: border-box;
              cursor: pointer;
              display: block;
              text-align: center;
              text-decoration: none;
            }
            .pin-container { display: flex; flex-direction: column; align-items: center; }
            .pin-badge {
              background: rgba(15, 23, 42, 0.92);
              color: #E5B869;
              border: 1px solid rgba(229,184,105,0.7);
              border-radius: 4px;
              font-size: 11px;
              font-weight: 800;
              padding: 1px 5px;
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
            var map = L.map('map', { zoomControl: false, attributionControl: false }).setView([9.0820, 8.6753], 6);

            var currentTile = L.tileLayer('$tileUrl', { maxZoom: 19 }).addTo(map);

            var markers = {};

            function createPinIcon(p, isSelected) {
              var scale = isSelected ? 1.25 : 1.0;
              var svg = '<svg width="' + (22 * scale) + '" height="' + (30 * scale) + '" viewBox="0 0 24 32" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M12 0C5.37258 0 0 5.37258 0 12C0 19.5 12 32 12 32C12 32 24 19.5 24 12C24 5.37258 18.6274 0 12 0Z" fill="' + p.color + '" stroke="#FFFFFF" stroke-width="1.5"/>' +
                '<circle cx="12" cy="12" r="5" fill="#FFFFFF"/>' +
                (isSelected ? '<circle cx="12" cy="12" r="2.5" fill="' + p.color + '"/>' : '') +
                '</svg>';
              var html = '<div class="pin-container">' +
                '<div class="pin-badge">' + p.badge + '</div>' +
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

            rawData.forEach(function(p) {
              var isSel = (p.id === '${jsSafe(selectedId ?: "")}');
              var marker = L.marker([p.lat, p.lng], { icon: createPinIcon(p, isSel) }).addTo(map);

              var popupContent = '<div class="popup-id">' + p.title + '</div>' +
                '<div class="popup-desc">' + p.desc + '</div>' +
                '<button class="popup-btn" onclick="openDirections(\'' + p.id + '\')">Google Maps Directions</button>';

              marker.bindPopup(popupContent);

              marker.on('click', function() {
                if (window.AndroidBridge && window.AndroidBridge.onPinSelected) {
                  window.AndroidBridge.onPinSelected(p.id);
                }
              });

              markers[p.id] = marker;
            });

            if (rawData.length > 0) {
              var group = L.featureGroup(Object.keys(markers).map(function(k) { return markers[k]; }));
              map.fitBounds(group.getBounds().pad(0.3), { maxZoom: 12 });
            }

            function openDirections(id) {
              if (window.AndroidBridge && window.AndroidBridge.onDirections) {
                window.AndroidBridge.onDirections(id);
              }
            }

            window.focusPin = function(id, lat, lng, zoom) {
              if (map) {
                map.flyTo([lat, lng], zoom, { animate: true, duration: 0.8 });
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

/** Detail card for the selected outage pin. */
@Composable
private fun OutagePinInspectorCard(
    pin: OutagePin,
    onGoogleMapsDirections: () -> Unit,
    onGoogleMapsPinpoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = Color(android.graphics.Color.parseColor(pin.colorHex))
    Card(
        modifier = modifier.testTag("selected_outage_pin_card"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = pin.statusLabel,
                        color = statusColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = timeAgo(pin.latestAt),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = pin.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = listOf(pin.place, pin.discoCode).filter { it.isNotBlank() }.joinToString(" · "),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Source: ${pin.sourceLabel}" +
                        if (pin.isApproximate) ". Shown at the state capital because the exact spot was not shared." else "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGoogleMapsDirections,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Maps Directions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onGoogleMapsPinpoint,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pinpoint Map", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
