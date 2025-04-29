package com.example.transitapp.data

import android.util.Log
import com.example.transitapp.network.TransitApiService
import com.example.transitapp.proto.GtfsRealtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.transitapp.network.GeocodingService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class TransitRepository(
    private val apiService: TransitApiService
) {
    private val apiKey = "7w7PJE7dxvuqYy1pOJL7FhfaKYVs70Pe"
    private val TAG = "TransitRepository"

    private val geocodingService = Retrofit.Builder()
        .baseUrl("https://maps.gomaps.pro/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingService::class.java)

    suspend fun getAllVehicles(limit: Int = 100): List<VehicleData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Making API request to get all vehicles (limit: $limit)")
            val response = apiService.getVehiclePositions(apiKey)
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API request failed with code: ${response.code()}")
                return@withContext createSampleVehicleList(limit)
            }
            
            val responseBody = response.body()
            if (responseBody == null) {
                Log.e(TAG, "Response body is null")
                return@withContext createSampleVehicleList(limit)
            }
            
            try {
                Log.d(TAG, "Received response, parsing protobuf data")
                val bytes = responseBody.bytes()
                Log.d(TAG, "Response size: ${bytes.size} bytes")
                
                if (bytes.isEmpty()) {
                    Log.e(TAG, "Empty response received")
                    return@withContext createSampleVehicleList(limit)
                }
                
                val feedMessage = FeedMessage.parseFrom(bytes)
                Log.d(TAG, "Total entities in feed: ${feedMessage.entityCount}")
                
                if (feedMessage.entityCount == 0) {
                    Log.e(TAG, "No entities found in feed")
                    return@withContext createSampleVehicleList(limit)
                }
                
                val vehicleList = feedMessage.entityList
                    .take(limit)
                    .mapNotNull { entity ->
                        if (entity.hasVehicle()) {
                            val vehicle = entity.vehicle
                            val trip = if (vehicle.hasTrip()) vehicle.trip else null
                            val position = if (vehicle.hasPosition()) vehicle.position else null
                            
                            VehicleData(
                                id = entity.id,
                                routeId = trip?.routeId,
                                tripId = trip?.tripId,
                                startTime = trip?.startTime,
                                startDate = trip?.startDate,
                                latitude = position?.latitude,
                                longitude = position?.longitude,
                                speed = position?.speed,
                                timestamp = vehicle.timestamp
                            )
                        } else null
                    }
                
                Log.d(TAG, "Processed ${vehicleList.size} vehicles")
                
                if (vehicleList.isEmpty()) {
                    Log.e(TAG, "No vehicles found in feed")
                    return@withContext createSampleVehicleList(limit)
                }
                
                return@withContext vehicleList
                
            } catch (e: IOException) {
                Log.e(TAG, "Error parsing protobuf data", e)
                e.printStackTrace()
                return@withContext createSampleVehicleList(limit)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle data", e)
            e.printStackTrace()
            return@withContext createSampleVehicleList(limit)
        }
    }

    suspend fun getVehicleData(vehicleId: String): VehicleData? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Making API request for vehicle ID: $vehicleId")
            val response = apiService.getVehiclePositions(apiKey)
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API request failed with code: ${response.code()}")
                return@withContext createSampleVehicleData(vehicleId)
            }
            
            val responseBody = response.body()
            if (responseBody == null) {
                Log.e(TAG, "Response body is null")
                return@withContext createSampleVehicleData(vehicleId)
            }
            
            try {
                Log.d(TAG, "Received response, parsing protobuf data")
                val bytes = responseBody.bytes()
                Log.d(TAG, "Response size: ${bytes.size} bytes")
                
                if (bytes.isEmpty()) {
                    Log.e(TAG, "Empty response received")
                    return@withContext createSampleVehicleData(vehicleId)
                }
                
                val feedMessage = FeedMessage.parseFrom(bytes)
                Log.d(TAG, "Total entities in feed: ${feedMessage.entityCount}")
                
                if (feedMessage.entityCount == 0) {
                    Log.e(TAG, "No entities found in feed")
                    return@withContext createSampleVehicleData(vehicleId)
                }
                
                // Print first few entity IDs for debugging
                feedMessage.entityList.take(5).forEach { 
                    Log.d(TAG, "Entity ID: ${it.id}")
                }
                
                // First try exact match
                var entity = feedMessage.entityList.find { it.id == vehicleId }
                
                // If not found, try case-insensitive search
                if (entity == null) {
                    Log.d(TAG, "Exact match not found, trying case-insensitive search")
                    entity = feedMessage.entityList.find { it.id.equals(vehicleId, ignoreCase = true) }
                }
                
                // If still not found, try partial match (vehicle ID might be part of entity ID)
                if (entity == null) {
                    Log.d(TAG, "Case-insensitive match not found, trying partial match")
                    entity = feedMessage.entityList.find { it.id.contains(vehicleId, ignoreCase = true) }
                }
                
                entity?.let { foundEntity ->
                    if (foundEntity.hasVehicle()) {
                        val vehicle = foundEntity.vehicle
                        val trip = if (vehicle.hasTrip()) vehicle.trip else null
                        val position = if (vehicle.hasPosition()) vehicle.position else null
                        
                        Log.d(TAG, "Found vehicle: ${foundEntity.id}")
                        if (trip != null) {
                            Log.d(TAG, "Trip details - Route: ${trip.routeId}, Trip ID: ${trip.tripId}")
                        }
                        if (position != null) {
                            Log.d(TAG, "Position - Lat: ${position.latitude}, Lon: ${position.longitude}")
                        }
                        
                        return@withContext VehicleData(
                            id = foundEntity.id,
                            routeId = trip?.routeId,
                            tripId = trip?.tripId,
                            startTime = trip?.startTime,
                            startDate = trip?.startDate,
                            latitude = position?.latitude,
                            longitude = position?.longitude,
                            speed = position?.speed,
                            timestamp = vehicle.timestamp
                        )
                    } else {
                        Log.e(TAG, "Entity found but has no vehicle data")
                    }
                } ?: Log.e(TAG, "No matching vehicle found for ID: $vehicleId")
                
            } catch (e: IOException) {
                Log.e(TAG, "Error parsing protobuf data", e)
                e.printStackTrace()
            }
            
            // If we reach here, either no vehicle was found or there was an error parsing the protobuf
            return@withContext createSampleVehicleData(vehicleId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle data", e)
            e.printStackTrace()
            return@withContext createSampleVehicleData(vehicleId)
        }
    }
    
    // Create sample vehicle data for demo purposes or when API fails
    private fun createSampleVehicleData(vehicleId: String): VehicleData {
        Log.d(TAG, "Creating sample vehicle data for $vehicleId")
        return VehicleData(
            id = vehicleId,
            routeId = "Route_${vehicleId.takeLast(3)}",
            tripId = "Trip_${System.currentTimeMillis()}",
            startTime = "08:00:00",
            startDate = "20250413",
            latitude = 28.6139f,  // Delhi coordinates
            longitude = 77.2090f,
            speed = 25.5f,
            timestamp = System.currentTimeMillis() / 1000
        )
    }
    
    // Create a list of sample vehicles for demo purposes
    private fun createSampleVehicleList(count: Int): List<VehicleData> {
        Log.d(TAG, "Creating sample vehicle list with $count items")
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

    suspend fun searchNearbyBuses(address: String, maxResults: Int = 100): Result<List<VehicleData>> {
        return withContext(Dispatchers.IO) {
            try {
                // First, get coordinates for the address
                val geocodeResponse = geocodingService.getGeocode(address)
                if (!geocodeResponse.isSuccessful || geocodeResponse.body()?.status != "OK") {
                    return@withContext Result.failure(Exception("Failed to geocode address"))
                }

                val location = geocodeResponse.body()?.results?.firstOrNull()?.geometry?.location
                    ?: return@withContext Result.failure(Exception("No location found for address"))

                // Get all vehicles (using sample data for now)
                val allVehicles = createSampleVehicleList(200) // Get more vehicles to filter from

                // Calculate distances and sort by proximity
                val nearbyVehicles = allVehicles
                    .map { vehicle ->
                        val distance = calculateDistance(
                            LatLng(location.lat, location.lng),
                            LatLng(vehicle.latitude!!.toDouble(), vehicle.longitude!!.toDouble())
                        )
                        vehicle to distance
                    }
                    .sortedBy { it.second }
                    .take(maxResults)
                    .map { it.first }

                Result.success(nearbyVehicles)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching nearby buses", e)
                Result.failure(e)
            }
        }
    }

    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        return earthRadius * acos(
            sin(lat1) * sin(lat2) +
            cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        )
    }
}

data class VehicleData(
    val id: String,
    val routeId: String? = null,
    val tripId: String? = null,
    val startTime: String? = null,
    val startDate: String? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val speed: Float? = null,
    val timestamp: Long? = null
) 