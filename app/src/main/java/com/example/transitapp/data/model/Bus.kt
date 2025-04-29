package com.example.transitapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buses")
data class Bus(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double = 0.0,
    val route: String = "",
    val status: String = "Active"
) 