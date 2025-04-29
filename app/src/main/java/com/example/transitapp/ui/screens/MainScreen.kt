package com.example.transitapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.transitapp.R
import com.example.transitapp.ui.components.DebugInfo
import com.example.transitapp.ui.components.ErrorMessage
import com.example.transitapp.ui.components.LoadingIndicator
import com.example.transitapp.ui.components.SearchBar
import com.example.transitapp.ui.components.VehicleDetails
import com.example.transitapp.ui.components.VehicleListScreen
import com.example.transitapp.ui.navigation.Screen
import com.example.transitapp.ui.viewmodel.FuelStationViewModel
import com.example.transitapp.ui.viewmodel.TransitUiState
import com.example.transitapp.ui.viewmodel.TransitViewModel
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    transitViewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    var showFuelStations by remember { mutableStateOf(false) }
    val vehicleListState by transitViewModel.vehicleListState.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val fuelStationViewModel = remember { FuelStationViewModel(application) }
    
    if (showSearch) {
        SearchScreen(
            viewModel = transitViewModel,
            onBackClick = { showSearch = false }
        )
    } else if (showFuelStations) {
        FuelStationsScreen(
            viewModel = fuelStationViewModel,
            onBackClick = { showFuelStations = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Delhi Bus Tracker",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column {
                    // Fuel Station Button at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { showFuelStations = true },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White,
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fuel_stations),
                                    contentDescription = "Fuel Stations",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = { Text("Find Fuel Stations") }
                        )
                    }
                    
                    // Vehicle list content
                VehicleListScreen(
                    state = vehicleListState,
                    onRefresh = transitViewModel::refreshVehicleList,
                    onVehicleSelected = transitViewModel::selectVehicle,
                        modifier = Modifier.weight(1f)
                )
                }
                
                // Add floating action button for search
                FloatingActionButton(
                    onClick = { showSearch = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
} 