package com.example.transitapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transitapp.data.TransitRepository
import com.example.transitapp.data.VehicleData
import com.example.transitapp.network.TransitApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransitViewModel : ViewModel() {
    private val TAG = "TransitViewModel"
    private val repository = TransitRepository(TransitApiService.create())
    
    private val _uiState = MutableStateFlow<TransitUiState>(TransitUiState.Initial)
    val uiState: StateFlow<TransitUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _debugInfo = MutableStateFlow("")
    val debugInfo: StateFlow<String> = _debugInfo.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchVehicle() {
        val query = _searchQuery.value.trim().uppercase()
        if (query.isEmpty()) {
            _uiState.value = TransitUiState.Error("Please enter a valid vehicle ID")
            return
        }
        
        _uiState.value = TransitUiState.Loading
        _debugInfo.value = "Searching for vehicle ID: $query"
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting search for vehicle ID: $query")
                _debugInfo.value += "\nStarting API request..."
                
                val result = repository.getVehicleData(query)
                
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
}

sealed class TransitUiState {
    object Initial : TransitUiState()
    object Loading : TransitUiState()
    data class Success(val vehicleData: VehicleData) : TransitUiState()
    data class Error(val message: String) : TransitUiState()
} 