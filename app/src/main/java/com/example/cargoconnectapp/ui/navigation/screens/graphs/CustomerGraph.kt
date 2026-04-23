package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.customerGraph(
    navController: NavHostController,
    session: UserSessionViewModel,
    authViewModel: AuthViewModel,
    customerViewModel: CustomerViewModel
) {
    fun NavHostController.navigateTab(destination: CustomerDestination) {
        session.setCustomerTab(destination)
        navigate(destination) {
            popUpTo(CustomerDashboard()) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Shared bottom bar args to keep code clean and dry
    fun sharedArgs() = object {
        val tab            = session.currentCustomerTab
        val onHomeClick    = { navController.navigateTab(CustomerDashboard()) }
        val onOrdersClick  = { navController.navigateTab(CustomerNewShipment()) }
        val onHistoryClick = { navController.navigateTab(CustomerBookings()) }
        val onChatClick    = { navController.navigateTab(CustomerChat()) }
        val onProfileClick = { navController.navigateTab(CustomerProfile()) }
    }

    // ── HOME ──
    composable<CustomerDashboard> {
        val page = it.toRoute<CustomerDashboard>().page
        val args = sharedArgs()
        
        LaunchedEffect(Unit) {
            customerViewModel.createOrGetDraft(session.roleBasedId)
        }

        when (page) {
            CustomerDashboardPage.Summary,
            CustomerDashboardPage.Details -> {
                CustomerDashboardScreen(
                    customerViewModel    = customerViewModel,
                    currentTab           = args.tab,
                    onHomeClick          = args.onHomeClick,
                    onOrdersClick        = args.onOrdersClick,
                    onHistoryClick       = args.onHistoryClick,
                    onChatClick          = args.onChatClick,
                    onProfileClick       = args.onProfileClick,
                    onNewBookingClick    = { navController.navigate(CustomerNewShipment()) },
                    onDetailsClick       = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Details)) },
                    onContactDriverClick = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onTrackClick         = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) },
                    onCancelClick        = { navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) }
                )
            }
            CustomerDashboardPage.Tracking -> {
                FindingTruckScreen(
                    currentTab     = args.tab,
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
        }
    }

    // ── NEW SHIPMENT ──
    composable<CustomerNewShipment> {
        val page = it.toRoute<CustomerNewShipment>().page
        val args = sharedArgs()
        when (page) {
            CustomerNewShipmentPage.AddPackages -> {
                NewShipmentPackagesScreen(
                    currentTab = args.tab,
                    addedPackages = customerViewModel.addedPackages,
                    onAddNewPackage = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.AddDetails)) },
                    onRemovePackage = { pkg -> customerViewModel.removePackage(pkg) },
                    onNext = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.ChooseLocation)) },
                    onBack = { navController.popBackStack() },
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.AddDetails  -> {
                NewShipmentPackageDetailsScreen(
                    customerViewModel = customerViewModel,
                    currentTab = args.tab,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.popBackStack() },
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.ChooseLocation,
            CustomerNewShipmentPage.ChooseDates -> {
                NewShipmentRouteScreen(
                    customerViewModel = customerViewModel,
                    currentTab = args.tab,
                    onBookNow = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.RecipientDetails)) },
                    onHomeClick    = { args.onHomeClick() },
                    onOrdersClick  = { args.onOrdersClick() },
                    onHistoryClick = { args.onHistoryClick() },
                    onChatClick    = { args.onChatClick() },
                    onProfileClick = { args.onProfileClick() }
                )
            }
            CustomerNewShipmentPage.RecipientDetails -> {
                NewShipmentRecipientScreen(
                    customerViewModel = customerViewModel,
                    currentTab     = args.tab,
                    onBack         = { navController.popBackStack() },
                    onNext = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck)) },
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.FindingTruck -> {
                FindingTruckScreen(
                    currentTab     = args.tab,
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.AvailableTrucks -> {
                // Removed available trucks screen logic
            }
            CustomerNewShipmentPage.TruckDetails -> {
                DriverFoundScreen(
                    currentTab       = args.tab,
                    onViewDetails    = { /* Navigate to driver profile */ },
                    onContactDriver  = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onHomeClick      = args.onHomeClick,
                    onOrdersClick    = args.onOrdersClick,
                    onHistoryClick   = args.onHistoryClick,
                    onChatClick      = args.onChatClick,
                    onProfileClick   = args.onProfileClick
                )
            }
        }
    }

    // ── BOOKINGS / HISTORY (TAB 3) ──
    composable<CustomerBookings> {
        val page = it.toRoute<CustomerBookings>().page
        val args = sharedArgs()
        when (page) {
            CustomerBookingPage.Active,
            CustomerBookingPage.Completed -> {
                YourBookingsScreen(
                    currentTab           = args.tab,
                    onHomeClick          = args.onHomeClick,
                    onOrdersClick        = args.onOrdersClick,
                    onHistoryClick       = args.onHistoryClick,
                    onChatClick          = args.onChatClick,
                    onProfileClick       = args.onProfileClick,
                    onDetailsClick       = { /* navController.navigate(CustomerBookings(CustomerBookingPage.Details)) */ },
                    onContactDriverClick = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onTrackClick         = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) },
                    onCancelClick        = { navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) },
                    onReviewClick        = { }
                )
            }
            CustomerBookingPage.Details -> {
                // Booking Details Screen placeholder
            }
            CustomerBookingPage.CancelBooking -> {
                CancelBookingScreen(
                    currentTab       = args.tab,
                    onBack           = { navController.popBackStack() },
                    onGetInContact   = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onConfirmCancel  = { navController.popBackStack() },
                    onHomeClick      = args.onHomeClick,
                    onOrdersClick    = args.onOrdersClick,
                    onHistoryClick   = args.onHistoryClick,
                    onChatClick      = args.onChatClick,
                    onProfileClick   = args.onProfileClick
                )
            }
        }
    }

    // ── CHAT / ACTIVITY (TAB 4) ──
    composable<CustomerChat> {
        val page = it.toRoute<CustomerChat>().page
        val args = sharedArgs()
        when (page) {
            CustomerChatPage.Main,
            CustomerChatPage.Notifications -> {
                CustomerActivityScreen(
                    currentTab     = args.tab,
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatNavClick = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = args.onProfileClick,
                    onChatClick    = { navController.navigate(CustomerChat(CustomerChatPage.Details)) }
                )
            }
            CustomerChatPage.Details -> {
                // Individual Chat Screen placeholder
            }
        }
    }

    // ── PROFILE (TAB 5) ──
    composable<CustomerProfile> {
        val page = it.toRoute<CustomerProfile>().page
        val args = sharedArgs()
        when (page) {
            CustomerProfilePage.Main -> {
                CustomerProfileScreen(
                    currentTab             = args.tab,
                    onHomeClick            = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick          = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick         = { navController.navigateTab(CustomerBookings()) },
                    onChatClick            = { navController.navigateTab(CustomerChat()) },
                    onProfileClick         = { navController.navigateTab(CustomerProfile()) },
                    onBookingDetailsClick  = { navController.navigate(CustomerBookings()) },
                    onYourInformationClick = { /* navController.navigate(CustomerProfile(CustomerProfilePage.YourInformation)) */ },
                    onLogoutClick          = { authViewModel.logout() }
                )
            }
            CustomerProfilePage.YourInformation,
            CustomerProfilePage.EditInformation -> {
                // Placeholder
            }
        }
    }
}
