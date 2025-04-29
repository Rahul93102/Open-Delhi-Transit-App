package com.example.transitapp.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val status: String,
    val results: List<GeocodingResult>
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "formatted_address")
    val formattedAddress: String,
    val geometry: Geometry
)

@JsonClass(generateAdapter = true)
data class Geometry(
    val location: Location
)

@JsonClass(generateAdapter = true)
data class Location(
    val lat: Double,
    val lng: Double
) 