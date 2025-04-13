package com.example.transitapp

import android.app.Application
import com.example.transitapp.data.database.TransitAppDatabase

class TransitApplication : Application() {
    
    val database by lazy { TransitAppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any required components here
    }
} 