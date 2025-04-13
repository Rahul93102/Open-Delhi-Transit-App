package com.example.transitapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.transitapp.ui.components.DebugInfo
import com.example.transitapp.ui.components.ErrorMessage
import com.example.transitapp.ui.components.LoadingIndicator
import com.example.transitapp.ui.components.SearchBar
import com.example.transitapp.ui.components.VehicleDetails
import com.example.transitapp.ui.components.VehicleListScreen
import com.example.transitapp.ui.viewmodel.TransitUiState
import com.example.transitapp.ui.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    transitViewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    val vehicleListState by transitViewModel.vehicleListState.collectAsState()
    
    if (showSearch) {
        SearchScreen(
            viewModel = transitViewModel,
            onBackClick = { showSearch = false }
        )
    } else {
        Scaffold { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                VehicleListScreen(
                    state = vehicleListState,
                    onRefresh = transitViewModel::refreshVehicleList,
                    onVehicleSelected = transitViewModel::selectVehicle,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Add floating action button
                FloatingActionButton(
                    onClick = { showSearch = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
} 