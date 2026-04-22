package com.example.cargoconnectapp.ui.navigation.screens.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboard
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboardPage
import com.example.cargoconnectapp.ui.models.destinations.CustomerDestination
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard

fun NavGraphBuilder.customerGraph(navController: NavHostController) {
    composable<CustomerDashboard> {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Customer Dashboard - Coming Soon")
        }
    }
}