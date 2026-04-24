package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("Email") val email: String?,
    @SerializedName("Password") val password: String?
)

data class RegisterRequest(
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String,
    @SerializedName("Role") val role: String,
    @SerializedName("FirstName") val firstName: String,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("CNIC") val cnic: String,
    @SerializedName("ContactNo") val contactNo: String,
    @SerializedName("StreetNo") val streetNo: String,
    @SerializedName("City") val city: String,
    @SerializedName("PhotoLink") val photoLink: String,
    @SerializedName("LicenseNo") val licenseNo: String? = null,
    @SerializedName("Vehicle") val vehicle: VehicleData? = null,
    @SerializedName("Documents") val documents: DocumentsData? = null
)

data class AuthResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("roleID") val roleId: Int,
    @SerializedName("roleName") val roleName: String?,
    @SerializedName(value = "userId", alternate = ["userID"]) val userId: Int?,
    @SerializedName("roleBasedId") val roleBasedId: Int?
)

data class VehicleData(
    @SerializedName("RegNo") val regNo: String,
    @SerializedName("Model") val model: String,
    @SerializedName("Type") val type: String,
    @SerializedName("WeightCapacity") val weightCapacity: String,
    @SerializedName("Length") val length: String,
    @SerializedName("Width") val width: String,
    @SerializedName("Height") val height: String
)
data class DocumentsData(
    @SerializedName("CnicLink") val cnicLink: String,
    @SerializedName("LicenseLink") val licenseLink: String,
    @SerializedName("FrontLink") val frontLink: String,
    @SerializedName("BackLink") val backLink: String
)

data class Points(
    @SerializedName("name") val name: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("reached") val reached: Boolean,
    @SerializedName("sequence_no") val sequenceNo: Int?
)
data class UploadResponse(
    @SerializedName("link") val link: String
)
data class UserDataRequest(
    val userId: Int
)
data class UserDataResponse(
    @SerializedName("name")
    val fullName: String,
    @SerializedName("contact")
    val contactNo: String,
    @SerializedName("license_no")
    val licenseNo: String?,
    @SerializedName("street_no")
    val streetNo: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?
)
