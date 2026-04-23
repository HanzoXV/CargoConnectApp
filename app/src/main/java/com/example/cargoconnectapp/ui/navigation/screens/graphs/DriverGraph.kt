package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.cargoconnectapp.ui.models.datamodels.CreateRouteRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteCheckpointRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*
import com.example.cargoconnectapp.ui.models.map.getAddressFromGeoPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        LaunchedEffect(Unit) { driverViewModel.fetchActiveRoute(session.roleBasedId) }

        DriverDashboardScreen(
            currentTab     = session.currentDriverTab,
            onHomeClick    = { navController.navigateTab(DriverDashboard()) },
            onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
            onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
            onChatClick    = { navController.navigateTab(DriverChat()) },
            onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
            onDelayClick   = { navController.navigate(DriverDashboard(DriverDashboardPage.AddDelay)) },
            onReachedClick = { navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery)) },
            driverViewModel = driverViewModel
        )
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
                    ShipmentRequest(
                        title = "Shipment #${shipment.shipment_id}",
                        route = "${shipment.pickup_address} to ${shipment.delivery_address}",
                        weight = "~${shipment.total_weight} KGs",
                        pickup = "Pickup: ${shipment.pickup_address}",
                        amount = "Fare: TBD", // Amount not in shipmentData directly
                        isShared = false
                    )
                } ?: emptyList()

                ShipmentRequestsScreen(
                    requests = requests,
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
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
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
        }
    }

    // ── HISTORY ──
    composable<DriverDeliveries> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverDeliveries>().page
        when (page) {
            DeliveryPage.Main -> {
                CustomersScreen(
                    currentTab     = session.currentDriverTab,
                    onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick    = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
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
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                )
            }
        }
    }

    // ── CHAT ──
    composable<DriverChat> { backStackEntry ->
        val page = backStackEntry.toRoute<DriverChat>().page
        when (page) {
            ChatPage.Main,
            ChatPage.Notifications -> {
                ActivityScreen(
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
                    profileImageUrl        = session.userProfileImageUrl,
                    currentTab             = session.currentDriverTab,
                    onHomeClick            = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick          = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick         = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick            = { navController.navigateTab(DriverChat()) },
                    onProfileClick         = { navController.navigateTab(DriverProfileScreen()) },
                    onYourInformationClick = { navController.navigate(DriverProfileScreen(DriverProfilePage.YourInformation)) },
                    onActivateRouteClick   = { navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) },
                    onLogoutClick          = { authViewModel.logout() }
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
                    onTapToChange  = { navController.navigate(DriverProfileScreen(DriverProfilePage.SelectRoute)) },
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
                    onCreateNewRoute = { navController.navigate(DriverProfileScreen(DriverProfilePage.AddNewRoute)) },
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
                        LocalDate.parse(it)
                    } catch (e: Exception) {
                        try {
                            LocalDateTime.parse(it).toLocalDate()
                        } catch (e2: Exception) {
                            null
                        }
                    }
                }
                val minDate = if (driverViewModel.activeRoute != null && driverViewModel.selectedTemplateRoute != null) {
                    activeEnd
                } else null

                CreateRouteStep2(
                    minDate        = minDate,
                    onNext         = { dep, arr ->
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
                    isLoading      = driverViewModel.isLoading,
                    onSave         = { fare ->
                        val template = driverViewModel.selectedTemplateRoute
                        if (template != null) {
                            val request = CreateRouteRequest(
                                driverId      = session.roleBasedId,
                                departureDate = driverViewModel.plannedDepartureDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                                arrivalDate   = driverViewModel.plannedArrivalDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                                baseFare      = fare,
                                activateNow   = driverViewModel.activeRoute == null,
                                points        = template.checkpoints?.map { cp ->
                                    RouteCheckpointRequest(
                                        name = cp.name ?: "",
                                        latitude = cp.latitude ?: 0.0,
                                        longitude = cp.longitude ?: 0.0,
                                        sequenceNo = cp.sequenceNo ?: 0
                                    )
                                } ?: emptyList()
                            )
                            driverViewModel.saveRoute(request, session.roleBasedId) {
                                navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                    popUpTo(DriverProfileScreen(DriverProfilePage.Main))
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            val request = CreateRouteRequest(
                                driverId      = session.roleBasedId,
                                departureDate = driverViewModel.plannedDepartureDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                                arrivalDate   = driverViewModel.plannedArrivalDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                                baseFare      = fare,
                                activateNow   = driverViewModel.activeRoute == null,
                                points        = driverViewModel.plannedPoints.mapIndexed { index, gp ->
                                    RouteCheckpointRequest(
                                        name       = getAddressFromGeoPoint(context, gp),
                                        latitude   = gp.latitude,
                                        longitude  = gp.longitude,
                                        sequenceNo = index + 1
                                    )
                                }
                            )
                            driverViewModel.saveRoute(request, session.roleBasedId) {
                                navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                    popUpTo(DriverProfileScreen(DriverProfilePage.Main))
                                    launchSingleTop = true
                                }
                            }
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
