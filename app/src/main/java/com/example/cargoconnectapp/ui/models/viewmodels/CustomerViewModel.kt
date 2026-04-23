package com.example.cargoconnectapp.ui.models.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
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

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

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

    // ── Local Management (No API) ─────────────────────────────────────────────
    fun addPackageLocally() {
        val newPkg = Package(
            name = packageName,
            dimensions = "${packageLength}x${packageWidth}x${packageHeight}",
            weight = packageWeight,
            quantity = packageQuantity.toIntOrNull() ?: 1,
            fragile = isFragile
        )
        addedPackages.add(newPkg)
        clearPackageForm()
    }

    fun removePackage(pkg: Package) {
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
    fun finalizeShipment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val shipmentId = assignedShipmentId ?: return
        
        viewModelScope.launch {
            isLoading = true
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
                    if (isFlammable) attributeIds.add(PackageAttributes.FLAMMABLE)
                    if (isLiquid) attributeIds.add(PackageAttributes.LIQUID)
                    if (isKeepUpright) attributeIds.add(PackageAttributes.KEEP_UPRIGHT)
                    
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
                    booking_date = selectedLocalDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: ""
                )

                val response = RetrofitClient.instance.completeShipment(completeRequest)
                if (response.isSuccessful) {
                    addedPackages.clear()
                    assignedShipmentId = null // Clear for next use
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: "Finalization failed")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Unknown connection error")
            } finally {
                isLoading = false
            }
        }
    }
}
