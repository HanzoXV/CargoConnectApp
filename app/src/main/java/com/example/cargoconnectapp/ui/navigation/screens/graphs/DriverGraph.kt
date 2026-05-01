package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.datamodels.CreateRouteRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteCheckpointRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.RouteTarget
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*
import com.example.cargoconnectapp.ui.models.map.getAddressFromGeoPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.navigation.NavGraphBuilder
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.driverGraph(
    navController: NavHostController,
    session: UserSessionViewModel,
    authViewModel: AuthViewModel,
    driverViewModel: DriverViewModel,
    customerViewModel: CustomerViewModel
) {
    fun NavHostController.navigateTab(destination: DriverDestination) {
        session.setDriverTab(destination)
        navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun formatRouteLabel(route: RouteData?): String? {
        if (route == null) return null
        val start = route.startPoint ?: route.checkpoints?.firstOrNull()?.name ?: "N/A"
        val end = route.endPoint ?: route.checkpoints?.lastOrNull()?.name ?: "N/A"
        return "$start to $end"
    }

    // ── HOME ──
    composable<DriverDashboard> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverDashboard>().page
        
        LaunchedEffect(Unit) { driverViewModel.fetchActiveRoute(session.roleBasedId) }

        when (page) {
            DriverDashboardPage.TripInfo,
            DriverDashboardPage.TruckInfo -> {
                DriverDashboardScreen(
                    currentTab = session.currentDriverTab,
                    onHomeClick = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onDelayClick = { index ->
                        val checkpoint = driverViewModel.activeRoute?.checkpoints?.getOrNull(index)
                        if (checkpoint != null) {
                            driverViewModel.selectedCheckpointId = checkpoint.checkpointId ?: 0
                            navController.navigate(DriverDashboard(DriverDashboardPage.AddDelay))
                        }
                    },
                    onReachedClick = { index ->
                        val checkpoint = driverViewModel.activeRoute?.checkpoints?.getOrNull(index)
                        if (checkpoint != null) {
                            if (index == 0) {
                                // First checkpoint - show pickups before starting
                                driverViewModel.fetchShipmentsAtCheckpoint(
                                    checkpoint.checkpointId ?: 0,
                                    session.roleBasedId
                                ) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery))
                                }
                            } else {
                                driverViewModel.fetchShipmentsAtCheckpoint(
                                    checkpoint.checkpointId ?: 0, session.roleBasedId
                                ) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery))
                                }
                            }
                        }
                    },
                    driverViewModel = driverViewModel
                )
            }

            DriverDashboardPage.ConfirmDelivery -> {
                val data = driverViewModel.checkpointShipments

                // Prevent LaunchedEffect from firing during navigation teardown
                val isNavigating = remember { mutableStateOf(false) }

                if (data != null) {
                    CheckpointShipmentsScreen(
                        data = data,
                        isLoading = driverViewModel.isLoading,
                        isStartCheckpoint = driverViewModel.isStartCheckpoint,
                        onPickup = { bookingId ->
                            driverViewModel.pickupBooking(
                                bookingId,
                                session.roleBasedId,
                                data.checkpoint_id
                            )
                        },
                        onDeliver = { bookingId ->
                            driverViewModel.deliverBooking(
                                bookingId,
                                session.roleBasedId,
                                data.checkpoint_id
                            )
                        },
                        onConfirm = {
                            isNavigating.value = true  // Lock before async work
                            if (driverViewModel.isStartCheckpoint) {
                                driverViewModel.startTrip(data.checkpoint_id, session.roleBasedId)
                                navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                                    popUpTo(DriverDashboard(DriverDashboardPage.ConfirmDelivery)) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                driverViewModel.confirmCheckpoint(
                                    data.checkpoint_id,
                                    session.roleBasedId
                                ) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                                        popUpTo(DriverDashboard(DriverDashboardPage.ConfirmDelivery)) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        },
                        onBack = { navController.popBackStack() },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                } else {
                    if (!isNavigating.value) {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
            }
            DriverDashboardPage.AddDelay -> {
                val checkpoint = driverViewModel.activeRoute?.checkpoints
                    ?.firstOrNull { it.checkpointId == driverViewModel.selectedCheckpointId }

                DelayReportScreen(
                    viewModel = driverViewModel,
                    checkpointId = driverViewModel.selectedCheckpointId,
                    checkpointName = checkpoint?.name ?: "Unknown Stop",
                    onConfirm = {
                        driverViewModel.fetchActiveRoute(session.roleBasedId)
                        navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                            popUpTo(DriverDashboard(DriverDashboardPage.AddDelay)) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // ── ORDERS ──
    composable<DriverShipmentRequest> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverShipmentRequest>().page
        
        LaunchedEffect(Unit) {
            driverViewModel.fetchDriverRequests(session.roleBasedId)
        }

        when (page) {
            ShipmentRequestPage.Main -> {
                val requests = driverViewModel.driverRequests?.shipmentData?.map { shipment ->
                    val reqData =
                        driverViewModel.driverRequests?.requestsData?.find { it.shipment_id == shipment.shipment_id }
                    ShipmentRequest(
                        requestId = reqData?.request_id ?: 0,
                        title = "Shipment #${shipment.shipment_id}",
                        route = "${shipment.pickup_address} to ${shipment.delivery_address}",
                        weight = "~${shipment.total_weight} KGs",
                        pickup = "Pickup: ${shipment.pickup_address}",
                        amount = "Fare: ${reqData?.fare ?: "TBD"} PKR",
                        isShared = false,
                        totalWeight = shipment.total_weight,
                        packageCount = shipment.package_count,
                        pickupAddress = shipment.pickup_address,
                        deliveryAddress = shipment.delivery_address,
                        packages = shipment.packages
                    )
                } ?: emptyList()

                ShipmentRequestsScreen(
                    requests = requests,
                    currentTab = session.currentDriverTab,
                    onHomeClick = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onDetailsClick = {
                        driverViewModel.selectedRequest = it
                        navController.navigate(DriverShipmentRequest(ShipmentRequestPage.Details))
                    },
                    onAccept = {
                        driverViewModel.acceptShipmentRequest(
                            it.requestId,
                            session.roleBasedId
                        )
                    },
                    onDecline = {
                        driverViewModel.declineShipmentRequest(
                            it.requestId,
                            session.roleBasedId
                        )
                    },
                    successMessage = driverViewModel.successMessage,
                    errorMessage = driverViewModel.errorMessage,
                    onClearMessages = { driverViewModel.clearError() }
                )
            }
            ShipmentRequestPage.Details -> {
                val selected = driverViewModel.selectedRequest
                if (selected != null) {
                    ShipmentRequestDetailScreen(
                        request = selected,
                        onBack = { navController.popBackStack() },
                        onAccept = {
                            driverViewModel.acceptShipmentRequest(selected.requestId, session.roleBasedId) {
                                navController.popBackStack()
                            }
                        },
                        onDecline = {
                            driverViewModel.declineShipmentRequest(selected.requestId, session.roleBasedId) {
                                navController.popBackStack()
                            }
                        },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick    = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }

    // ── HISTORY / DELIVERIES ──
    composable<DriverDeliveries> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverDeliveries>().page
        
        LaunchedEffect(Unit) {
            driverViewModel.fetchAllBookings(session.roleBasedId)
        }

        when (page) {
            DeliveryPage.Main -> {
                CustomersScreen(
                    pendingBookings = driverViewModel.pendingBookings,
                    inTransitBookings = driverViewModel.inTransitBookings,
                    completedBookings = driverViewModel.completedBookings,
                    canceledBookings = driverViewModel.canceledBookings,
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onDetailsClick = { 
                        driverViewModel.selectedBooking = it
                        navController.navigate(DriverDeliveries(DeliveryPage.Details)) 
                    }
                )
            }
            DeliveryPage.Details -> {
                val selected = driverViewModel.selectedBooking
                if (selected != null) {
                    DriverBookingDetailScreen(
                        booking = selected,
                        onBack         = { navController.popBackStack() },
                        onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick    = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }

    // ── CHAT ──
    composable<DriverChat> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverChat>().page
        when (page) {
            ChatPage.Main,
            ChatPage.Notifications -> {
                LaunchedEffect(Unit) {
                    driverViewModel.fetchNotifications(session.userId)
                }
                ActivityScreen(
                    notifications = driverViewModel.notifications,
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatNavClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onChatClick    = { navController.navigate(DriverChat(ChatPage.Details)) }
                )
            }
            ChatPage.Details -> {
                ChatScreen(onBack = { navController.popBackStack() })
            }
        }
    }

    // ── PROFILE ──
    composable<DriverProfileScreen> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverProfileScreen>().page
        val context = LocalContext.current

        when (page) {
            DriverProfilePage.Main -> {
                ProfileScreen(
                    fullName               = session.userFullName,
                    currentTab             = session.currentDriverTab,
                    onHomeClick            = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick          = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick         = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick            = { navController.navigateTab(DriverChat()) },
                    onProfileClick         = { navController.navigateTab(DriverProfileScreen()) },
                    onYourInformationClick = { navController.navigate(DriverProfileScreen(DriverProfilePage.YourInformation)) },
                    onActivateRouteClick   = { navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) },
                    onLogoutClick          = { authViewModel.logout()},
                    session = session
                )
            }
            DriverProfilePage.YourInformation -> {
                YourInformationScreen(
                    fullName       = session.userFullName,
                    phone          = session.userContact,
                    email          = session.userEmail,
                    cnic           = "Not specified", 
                    licenseNo      = session.userLicenseNo,
                    currentTab     = session.currentDriverTab,
                    onBack         = { navController.popBackStack() },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
            DriverProfilePage.ActivateRoute -> {
                LaunchedEffect(Unit) {
                    driverViewModel.fetchActiveRoute(session.roleBasedId)
                    driverViewModel.fetchNextRoute(session.roleBasedId)
                }
                ActivateRouteScreen(
                    activeRoute    = formatRouteLabel(driverViewModel.activeRoute),
                    activeVia      = driverViewModel.activeRoute?.checkpoints?.getOrNull(1)?.name?.let { viaName -> "via $viaName" },
                    nextRoute      = formatRouteLabel(driverViewModel.nextRoute),
                    currentTab     = session.currentDriverTab,
                    onTapToChange  = { 
                        driverViewModel.routeActivationTarget = RouteTarget.ACTIVE
                        navController.navigate(DriverProfileScreen(DriverProfilePage.SelectRoute)) 
                    },
                    onTapToChangeUpcoming = { 
                        driverViewModel.routeActivationTarget = RouteTarget.NEXT
                        navController.navigate(DriverProfileScreen(DriverProfilePage.SelectRoute)) 
                    },
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
            DriverProfilePage.SelectRoute -> {
                LaunchedEffect(Unit) { driverViewModel.fetchAllRoutes(session.roleBasedId) }
                SelectRouteScreen(
                    savedRoutes      = driverViewModel.pastRoutes.map { r ->
                        val label = formatRouteLabel(r) ?: "Unknown"
                        val via = r.checkpoints?.getOrNull(1)?.name?.let { viaName -> "via $viaName" }
                        label to via
                    },
                    currentTab       = session.currentDriverTab,
                    onRouteSelected  = { index ->
                        val selected = driverViewModel.pastRoutes.getOrNull(index)
                        if (selected != null) {
                            driverViewModel.clearPlannedRoute()
                            driverViewModel.selectedTemplateRoute = selected
                            navController.navigate(DriverProfileScreen(DriverProfilePage.EstimatedDelivery))
                        }
                    },
                    onCreateNewRoute = { 
                        driverViewModel.clearPlannedRoute()
                        navController.navigate(DriverProfileScreen(DriverProfilePage.AddNewRoute)) 
                    },
                    onHomeClick      = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick    = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick   = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick      = { navController.navigateTab(DriverChat()) },
                    onProfileClick   = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
            DriverProfilePage.AddNewRoute -> {
                CreateRouteStep1(
                    routePoints    = driverViewModel.plannedPoints,
                    onNext         = { navController.navigate(DriverProfileScreen(DriverProfilePage.EstimatedDelivery)) },
                    onBack         = { navController.popBackStack() },
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
            DriverProfilePage.EstimatedDelivery -> {
                val activeEnd = driverViewModel.activeRoute?.arrivalDate?.let { 
                    try {
                        if (it.contains("T")) LocalDateTime.parse(it).toLocalDate()
                        else LocalDate.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                // minDate is either active route's end or today
                val minDate = if (driverViewModel.activeRoute != null && driverViewModel.routeActivationTarget == RouteTarget.NEXT) {
                    activeEnd?.plusDays(1) ?: LocalDate.now()
                } else {
                    LocalDate.now()
                }

                CreateRouteStep2(
                    minDate = minDate,
                    onNext = { dep, arr ->
                        driverViewModel.plannedDepartureDate = dep
                        driverViewModel.plannedArrivalDate = arr
                        navController.navigate(DriverProfileScreen(DriverProfilePage.EditInformation))
                    },
                    onBack         = { navController.popBackStack() },
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
            DriverProfilePage.EditInformation -> {
                CreateRouteStep3(
                    isLoading = driverViewModel.isLoading,
                    viewModel = driverViewModel,
                    onSave = { fare ->
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val finalPoints = driverViewModel.previewedPoints

                        // Safety check: Don't save if the preview hasn't finished loading yet
                        if (finalPoints.isNotEmpty()) {
                            val request = CreateRouteRequest(
                                driverId      = session.roleBasedId,
                                departureDate = driverViewModel.plannedDepartureDate?.format(formatter) ?: "",
                                arrivalDate   = driverViewModel.plannedArrivalDate?.format(formatter) ?: "",
                                baseFare      = fare,
                                activateNow   = false,
                                isFragile     = driverViewModel.plannedIsFragile,
                                isLiquid      = driverViewModel.plannedIsLiquid,
                                isFlammable   = driverViewModel.plannedIsFlammable,
                                keepUpright   = driverViewModel.plannedKeepUpright,
                                shipmentType  = driverViewModel.plannedShipmentType,
                                points        = finalPoints // This list has the adjusted timestamps!
                            )

                            driverViewModel.saveRoute(request, session.roleBasedId) {
                                navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                    popUpTo(DriverProfileScreen(DriverProfilePage.Main))
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            Log.e("DriverGraph", "Save blocked: previewedPoints not ready.")
                        }
                    },
                    onBack         = { navController.popBackStack() },
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
        }
    }
}
