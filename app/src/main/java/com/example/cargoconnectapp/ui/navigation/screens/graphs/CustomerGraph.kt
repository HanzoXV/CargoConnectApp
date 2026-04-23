package com.example.cargoconnectapp.ui.navigation.screens.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.destinations.CustomerBookingPage
import com.example.cargoconnectapp.ui.models.destinations.CustomerBookings
import com.example.cargoconnectapp.ui.models.destinations.CustomerChat
import com.example.cargoconnectapp.ui.models.destinations.CustomerChatPage
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboard
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboardPage
import com.example.cargoconnectapp.ui.models.destinations.CustomerDestination
import com.example.cargoconnectapp.ui.models.destinations.CustomerNewShipment
import com.example.cargoconnectapp.ui.models.destinations.CustomerNewShipmentPage
import com.example.cargoconnectapp.ui.models.destinations.CustomerProfile
import com.example.cargoconnectapp.ui.models.destinations.CustomerProfilePage
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.AvailableTrucksScreen
import com.example.cargoconnectapp.ui.navigation.screens.BookingShipmentsDetailScreen
import com.example.cargoconnectapp.ui.navigation.screens.CancelBookingScreen
import com.example.cargoconnectapp.ui.navigation.screens.ChatScreen
import com.example.cargoconnectapp.ui.navigation.screens.CustomerActivityScreen
import com.example.cargoconnectapp.ui.navigation.screens.CustomerDashboardScreen
import com.example.cargoconnectapp.ui.navigation.screens.CustomerProfileScreen
import com.example.cargoconnectapp.ui.navigation.screens.DriverFoundScreen
import com.example.cargoconnectapp.ui.navigation.screens.FindingTruckScreen
import com.example.cargoconnectapp.ui.navigation.screens.NewShipmentRecipientScreen
import com.example.cargoconnectapp.ui.navigation.screens.YourBookingsScreen

fun NavGraphBuilder.customerGraph(
    navController: NavHostController,
    session: UserSessionViewModel,
    authViewModel: AuthViewModel
) {
    fun NavHostController.navigateTab(destination: CustomerDestination) {
        session.setCustomerTab(destination)
        navigate(destination) {
            popUpTo(CustomerDashboard()) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // ── HOME ──
    composable<CustomerDashboard> {
        val page = it.toRoute<CustomerDashboard>().page
        when (page) {
            CustomerDashboardPage.Summary,
            CustomerDashboardPage.Details -> {
                CustomerDashboardScreen(
                    currentTab          = session.currentCustomerTab,
                    onHomeClick         = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick       = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick      = { navController.navigateTab(CustomerBookings()) },
                    onChatClick         = { navController.navigateTab(CustomerChat()) },
                    onProfileClick      = { navController.navigateTab(CustomerProfile()) },
                    onNewBookingClick   = { navController.navigate(CustomerNewShipment()) },
                    onDetailsClick      = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Details)) },
                    onContactDriverClick = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onTrackClick        = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) },
                    onCancelClick       = { navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) }
                )
            }
            CustomerDashboardPage.Tracking -> {
                FindingTruckScreen(
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatClick    = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) }
                )
            }
        }
    }

    // ── NEW SHIPMENT ──
    composable<CustomerNewShipment> {
        val page = it.toRoute<CustomerNewShipment>().page
        when (page) {
            CustomerNewShipmentPage.AddPackages -> {
                // Placeholder — screen is commented out in your file
            }
            CustomerNewShipmentPage.AddDetails -> { }
            CustomerNewShipmentPage.ChooseLocation -> { }
            CustomerNewShipmentPage.ChooseDates -> { }
            CustomerNewShipmentPage.RecipientDetails -> {
                NewShipmentRecipientScreen(
                    onBack         = { navController.popBackStack() },
                    onNext         = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck)) },
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatClick    = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) }
                )
            }
            CustomerNewShipmentPage.FindingTruck -> {
                FindingTruckScreen(
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatClick    = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) }
                )
            }
            CustomerNewShipmentPage.AvailableTrucks -> {
                AvailableTrucksScreen(
                    onDetailsClick = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.TruckDetails)) },
                    onSendRequest  = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck)) },
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatClick    = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) }
                )
            }
            CustomerNewShipmentPage.TruckDetails -> {
                DriverFoundScreen(
                    onViewDetails    = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.TruckDetails)) },
                    onContactDriver  = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onHomeClick      = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick    = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick   = { navController.navigateTab(CustomerBookings()) },
                    onChatClick      = { navController.navigateTab(CustomerChat()) },
                    onProfileClick   = { navController.navigateTab(CustomerProfile()) }
                )
            }
        }
    }

    // ── BOOKINGS / HISTORY ──
    composable<CustomerBookings> {
        val page = it.toRoute<CustomerBookings>().page
        when (page) {
            CustomerBookingPage.Active,
            CustomerBookingPage.Completed -> {
                YourBookingsScreen(
                    onHomeClick          = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick        = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick       = { navController.navigateTab(CustomerBookings()) },
                    onChatClick          = { navController.navigateTab(CustomerChat()) },
                    onProfileClick       = { navController.navigateTab(CustomerProfile()) },
                    onDetailsClick       = { navController.navigate(CustomerBookings(CustomerBookingPage.Details)) },
                    onContactDriverClick = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onTrackClick         = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) },
                    onCancelClick        = { navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) },
                    onReviewClick        = { }
                )
            }
            CustomerBookingPage.Details -> {
                BookingShipmentsDetailScreen(
                    onBack         = { navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatClick    = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) }
                )
            }
            CustomerBookingPage.CancelBooking -> {
                CancelBookingScreen(
                    onBack           = { navController.popBackStack() },
                    onGetInContact   = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onConfirmCancel  = { navController.popBackStack() },
                    onHomeClick      = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick    = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick   = { navController.navigateTab(CustomerBookings()) },
                    onChatClick      = { navController.navigateTab(CustomerChat()) },
                    onProfileClick   = { navController.navigateTab(CustomerProfile()) }
                )
            }
        }
    }

    // ── CHAT ──
    composable<CustomerChat> {
        val page = it.toRoute<CustomerChat>().page
        when (page) {
            CustomerChatPage.Main,
            CustomerChatPage.Notifications -> {
                CustomerActivityScreen(
                    onHomeClick    = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick = { navController.navigateTab(CustomerBookings()) },
                    onChatNavClick = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = { navController.navigateTab(CustomerProfile()) },
                    onChatClick    = { navController.navigate(CustomerChat(CustomerChatPage.Details)) }
                )
            }
            CustomerChatPage.Details -> {
                ChatScreen(
                    onBack      = { navController.popBackStack() },
                    onCallClick = {}
                )
            }
        }
    }

    // ── PROFILE ──
    composable<CustomerProfile> {
        val page = it.toRoute<CustomerProfile>().page
        when (page) {
            CustomerProfilePage.Main -> {
                CustomerProfileScreen(
                    onHomeClick              = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick            = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick           = { navController.navigateTab(CustomerBookings()) },
                    onChatClick              = { navController.navigateTab(CustomerChat()) },
                    onProfileClick           = { navController.navigateTab(CustomerProfile()) },
                    onBookingDetailsClick    = { navController.navigate(CustomerBookings()) },
                    onYourInformationClick   = { navController.navigate(
                        CustomerProfile(
                            CustomerProfilePage.YourInformation
                        )
                    ) },
                    onLogoutClick            = { authViewModel.logout() }
                )
            }
            CustomerProfilePage.YourInformation,
            CustomerProfilePage.EditInformation -> {
                // Placeholder until screens are built
            }
        }
    }
}