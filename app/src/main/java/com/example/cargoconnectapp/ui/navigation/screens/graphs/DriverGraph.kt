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
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.RouteTarget
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import com.example.cargoconnectapp.ui.navigation.screens.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.navigation.NavGraphBuilder
import com.example.cargoconnectapp.ui.models.datamodels.RouteCheckpointUpdate
import com.example.cargoconnectapp.ui.models.datamodels.UpdateRouteRequest
import com.example.cargoconnectapp.ui.models.services.startTruckAnimationService
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel
import org.osmdroid.util.GeoPoint

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
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
    composable<DriverDashboard>  { backStackEntry ->

        val context = LocalContext.current
        val page = backStackEntry.toRoute<DriverDashboard>().page

        LaunchedEffect(Unit) { driverViewModel.fetchActiveRoute(session.roleBasedId) }


        when (page) {
            DriverDashboardPage.TripInfo,
            DriverDashboardPage.TruckInfo -> {
                DriverDashboardScreen(
                    driverId = session.roleBasedId,
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
                                driverViewModel.isStartCheckpoint = true
                                driverViewModel.fetchShipmentsAtCheckpoint(
                                    checkpoint.checkpointId ?: 0,
                                    session.roleBasedId
                                ) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery))
                                }
                            } else {
                                driverViewModel.isStartCheckpoint = false

                                driverViewModel.fetchShipmentsAtCheckpoint(
                                    checkpoint.checkpointId ?: 0, session.roleBasedId
                                ) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.ConfirmDelivery))
                                }
                            }
                        }
                    },
                    driverViewModel = driverViewModel,
                    onDetailsClick = { index ->
                        val checkpoint = driverViewModel.activeRoute?.checkpoints?.getOrNull(index)
                        if(checkpoint != null) {
                            driverViewModel.routeShipmentsAtCheckpoint(
                                checkpoint.checkpointId ?: 0,
                                session.roleBasedId
                            ) {
                                navController.navigate(DriverDashboard(DriverDashboardPage.RouteDetails))
                            }
                        }

                    }
                )
            }
            DriverDashboardPage.RouteDetails -> {
                val data = driverViewModel.routeCustomerDetail

                if (data != null) {
                    CustomerShipmentsDetailScreen(
                        data = data,
                        onBack = { navController.popBackStack() },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    )
                }
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
                            ) {
                                driverViewModel.checkAndTriggerReview(bookingId, session.userId)
                            }
                        },
                        showReviewDialog = driverViewModel.showReviewDialog,
                        onReviewSubmit = { rating, comment ->
                            val tripId = driverViewModel.pendingReviewTripId ?: return@CheckpointShipmentsScreen

                            driverViewModel.submitReview(
                                fromUserId = session.userId,
                                tripId = tripId,
                                rating = rating,
                                comments = comment
                            )
                        },
                        onReviewDismiss = { driverViewModel.dismissReview() },
                        onConfirm = {
                            if (driverViewModel.isStartCheckpoint) {
                                // Start of trip — no animation, just API call
                                driverViewModel.startTrip(data.checkpoint_id, session.roleBasedId) {
                                    navController.navigate(DriverDashboard(DriverDashboardPage.TripInfo)) {
                                        popUpTo(DriverDashboard(DriverDashboardPage.ConfirmDelivery)) {
                                            inclusive = true
                                        }
                                    }
                                }
                            } else {
                                val checkpoints = driverViewModel.activeRoute?.checkpoints?.sortedBy { it.sequenceNo }
                                val confirmedIndex = checkpoints?.indexOfFirst { it.checkpointId == data.checkpoint_id } ?: -1

                                if (confirmedIndex > 0) {
                                    val fromGeo = checkpoints!![confirmedIndex - 1].let {
                                        GeoPoint(it.latitude ?: 0.0, it.longitude ?: 0.0)
                                    }
                                    val toGeo = checkpoints[confirmedIndex].let {
                                        GeoPoint(it.latitude ?: 0.0, it.longitude ?: 0.0)
                                    }

                                    // Set up service params via ViewModel
                                    driverViewModel.pendingAnimationFrom = fromGeo
                                    driverViewModel.pendingAnimationTo   = toGeo
                                    driverViewModel.pendingCheckpointId  = data.checkpoint_id
                                    driverViewModel.pendingDriverId      = session.roleBasedId
                                    driverViewModel.pendingConfirmAction = null  // service handles completion now

                                    // Start the foreground service
                                    context.startTruckAnimationService()

                                    // Navigate back to map so user sees the animation
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
                            popUpTo(DriverDashboard(DriverDashboardPage.AddDelay)) {
                                inclusive = true
                            }
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
                            driverViewModel.acceptShipmentRequest(
                                selected.requestId,
                                session.roleBasedId
                            ) {
                                navController.popBackStack()
                            }
                        },
                        onDecline = {
                            driverViewModel.declineShipmentRequest(
                                selected.requestId,
                                session.roleBasedId
                            ) {
                                navController.popBackStack()
                            }
                        },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }

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
                    currentTab = session.currentDriverTab,
                    onHomeClick = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onDetailsClick = {
                        driverViewModel.selectedBooking = it
                        navController.navigate(DriverDeliveries(DeliveryPage.Details))
                    },
                    onContactClick = { booking ->
                        driverViewModel.getOrCreateThread(
                            driverUserId = session.userId,
                            customerBooking = booking,
                            onSuccess = { threadId, otherName ->
                                driverViewModel.selectedThreadId = threadId
                                driverViewModel.selectedThreadName = otherName
                                driverViewModel.fetchChatHistory(threadId, session.userId)
                                navController.navigate(DriverChat(ChatPage.Details))
                            }
                        )
                    }
                )
            }

            DeliveryPage.Details -> {
                val selected = driverViewModel.selectedBooking
                if (selected != null) {
                    DriverBookingDetailScreen(
                        booking = selected,
                        onBack = { navController.popBackStack() },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
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
                    driverViewModel.fetchChatList(session.userId)  // ← add this
                }
                ActivityScreen(
                    chats = driverViewModel.chatThreads.map { it.toChatContact() },  // ← map here
                    notifications = driverViewModel.notifications,
                    currentTab = session.currentDriverTab,
                    onHomeClick = { navController.navigateTab(DriverDashboard()) },
                    onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                    onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                    onChatNavClick = { navController.navigateTab(DriverChat()) },
                    onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                    onChatClick = { contact ->
                        val thread =
                            driverViewModel.chatThreads.find { it.otherName == contact.name }
                        if (thread != null) {
                            driverViewModel.selectedThreadId = thread.threadId
                            driverViewModel.selectedThreadName = thread.otherName
                            driverViewModel.fetchChatHistory(thread.threadId, session.userId)
                            navController.navigate(DriverChat(ChatPage.Details))
                        }
                    }
                )
            }

            ChatPage.Details -> {
                val threadId = driverViewModel.selectedThreadId
                if (threadId != null) {
                    ChatScreen(
                        contactName = driverViewModel.selectedThreadName,
                        contactStatus = "CargoConnect User",
                        messages = driverViewModel.chatMessages.map {
                            ChatMessage(
                                content = it.messageText,
                                isSentByMe = it.isMine,
                                timestamp = it.sentAt
                            )
                        },
                        onBack = { navController.popBackStack() },
                        onCallClick = {},
                        onSendMessage = { text ->
                            driverViewModel.sendMessage(threadId, session.userId, text)
                        }
                    )
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
        // ── PROFILE ──
        composable<DriverProfileScreen> { backStackEntry ->
            val page = backStackEntry.toRoute<DriverProfileScreen>().page
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                driverViewModel.fetchReviews(session.userId)
                driverViewModel.fetchAverageRating(session.userId)
            }

            when (page) {
                DriverProfilePage.Main -> {
                    ProfileScreen(
                        rating = driverViewModel.averageRating,
                        numberOfReviews = driverViewModel.reviews.size,
                        fullName = session.userFullName,
                        currentTab = session.currentDriverTab,
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                        onReviewsClick = { navController.navigate(DriverProfileScreen(DriverProfilePage.AllReviews)) },
                        onYourInformationClick = {
                            navController.navigate(
                                DriverProfileScreen(
                                    DriverProfilePage.YourInformation
                                )
                            )
                        },
                        onActivateRouteClick = {
                            navController.navigate(
                                DriverProfileScreen(
                                    DriverProfilePage.ActivateRoute
                                )
                            )
                        },
                        onLogoutClick = { authViewModel.logout()
                            driverViewModel.clearAllData()

                            // 2. Clear navigation stack and move to Login
                            navController.navigate(Login) {
                                // Pop up to the very beginning of the navigation host
                                popUpTo(0) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                                        },
                        session = session
                    )
                }

                DriverProfilePage.YourInformation -> {
                    YourInformationScreen(
                        fullName = session.userFullName,
                        phone = session.userContact,
                        email = session.userEmail,
                        cnic = session.userCNIC,
                        address = session.userStreetAddress,
                        city = session.userCity,
                        licenseNo = session.userLicenseNo,
                        currentTab = session.currentDriverTab,
                        onBack = { navController.popBackStack() },
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                }

                DriverProfilePage.ActivateRoute -> {
                    LaunchedEffect(Unit) {
                        driverViewModel.fetchActiveRoute(session.roleBasedId)
                        driverViewModel.fetchNextRoute(session.roleBasedId)
                        driverViewModel.fetchAllRoutes(session.roleBasedId)
                    }

                    Log.e("ActivateRouteScreen", "ALMOST. ACTIVE: ${driverViewModel.activeRoute?.checkpoints?.getOrNull(1)?.name} - NEXT: ${driverViewModel.nextRoute?.checkpoints?.getOrNull(1)?.name} ")

                    val firstReached = driverViewModel.activeRoute?.checkpoints?.getOrNull(0)?.reached == true

                    val canSetActive = !firstReached

                    ActivateRouteScreen(
                        activeRouteLabel = formatRouteLabel(driverViewModel.activeRoute),
                        activeVia = driverViewModel.activeRoute?.checkpoints?.getOrNull(1)?.name
                            ?.let { "via $it" },
                        nextRouteLabel = formatRouteLabel(driverViewModel.nextRoute),
                        nextVia = driverViewModel.nextRoute?.checkpoints?.getOrNull(1)?.name
                            ?.let { "via $it" },
                        pastRoutes = driverViewModel.pastRoutes.take(5),
                        isLoading = driverViewModel.isLoading,
                        currentTab = session.currentDriverTab,

                        onCreateNew = {
                            // Brand-new route: clear everything, go straight to map
                            driverViewModel.clearPlannedRoute()
                            driverViewModel.routeActivationTarget = RouteTarget.ACTIVE
                            navController.navigate(DriverProfileScreen(DriverProfilePage.AddNewRoute))
                        },

                        onMakeActive = { route ->
                            driverViewModel.clearPlannedRoute()
                            driverViewModel.routeActivationTarget = RouteTarget.ACTIVE
                            driverViewModel.selectedTemplateRoute = route
                            driverViewModel.templateRouteForUpdate = route
                            navController.navigate(DriverProfileScreen(DriverProfilePage.EstimatedDelivery))
                        },

                        onMakeNext = { route ->
                            // Re-use past route as NEXT: skip map, go to dates
                            driverViewModel.clearPlannedRoute()
                            driverViewModel.routeActivationTarget = RouteTarget.NEXT
                            driverViewModel.selectedTemplateRoute = route
                            driverViewModel.templateRouteForUpdate = route
                            navController.navigate(DriverProfileScreen(DriverProfilePage.EstimatedDelivery))
                        },

                        onHomeClick    = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick  = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick    = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) },
                        driverViewModel = driverViewModel,
                        canSetActive = canSetActive
                    )
                }

                DriverProfilePage.SelectRoute -> {
                    LaunchedEffect(Unit) { driverViewModel.fetchAllRoutes(session.roleBasedId) }
                    SelectRouteScreen(
                        savedRoutes = driverViewModel.pastRoutes.map { r ->
                            val label = formatRouteLabel(r) ?: "Unknown"
                            val via =
                                r.checkpoints?.getOrNull(1)?.name?.let { viaName -> "via $viaName" }
                            label to via
                        },
                        currentTab = session.currentDriverTab,
                        onRouteSelected = { index ->
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
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                }

                DriverProfilePage.AddNewRoute -> {
                    CreateRouteStep1(
                        routePoints = driverViewModel.plannedPoints,
                        onNext = { navController.navigate(DriverProfileScreen(DriverProfilePage.EstimatedDelivery)) },
                        onBack = { navController.popBackStack() },
                        currentTab = session.currentDriverTab,
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                }

                DriverProfilePage.EstimatedDelivery -> {
                    LaunchedEffect(driverViewModel.templateRouteForUpdate) {
                        val t = driverViewModel.templateRouteForUpdate
                        if (t != null) {
                            driverViewModel.plannedIsFragile    = t.isFragile    ?: false
                            driverViewModel.plannedIsLiquid     = t.isLiquid     ?: false
                            driverViewModel.plannedIsFlammable  = t.isFlammable  ?: false
                            driverViewModel.plannedKeepUpright  = t.keepUpright  ?: false
                            driverViewModel.plannedShipmentType = t.shipmentType ?: "Shared"
                        }
                    }

                    val activeEnd = driverViewModel.activeRoute?.arrivalDate?.let {
                        try {
                            if (it.contains("T")) LocalDateTime.parse(it).toLocalDate()
                            else LocalDate.parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // minDate is either active route's end or today
                    val minDate =
                        if (driverViewModel.activeRoute != null && driverViewModel.routeActivationTarget == RouteTarget.NEXT) {
                            activeEnd?.plusDays(1) ?: LocalDate.now()
                        } else {
                            LocalDate.now()
                        }

                    CreateRouteStep2(
                        minDate = minDate,
                        onNext = { dep, arr ->
                            driverViewModel.plannedDepartureDate = dep
                            driverViewModel.plannedArrivalDate = arr
                            driverViewModel.updatePreviewedPoints(emptyList())
                            navController.navigate(DriverProfileScreen(DriverProfilePage.EditInformation))
                        },
                        onBack = { navController.popBackStack() },
                        currentTab = session.currentDriverTab,
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                }

                DriverProfilePage.EditInformation -> {
                    val route = driverViewModel.selectedTemplateRoute
                    LaunchedEffect(route?.routeId) {
                        if (route != null && driverViewModel.plannedPoints.isEmpty()) {
                            driverViewModel.reFillPlannedPoints()
                        }
                    }
                    CreateRouteStep3(
                        isLoading = driverViewModel.isLoading,
                        viewModel = driverViewModel,
                        onSave = { fare ->
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                            val finalPoints = driverViewModel.previewedPoints

                            if (finalPoints.isEmpty()) {
                                Log.e("DriverGraph", "Save blocked: previewedPoints not ready.")
                                return@CreateRouteStep3
                            }

                            val templateRoute = driverViewModel.templateRouteForUpdate

                            if (templateRoute != null) {
                                // ── UPDATE existing route ─────────────────────────────────


                                val updateRequest = UpdateRouteRequest(
                                    routeId = templateRoute.routeId ?: -1,
                                    driverId = session.roleBasedId,
                                    departureDate = driverViewModel.plannedDepartureDate
                                        ?.format(formatter) ?: "",
                                    arrivalDate = driverViewModel.plannedArrivalDate
                                        ?.format(formatter) ?: "",
                                    baseFare = fare,
                                    isFragile = driverViewModel.plannedIsFragile,
                                    isLiquid = driverViewModel.plannedIsLiquid,
                                    isFlammable = driverViewModel.plannedIsFlammable,
                                    keepUpright = driverViewModel.plannedKeepUpright,
                                    shipmentType = driverViewModel.plannedShipmentType,
                                    points = finalPoints.map { cp ->
                                        RouteCheckpointUpdate(
                                            sequenceNo = cp.sequenceNo,
                                            estimatedArrival = cp.estimatedArrival ?: ""
                                        )
                                    }
                                )
                                val points = finalPoints.map { cp ->
                                    RouteCheckpointUpdate(
                                        sequenceNo = cp.sequenceNo,
                                        estimatedArrival = cp.estimatedArrival ?: ""
                                    )
                                }
                                Log.e("DriverGraph", "Sending Points [${points}]")
                                driverViewModel.updateRoute(updateRequest, session.roleBasedId) {
                                    navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                        popUpTo(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                // ── CREATE brand-new route (existing logic) ───────────────
                                val request = CreateRouteRequest(
                                    driverId      = session.roleBasedId,
                                    departureDate = driverViewModel.plannedDepartureDate
                                        ?.format(formatter) ?: "",
                                    arrivalDate   = driverViewModel.plannedArrivalDate
                                        ?.format(formatter) ?: "",
                                    baseFare      = fare,
                                    activateNow   = (driverViewModel.routeActivationTarget == RouteTarget.ACTIVE),
                                    isFragile     = driverViewModel.plannedIsFragile,
                                    isLiquid      = driverViewModel.plannedIsLiquid,
                                    isFlammable   = driverViewModel.plannedIsFlammable,
                                    keepUpright   = driverViewModel.plannedKeepUpright,
                                    shipmentType  = driverViewModel.plannedShipmentType,
                                    points        = finalPoints
                                )
                                driverViewModel.saveRoute(request, session.roleBasedId) {
                                    navController.navigate(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                        popUpTo(DriverProfileScreen(DriverProfilePage.ActivateRoute)) {
                                            inclusive = true
                                        }
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
                DriverProfilePage.AllReviews -> {
                    // 1. Trigger the data fetch
                    LaunchedEffect(Unit) {
                        driverViewModel.fetchReviews(session.userId)
                        driverViewModel.fetchAverageRating(session.userId)
                    }

                    // 2. Display the screen
                    AllReviewsScreen(
                        reviews = driverViewModel.reviews,
                        averageRating = driverViewModel.averageRating,
                        totalReviews = driverViewModel.totalReviews,
                        isLoading = driverViewModel.isLoading,
                        onBack = { navController.popBackStack() },
                        currentTab = session.currentDriverTab,
                        onHomeClick = { navController.navigateTab(DriverDashboard()) },
                        onOrdersClick = { navController.navigateTab(DriverShipmentRequest()) },
                        onHistoryClick = { navController.navigateTab(DriverDeliveries()) },
                        onChatClick = { navController.navigateTab(DriverChat()) },
                        onProfileClick = { navController.navigateTab(DriverProfileScreen()) }
                    )
                }
            }
        }
    }
