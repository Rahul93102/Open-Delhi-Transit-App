package com.example.transitapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.transitapp.R
import com.example.transitapp.ui.components.VehicleListScreen
import com.example.transitapp.ui.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusListScreen(
    viewModel: TransitViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToFuelStations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicleListState by viewModel.vehicleListState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
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
                
                // Secondary action row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToFuelStations,
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
                
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSearch,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        VehicleListScreen(
            state = vehicleListState,
            onRefresh = viewModel::refreshVehicleList,
            onVehicleSelected = viewModel::selectVehicle,
            modifier = Modifier.padding(paddingValues)
        )
    }
} 