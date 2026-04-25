package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.datamodels.BookingStatus
import com.example.cargoconnectapp.ui.models.datamodels.CustomerBooking
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*
import org.osmdroid.util.GeoPoint

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
            customerViewModel.fetchCustomerBookings(session.roleBasedId)
        }

        when (page) {
            CustomerDashboardPage.Summary,
            CustomerDashboardPage.Details -> {
                // Map real bookings for the dashboard list
                val activeBookings = customerViewModel.customerBookings
                    .filter { it.status.lowercase() !in listOf("completed", "delivered", "closed", "pending") }
                    .map { dto ->
                        CustomerBooking(
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" } ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = when(dto.status.lowercase()) {
                                "intransit", "moving" -> BookingStatus.InTransit
                                else -> BookingStatus.Confirmed
                            }
                        )
                    }

                CustomerDashboardScreen(
                    customerViewModel    = customerViewModel,
                    bookings             = activeBookings,
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
                    onTimeout      = { navController.popBackStack() }, 
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
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.RecipientDetails -> {
                NewShipmentRecipientScreen(
                    customerViewModel = customerViewModel,
                    currentTab     = args.tab,
                    onBack         = { navController.popBackStack() },
                    onNext = { 
                        Log.d("CustomerGraph", "Finalizing shipment...")
                        customerViewModel.finalizeShipment(
                            onSuccess = {
                                Log.d("CustomerGraph", "Shipment finalized! Moving to finding truck.")
                                navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck))
                            },
                            onError = { error ->
                                Log.e("CustomerGraph", "Finalization failed: $error")
                            }
                        )
                    },
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
                    onTimeout      = { 
                        navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.AvailableTrucks)) {
                            popUpTo(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck)) { inclusive = true }
                        }
                    },
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.AvailableTrucks -> { 
                DriverFoundScreen(
                    availableTrucks  = customerViewModel.availableTrucks,
                    currentTab       = args.tab,
                    onSendRequest    = { truck -> /* Send request logic */ },
                    onShowDetails    = { truck -> 
                        customerViewModel.selectedTruck = truck
                        navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.TruckDetails))
                    },
                    onHomeClick      = args.onHomeClick,
                    onOrdersClick    = args.onOrdersClick,
                    onHistoryClick   = args.onHistoryClick,
                    onChatClick      = args.onChatClick,
                    onProfileClick   = args.onProfileClick
                )
            }
            CustomerNewShipmentPage.TruckDetails -> {
                customerViewModel.selectedTruck?.let { truck ->
                    DriverDetailsScreen(
                        truck            = truck,
                        routePoints      = listOf(
                            GeoPoint(truck.pickupLat, truck.pickupLong),
                            GeoPoint(truck.destLat, truck.destLong)
                        ),
                        onSendRequest    = { /* Send request logic */ },
                        onBack           = { navController.popBackStack() },
                        currentTab       = args.tab,
                        onHomeClick      = args.onHomeClick,
                        onOrdersClick    = args.onOrdersClick,
                        onHistoryClick   = args.onHistoryClick,
                        onChatClick      = args.onChatClick,
                        onProfileClick   = args.onProfileClick
                    )
                }
            }
        }
    }

    // ── BOOKINGS / HISTORY (TAB 3) ──
    composable<CustomerBookings> {
        val page = it.toRoute<CustomerBookings>().page
        val args = sharedArgs()
        
        LaunchedEffect(Unit) {
            customerViewModel.fetchCustomerBookings(session.roleBasedId)
            customerViewModel.fetchPendingShipments(session.roleBasedId)
        }

        when (page) {
            CustomerBookingPage.Active,
            CustomerBookingPage.Pending,
            CustomerBookingPage.Completed -> {
                
                val activeList = customerViewModel.customerBookings
                    .filter { it.status.lowercase() in listOf("confirmed", "intransit", "moving") }
                    .map { dto ->
                        CustomerBooking(
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" } ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = if (dto.status.lowercase() == "confirmed") BookingStatus.Confirmed else BookingStatus.InTransit
                        )
                    }

                val completedList = customerViewModel.customerBookings
                    .filter { it.status.lowercase() in listOf("completed", "delivered", "closed") }
                    .map { dto ->
                        CustomerBooking(
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" } ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = BookingStatus.Completed
                        )
                    }

                YourBookingsScreen(
                    currentTab           = args.tab,
                    onHomeClick          = args.onHomeClick,
                    onOrdersClick        = args.onOrdersClick,
                    onHistoryClick       = args.onHistoryClick,
                    onChatClick          = args.onChatClick,
                    onProfileClick       = args.onProfileClick,
                    activeBookings       = activeList,
                    pendingBookings      = customerViewModel.pendingShipments,
                    completedBookings    = completedList,
                    onDetailsClick       = { navController.navigate(CustomerBookings(CustomerBookingPage.Details)) },
                    onContactDriverClick = { navController.navigate(CustomerChat(CustomerChatPage.Details)) },
                    onTrackClick         = { navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) },
                    onCancelClick        = { navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) },
                    onReviewClick        = { },
                    onFindTruckClick     = { shipment ->
                        customerViewModel.searchAvailableTrucks(
                            shipmentId = shipment.shipmentId,
                            isStrict = false,
                            onSuccess = {
                                navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck))
                            },
                            onError = { }
                        )
                    }
                )
            }
            CustomerBookingPage.Details -> {
                BookingShipmentsDetailScreen(
                    currentTab     = args.tab,
                    onBack         = { navController.popBackStack() },
                    onHomeClick    = args.onHomeClick,
                    onOrdersClick  = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick    = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerBookingPage.CancelBooking -> {
                CancelBookingScreen(
                    currentTab       = args.tab,
                    onBack           = { navController.popBackStack() },
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
                ChatScreen(
                    onBack      = { navController.popBackStack() },
                    onCallClick = {}
                )
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
                    fullName               = session.userFullName,
                    profileImageUrl        = session.userProfileImageUrl,
                    currentTab             = args.tab,
                    onHomeClick            = { navController.navigateTab(CustomerDashboard()) },
                    onOrdersClick          = { navController.navigateTab(CustomerNewShipment()) },
                    onHistoryClick         = { navController.navigateTab(CustomerBookings()) },
                    onChatClick            = { navController.navigateTab(CustomerChat()) },
                    onProfileClick         = { navController.navigateTab(CustomerProfile()) },
                    onBookingDetailsClick  = { navController.navigate(CustomerBookings()) },
                    onYourInformationClick = { },
                    onLogoutClick          = { authViewModel.logout() }
                )
            }
            CustomerProfilePage.YourInformation,
            CustomerProfilePage.EditInformation -> { }
        }
    }
}
