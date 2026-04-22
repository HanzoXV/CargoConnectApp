package com.example.cargoconnectapp.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.cargoconnectapp.ui.models.destinations.Splash
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.graphs.authGraph
import com.example.cargoconnectapp.ui.navigation.screens.graphs.customerGraph
import com.example.cargoconnectapp.ui.navigation.screens.graphs.driverGraph


class MainActivity : ComponentActivity() {
    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sessionViewModel: UserSessionViewModel by viewModels()

        val authViewModel: AuthViewModel by viewModels {
            viewModelFactory {
                initializer {
                    AuthViewModel(sessionViewModel)
                }
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppNavigation(sessionViewModel, authViewModel)
        }
    }
}
@RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(sessionViewModel: UserSessionViewModel,
                  authViewModel: AuthViewModel,
                  navController: NavHostController = rememberNavController()
) {

    navController.enableOnBackPressed(true)

    NavHost(navController = navController,
        startDestination = Splash,
        enterTransition = {
            fadeIn(animationSpec = tween(200)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(300)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        })
    {
        authGraph(navController, sessionViewModel, authViewModel)
        driverGraph(navController, sessionViewModel, authViewModel)
        customerGraph(navController)
    }
}