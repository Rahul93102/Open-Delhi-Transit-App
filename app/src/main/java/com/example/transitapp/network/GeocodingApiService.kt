package com.example.transitapp.network

import com.example.transitapp.data.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("maps/api/geocode/json")
    suspend fun getGeocode(
        @Query("address") address: String,
        @Query("key") apiKey: String = "AlzaSyCTuTnPjNntqTTs2I_zMHCbXfWDcoTqGVJ"
    ): GeocodingResponse
}

object GeocodingApi {
    private const val BASE_URL = "https://maps.gomaps.pro/"
    
    val retrofitService: GeocodingApiService = RetrofitClient.create(BASE_URL, GeocodingApiService::class.java)
} 