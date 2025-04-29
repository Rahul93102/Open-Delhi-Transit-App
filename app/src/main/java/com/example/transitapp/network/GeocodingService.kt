package com.example.transitapp.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String
)

data class GeocodingResult(
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String
)

data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

interface GeocodingService {
    @GET("maps/api/geocode/json")
    suspend fun getGeocode(
        @Query("address") address: String,
        @Query("key") apiKey: String = "AlzaSyCTuTnPjNntqTTs2I_zMHCbXfWDcoTqGVJ"
    ): Response<GeocodingResponse>
} 