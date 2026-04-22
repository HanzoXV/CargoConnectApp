package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.LoginRequest
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboard
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboardPage
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboardPage
import com.example.cargoconnectapp.ui.models.destinations.Login
import com.example.cargoconnectapp.ui.models.destinations.Register
import com.example.cargoconnectapp.ui.models.destinations.RegisterStep
import com.example.cargoconnectapp.ui.models.destinations.Splash
import com.example.cargoconnectapp.ui.models.helpermodels.UserRoles
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
fun NavGraphBuilder.authGraph (navController: NavHostController,
                               session: UserSessionViewModel, authViewModel: AuthViewModel) {
    composable<Splash> {
        SplashScreen(
            onNavigateToLogin = {
                navController.navigate(Login) {
                    popUpTo(Splash) { inclusive = true }
                }
            }
        )
    }
    composable<Login> {
        LoginScreen(authViewModel,
            onRegisterClick = { navController.navigate(Register(RegisterStep.Step1)) },
            onLoginClick = {
                if (authViewModel.userEmail.isBlank() || authViewModel.userPassword.isBlank()) {
                    authViewModel.errorMessage = "Email or password cannot be empty."
                } else {
                    authViewModel.login {
                        when (authViewModel.userRole) {
                            UserRoles.Driver -> navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                                popUpTo(Splash) { inclusive = true }
                            }
                            UserRoles.Customer -> navController.navigate(CustomerDashboard(CustomerDashboardPage.Summary)) {
                                popUpTo(Splash) { inclusive = true }
                            }
                            else -> {
                                authViewModel.errorMessage = "Login failed. Please try again."
                            }
                        }
                    }
                }
            }
        )
    }
    composable<Register> { backStackEntry ->
        val route: Register = backStackEntry.toRoute()
        when (route.step) {
            RegisterStep.Step1 -> {
                RegisterStep1(
                    authViewModel,
                    onNavigate = {
                        navController.navigate(Register(step = RegisterStep.Step2))
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            RegisterStep.Step2 -> {
                RegisterStep2(
                    viewModel = authViewModel,
                    onNext = {
                        if (authViewModel.userRole == UserRoles.Driver) {
                            navController.navigate(Register(step = RegisterStep.Step3))
                        } else {
                            navController.navigate(Register(step = RegisterStep.Step4))
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            RegisterStep.Step3 -> {
                if (authViewModel.userRole == UserRoles.Driver) {
                    RegisterStep3(
                        viewModel = authViewModel,
                        onNext = {
                            navController.navigate(Register(step = RegisterStep.Step4))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            RegisterStep.Step4 -> {
                val context = LocalContext.current
                RegisterStep4(
                    viewModel = authViewModel,
                    context = LocalContext.current,
                    onNext = {
                        if (authViewModel.userRole == UserRoles.Customer) {
                            authViewModel.registerUser(context = context) {
                                navController.navigate(Login)
                            }
                        } else {
                            navController.navigate(Register(step = RegisterStep.Step5))
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            RegisterStep.Step5 -> {
                val context = LocalContext.current
                if (authViewModel.userRole == UserRoles.Driver) {
                    RegisterStep5(
                        viewModel = authViewModel,
                        context = LocalContext.current,
                        onNext = {
                            authViewModel.registerDriver(context = context) {
                                navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                                    popUpTo(Splash) { inclusive = true }
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                else if (authViewModel.userRole == UserRoles.Customer) {
                    RegisterStep5(
                        viewModel = authViewModel,
                        context = LocalContext.current,
                        onNext = {
                            authViewModel.registerDriver(context = context)
                            {
                                navController.navigate(Login) {
                                    popUpTo(Splash) { inclusive = true }
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}