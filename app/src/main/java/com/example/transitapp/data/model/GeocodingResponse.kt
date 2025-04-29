package com.example.transitapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results")
    val results: List<GeocodingResult>,
    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "formatted_address")
    val formattedAddress: String,
    @Json(name = "geometry")
    val geometry: Geometry,
    @Json(name = "place_id")
    val placeId: String,
    @Json(name = "types")
    val types: List<String>
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "location")
    val location: Location,
    @Json(name = "location_type")
    val locationType: String,
    @Json(name = "viewport")
    val viewport: Viewport
)

@JsonClass(generateAdapter = true)
data class Location(
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lng")
    val lng: Double
)

@JsonClass(generateAdapter = true)
data class Viewport(
    @Json(name = "northeast")
    val northeast: Location,
    @Json(name = "southwest")
    val southwest: Location
) 