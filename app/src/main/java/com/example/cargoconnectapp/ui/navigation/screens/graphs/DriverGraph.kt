package com.example.cargoconnectapp.ui.navigation.screens.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel

fun NavGraphBuilder.driverGraph(
    navController: NavHostController,
    session: UserSessionViewModel,
    authViewModel: AuthViewModel
) {
    composable<DriverDashboard> {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Driver Dashboard - Coming Soon")
        }
    }
}