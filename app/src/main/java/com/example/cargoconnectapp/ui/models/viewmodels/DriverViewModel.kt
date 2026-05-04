package com.example.cargoconnectapp.ui.models.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.*
import com.example.cargoconnectapp.ui.navigation.screens.ShipmentRequest
import com.example.cargoconnectapp.ui.models.helpermodels.Dimensions
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class RouteTarget { ACTIVE, NEXT }

class DriverViewModel : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    var templateRouteForUpdate by mutableStateOf<RouteData?>(null)

    var chatThreads by mutableStateOf<List<ChatThreadDto>>(emptyList())
        private set

    var showReviewDialog by mutableStateOf(false)
    var pendingReviewBookingId by mutableStateOf<Int?>(null)
    var reviewTargetUserId by mutableStateOf<Int?>(null)

    var truckInfo by mutableStateOf<TruckInfoResponse?>(null)
        private set
    var selectedThreadId by mutableStateOf<Int?>(null)
    var selectedThreadName by mutableStateOf<String>("")
    var chatMessages by mutableStateOf<List<ChatMessageDto>>(emptyList())
        private set

    var pendingReviewTripId by mutableStateOf<Int?>(null)

    var activeRoute by mutableStateOf<RouteData?>(null)
        private set
    var nextRoute by mutableStateOf<RouteData?>(null)
        private set
    var pastRoutes by mutableStateOf<List<RouteData>>(emptyList())
        private set

    var previewedPoints by mutableStateOf<List<RouteCheckpointRequest>>(emptyList())
        private set

    var isStartCheckpoint by mutableStateOf(false)

    var estimatedArrivalForDelay by mutableStateOf<String?>(null)
        private set

    var currentCapacity by mutableIntStateOf(0)
        private set
    var totalCapacity by mutableIntStateOf(0)
        private set

    var totalDimensions by mutableStateOf(Dimensions())
        private set
    var currentDimensions by mutableStateOf(Dimensions())
        private set

    var totalStops by mutableIntStateOf(0)
        private set
    var completedStops by mutableIntStateOf(0)
        private set

    val distances = mutableStateListOf<Int>()

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    var driverRequests by mutableStateOf<DriverRequestsResponse?>(null)
        private set

    var selectedRequest by mutableStateOf<ShipmentRequest?>(null)

    // ── Booking State ──
    var pendingBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var inTransitBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var completedBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var canceledBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set

    var selectedBooking by mutableStateOf<BookingData?>(null)

    // ── Checkpoint State ──
    var checkpointShipments by mutableStateOf<CheckpointShipmentsResponse?>(null)
        private set
    var selectedCheckpointId by mutableIntStateOf(0)

    // ── Activity State ──
    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set

    // ── Review State ──
    var reviews by mutableStateOf<List<ReviewDto>>(emptyList())
        private set
    var averageRating by mutableDoubleStateOf(0.0)
        private set
    var totalReviews by mutableIntStateOf(0)
        private set

    // ── Route Planning State (Temporary) ──────────────────────────────────────
    
    val plannedPoints = mutableStateListOf<GeoPoint>()
    var plannedDepartureDate by mutableStateOf<LocalDateTime?>(null)
    var plannedArrivalDate by mutableStateOf<LocalDateTime?>(null)

    // New Route Attributes
    var plannedIsFragile by mutableStateOf(false)
    var plannedIsLiquid by mutableStateOf(false)
    var plannedIsFlammable by mutableStateOf(false)
    var plannedKeepUpright by mutableStateOf(false)
    var plannedShipmentType by mutableStateOf("Shared") // "Full" or "Shared"

    // Stores a route template if we are activating an existing one
    var selectedTemplateRoute by mutableStateOf<RouteData?>(null)
    
    // Target position for the new/selected route
    var routeActivationTarget by mutableStateOf(RouteTarget.ACTIVE) 

    fun clearPlannedRoute() {
        plannedPoints.clear()
        plannedDepartureDate = null
        plannedArrivalDate = null
        selectedTemplateRoute = null
        plannedIsFragile = false
        plannedIsLiquid = false
        plannedIsFlammable = false
        plannedKeepUpright = false
        plannedShipmentType = "Shared"
        templateRouteForUpdate = null
    }

    fun clearAllData() {
        truckInfo = null
        currentCapacity = 0
        totalCapacity = 0
        activeRoute = null
        nextRoute = null
        pastRoutes = emptyList()
        currentCapacity = 0
        totalCapacity = 0
        totalDimensions = Dimensions()
        currentDimensions = Dimensions()
        totalStops = 0
        completedStops = 0
        distances.clear()
        driverRequests = null
        templateRouteForUpdate = null
        clearPlannedRoute()
        routeActivationTarget = RouteTarget.ACTIVE
        errorMessage = null
        successMessage = null
        selectedRequest = null
        pendingBookings = emptyList()
        inTransitBookings = emptyList()
        completedBookings = emptyList()
        canceledBookings = emptyList()
        selectedBooking = null
        checkpointShipments = null
        notifications = emptyList()
        selectedCheckpointId = 0
        reviews = emptyList()
        averageRating = 0.0
        totalReviews = 0
    }

    // ── Fetch functions ───────────────────────────────────────────────────────

    private fun String?.isNotFoundError(): Boolean {
        if (this == null) return false
        val low = this.lowercase()
        return low.contains("no route found") || 
               low.contains("no next route found") || 
               low.contains("not found") ||
               low.contains("no active route") ||
               low.contains("no requests") ||
               low.contains("no notifications")
    }

    fun fetchActiveRoute(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getActiveRoute(driverId)
                if (response.isSuccessful) {
                    activeRoute = response.body()
                    totalStops = activeRoute?.checkpoints?.size ?: 0
                    completedStops = activeRoute?.checkpoints?.count { it.reached ?: false } ?: 0
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody.isNotFoundError()) {
                        activeRoute = null
                    } else {
                        errorMessage = errorBody
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchTruckInfo(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getTruckInfo(driverId)
                if (response.isSuccessful) {
                    val info = response.body()
                    truckInfo = info

                    // Sync with existing capacity/dimension states in your ViewModel
                    info?.let {
                        // Update Weight
                        currentCapacity = it.weight.used_capacity.toInt()
                        totalCapacity = it.weight.max_capacity.toInt()

                        // Update Dimensions
                        totalDimensions = Dimensions(
                            length = it.dimensions.max_dimensions.l.toInt(),
                            width = it.dimensions.max_dimensions.w.toInt(),
                            height = it.dimensions.max_dimensions.h.toInt()
                        )
                        currentDimensions = Dimensions(
                            length = it.dimensions.remaining_dimensions.l.toInt(),
                            width = it.dimensions.remaining_dimensions.w.toInt(),
                            height = it.dimensions.remaining_dimensions.h.toInt()
                        )
                    }
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("DriverViewModel", "fetchTruckInfo failed", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchNextRoute(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getNextRoute(driverId)
                if (response.isSuccessful) nextRoute = response.body()
                else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody.isNotFoundError()) {
                        nextRoute = null
                    } else {
                        errorMessage = errorBody
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun checkAndTriggerReview(bookingId: Int, driverUserId: Int) {
        viewModelScope.launch {
            try {
                val infoResponse = RetrofitClient.instance.getReviewInfoForBooking(bookingId)
                if (infoResponse.isSuccessful) {
                    val info = infoResponse.body() ?: return@launch
                    val canReviewResponse = RetrofitClient.instance.canReview(
                        reviewerUserId = driverUserId,
                        bookingId = bookingId
                    )
                    if (canReviewResponse.isSuccessful && canReviewResponse.body()?.canReview == true) {
                        pendingReviewBookingId = bookingId
                        reviewTargetUserId = info.targetUserId
                        pendingReviewTripId = info.tripId  // ← store it here
                        showReviewDialog = true
                    }
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "checkAndTriggerReview failed", e)
            }
        }
    }

    fun submitReview(
        fromUserId: Int,
        tripId: Int,
        rating: Int,
        comments: String,
        onComplete: () -> Unit = {}
    ) {
        val toUserId = reviewTargetUserId ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.instance.sendReview(
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    tripId = tripId,
                    rating = rating,
                    comments = comments
                )
            } catch (e: Exception) {
                Log.e("DriverViewModel", "submitReview failed", e)
            } finally {
                showReviewDialog = false
                pendingReviewBookingId = null
                reviewTargetUserId = null
                onComplete()
            }
        }
    }

    fun dismissReview() {
        showReviewDialog = false
        pendingReviewBookingId = null
        reviewTargetUserId = null
        pendingReviewTripId = null
    }
    fun fetchAllRoutes(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getRoutes(driverId)
                if (response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    // Filter and take 10 most recent
                    pastRoutes = all.filter {
                        it.isActive != true && it.isNextRoute != true
                    }.sortedByDescending { it.routeId }.take(10)
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchRoutePreview(request: CreateRouteRequest) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.previewRouteTimes(request)
                if (response.isSuccessful) {
                    previewedPoints = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to calculate schedule."
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun saveRoute(request: CreateRouteRequest, driverId: Int, onComplete: () -> Unit = {}) {
        val target = routeActivationTarget // Snapshot BEFORE any async work
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d("DriverViewModel", "Saving route with target=$target")
                val response = RetrofitClient.instance.saveRoute(request)
                if (response.isSuccessful) {
                    val routeId = response.body()?.routeId ?: -1
                    Log.d("DriverViewModel", "Route saved. ID: $routeId")

                    if (routeId != -1) {
                        if (target == RouteTarget.ACTIVE) {
                            Log.d("DriverViewModel", "Activating route $routeId")
                            RetrofitClient.instance.activateRoute(routeId)
                        } else {
                            Log.d("DriverViewModel", "Scheduling next route $routeId")
                            RetrofitClient.instance.scheduleNextRoute(routeId)
                        }
                    }

                    onComplete()             // Navigate FIRST
                    clearPlannedRoute()      // Then clean up
                    fetchActiveRoute(driverId)
                    fetchNextRoute(driverId)
                    fetchAllRoutes(driverId)
                } else {
                    errorMessage = response.errorBody()?.string()
                    Log.e("DriverViewModel", "Save route failed: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("DriverViewModel", "Exception saving route", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun activateRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.activateRoute(routeId)
                if (response.isSuccessful) fetchActiveRoute(driverId)
                else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun scheduleNextRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.scheduleNextRoute(routeId)
                if (response.isSuccessful) {
                    fetchNextRoute(driverId)
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun updateRoute(
        request: UpdateRouteRequest,
        driverId: Int,
        onComplete: () -> Unit = {}
    ) {
        val target = routeActivationTarget
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val updateResponse = RetrofitClient.instance.updateRoute(request)
                if (updateResponse.isSuccessful) {
                    val routeId = updateResponse.body()?.routeId ?: -1
                    if (routeId != -1) {
                        if (target == RouteTarget.ACTIVE) {
                            RetrofitClient.instance.activateRoute(routeId)
                        } else {
                            RetrofitClient.instance.scheduleNextRoute(routeId)
                        }
                    }
                    onComplete()
                    clearPlannedRoute()
                    templateRouteForUpdate = null
                    fetchActiveRoute(driverId)
                    fetchNextRoute(driverId)
                    fetchAllRoutes(driverId)
                } else {
                    errorMessage = updateResponse.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.deleteRoute(routeId)
                if (response.isSuccessful) fetchAllRoutes(driverId)
                else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun pickupBooking(bookingId: Int, driverId: Int, checkpointId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.pickupBooking(bookingId, driverId)
                if (response.isSuccessful) {
                    // Refresh checkpoint shipments to reflect updated status
                    fetchShipmentsAtCheckpoint(checkpointId, driverId)
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deliverBooking(bookingId: Int, driverId: Int, checkpointId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.deliverBooking(bookingId, driverId)
                if (response.isSuccessful) {
                    // Refresh checkpoint shipments to reflect updated status
                    fetchShipmentsAtCheckpoint(checkpointId, driverId)
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchDriverRequests(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getDriverRequests(driverId)
                if (response.isSuccessful) {
                    driverRequests = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody.isNotFoundError()) {
                        driverRequests = null
                    } else {
                        errorMessage = errorBody
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun acceptShipmentRequest(requestId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.acceptRequest(requestId)
                if (response.isSuccessful) {
                    successMessage = response.body()?.message ?: "Request accepted successfully"
                    fetchDriverRequests(driverId)
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to accept request"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun declineShipmentRequest(requestId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.declineRequest(requestId)
                if (response.isSuccessful) {
                    successMessage = response.body()?.message ?: "Request declined successfully"
                    fetchDriverRequests(driverId)
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to decline request"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAllBookings(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            Log.d("DriverViewModel", "Fetching all bookings for driver: $driverId")
            try {
                val p = async { RetrofitClient.instance.getConfirmedBookings(driverId) }
                val i = async { RetrofitClient.instance.getInTransitBookings(driverId) }
                val co = async { RetrofitClient.instance.getCompletedBookings(driverId) }
                val ca = async { RetrofitClient.instance.getCanceledBookings(driverId) }

                val resP = p.await()
                val resI = i.await()
                val resCo = co.await()
                val resCa = ca.await()

                if (resP.isSuccessful) pendingBookings = resP.body() ?: emptyList() else Log.e("DriverViewModel", "Pending failed: ${resP.errorBody()?.string()}")
                if (resI.isSuccessful) inTransitBookings = resI.body() ?: emptyList() else Log.e("DriverViewModel", "InTransit failed: ${resI.errorBody()?.string()}")
                if (resCo.isSuccessful) completedBookings = resCo.body() ?: emptyList() else Log.e("DriverViewModel", "Completed failed: ${resCo.errorBody()?.string()}")
                if (resCa.isSuccessful) canceledBookings = resCa.body() ?: emptyList() else Log.e("DriverViewModel", "Canceled failed: ${resCa.errorBody()?.string()}")

                Log.d("DriverViewModel", "Bookings fetched. Pending: ${pendingBookings.size}, InTransit: ${inTransitBookings.size}")

            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching bookings", e)
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchEstimatedArrival(checkpointId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getEstimatedArrivalDate(checkpointId)
                if (response.isSuccessful) {
                    estimatedArrivalForDelay = response.body()
                }
            } catch (e: Exception) {
                // silently fail, UI shows fallback
            }
        }
    }

    fun confirmCheckpoint(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.confirmCheckpointReached(checkpointId, driverId)
                if (response.isSuccessful) {
                    onComplete()                  // Navigate FIRST
                    fetchActiveRoute(driverId)    // Then refresh in background
                    checkpointShipments = null    // Clear LAST
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun startTrip(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.startTrip(checkpointId, driverId)
                if (response.isSuccessful) {
                    onComplete()                  // Navigate FIRST
                    fetchActiveRoute(driverId)    // Then refresh in background
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    fun updatePreviewedPoints(newList: List<RouteCheckpointRequest>) {
        previewedPoints = newList
    }
    fun fetchShipmentsAtCheckpoint(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response =
                    RetrofitClient.instance.getShipmentsAtCheckpoint(checkpointId, driverId)
                if (response.isSuccessful) {
                    checkpointShipments = response.body()
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun reportDelay(checkpointId: Int, days: Int, hours: Int, reason: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val totalHours = (days * 24.0) + hours.toDouble()
                val response = RetrofitClient.instance.reportDelay(checkpointId, totalHours, reason)
                if (response.isSuccessful) {
                    successMessage = "Delay reported successfully"
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to report delay"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchNotifications(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getNotifications(userId)
                if (response.isSuccessful) {
                    notifications = response.body() ?: emptyList()
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody.isNotFoundError()) {
                        notifications = emptyList()
                    } else {
                        errorMessage = errorBody
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchChatList(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getChatList(userId)
                if (response.isSuccessful) {
                    chatThreads = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "fetchChatList failed", e)
            }
        }
    }
    fun getOrCreateThread(
        driverUserId: Int,
        customerBooking: BookingData,
        onSuccess: (threadId: Int, otherName: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getOrCreateThread(
                    customerUserId = customerBooking.customer_user_id ?: return@launch,
                    driverUserId = driverUserId
                )
                if (response.isSuccessful) {
                    val threadId = response.body()?.threadId ?: return@launch
                    selectedThreadId = threadId
                    selectedThreadName = customerBooking.sender_name ?: "Customer"
                    onSuccess(threadId, customerBooking.sender_name ?: "Customer")
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "getOrCreateThread failed", e)
            }
        }
    }
    fun fetchChatHistory(threadId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getChatHistory(threadId, userId)
                if (response.isSuccessful) {
                    chatMessages = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "fetchChatHistory failed", e)
            }
        }
    }

    fun sendMessage(threadId: Int, senderUserId: Int, messageText: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.sendMessage(threadId, senderUserId, messageText)
                if (response.isSuccessful) {
                    fetchChatHistory(threadId, senderUserId)
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "sendMessage failed", e)
            }
        }
    }

    fun fetchReviews(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getAllReviews(userId)
                if (response.isSuccessful) {
                    reviews = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "fetchReviews failed", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAverageRating(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAverageRating(userId)
                if (response.isSuccessful) {
                    averageRating = response.body()?.averageRating ?: 0.0
                    totalReviews = response.body()?.totalReviews ?: 0
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "fetchAverageRating failed", e)
            }
        }
    }

    // ── Local setters ─────────────────────────────────────────────────────────

    fun setCapacity(current: Int, total: Int) {
        currentCapacity = current
        totalCapacity = total
    }

    fun setTotalDimensions(length: Int, width: Int, height: Int) {
        totalDimensions = Dimensions(length, width, height)
    }

    fun setCurrentDimensions(length: Int, width: Int, height: Int) {
        currentDimensions = Dimensions(length, width, height)
    }

    fun setDistances(values: List<Int>) {
        distances.clear()
        distances.addAll(values)
    }

    fun clearError() {
        errorMessage = null
        successMessage = null
    }

}
