package com.example.transitapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.transitapp.ui.components.FuelStationsScreen
import com.example.transitapp.ui.viewmodel.FuelStationViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelStationsScreen(
    viewModel: FuelStationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFuelType by viewModel.selectedFuelType.collectAsState()
    val searchRadius by viewModel.searchRadius.collectAsState()
    val currentLocationState by viewModel.currentLocation.collectAsState()
    
    // Convert Location to LatLng for GoogleMap
    val currentLocation = currentLocationState?.let {
        LatLng(it.latitude, it.longitude)
    }
    
    // Remember permission state
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
            if (isGranted) {
                viewModel.fetchCurrentLocation()
            }
        }
    )
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alternative Fuel Stations",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        FuelStationsScreen(
            uiState = uiState,
            selectedFuelType = selectedFuelType,
            searchRadius = searchRadius,
            onFuelTypeSelected = viewModel::selectFuelType,
            onRadiusChanged = viewModel::setSearchRadius,
            onLocationRequest = {
                if (hasLocationPermission) {
                    viewModel.fetchCurrentLocation()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            onFetchAllTypesRequest = {
                currentLocation?.let { location ->
                    viewModel.fetchAllFuelTypeStations(location.latitude, location.longitude)
                }
            },
            currentLocation = currentLocation,
            modifier = Modifier.padding(paddingValues)
        )
    }
} 