package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val notification_id: Int,
    val message: String,
    val is_read: Boolean,
    val created_at: String
)
data class RouteData(
    @SerializedName("routeId")
    val routeId: Int,
    @SerializedName("driverId")
    val driverId: Int? = null,
    @SerializedName("fare")
    val fare: Double? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("isNextRoute")
    val isNextRoute: Boolean? = null,
    @SerializedName("departureDate")
    val departureDate: String? = null,
    @SerializedName("arrivalDate")
    val arrivalDate: String? = null,

    @SerializedName("startPoint")
    val startPoint: String? = null,
    @SerializedName("endPoint")
    val endPoint: String? = null,
    @SerializedName("totalStops")
    val totalStops: Int? = null,
    @SerializedName("schedule")
    val schedule: RouteSchedule? = null,

    @SerializedName("checkpoints")
    val checkpoints: List<CheckpointData>? = null,

    @SerializedName("isFragile")
    val isFragile: Boolean = false,
    @SerializedName("isLiquid")
    val isLiquid: Boolean = false,
    @SerializedName("isFlammable")
    val isFlammable: Boolean = false,
    @SerializedName("keepUpright")
    val keepUpright: Boolean = false,
    @SerializedName("shipmentType")
    val shipmentType: String,
)

data class RouteSchedule(
    @SerializedName("departureDate")
    val departureDate: String? = null,
    @SerializedName("arrivalDate")
    val arrivalDate: String? = null
)

data class CheckpointData(
    @SerializedName("checkpointId")
    val checkpointId: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("sequenceNo")
    val sequenceNo: Int? = null,
    @SerializedName("reached")
    val reached: Boolean? = null,
)
data class RouteIdResponse (
    @SerializedName("routeId")
    val routeId: Int
)
data class SaveRouteResponse(
    @SerializedName("routeId")
    val routeId: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("isNext")
    val isNext: Boolean
)

data class CreateRouteRequest(
    @SerializedName("driverId")
    val driverId: Int,
    @SerializedName("departureDate")
    val departureDate: String,
    @SerializedName("arrivalDate")
    val arrivalDate: String,
    @SerializedName("activateNow")
    val activateNow: Boolean = false,
    @SerializedName("baseFare")
    val baseFare: Double,
    @SerializedName("isFragile")
    val isFragile: Boolean = false,
    @SerializedName("isLiquid")
    val isLiquid: Boolean = false,
    @SerializedName("isFlammable")
    val isFlammable: Boolean = false,
    @SerializedName("keepUpright")
    val keepUpright: Boolean = false,
    @SerializedName("shipmentType")
    val shipmentType: String,
    @SerializedName("points")
    val points: List<RouteCheckpointRequest>
)

data class RouteCheckpointRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("sequenceNo")
    val sequenceNo: Int,
    val estimatedArrival: String? = null
)

data class CheckpointShipmentsResponse(
    val checkpoint_id: Int,
    val checkpoint_name: String,
    val to_load: CheckpointShipmentGroup,
    val to_drop: CheckpointShipmentGroup
)

data class CheckpointShipmentGroup(
    val total: Int,
    val shipments: List<CheckpointShipmentData>
)

data class CheckpointShipmentData(
    val booking_id: Int,
    val shipment_id: Int,
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
    val recipient_name: String?,
    val recipient_contact: String?,
    val packages: List<PackageDto> = emptyList()
)

data class CheckpointConfirmResponse(
    val message: String,
    val checkpoint_id: Int,
    val checkpoint_name: String,
    val loaded: Int,
    val delivered: Int
)

data class StartTripResponse(
    val message: String,
    val checkpoint_id: Int,
    val checkpoint_name: String,
    val trip_id: Int?,
    val updated_bookings: Int
)

data class UpdateRouteRequest(
    val routeId: Int,
    val driverId: Int,
    val departureDate: String,
    val arrivalDate: String,
    val baseFare: Double,
    val isFragile: Boolean,
    val isLiquid: Boolean,
    val isFlammable: Boolean,
    val keepUpright: Boolean,
    val shipmentType: String,
    val points: List<RouteCheckpointUpdate>
)

data class RouteCheckpointUpdate(
    val sequenceNo: Int,
    val estimatedArrival: String
)

data class CustomerRequestDto(
    val RequestId: Int,
    val ShipmentId: Int,
    val DriverName: String,
    val DriverContact: String?,
    val Status: String,
    val Fare: Double,
    val DateRequested: String,
    val ShipmentType: String?,
    val PickupLocation: String?,
    val DropoffLocation: String?
)