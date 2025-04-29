package com.example.transitapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.transitapp.R
import com.example.transitapp.data.FuelStation
import com.example.transitapp.data.FuelType
import com.example.transitapp.ui.viewmodel.FuelStationUiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun FuelStationsScreen(
    uiState: FuelStationUiState,
    selectedFuelType: FuelType,
    searchRadius: Int,
    onFuelTypeSelected: (FuelType) -> Unit,
    onRadiusChanged: (Int) -> Unit,
    onLocationRequest: () -> Unit,
    onFetchAllTypesRequest: () -> Unit,
    currentLocation: LatLng?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Filter bar
        FuelTypeFilterBar(
            selectedFuelType = selectedFuelType,
            onFuelTypeSelected = onFuelTypeSelected,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        // Radius slider
        RadiusSlider(
            radius = searchRadius,
            onRadiusChanged = onRadiusChanged,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        
        // Main content based on state
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is FuelStationUiState.Initial -> {
                    InitialStateContent(
                        onLocationRequest = onLocationRequest,
                        onFetchAllTypesRequest = onFetchAllTypesRequest,
                        currentLocation = currentLocation
                    )
                }
                is FuelStationUiState.LoadingLocation -> {
                    LoadingStateContent(message = "Getting your location...")
                }
                is FuelStationUiState.Loading -> {
                    LoadingStateContent(message = "Finding ${selectedFuelType.displayName} stations...")
                }
                is FuelStationUiState.LoadingAll -> {
                    LoadingStateContent(message = "Finding all alternative fuel stations...")
                }
                is FuelStationUiState.Success -> {
                    SuccessStateContent(
                        stations = uiState.stations,
                        fuelType = selectedFuelType,
                        currentLocation = currentLocation
                    )
                }
                is FuelStationUiState.SuccessAllTypes -> {
                    AllFuelTypesContent(
                        stationsByType = uiState.stationsByType,
                        currentLocation = currentLocation
                    )
                }
                is FuelStationUiState.Empty -> {
                    EmptyStateContent(message = uiState.message)
                }
                is FuelStationUiState.Error -> {
                    ErrorStateContent(
                        message = uiState.message,
                        onRetry = onLocationRequest
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelTypeFilterBar(
    selectedFuelType: FuelType,
    onFuelTypeSelected: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FuelType.getAllFuelTypes()) { fuelType ->
            FilterChip(
                selected = selectedFuelType == fuelType,
                onClick = { onFuelTypeSelected(fuelType) },
                label = { Text(fuelType.displayName) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(getFuelTypeIcon(fuelType)),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun RadiusSlider(
    radius: Int,
    onRadiusChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search Radius",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$radius miles",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = radius.toFloat(),
            onValueChange = { onRadiusChanged(it.toInt()) },
            valueRange = 5f..1000f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun InitialStateContent(
    onLocationRequest: () -> Unit,
    onFetchAllTypesRequest: () -> Unit,
    currentLocation: LatLng?
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fuel_stations),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Find Alternative Fuel Stations Near You",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Locate electric charging, hydrogen, CNG, biodiesel and other alternative fuel stations.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onLocationRequest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use My Current Location")
        }
        
        if (currentLocation != null) {
            Button(
                onClick = onFetchAllTypesRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show All Fuel Types")
            }
        }
    }
}

@Composable
fun LoadingStateContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SuccessStateContent(
    stations: List<FuelStation>,
    fuelType: FuelType,
    currentLocation: LatLng?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Map preview
        if (currentLocation != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(currentLocation, 11f)
                }
                
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // Current location marker
                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Your Location"
                    )
                    
                    // Station markers
                    stations.forEach { station ->
                        val position = LatLng(station.latitude, station.longitude)
                        Marker(
                            state = MarkerState(position = position),
                            title = station.stationName
                        )
                    }
                }
            }
        }
        
        // Station list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "${stations.size} ${fuelType.displayName} stations found",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            items(stations) { station ->
                FuelStationCard(station = station, fuelType = fuelType)
            }
        }
    }
}

@Composable
fun AllFuelTypesContent(
    stationsByType: Map<FuelType, List<FuelStation>>,
    currentLocation: LatLng?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Map preview at the top
        if (currentLocation != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(currentLocation, 11f)
                    }
                    
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        // Current location marker
                        Marker(
                            state = MarkerState(position = currentLocation),
                            title = "Your Location"
                        )
                        
                        // Station markers for all types
                        stationsByType.forEach { (_, stations) ->
                            stations.forEach { station ->
                                val position = LatLng(station.latitude, station.longitude)
                                Marker(
                                    state = MarkerState(position = position),
                                    title = station.stationName
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Sections for each fuel type that has stations
        stationsByType.forEach { (fuelType, stations) ->
            if (stations.isNotEmpty()) {
                item {
                    FuelTypeSection(fuelType = fuelType, stations = stations)
                }
            }
        }
    }
}

@Composable
fun FuelTypeSection(
    fuelType: FuelType,
    stations: List<FuelStation>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(getFuelTypeIcon(fuelType)),
                contentDescription = null,
                tint = getFuelTypeColor(fuelType),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = fuelType.description,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Preview the first station
        if (stations.isNotEmpty()) {
            FuelStationCard(station = stations[0], fuelType = fuelType)
        }
        
        // Show count if there are more
        if (stations.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+${stations.size - 1} more ${fuelType.displayName} stations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun FuelStationCard(
    station: FuelStation,
    fuelType: FuelType,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rotation"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon for the fuel type
                Surface(
                    shape = CircleShape,
                    color = getFuelTypeColor(fuelType).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, getFuelTypeColor(fuelType)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(getFuelTypeIcon(fuelType)),
                        contentDescription = null,
                        tint = getFuelTypeColor(fuelType),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Station name and basic info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.stationName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.city + ", " + station.state,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Distance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.1f mi", station.distance),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Toggle details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Details" else "Show Details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Hide details" else "Show details",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(20.dp)
                        .rotate(rotationState),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Expanded details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    DetailItem(
                        label = "Address",
                        value = "${station.streetAddress}, ${station.city}, ${station.state} ${station.zip}"
                    )
                    DetailItem(
                        label = "Access",
                        value = station.accessDaysTime ?: "Not specified"
                    )
                    
                    // Fuel-specific details
                    when (fuelType) {
                        FuelType.ELECTRIC -> {
                            station.evConnectorTypes?.let { connectors ->
                                DetailItem(
                                    label = "Connectors",
                                    value = connectors.joinToString(", ")
                                )
                            }
                            DetailItem(
                                label = "Level 2 Ports",
                                value = station.evLevel2Count?.toString() ?: "0"
                            )
                            DetailItem(
                                label = "DC Fast Ports",
                                value = station.evDcFastCount?.toString() ?: "0"
                            )
                        }
                        FuelType.CNG, FuelType.LNG -> {
                            DetailItem(
                                label = "Fill Type",
                                value = station.cngFillTypeCode ?: station.ngFillTypeCode ?: "Not specified"
                            )
                            DetailItem(
                                label = "Pressure",
                                value = "${station.cngPsi ?: station.ngPsi ?: "Not specified"} psi"
                            )
                        }
                        FuelType.HYDROGEN -> {
                            DetailItem(
                                label = "Retail",
                                value = if (station.hyIsRetail == true) "Yes" else "No"
                            )
                            station.hyPressures?.let { pressures ->
                                DetailItem(
                                    label = "Pressures",
                                    value = "${pressures.joinToString(", ")} bar"
                                )
                            }
                        }
                        else -> {
                            // No special details for other fuel types
                        }
                    }
                    
                    // Phone number if available
                    station.phone?.let { phone ->
                        DetailItem(label = "Phone", value = phone)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun EmptyStateContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fuel_stations),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorStateContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fuel_stations),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun getFuelTypeIcon(fuelType: FuelType): Int {
    return when (fuelType) {
        FuelType.ELECTRIC -> R.drawable.ic_fuel_electric
        FuelType.ETHANOL -> R.drawable.ic_fuel_gas
        FuelType.CNG, FuelType.LNG -> R.drawable.ic_fuel_natural_gas
        FuelType.PROPANE -> R.drawable.ic_fuel_gas
        FuelType.BIODIESEL, FuelType.RENEWABLE_DIESEL -> R.drawable.ic_fuel_biodiesel
        FuelType.HYDROGEN -> R.drawable.ic_fuel_hydrogen
    }
}

private fun getFuelTypeColor(fuelType: FuelType): Color {
    return when (fuelType) {
        FuelType.ELECTRIC -> Color(0xFF4CAF50)  // Green
        FuelType.ETHANOL -> Color(0xFFFF9800)  // Orange
        FuelType.CNG, FuelType.LNG -> Color(0xFF673AB7)  // Purple
        FuelType.PROPANE -> Color(0xFFFF5722)  // Deep Orange
        FuelType.BIODIESEL, FuelType.RENEWABLE_DIESEL -> Color(0xFF8BC34A)  // Light Green
        FuelType.HYDROGEN -> Color(0xFF2196F3)  // Blue
    }
} 