package com.example.transitapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.transitapp.ui.screens.BusListScreen
import com.example.transitapp.ui.screens.SearchScreen
import com.example.transitapp.ui.viewmodel.TransitViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object BusList : Screen("busList")
    object Search : Screen("search")
}

@Composable
fun TransitNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val viewModel = remember { TransitViewModel(context.applicationContext as android.app.Application) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.BusList.route,
        modifier = modifier
    ) {
        composable(Screen.BusList.route) {
            BusListScreen(
                viewModel = viewModel,
                onNavigateToSearch = { 
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
} 