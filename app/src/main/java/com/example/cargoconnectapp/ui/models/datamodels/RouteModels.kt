package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

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
    val checkpoints: List<CheckpointData>? = null
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
    val reached: Boolean? = null
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
    val sequenceNo: Int
)