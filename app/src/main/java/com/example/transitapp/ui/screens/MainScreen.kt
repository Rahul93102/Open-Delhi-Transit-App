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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.transitapp.ui.viewmodel.TransitUiState
import com.example.transitapp.ui.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    transitViewModel: TransitViewModel = remember { TransitViewModel() }
) {
    val uiState by transitViewModel.uiState.collectAsState()
    val searchQuery by transitViewModel.searchQuery.collectAsState()
    val debugInfo by transitViewModel.debugInfo.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Delhi Bus Tracker",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Search bar always visible at the top
                SearchBar(
                    query = searchQuery,
                    onQueryChange = transitViewModel::updateSearchQuery,
                    onSearch = transitViewModel::searchVehicle
                )

                // Content based on UI state
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "state_transition"
                ) { state ->
                    when (state) {
                        is TransitUiState.Initial -> {
                            // Initial state, show nothing or guidance
                        }
                        is TransitUiState.Loading -> {
                            LoadingIndicator()
                        }
                        is TransitUiState.Success -> {
                            VehicleDetails(vehicleData = state.vehicleData)
                        }
                        is TransitUiState.Error -> {
                            Column {
                                ErrorMessage(message = state.message)
                                
                                if (debugInfo.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DebugInfo(rawResponse = debugInfo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 