package com.example.transitapp.data.repository

import com.example.transitapp.data.model.Bus
import com.example.transitapp.network.GeocodingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.*

class TransitRepository {
    private val geocodingService = GeocodingApi.retrofitService

    suspend fun findNearbyBuses(address: String): Flow<List<Bus>> = flow {
        // First get coordinates from the address
        val geocodeResponse = geocodingService.getGeocode(address)
        
        if (geocodeResponse.results.isNotEmpty()) {
            val location = geocodeResponse.results.first().geometry.location
            
            // Get all buses and sort by distance
            val buses = getAllBuses()
            val nearbyBuses = buses.map { bus ->
                val distance = calculateDistance(
                    location.lat, location.lng,
                    bus.latitude, bus.longitude
                )
                Pair(bus, distance)
            }
            .sortedBy { it.second } // Sort by distance
            .take(100) // Get top 100 nearest buses
            .map { it.first } // Get just the bus objects
            
            emit(nearbyBuses)
        } else {
            emit(emptyList())
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val p = Math.PI / 180
        val a = 0.5 - cos((lat2 - lat1) * p) / 2 + 
                cos(lat1 * p) * cos(lat2 * p) * 
                (1 - cos((lon2 - lon1) * p)) / 2
        return 12742 * asin(sqrt(a)) // 2 * R; R = 6371 km
    }

    private suspend fun getAllBuses(): List<Bus> {
        // TODO: Implement getting all buses from your data source
        // This should connect to your existing bus data source
        return emptyList()
    }
} 