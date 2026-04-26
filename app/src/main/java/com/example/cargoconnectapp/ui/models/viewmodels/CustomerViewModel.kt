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
    var customerBookings = mutableStateListOf<BookingDto>()
    var pendingShipments = mutableStateListOf<ShipmentDto>()
    
    // Selection for Details
    var selectedBooking by mutableStateOf<CustomerBooking?>(null)
    var selectedTruck by mutableStateOf<AvailabilityDto?>(null)

    // --- NEW SHIPMENT FLOW STATE ---
    
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

    // Available Trucks
    var availableTrucks = mutableStateListOf<AvailabilityDto>()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var radius by mutableDoubleStateOf(1.0)
    var shipmentType by mutableStateOf("Full")

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

    fun fetchCustomerBookings(customerId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getCustomerBookings(customerId)
                if (response.isSuccessful) {
                    customerBookings.clear()
                    response.body()?.let { bookings ->
                        customerBookings.addAll(bookings)
                        
                        // Update Dashboard Stats
                        totalBookings = bookings.size
                        confirmedCount = bookings.count { it.status.lowercase() == "confirmed" }
                        inTransitCount = bookings.count { it.status.lowercase() == "intransit" || it.status.lowercase() == "moving" }
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

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val response = RetrofitClient.instance.sendRequest(shipmentId, driverId, routeId)
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

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
