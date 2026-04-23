package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.destinations.ChatPage
import com.example.cargoconnectapp.ui.models.destinations.DeliveryPage
import com.example.cargoconnectapp.ui.models.destinations.DriverChat
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboardPage
import com.example.cargoconnectapp.ui.models.destinations.DriverDeliveries
import com.example.cargoconnectapp.ui.models.destinations.DriverDestination
import com.example.cargoconnectapp.ui.models.destinations.DriverProfile
import com.example.cargoconnectapp.ui.models.destinations.DriverProfilePage
import com.example.cargoconnectapp.ui.models.destinations.DriverShipmentRequest
import com.example.cargoconnectapp.ui.models.destinations.ShipmentRequestPage
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.ActivityScreen
import com.example.cargoconnectapp.ui.navigation.screens.ChatScreen
import com.example.cargoconnectapp.ui.navigation.screens.CustomerShipmentsDetailScreen
import com.example.cargoconnectapp.ui.navigation.screens.CustomersScreen
import com.example.cargoconnectapp.ui.navigation.screens.DashboardShipmentsScreen
import com.example.cargoconnectapp.ui.navigation.screens.DeliveryDelayScreen
import com.example.cargoconnectapp.ui.navigation.screens.DriverDashboardScreen
import com.example.cargoconnectapp.ui.navigation.screens.ProfileScreen
import com.example.cargoconnectapp.ui.navigation.screens.ShipmentRequestDetailScreen
import com.example.cargoconnectapp.ui.navigation.screens.ShipmentRequestsScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.driverGraph(
    navController: NavHostController,
    session: UserSessionViewModel,
    authViewModel: AuthViewModel,
    driverViewModel: DriverViewModel
) {
    fun NavHostController.navigateTab(destination: DriverDestination) {
        session.setDriverTab(destination)
        navigate(destination) {
            popUpTo(DriverDashboard()) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // ── HOME ──
    composable<DriverDashboard> {
        val page = it.toRoute<DriverDashboard>().page
        when (page) {
            DriverDashboardPage.TripInfo,
            DriverDashboardPage.TruckInfo -> {
                DriverDashboardScreen(
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) },
                    onDelayClick   = { navController.navigate(DriverDashboard(DriverDashboardPage.AddDelay)) },
                    onReachedClick = { navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery)) },
                    driverViewModel = driverViewModel
                )
            }
            DriverDashboardPage.AddDelay -> {
                DeliveryDelayScreen(
                    onBack = { navController.popBackStack() },
                    onConfirmDelay = { _, _, _, _ -> navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) }
                )
            }
            DriverDashboardPage.ConfirmDelivery -> {
                DashboardShipmentsScreen(
                    onBack = { navController.popBackStack() },
                    onConfirmDelivery = { navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) }
                )
            }
        }
    }

    // ── ORDERS ──
    composable<DriverShipmentRequest> {
        val page = it.toRoute<DriverShipmentRequest>().page
        when (page) {
            ShipmentRequestPage.Main -> {
                ShipmentRequestsScreen(
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) },
                    onDetailsClick = { navController.navigate(DriverShipmentRequest(ShipmentRequestPage.Details)) },
                    onAccept       = { navController.popBackStack() },
                    onDecline      = { navController.popBackStack() }
                )
            }
            ShipmentRequestPage.Details -> {
                ShipmentRequestDetailScreen(
                    onBack         = { navController.popBackStack() },
                    onAccept       = { navController.popBackStack() },
                    onDecline      = { navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) }
                )
            }
        }
    }

    // ── HISTORY / CUSTOMERS ──
    composable<DriverDeliveries> {
        val page = it.toRoute<DriverDeliveries>().page
        when (page) {
            DeliveryPage.Main -> {
                CustomersScreen(
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) },
                    onDetailsClick = { navController.navigate(DriverDeliveries(DeliveryPage.Details)) },
                    onConfirmClick = { navController.popBackStack() }
                )
            }
            DeliveryPage.Details -> {
                CustomerShipmentsDetailScreen(
                    onBack         = { navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) }
                )
            }
        }
    }

    // ── CHAT ──
    composable<DriverChat> {
        val page = it.toRoute<DriverChat>().page
        when (page) {
            ChatPage.Main,
            ChatPage.Notifications -> {
                ActivityScreen(
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatNavClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfile()) },
                    onChatClick    = { navController.navigate(DriverChat(ChatPage.Details)) }
                )
            }
            ChatPage.Details -> {
                ChatScreen(
                    onBack      = { navController.popBackStack() },
                    onCallClick = {}
                )
            }
        }
    }

    // ── PROFILE ──
    composable<DriverProfile> {
        val page = it.toRoute<DriverProfile>().page
        when (page) {
            DriverProfilePage.Main -> {
                ProfileScreen(
                    currentTab             = session.currentDriverTab,
                    onHomeClick            = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick          = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick         = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick            = { navController.navigateTab(DriverChat()) },
                    onProfileClick         = { navController.navigateTab(DriverProfile()) },
                    onYourInformationClick = { navController.navigate(DriverProfile(DriverProfilePage.YourInformation)) },
                    onActivateRouteClick   = { navController.navigate(DriverProfile(DriverProfilePage.ActivateRoute)) },
                    onLogoutClick          = { authViewModel.logout() }
                )
            }
            DriverProfilePage.YourInformation,
            DriverProfilePage.EditInformation,
            DriverProfilePage.ActivateRoute,
            DriverProfilePage.AddNewRoute,
            DriverProfilePage.EstimatedDelivery -> {
                // Placeholder until screens are built
            }
        }
    }
}