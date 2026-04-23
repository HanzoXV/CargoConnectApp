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
    val status: String
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
    val total_weight: Double
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
    val booking_date: String
)
data class ShipmentDto(
    @SerializedName("shipment_id") val shipmentId: Int,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("status") val status: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("sender_contact") val senderContact: String,
    @SerializedName("total_weight") val totalWeight: Double
)
data class BookingDto(
    val id: Int,
    val status: String,
    val amount: Double,
    @SerializedName("bookingType") val bookingType: String,
    val fromCheckpoint: String?, // Nullable because fromCp can be null
    val toCheckpoint: String?    // Nullable because toCp can be null
)