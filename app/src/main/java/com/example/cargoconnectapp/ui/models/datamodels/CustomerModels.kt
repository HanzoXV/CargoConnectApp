package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class CustomerBookingDto(
    val driver_user_id: Int?,
    @SerializedName(value = "id", alternate = ["Id", "ID", "booking_id", "BookingId"]) val id: Int,
    @SerializedName(value = "status", alternate = ["Status"]) val status: String,
    @SerializedName(value = "amount", alternate = ["Amount", "fare", "Fare"]) val amount: Double,
    @SerializedName(value = "booking_type", alternate = ["BookingType"]) val booking_type: String?,
    @SerializedName(value = "pickup_date", alternate = ["PickupDate", "pickupDate"]) val pickup_date: String?,
    @SerializedName(value = "estimated_delivery", alternate = ["EstimatedDelivery", "estimatedDelivery"]) val estimated_delivery: String?,
    @SerializedName(value = "actual_delivery", alternate = ["ActualDelivery", "actualDelivery"]) val actual_delivery: String?,

    // Route
    @SerializedName(value = "fromCheckpoint", alternate = ["FromCheckpoint", "from_checkpoint"]) val fromCheckpoint: String?,
    @SerializedName(value = "toCheckpoint", alternate = ["ToCheckpoint", "to_checkpoint"]) val toCheckpoint: String?,
    @SerializedName(value = "departure_date", alternate = ["DepartureDate", "departureDate"]) val departure_date: String?,
    @SerializedName(value = "arrival_date", alternate = ["ArrivalDate", "arrivalDate"]) val arrival_date: String?,

    // Driver
    @SerializedName(value = "driver_name", alternate = ["DriverName", "driverName"]) val driver_name: String?,
    @SerializedName(value = "driver_contact", alternate = ["DriverContact", "driverContact"]) val driver_contact: String?,
    @SerializedName(value = "license_no", alternate = ["LicenseNo", "licenseNo"]) val license_no: String?,

    // Vehicle
    @SerializedName(value = "vehicle_model", alternate = ["VehicleModel", "vehicleModel"]) val vehicle_model: String?,
    @SerializedName(value = "vehicle_reg_no", alternate = ["VehicleRegNo", "vehicleRegNo"]) val vehicle_reg_no: String?,

    // Shipment
    @SerializedName(value = "shipment_id", alternate = ["ShipmentId", "shipmentId"]) val shipment_id: Int?,
    @SerializedName(value = "pickup_address", alternate = ["PickupAddress", "pickupAddress"]) val pickup_address: String?,
    @SerializedName(value = "delivery_address", alternate = ["DeliveryAddress", "deliveryAddress"]) val delivery_address: String?,
    @SerializedName(value = "total_weight", alternate = ["TotalWeight", "totalWeight"]) val total_weight: Double?,
    @SerializedName(value = "package_count", alternate = ["PackageCount", "packageCount"]) val package_count: Int?,

    // Recipient
    @SerializedName(value = "recipient_name", alternate = ["RecipientName", "recipientName"]) val recipient_name: String?,
    @SerializedName(value = "recipient_contact", alternate = ["RecipientContact", "recipientContact"]) val recipient_contact: String?,

    // Packages
    @SerializedName(value = "packages", alternate = ["Packages", "packagesData"]) val packages: List<PackageDto> = emptyList()
)
data class RecentPackageDto(
    val name: String,
    val weight: Double,
    val length: Double,
    val width: Double,
    val height: Double,
    val quantity: Int
)