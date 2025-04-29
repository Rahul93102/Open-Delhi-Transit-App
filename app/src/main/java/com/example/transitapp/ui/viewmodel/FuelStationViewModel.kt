package com.example.transitapp.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.transitapp.data.FuelStation
import com.example.transitapp.data.FuelStationRepository
import com.example.transitapp.data.FuelType
import com.example.transitapp.network.FuelStationApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

// Extension function to convert Task to suspending function
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw CancellationException("Task $this was cancelled")
            } else {
                result
            }
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            val e = exception
            if (e == null) {
                if (isCanceled) {
                    cont.cancel()
                } else {
                    cont.resume(result)
                }
            } else {
                cont.resumeWithException(e)
            }
        }
    }
}

class FuelStationViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FuelStationViewModel"
    
    // Repository
    private val fuelStationRepository = FuelStationRepository(FuelStationApiService.create())
    
    // Location provider
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(application)
    private val cancellationTokenSource = CancellationTokenSource()
    private val context: Context = application.applicationContext
    
    // UI states
    private val _uiState = MutableStateFlow<FuelStationUiState>(FuelStationUiState.Initial)
    val uiState: StateFlow<FuelStationUiState> = _uiState.asStateFlow()
    
    private val _selectedFuelType = MutableStateFlow<FuelType>(FuelType.ELECTRIC)
    val selectedFuelType: StateFlow<FuelType> = _selectedFuelType.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _searchRadius = MutableStateFlow(10)  // Default 10 miles
    val searchRadius: StateFlow<Int> = _searchRadius.asStateFlow()
    
    init {
        // We don't auto-fetch user location on init to avoid permission issues 
        // This will be initiated by user action
    }
    
    override fun onCleared() {
        super.onCleared()
        cancellationTokenSource.cancel()
    }
    
    fun selectFuelType(fuelType: FuelType) {
        if (_selectedFuelType.value != fuelType) {
            _selectedFuelType.value = fuelType
            
            // Reload stations if we have a location
            _currentLocation.value?.let { location ->
                fetchNearbyStations(fuelType, location.latitude, location.longitude)
            }
        }
    }
    
    fun setSearchRadius(radius: Int) {
        if (_searchRadius.value != radius) {
            _searchRadius.value = radius
            
            // Reload stations with new radius if we have a location
            _currentLocation.value?.let { location ->
                fetchNearbyStations(_selectedFuelType.value, location.latitude, location.longitude)
            }
        }
    }
    
    // Check if we have location permissions
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun fetchCurrentLocation() {
        _uiState.value = FuelStationUiState.LoadingLocation
        
        viewModelScope.launch {
            try {
                // Check permissions before attempting to get location
                if (!hasLocationPermission()) {
                    Log.e(TAG, "Location permission not granted")
                    _uiState.value = FuelStationUiState.Error("Location permission not granted. Please enable location permissions in settings.")
                    return@launch
                }
                
                try {
                    val currentLocation = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, 
                        cancellationTokenSource.token
                    ).await()
                    
                    if (currentLocation != null) {
                        Log.d(TAG, "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")
                        _currentLocation.value = currentLocation
                        
                        // Fetch fuel stations near the current location
                        fetchNearbyStations(
                            _selectedFuelType.value,
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                    } else {
                        Log.e(TAG, "Failed to get current location")
                        _uiState.value = FuelStationUiState.Error("Unable to determine your location.")
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception getting location", e)
                    _uiState.value = FuelStationUiState.Error("Location permission denied: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting location", e)
                _uiState.value = FuelStationUiState.Error("Error getting location: ${e.message}")
            }
        }
    }
    
    fun fetchNearbyStations(fuelType: FuelType, latitude: Double, longitude: Double) {
        _uiState.value = FuelStationUiState.Loading
        
        viewModelScope.launch {
            try {
                val stations = fuelStationRepository.getNearestStations(
                    latitude = latitude,
                    longitude = longitude,
                    fuelType = fuelType,
                    radius = _searchRadius.value,
                    limit = 10
                )
                
                if (stations.isNotEmpty()) {
                    Log.d(TAG, "Fetched ${stations.size} ${fuelType.displayName} stations")
                    _uiState.value = FuelStationUiState.Success(stations)
                } else {
                    Log.d(TAG, "No ${fuelType.displayName} stations found within ${_searchRadius.value} miles")
                    _uiState.value = FuelStationUiState.Empty("No ${fuelType.displayName} stations found within ${_searchRadius.value} miles.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching stations", e)
                _uiState.value = FuelStationUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun fetchAllFuelTypeStations(latitude: Double, longitude: Double) {
        _uiState.value = FuelStationUiState.LoadingAll
        
        viewModelScope.launch {
            try {
                val allStations = fuelStationRepository.getAllFuelTypeStations(
                    latitude = latitude,
                    longitude = longitude,
                    radius = _searchRadius.value,
                    limit = 3
                )
                
                if (allStations.any { it.value.isNotEmpty() }) {
                    Log.d(TAG, "Fetched stations for multiple fuel types")
                    _uiState.value = FuelStationUiState.SuccessAllTypes(allStations)
                } else {
                    Log.d(TAG, "No stations found for any fuel type")
                    _uiState.value = FuelStationUiState.Empty("No alternative fuel stations found within ${_searchRadius.value} miles.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all fuel type stations", e)
                _uiState.value = FuelStationUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed class FuelStationUiState {
    object Initial : FuelStationUiState()
    object LoadingLocation : FuelStationUiState()
    object Loading : FuelStationUiState()
    object LoadingAll : FuelStationUiState()
    data class Success(val stations: List<FuelStation>) : FuelStationUiState()
    data class SuccessAllTypes(val stationsByType: Map<FuelType, List<FuelStation>>) : FuelStationUiState()
    data class Empty(val message: String) : FuelStationUiState()
    data class Error(val message: String) : FuelStationUiState()
} 