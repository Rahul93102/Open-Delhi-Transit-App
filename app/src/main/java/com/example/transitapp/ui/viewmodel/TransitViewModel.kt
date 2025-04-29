package com.example.transitapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.transitapp.data.SearchHistoryRepository
import com.example.transitapp.data.TransitRepository
import com.example.transitapp.data.VehicleData
import com.example.transitapp.data.database.TransitAppDatabase
import com.example.transitapp.network.TransitApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransitViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "TransitViewModel"
    
    // Transit repository
    private val transitRepository = TransitRepository(TransitApiService.create())
    
    // Search history repository
    private val searchHistoryDao = TransitAppDatabase.getDatabase(application).searchHistoryDao()
    private val searchHistoryRepository = SearchHistoryRepository(searchHistoryDao)
    
    // UI states
    private val _uiState = MutableStateFlow<TransitUiState>(TransitUiState.Initial)
    val uiState: StateFlow<TransitUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    
    private val _debugInfo = MutableStateFlow("")
    val debugInfo: StateFlow<String> = _debugInfo.asStateFlow()
    
    // Vehicle list state for displaying all vehicles
    private val _vehicleListState = MutableStateFlow<VehicleListState>(VehicleListState.Initial)
    val vehicleListState: StateFlow<VehicleListState> = _vehicleListState.asStateFlow()
    
    init {
        // Load all vehicles on initialization
        loadAllVehicles()
        // Load search suggestions
        loadSuggestions()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private fun loadSuggestions() {
        // Set initial default suggestions
        viewModelScope.launch {
            try {
                val defaultSuggestions = searchHistoryRepository.getDefaultSuggestions()
                _suggestions.value = defaultSuggestions
                
                // Then collect from the flow
                searchHistoryDao.getTopSearches(3).collect { entities ->
                    if (entities.isNotEmpty()) {
                        _suggestions.value = entities.map { it.query }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading suggestions", e)
                _suggestions.value = listOf("DL1PD1034", "DL1PC6453", "DL1PB2277")
            }
        }
    }
    
    fun searchVehicle() {
        val query = _searchQuery.value.trim().uppercase()
        if (query.isEmpty()) {
            _uiState.value = TransitUiState.Error("Please enter a valid vehicle ID")
            return
        }
        
        _uiState.value = TransitUiState.Loading
        _debugInfo.value = "Searching for vehicle ID: $query"
        
        // Record search in history
        viewModelScope.launch {
            searchHistoryRepository.addSearch(query)
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting search for vehicle ID: $query")
                _debugInfo.value += "\nStarting API request..."
                
                val result = transitRepository.getVehicleData(query)
                
                if (result != null) {
                    Log.d(TAG, "Vehicle found: ${result.id}")
                    _debugInfo.value += "\nVehicle found: ${result.id}"
                    _debugInfo.value += "\nRoute ID: ${result.routeId ?: "N/A"}"
                    _debugInfo.value += "\nTrip ID: ${result.tripId ?: "N/A"}"
                    _debugInfo.value += "\nPosition: Lat ${result.latitude}, Lon ${result.longitude}"
                    
                    // Check if this is sample data
                    if (result.routeId?.startsWith("Route_") == true) {
                        _debugInfo.value += "\nNOTE: Using sample data as API data was unavailable"
                    }
                    
                    _uiState.value = TransitUiState.Success(result)
                } else {
                    Log.e(TAG, "Vehicle not found: $query")
                    _debugInfo.value += "\nVehicle not found in API response"
                    _uiState.value = TransitUiState.Error("Vehicle with ID $query not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for vehicle", e)
                _debugInfo.value += "\nError: ${e.message}"
                _debugInfo.value += "\nStack trace: ${e.stackTraceToString()}"
                _uiState.value = TransitUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun selectSuggestion(suggestion: String) {
        _searchQuery.value = suggestion
        searchVehicle()
    }
    
    fun searchNearbyBuses() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _vehicleListState.value = VehicleListState.Error("Please enter a location to search")
            return
        }

        _vehicleListState.value = VehicleListState.Loading

        viewModelScope.launch {
            try {
                val result = transitRepository.searchNearbyBuses(query)
                result.fold(
                    onSuccess = { vehicles ->
                        if (vehicles.isEmpty()) {
                            _vehicleListState.value = VehicleListState.Empty("No buses found near $query")
                        } else {
                            _vehicleListState.value = VehicleListState.Success(vehicles)
                        }
                    },
                    onFailure = { error ->
                        _vehicleListState.value = VehicleListState.Error("Error: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for nearby buses", e)
                _vehicleListState.value = VehicleListState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun loadAllVehicles(limit: Int = 100) {
        _vehicleListState.value = VehicleListState.Loading
        
        viewModelScope.launch {
            try {
                val vehicles = transitRepository.getAllVehicles(limit)
                if (vehicles.isNotEmpty()) {
                    _vehicleListState.value = VehicleListState.Success(vehicles)
                } else {
                    _vehicleListState.value = VehicleListState.Empty("No vehicles found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicles", e)
                _vehicleListState.value = VehicleListState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun refreshVehicleList() {
        loadAllVehicles()
    }
    
    fun selectVehicle(vehicleData: VehicleData) {
        _uiState.value = TransitUiState.Success(vehicleData)
    }
    
    // Create sample vehicle data for testing
    private fun createSampleVehicleList(count: Int): List<VehicleData> {
        return List(count) { index ->
            val id = "DL${1000 + index}"
            VehicleData(
                id = id,
                routeId = "Route_${index % 20 + 1}",
                tripId = "Trip_${System.currentTimeMillis() + index}",
                startTime = "0${6 + (index % 12)}:${index % 60}:00",
                startDate = "20250413",
                latitude = 28.6139f + (index % 10) * 0.01f,  // Varied Delhi coordinates
                longitude = 77.2090f + (index % 15) * 0.01f,
                speed = 15f + (index % 40),
                timestamp = System.currentTimeMillis() / 1000
            )
        }
    }
}

sealed class TransitUiState {
    object Initial : TransitUiState()
    object Loading : TransitUiState()
    data class Success(val vehicleData: VehicleData) : TransitUiState()
    data class Error(val message: String) : TransitUiState()
}

sealed class VehicleListState {
    object Initial : VehicleListState()
    object Loading : VehicleListState()
    data class Success(val vehicles: List<VehicleData>) : VehicleListState()
    data class Empty(val message: String) : VehicleListState()
    data class Error(val message: String) : VehicleListState()
} 