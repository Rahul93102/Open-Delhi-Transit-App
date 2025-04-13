package com.example.transitapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.transitapp.ui.screens.MainScreen
import com.example.transitapp.ui.theme.TransitAppTheme
import com.example.transitapp.ui.viewmodel.TransitViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    
    // Use viewModels() delegate to create the ViewModel
    private val viewModel: TransitViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d(TAG, "App starting...")
        
        setContent {
            TransitAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(transitViewModel = viewModel)
                }
            }
        }
        
        Log.d(TAG, "UI initialized")
    }
}