package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class DriverRequestsResponse(
    val requestsData: List<RequestData>,
    val totalRequests: Int,
    val shipmentData: List<ShipmentData>
)

data class RequestData(
    val request_id: Int,
    val shipment_id: Int,
    val status: String,
    val route_id: Int,
    val fare: Double
)

data class ShipmentData(
    val shipment_id: Int,
    val sender_name: String,
    val sender_contact: String,
    val delivery_lat: Double,
    val delivery_long: Double,
    val delivery_address: String,
    val pickup_lat: Double,
    val pickup_long: Double,
    val pickup_address: String,
    val customer_id: Int,
    val package_count: Int,
    val total_weight: Double,
    val packages: List<PackageDto> = emptyList()  // <-- added
)

data class PackageDto(
    val shipment_id: Int,
    val name: String,
    val weight: Double,
    val length: Double,
    val width: Double,
    val height: Double,
    val quantity: Int,
    val color: String,
    val tagNo: String
)

// --- CUSTOMER MODELS ---

data class CustomerBooking(
    val title: String,
    val route: String,
    val amount: String,
    val status: BookingStatus
)

enum class BookingStatus { InTransit, Confirmed, Completed, Reviewed }

data class Package(
    val name: String,
    val dimensions: String,
    val weight: String,
    val quantity: Int,
    val fragile: Boolean = false,
    val flammable: Boolean = false,
    val liquid: Boolean = false,
    val keepUpright: Boolean = false
)

data class AvailableTruck(
    val shipmentTitle: String,
    val route: String,
    val amount: String,
    val badge: TruckBadge
)

enum class TruckBadge { Shared, Full }

data class DriverProfile(
    val name: String,
    val email: String,
    val location: String,
    val memberSince: String,
    val isTopRated: Boolean,
    val rating: Float,
    val reviewerName: String,
    val reviewDate: String,
    val reviewText: String,
    val vehicleModel: String,
    val vehicleType: String,
    val licensePlate: String,
    val color: String,
    val year: String,
    val capacity: String,
    val registrationNo: String
)

data class ChatContact(
    val name: String,
    val lastMessage: String,
    val time: String,
    val hasUnread: Boolean,
    val isSupport: Boolean = false
)

data class NotificationItem(
    val message: String,
    val time: String,
    val hasUnread: Boolean
)

data class CreateShipmentResponse(
    val shipmentId: Int
)

data class PackageCreateItem(
    @SerializedName("shipment_id") val shipmentId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("weight") val weight: Double,
    @SerializedName("length") val length: Double,
    @SerializedName("width") val width: Double,
    @SerializedName("height") val height: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("color") val color: String?,
    @SerializedName("tagNo") val tagNo: String?
)

data class PackageWithAttributesRequest(
    @SerializedName("Package") val packageItem: PackageCreateItem,
    @SerializedName("AttributeIds") val attributeIds: List<Int>
)

object PackageAttributes {
    const val FRAGILE = 1
    const val FLAMMABLE = 2
    const val LIQUID = 3
    const val KEEP_UPRIGHT = 4
}

data class CompleteShipmentRequest(
    val shipment_id: Int,
    val pickup_lat: Double,
    val pickup_long: Double,
    val pickup_address: String,
    val delivery_lat: Double,
    val delivery_long: Double,
    val delivery_address: String,
    val strict: Boolean,
    val recipient_fname: String,
    val recipient_lname: String?,
    val recipient_contact: String,
    val instructionsMessage: String?,
    val booking_date: String,
    val shipment_radius: Double,
    val shipment_type: String
)

data class ShipmentDto(
    @SerializedName(value = "shipment_id", alternate = ["ShipmentId"]) val shipmentId: Int,
    @SerializedName(value = "pickup_address", alternate = ["PickupAddress"]) val pickupAddress: String,
    @SerializedName(value = "delivery_address", alternate = ["DeliveryAddress"]) val deliveryAddress: String,
    @SerializedName(value = "status", alternate = ["Status"]) val status: String,
    @SerializedName(value = "sender_name", alternate = ["SenderName"]) val senderName: String,
    @SerializedName(value = "sender_contact", alternate = ["SenderContact"]) val senderContact: String,
    @SerializedName(value = "total_weight", alternate = ["TotalWeight"]) val totalWeight: Double,
    @SerializedName(value = "strict", alternate = ["Strict"]) val strict: Boolean,
    @SerializedName(value = "recipient_fname", alternate = ["RecipientFname"]) val recipientFname: String?,
    @SerializedName(value = "recipient_lname", alternate = ["RecipientLname"]) val recipientLname: String?,
    @SerializedName(value = "recipient_contact", alternate = ["RecipientContact"]) val recipientContact: String?,
    @SerializedName(value = "booking_date", alternate = ["BookingDate"]) val bookingDate: String?,
    @SerializedName(value = "shipment_radius", alternate = ["ShipmentRadius"]) val shipmentRadius: Double?,
    @SerializedName(value = "shipment_type", alternate = ["ShipmentType"]) val shipmentType: String?,
    @SerializedName(value = "packages", alternate = ["Packages"]) val packages: List<PackageCreateItem>?
)

data class BookingDto(
    val id: Int,
    val status: String,
    val amount: Double,
    @SerializedName("bookingType") val bookingType: String,
    val fromCheckpoint: String?, // Nullable because fromCp can be null
    val toCheckpoint: String?    // Nullable because toCp can be null
)
data class AvailabilityDto(
    // Shipment & Request Details
    @SerializedName("shipmentId") val shipmentId: Int,
    @SerializedName("pickupLat") val pickupLat: Double,
    @SerializedName("pickupLong") val pickupLong: Double,
    @SerializedName("destLat") val destLat: Double,
    @SerializedName("destLong") val destLong: Double,
    @SerializedName("requestedDate") val requestedDate: String,
    @SerializedName("isStrict") val isStrict: Boolean,

    // Driver & Truck Info
    @SerializedName("DriverId") val driverId: Int? = null,
    @SerializedName("DriverName") val driverName: String? = null,
    @SerializedName("ContactNo") val contactNo: String? = null,
    @SerializedName("TruckModel") val truckModel: String? = null,
    @SerializedName("LicenseNo") val licenseNo: String? = null,
    @SerializedName("TotalCapacity") val totalCapacity: Double? = null,
    @SerializedName("PickupCity") val pickupCity: String? = null,
    @SerializedName("DestinationCity") val destinationCity: String? = null,

    // Financials & Logic
    @SerializedName("Price") val price: Double? = null,
    @SerializedName("IsFull") val isFull: Boolean? = null,
    @SerializedName("RouteId") val routeId: Int? = null,
    @SerializedName("Distance") val distance: Double? = null,
    @SerializedName("Rating") val rating: Double? = null,
    @SerializedName("TotalReviews") val totalReviews: Int? = null,

    @SerializedName("DepartureDate") val departureDate: String? = null,
    @SerializedName("ArrivalDate") val arrivalDate: String? = null,
)

data class DeclineResponseDto(
    @SerializedName("message") val message: String
)

data class AcceptResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("tripId") val tripId: Int,
    @SerializedName("bookingId") val bookingId: Int
)

data class BookingData(
    val booking_id: Int,
    val shipment_id: Int,
    val route_id: Int,
    val trip_id: Int,
    val status: String,
    val amount: Double,
    val pickup_date: String?,

    // Shipment details
    val pickup_address: String?,
    val pickup_lat: Double?,
    val pickup_long: Double?,
    val delivery_address: String?,
    val delivery_lat: Double?,
    val delivery_long: Double?,
    val sender_name: String?,
    val sender_contact: String?,
    val total_weight: Double?,
    val package_count: Int?,

    // Recipient details
    val recipient_name: String?,
    val recipient_contact: String?,

    // Package details
    val packages: List<PackageDto> = emptyList()
)
