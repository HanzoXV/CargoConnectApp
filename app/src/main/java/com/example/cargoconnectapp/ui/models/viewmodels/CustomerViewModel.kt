package com.example.cargoconnectapp.ui.models.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CustomerViewModel : ViewModel() {
    var assignedShipmentId by mutableStateOf<Int?>(null)
    var currentCustomerId by mutableStateOf<Int?>(null)

    // Dashboard State
    var totalBookings by mutableStateOf(0)
    var confirmedCount by mutableStateOf(0)
    var inTransitCount by mutableStateOf(0)
    var deliveredCount by mutableStateOf(0)

    // Bookings State
    var customerBookings = mutableStateListOf<CustomerBookingDto>()
    var pendingShipments = mutableStateListOf<ShipmentDto>()
    
    // Selection for Details
    var selectedBooking by mutableStateOf<CustomerBookingDto?>(null)
    var selectedTruck by mutableStateOf<AvailabilityDto?>(null)

    // --- NEW SHIPMENT FLOW STATE ---

    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set
    
    val addedPackages = mutableStateListOf<Package>()

    var packageName by mutableStateOf("")
    var packageWeight by mutableStateOf("")
    var packageQuantity by mutableStateOf("")
    var packageLength by mutableStateOf("")
    var packageWidth by mutableStateOf("")
    var packageHeight by mutableStateOf("")
    
    var isFragile by mutableStateOf(false)
    var isFlammable by mutableStateOf(false)
    var isLiquid by mutableStateOf(false)
    var isKeepUpright by mutableStateOf(false)

    var recentPackages by mutableStateOf<List<RecentPackageDto>>(emptyList())

    var pickupPoint by mutableStateOf("")
    var destination by mutableStateOf("")
    var pickupGeoPoint by mutableStateOf<GeoPoint?>(null)
    var destinationGeoPoint by mutableStateOf<GeoPoint?>(null)
    var selectedLocalDate by mutableStateOf<LocalDate?>(null)
    var isStrictSchedule by mutableStateOf(false)

    // Selection mode: "Pickup" or "Destination"
    var locationSelectionMode by mutableStateOf("Pickup")

    var recipientFirstName by mutableStateOf("")
    var recipientLastName by mutableStateOf("")
    var recipientCnic by mutableStateOf("")
    var recipientContact by mutableStateOf("")
    var recipientInstructions by mutableStateOf("")

    var requestsHistory by mutableStateOf<List<CustomerRequestDto>>(emptyList())
        private set

    var reviews by mutableStateOf<List<ReviewDto>>(emptyList())
        private set

    var averageRating by mutableDoubleStateOf(0.0)
        private set
    var totalReviews by mutableIntStateOf(0)
        private set

    // Available Trucks
    var availableTrucks = mutableStateListOf<AvailabilityDto>()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var radius by mutableDoubleStateOf(1.0)
    var shipmentType by mutableStateOf("Full")

    // Review State
    var canReviewData by mutableStateOf<CanReviewDto?>(null)
    var reviewRating by mutableIntStateOf(0)
    var reviewComments by mutableStateOf("")

    var chatThreads by mutableStateOf<List<ChatThreadDto>>(emptyList())
        private set
    var selectedThreadId by mutableStateOf<Int?>(null)
    var selectedThreadName by mutableStateOf<String>("")
    var chatMessages by mutableStateOf<List<ChatMessageDto>>(emptyList())
        private set

    fun clearAllData() {
        assignedShipmentId = null
        currentCustomerId = null
        totalBookings = 0
        confirmedCount = 0
        inTransitCount = 0
        deliveredCount = 0
        customerBookings.clear()
        pendingShipments.clear()
        selectedBooking = null
        selectedTruck = null
        addedPackages.clear()
        clearPackageForm()
        pickupPoint = ""
        destination = ""
        pickupGeoPoint = null
        destinationGeoPoint = null
        selectedLocalDate = null
        isStrictSchedule = false
        locationSelectionMode = "Pickup"
        recipientFirstName = ""
        recipientLastName = ""
        recipientCnic = ""
        recipientContact = ""
        recipientInstructions = ""
        availableTrucks.clear()
        errorMessage = null
        successMessage = null
        shipmentType = "Full"
        canReviewData = null
        reviewRating = 0
        reviewComments = ""
        requestsHistory = emptyList()
        reviews = emptyList()
        chatThreads = emptyList()
    }

    // ── API Spot 1: Dashboard Load ───────────────────────────────────────────
    fun createOrGetDraft(customerId: Int) {
        currentCustomerId = customerId
        if (assignedShipmentId != null) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.createOrGetDraft(customerId)
                if (response.isSuccessful) {
                    assignedShipmentId = response.body()?.shipmentId
                    Log.d("CustomerViewModel", "Reserved Shipment ID: $assignedShipmentId")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Draft retrieval failed", e)
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
                Log.e("CustomerViewModel", "fetchReviews failed", e)
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchRequestHistory(customerId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getCustomerRequests(customerId)
                if (response.isSuccessful) {
                    requestsHistory = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "fetchRequestHistory failed.", e)
            }
            finally {
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
    fun fetchAverageRating(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAverageRating(userId)
                if (response.isSuccessful) {
                    averageRating = response.body()?.averageRating ?: 0.0
                    totalReviews = response.body()?.totalReviews ?: 0
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "fetchAverageRating failed", e)
            }
        }
    }
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

    fun fetchCustomerBookings(customerId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getCustomerBookings(customerId)
                if (response.isSuccessful) {
                    customerBookings.clear()
                    response.body()?.let { bookings ->
                        customerBookings.addAll(bookings)
                        
                        // Update Dashboard Stats (Excluding Canceled)
                        val active = bookings.filter { it.status.lowercase() != "canceled" }
                        totalBookings = active.size
                        confirmedCount = bookings.count { it.status.lowercase() == "confirmed" }
                        inTransitCount = bookings.count { it.status.lowercase() == "in-transit" || it.status.lowercase() == "moving" }
                        deliveredCount = bookings.count { it.status.lowercase() == "delivered" || it.status.lowercase() == "completed" }
                    }
                    Log.d("CustomerViewModel", "Fetched ${customerBookings.size} bookings")
                } else {
                    Log.e("CustomerViewModel", "Failed to fetch bookings: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Exception fetching bookings", e)
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchRecentPackages(customerId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRecentPackages(customerId)
                if (response.isSuccessful) {
                    recentPackages = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Failed to fetch recent packages", e)
            }
        }
    }

    fun addRecentPackageLocally(pkg: RecentPackageDto) {
        val newPkg = Package(
            name = pkg.name,
            dimensions = "${pkg.length}x${pkg.width}x${pkg.height}",
            weight = pkg.weight.toString(),
            quantity = pkg.quantity
        )
        addedPackages.add(newPkg)
    }

    fun fetchPendingShipments(customerId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getPendingShipments(customerId)
                if (response.isSuccessful) {
                    pendingShipments.clear()
                    response.body()?.let { pendingShipments.addAll(it) }
                    Log.d("CustomerViewModel", "Fetched ${pendingShipments.size} pending shipments")
                } else {
                    Log.e("CustomerViewModel", "Failed to fetch pending shipments: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Exception fetching pending shipments", e)
            } finally {
                isLoading = false
            }
        }
    }

    // ── Local Management (No API) ─────────────────────────────────────────────
    fun addPackageLocally() {
        Log.d("CustomerViewModel", "Adding package locally: $packageName")
        val newPkg = Package(
            name = packageName,
            dimensions = "${packageLength}x${packageWidth}x${packageHeight}",
            weight = packageWeight,
            quantity = packageQuantity.toIntOrNull() ?: 1,
            fragile = isFragile,
            flammable = isFlammable,
            liquid = isLiquid,
            keepUpright = isKeepUpright
        )
        addedPackages.add(newPkg)
        clearPackageForm()
    }
    fun cancelBooking(bookingId: Int) {
        Log.d("CustomerViewModel", "cancelBooking fun is run.")
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.cancelBooking(bookingId)
                if (response.isSuccessful) {
                    customerBookings.removeIf { it.id == bookingId }
                    Log.d("CustomerViewModel", "Booking ID #${bookingId} canceled successfully")
                } else {
                    Log.e("CustomerViewModel", "Failed to cancel booking: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Exception fetching bookings", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun removePackage(pkg: Package) {
        Log.d("CustomerViewModel", "Removing package locally: ${pkg.name}")
        addedPackages.remove(pkg)
    }

    private fun clearPackageForm() {
        packageName = ""
        packageWeight = ""
        packageQuantity = ""
        packageLength = ""
        packageWidth = ""
        packageHeight = ""
        isFragile = false
        isFlammable = false
        isLiquid = false
        isKeepUpright = false
    }

    // ── API Spot 2: Sync and Complete ────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    fun finalizeShipment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val shipmentId = assignedShipmentId ?: return
        
        viewModelScope.launch {
            isLoading = true
            Log.d("CustomerViewModel", "Starting finalizeShipment for ID: $shipmentId")
            try {
                // 1. Add all packages to the shipment
                for (pkg in addedPackages) {
                    val dims = pkg.dimensions.split("x")
                    val packageItem = PackageCreateItem(
                        shipmentId = shipmentId,
                        name = pkg.name,
                        weight = pkg.weight.removeSuffix(" kg").toDoubleOrNull() ?: 0.0,
                        length = dims.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
                        width = dims.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                        height = dims.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
                        quantity = pkg.quantity,
                        color = null,
                        tagNo = null
                    )
                    
                    val attributeIds = mutableListOf<Int>()
                    if (pkg.fragile) attributeIds.add(PackageAttributes.FRAGILE)
                    if (pkg.flammable) attributeIds.add(PackageAttributes.FLAMMABLE)
                    if (pkg.liquid) attributeIds.add(PackageAttributes.LIQUID)
                    if (pkg.keepUpright) attributeIds.add(PackageAttributes.KEEP_UPRIGHT)
                    
                    val request = PackageWithAttributesRequest(packageItem, attributeIds)
                    RetrofitClient.instance.addPackage(request)
                }

                // 2. Complete the shipment with details
                val completeRequest = CompleteShipmentRequest(
                    shipment_id = shipmentId,
                    pickup_lat = pickupGeoPoint?.latitude ?: 0.0,
                    pickup_long = pickupGeoPoint?.longitude ?: 0.0,
                    pickup_address = pickupPoint,
                    delivery_lat = destinationGeoPoint?.latitude ?: 0.0,
                    delivery_long = destinationGeoPoint?.longitude ?: 0.0,
                    delivery_address = destination,
                    strict = isStrictSchedule,
                    recipient_fname = recipientFirstName,
                    recipient_lname = recipientLastName,
                    recipient_contact = recipientContact,
                    instructionsMessage = recipientInstructions,
                    booking_date = selectedLocalDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                    shipment_radius = radius,
                    shipment_type = shipmentType
                )

                val response = RetrofitClient.instance.completeShipment(completeRequest)
                if (response.isSuccessful) {
                    Log.d("CustomerViewModel", "Shipment confirmed successfully. Fetching available trucks...")
                    
                    // 3. Search for available trucks (passing the isStrict boolean)
                    searchAvailableTrucks(shipmentId, isStrictSchedule, onSuccess, onError)
                    
                } else {
                    onError(response.errorBody()?.string() ?: "Finalization failed")
                    isLoading = false
                }

            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Finalize Exception", e)
                onError(e.message ?: "Unknown connection error")
                isLoading = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun searchAvailableTrucks(shipmentId: Int, isStrict: Boolean = false, onSuccess: () -> Unit, onError: (String) -> Unit) {

        Log.d("CustomerViewModel", "Searching trucks for shipment: $shipmentId with isStrict: $isStrict")
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAvailableTrucks(shipmentId)
                if (response.isSuccessful) {
                    availableTrucks.clear()
                    response.body()?.let { availableTrucks.addAll(it) }
                    Log.d("CustomerViewModel", "Found ${availableTrucks.size} available trucks")
                    
                    // Clear local state only after success
                    addedPackages.clear()
                    assignedShipmentId = null 
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string() ?: "Failed to fetch trucks"
                    Log.e("CustomerViewModel", "Search trucks failed: $err")
                    onError(err)
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Search trucks Exception", e)
                onError(e.message ?: "Search failed")
            } finally {
                isLoading = false
            }
        }
    }

    fun sendDeliveryRequest(truck: AvailabilityDto) {
        val shipmentId = truck.shipmentId
        val driverId = truck.driverId ?: return
        val routeId = truck.routeId ?: return
        val fare = truck.price ?: return
        val checkpointId = truck.checkpointId ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val response = RetrofitClient.instance.sendRequest(shipmentId, driverId, routeId, fare, checkpointId)
                if (response.isSuccessful) {
                    successMessage = "Request sent to ${truck.driverName} successfully!"
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Failed to send request"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Connection error"
            } finally {
                isLoading = false
            }
        }
    }

    // ── Review Logic ────────────────────────────────────────────────────────
    fun checkCanReview(reviewerUserId: Int, bookingId: Int, onCanReview: () -> Unit, onCannotReview: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.canReview(reviewerUserId, bookingId)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.canReview == true) {
                        canReviewData = data
                        reviewRating = 0
                        reviewComments = ""
                        onCanReview()
                    } else {
                        onCannotReview("You've already reviewed this driver")
                    }
                } else {
                    onCannotReview("Failed to check review eligibility")
                }
            } catch (e: Exception) {
                onCannotReview("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun submitReview(fromUserId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val data = canReviewData ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.sendReview(
                    fromUserId = fromUserId,
                    toUserId = data.targetUserId,
                    tripId = data.tripId,
                    rating = reviewRating,
                    comments = reviewComments
                )
                if (response.isSuccessful) {
                    successMessage = "Review submitted!"
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: "Submission failed")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
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
    fun getOrCreateThread(
        customerUserId: Int,
        booking: CustomerBookingDto,
        onSuccess: (threadId: Int, otherName: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getOrCreateThread(
                    customerUserId = customerUserId,
                    driverUserId = booking.shipment_id?: return@launch  // need this field
                )
                if (response.isSuccessful) {
                    val threadId = response.body()?.threadId ?: return@launch
                    selectedThreadId = threadId
                    selectedThreadName = booking.driver_name ?: "Driver"
                    onSuccess(threadId, booking.driver_name ?: "Driver")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "getOrCreateThread failed", e)
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

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
