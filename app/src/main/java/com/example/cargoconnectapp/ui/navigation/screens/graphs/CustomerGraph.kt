package com.example.cargoconnectapp.ui.navigation.screens.graphs

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
                // Map all bookings except canceled for the dashboard
                val dashboardBookings = customerViewModel.customerBookings
                    .filter { it.status.lowercase() != "canceled" }
                    .map { dto ->
                        CustomerBooking(
                            id = dto.id,
                            title = "Booking #${dto.id}",
                            route = dto.pickup_address?.let {
                                "$it → ${dto.delivery_address ?: "N/A"}"
                            } ?: dto.fromCheckpoint?.let {
                                "$it → ${dto.toCheckpoint ?: "N/A"}"
                            } ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = when(dto.status.lowercase()) {
                                "intransit", "moving" -> BookingStatus.InTransit
                                "completed", "delivered", "closed" -> BookingStatus.Completed
                                "canceled" -> BookingStatus.Canceled
                                else -> BookingStatus.Assigned
                            }
                        )
                    }

                CustomerDashboardScreen(
                    customerViewModel    = customerViewModel,
                    bookings             = dashboardBookings,
                    currentTab           = args.tab,
                    onHomeClick          = args.onHomeClick,
                    onOrdersClick        = args.onOrdersClick,
                    onHistoryClick       = args.onHistoryClick,
                    onChatClick          = args.onChatClick,
                    onProfileClick       = args.onProfileClick,
                    onNewBookingClick    = { navController.navigate(CustomerNewShipment()) },
                    onDetailsClick       = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerBookings(CustomerBookingPage.Details)) 
                    },
                    onContactDriverClick = { booking ->
                        val dto = customerViewModel.customerBookings.find { it.id == booking.id }
                        if (dto != null) {
                            customerViewModel.getOrCreateThread(
                                customerUserId = session.userId,
                                booking = dto,
                                onSuccess = { threadId, otherName ->
                                    customerViewModel.selectedThreadId = threadId
                                    customerViewModel.selectedThreadName = otherName
                                    customerViewModel.fetchChatHistory(threadId, session.userId)
                                    navController.navigate(CustomerChat(CustomerChatPage.Details))
                                }
                            )
                        }
                    },
                    onTrackClick         = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) 
                    },
                    onCancelClick        = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) 
                    }
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
                LaunchedEffect(Unit) {
                    customerViewModel.fetchRecentPackages(session.roleBasedId)
                }

                NewShipmentPackagesScreen(
                    currentTab = args.tab,
                    addedPackages = customerViewModel.addedPackages,
                    recentPackages = customerViewModel.recentPackages,
                    onAddNewPackage = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.AddDetails)) },
                    onRemovePackage = { pkg -> customerViewModel.removePackage(pkg) },
                    onQuickAdd = { customerViewModel.addRecentPackageLocally(it) },
                    onNext = { navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.ChooseLocation)) },
                    onBack = { navController.popBackStack() },
                    onCancel = {
                        customerViewModel.addedPackages.clear()
                        navController.popBackStack()
                    },
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
                    onSendRequest    = { truck ->
                        customerViewModel.sendDeliveryRequest(truck)
                        customerViewModel.availableTrucks.remove(truck)
                        if(customerViewModel.availableTrucks.isEmpty())
                            navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.AddPackages))
                                       },
                    onShowDetails    = { truck -> 
                        customerViewModel.selectedTruck = truck
                        navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.TruckDetails))
                    },
                    onHomeClick      = args.onHomeClick,
                    onOrdersClick    = args.onOrdersClick,
                    onHistoryClick   = args.onHistoryClick,
                    onChatClick      = args.onChatClick,
                    onProfileClick   = args.onProfileClick,
                    successMessage   = customerViewModel.successMessage,
                    errorMessage     = customerViewModel.errorMessage,
                    onClearMessages  = { customerViewModel.clearMessages() }
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
                        onSendRequest    = { customerViewModel.sendDeliveryRequest(truck) },
                        onBack           = { navController.popBackStack() },
                        currentTab       = args.tab,
                        onHomeClick      = args.onHomeClick,
                        onOrdersClick    = args.onOrdersClick,
                        onHistoryClick   = args.onHistoryClick,
                        onChatClick      = args.onChatClick,
                        onProfileClick   = args.onProfileClick,
                        successMessage   = customerViewModel.successMessage,
                        errorMessage     = customerViewModel.errorMessage,
                        onClearMessages  = { customerViewModel.clearMessages() }
                    )
                }
            }
        }
    }

    composable<CustomerBookings> {
        val page = it.toRoute<CustomerBookings>().page
        val args = sharedArgs()
        val context = LocalContext.current
        
        LaunchedEffect(Unit) {
            customerViewModel.fetchCustomerBookings(session.roleBasedId)
            customerViewModel.fetchPendingShipments(session.roleBasedId)
        }

        when (page) {
            CustomerBookingPage.Active,
            CustomerBookingPage.Assigned,
            CustomerBookingPage.Pending,
            CustomerBookingPage.Completed -> {
                
                val activeList = customerViewModel.customerBookings
                    .filter { it.status.lowercase() in listOf("assigned", "intransit", "moving") }
                    .map { dto ->
                        CustomerBooking(
                            id = dto.id,
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" } 
                                    ?: dto.pickup_address?.let { "$it to ${dto.delivery_address ?: "N/A"}" } 
                                    ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = if (dto.status.lowercase() == "assigned") BookingStatus.Assigned else BookingStatus.InTransit
                        )
                    }

                val completedList = customerViewModel.customerBookings
                    .filter { it.status.lowercase() in listOf("completed", "delivered", "closed") }
                    .map { dto ->
                        CustomerBooking(
                            id = dto.id,
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" } 
                                    ?: dto.pickup_address?.let { "$it to ${dto.delivery_address ?: "N/A"}" } 
                                    ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = BookingStatus.Completed
                        )
                    }
                val assignedList = customerViewModel.customerBookings
                    .filter { it.status.lowercase() in listOf("assigned") }
                    .map { dto ->
                        CustomerBooking(
                            id = dto.id,
                            title = "Booking #${dto.id}",
                            route = dto.fromCheckpoint?.let { "$it to ${dto.toCheckpoint ?: "N/A"}" }
                                ?: dto.pickup_address?.let { "$it to ${dto.delivery_address ?: "N/A"}" }
                                ?: "Route N/A",
                            amount = "Rs ${dto.amount}",
                            status = BookingStatus.Assigned
                        )
                    }

                YourBookingsScreen(
                    customerViewModel    = customerViewModel,
                    currentTab           = args.tab,
                    onHomeClick          = args.onHomeClick,
                    onOrdersClick        = args.onOrdersClick,
                    onHistoryClick       = args.onHistoryClick,
                    onChatClick          = args.onChatClick,
                    onProfileClick       = args.onProfileClick,
                    activeBookings       = activeList,
                    pendingBookings      = customerViewModel.pendingShipments,
                    assignedBookings = assignedList,
                    completedBookings    = completedList,
                    onDetailsClick       = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerBookings(CustomerBookingPage.Details)) 
                    },
                    onContactDriverClick = { booking ->
                        val dto = customerViewModel.customerBookings.find { it.id == booking.id }
                        if (dto != null) {
                            customerViewModel.getOrCreateThread(
                                customerUserId = session.userId,
                                booking = dto,
                                onSuccess = { threadId, otherName ->
                                    customerViewModel.selectedThreadId = threadId
                                    customerViewModel.selectedThreadName = otherName
                                    customerViewModel.fetchChatHistory(threadId, session.userId)
                                    navController.navigate(CustomerChat(CustomerChatPage.Details))
                                }
                            )
                        }
                    },
                    onTrackClick         = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerDashboard(CustomerDashboardPage.Tracking)) 
                    },
                    onCancelClick        = { booking -> 
                        customerViewModel.selectedBooking = customerViewModel.customerBookings.find { it.id == booking.id }
                        navController.navigate(CustomerBookings(CustomerBookingPage.CancelBooking)) 
                    },
                    onReviewClick        = { booking ->
                        customerViewModel.checkCanReview(
                            reviewerUserId = session.userId,
                            bookingId = booking.id,
                            onCanReview = {
                                navController.navigate(CustomerBookings(CustomerBookingPage.Review))
                            },
                            onCannotReview = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onFindTruckClick     = { shipment ->
                        customerViewModel.radius = shipment.shipmentRadius ?: 1.0
                        customerViewModel.searchAvailableTrucks(
                            shipmentId = shipment.shipmentId,
                            isStrict = shipment.strict,
                            onSuccess = {
                                navController.navigate(CustomerNewShipment(CustomerNewShipmentPage.FindingTruck))
                            },
                            onError = { }
                        )
                    },
                    onShipmentDetailsClick = { shipment ->
                    }
                )
            }
            CustomerBookingPage.Details -> {
                val booking = customerViewModel.selectedBooking
                BookingShipmentsDetailScreen(
                    pickupAddress = booking?.pickup_address ?: "N/A",
                    deliveryAddress = booking?.delivery_address ?: "N/A",
                    totalWeight = "${booking?.total_weight ?: 0.0} kg",
                    pickupDate = booking?.pickup_date ?: "N/A",
                    packages = booking?.packages ?: emptyList(),
                    recpName = booking?.recipient_name?: "N/A",
                    recpContact = booking?.recipient_contact ?: "N/A",
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
                    onConfirmCancel  = { _ ->
                        customerViewModel.selectedBooking?.id?.let { bookingId ->
                            customerViewModel.cancelBooking(bookingId)
                        }
                        navController.popBackStack()
                    },
                    onHomeClick      = args.onHomeClick,
                    onOrdersClick    = args.onOrdersClick,
                    onHistoryClick   = args.onHistoryClick,
                    onChatClick      = args.onChatClick,
                    onProfileClick   = args.onProfileClick
                )
            }
            CustomerBookingPage.Review -> {
                ReviewScreen(
                    customerViewModel = customerViewModel,
                    onBack = { navController.popBackStack() },
                    onSubmit = {
                        customerViewModel.submitReview(
                            fromUserId = session.userId,
                            onSuccess = {
                                navController.popBackStack()
                                Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
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
        }
    }

    composable<CustomerChat> {
        val page = it.toRoute<CustomerChat>().page
        val args = sharedArgs()
        when (page) {
            CustomerChatPage.Main,
            CustomerChatPage.Notifications -> {
                LaunchedEffect(Unit) {
                    customerViewModel.fetchNotifications(session.userId)
                    customerViewModel.fetchChatList(session.userId)
                }
                CustomerActivityScreen(
                    chats = customerViewModel.chatThreads.map { it.toChatContact() },
                    notifications = customerViewModel.notifications,
                    currentTab = args.tab,
                    onHomeClick = args.onHomeClick,
                    onOrdersClick = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatNavClick = { navController.navigateTab(CustomerChat()) },
                    onProfileClick = args.onProfileClick,
                    onChatClick = { contact ->

                        val thread = customerViewModel.chatThreads.find { it.otherName == contact.name }
                        if (thread != null) {
                            customerViewModel.selectedThreadId = thread.threadId
                            customerViewModel.selectedThreadName = thread.otherName
                            customerViewModel.fetchChatHistory(thread.threadId, session.userId)
                            navController.navigate(CustomerChat(CustomerChatPage.Details))
                        }
                    }
                )
            }
            CustomerChatPage.Details -> {
                val threadId = customerViewModel.selectedThreadId
                if (threadId != null) {
                    ChatScreen(
                        contactName = customerViewModel.selectedThreadName,
                        contactStatus = "CargoConnect User",
                        messages = customerViewModel.chatMessages.map {
                            ChatMessage(
                                content = it.messageText,
                                isSentByMe = it.isMine,
                                timestamp = it.sentAt
                            )
                        },
                        onBack = { navController.popBackStack() },
                        onCallClick = {},
                        onSendMessage = { text ->
                            customerViewModel.sendMessage(threadId, session.userId, text)
                        }
                    )
                } else {
                    navController.popBackStack()
                }
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
                    onRequestHistoryClick  = { navController.navigate(CustomerProfile(CustomerProfilePage.RequestHistory)) },
                    onReviewsClick  = { navController.navigate(CustomerProfile(CustomerProfilePage.AllReviews)) },
                    onYourInformationClick = { },
                    onLogoutClick          = { authViewModel.logout() }
                )
            }
            CustomerProfilePage.RequestHistory -> {
                LaunchedEffect(Unit) {
                    customerViewModel.fetchRequestHistory(session.roleBasedId)
                }
                RequestHistoryScreen(
                    requests = customerViewModel.requestsHistory,
                    isLoading = customerViewModel.isLoading,
                    onBack = { navController.popBackStack() },
                    currentTab = args.tab,
                    onHomeClick = args.onHomeClick,
                    onOrdersClick = args.onOrdersClick,
                    onHistoryClick = args.onHistoryClick,
                    onChatClick = args.onChatClick,
                    onProfileClick = args.onProfileClick
                )
            }
            CustomerProfilePage.YourInformation,
            CustomerProfilePage.EditInformation,
            CustomerProfilePage.AllReviews -> {
                LaunchedEffect(Unit) {
                    customerViewModel.fetchRequestHistory(session.roleBasedId)
                }
                CustomerReviewsScreen(

                )
            }
        }
    }
}
