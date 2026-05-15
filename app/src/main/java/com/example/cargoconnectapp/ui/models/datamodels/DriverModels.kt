package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class DriverRequestsResponse(
    val requestsData: List<RequestData>,
    val totalRequests: Int,
    val shipmentData: List<ShipmentData>
)
data class BookingActionResponse(
    val message: String,
    val bookingId: Int
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
    val packages: List<PackageDto> = emptyList()
)

data class PackageDto(
    @SerializedName(value = "shipment_id", alternate = ["ShipmentId", "shipmentId", "shipment_ID"]) val shipment_id: Int,
    @SerializedName(value = "name", alternate = ["Name", "packageName", "item_name"]) val name: String,
    @SerializedName(value = "weight", alternate = ["Weight", "packageWeight", "weight_kg", "total_weight"]) val weight: Double,
    @SerializedName(value = "length", alternate = ["Length", "packageLength", "L"]) val length: Double,
    @SerializedName(value = "width", alternate = ["Width", "packageWidth", "W"]) val width: Double,
    @SerializedName(value = "height", alternate = ["Height", "packageHeight", "H"]) val height: Double,
    @SerializedName(value = "quantity", alternate = ["Quantity", "packageQuantity", "qty"]) val quantity: Int,
    @SerializedName(value = "color", alternate = ["Color", "packageColor"]) val color: String?,
    @SerializedName(value = "tagNo", alternate = ["TagNo", "packageTag", "tag_no"]) val tagNo: String?
)

// --- CUSTOMER MODELS ---

data class CustomerBooking(
    val id: Int = 0,
    val title: String,
    val route: String,
    val amount: String,
    val status: BookingStatus,
    val weight: String = "",
    val packageCount: Int = 0
)

enum class BookingStatus { InTransit, Assigned, Completed, Canceled }

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
    const val FLAMMABLE = 3
    const val LIQUID = 2
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
    @SerializedName(value = "shipment_id", alternate = ["ShipmentId", "shipmentId"]) val shipmentId: Int,
    @SerializedName(value = "pickup_address", alternate = ["PickupAddress", "pickupAddress"]) val pickupAddress: String,
    @SerializedName(value = "delivery_address", alternate = ["DeliveryAddress", "deliveryAddress"]) val deliveryAddress: String,
    @SerializedName(value = "status", alternate = ["Status"]) val status: String,
    @SerializedName(value = "sender_name", alternate = ["SenderName", "senderName"]) val senderName: String,
    @SerializedName(value = "sender_contact", alternate = ["SenderContact", "senderContact"]) val senderContact: String,
    @SerializedName(value = "total_weight", alternate = ["TotalWeight", "totalWeight"]) val totalWeight: Double,
    @SerializedName(value = "strict", alternate = ["Strict"]) val strict: Boolean,
    @SerializedName(value = "recipient_fname", alternate = ["RecipientFname", "recipientFname"]) val recipientFname: String?,
    @SerializedName(value = "recipient_lname", alternate = ["RecipientLname", "recipientLname"]) val recipientLname: String?,
    @SerializedName(value = "recipient_contact", alternate = ["RecipientContact", "recipientContact"]) val recipientContact: String?,
    @SerializedName(value = "booking_date", alternate = ["BookingDate", "bookingDate"]) val bookingDate: String?,
    @SerializedName(value = "shipment_radius", alternate = ["ShipmentRadius", "shipmentRadius"]) val shipmentRadius: Double?,
    @SerializedName(value = "shipment_type", alternate = ["ShipmentType", "shipmentType"]) val shipmentType: String?,
    @SerializedName(value = "packages", alternate = ["Packages", "packagesData", "PackagesData"]) val packages: List<PackageCreateItem>?
)
data class AvailabilityDto(
    @SerializedName("shipmentId") val shipmentId: Int,
    @SerializedName("pickupLat") val pickupLat: Double,
    @SerializedName("pickupLong") val pickupLong: Double,
    @SerializedName("destLat") val destLat: Double,
    @SerializedName("destLong") val destLong: Double,
    @SerializedName("requestedDate") val requestedDate: String,
    @SerializedName("isStrict") val isStrict: Boolean,
    @SerializedName("DriverId") val driverId: Int? = null,
    @SerializedName("DriverName") val driverName: String? = null,
    @SerializedName("ContactNo") val contactNo: String? = null,
    @SerializedName("TruckModel") val truckModel: String? = null,
    @SerializedName("LicenseNo") val licenseNo: String? = null,
    @SerializedName("TotalCapacity") val totalCapacity: Double? = null,
    @SerializedName("PickupCity") val pickupCity: String? = null,
    @SerializedName("DestinationCity") val destinationCity: String? = null,
    @SerializedName("Price") val price: Double? = null,
    @SerializedName("IsFull") val isFull: Boolean? = null,
    @SerializedName("RouteId") val routeId: Int? = null,
    @SerializedName("Distance") val distance: Double? = null,
    @SerializedName("Rating") val rating: Double? = null,
    @SerializedName("TotalReviews") val totalReviews: Int? = null,
    @SerializedName("DepartureDate") val departureDate: String? = null,
    @SerializedName("ArrivalDate") val arrivalDate: String? = null,
    @SerializedName("CheckpointID") val checkpointId: Int? = null,
    @SerializedName("containsFragile") val containsFragile: Boolean = false,
    @SerializedName("containsKeepUpright") val containsKeepUpright: Boolean = false,
    @SerializedName("containsLiquid") val containsLiquid: Boolean = false,
    @SerializedName("containsFlammable") val containsFlammable: Boolean = false,
    @SerializedName("prefType") val prefType: String = "Shared",
)
data class DriverLocationRequestDto (
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("updated_at") val updatedAt: String
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

    val customer_user_id: Int?,

    val recipient_name: String?,
    val recipient_contact: String?,

    val packages: List<PackageDto> = emptyList()
)
data class TruckInfoResponse(
    val vehicle_model: String,
    val reg_no: String,
    val weight: WeightStats,
    val dimensions: DimensionStats,
    val active_package_count: Int
)

data class WeightStats(
    val max_capacity: Double,
    val used_capacity: Double,
    val remaining_capacity: Double
)

data class DimensionStats(
    val max_dimensions: Dimensions,
    val used_dimensions: Dimensions,
    val remaining_dimensions: Dimensions
)

data class Dimensions(
    val l: Double,
    val w: Double,
    val h: Double
)
