package com.example.cargoconnectapp.ui.models.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.DocumentsData
import com.example.cargoconnectapp.ui.models.datamodels.LoginRequest
import com.example.cargoconnectapp.ui.models.datamodels.Points
import com.example.cargoconnectapp.ui.models.datamodels.RegisterRequest
import com.example.cargoconnectapp.ui.models.datamodels.UserDataRequest
import com.example.cargoconnectapp.ui.models.datamodels.VehicleData
import com.example.cargoconnectapp.ui.models.helpermodels.UserRoles
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.LocalDate

class AuthViewModel (private val sessionViewModel: UserSessionViewModel) : ViewModel() {

    var userId by mutableIntStateOf(-1)
    var userEmail by mutableStateOf("")
    var userPassword by mutableStateOf("")
    var userConfirmPassword by mutableStateOf("")

    var userRole by mutableStateOf(UserRoles.NotSpecified)

    var userFirstName by mutableStateOf("")
    var userLastName by mutableStateOf("")
    var userCNIC by mutableStateOf("")
    var userContact by mutableStateOf("")
    var userCity by mutableStateOf("")
    var userStreetAddress by mutableStateOf("")

    var vehicleModel by mutableStateOf("")
    var vehicleRegNo by mutableStateOf("")
    var vehicleWeightCapacity by mutableStateOf("")
    var vehicleType by mutableStateOf("")
    var vehicleLength by mutableStateOf("")
    var vehicleWidth by mutableStateOf("")
    var vehicleHeight by mutableStateOf("")

    var userCNICLink by mutableStateOf<Uri?>(null)
    var userLicenseLink by mutableStateOf<Uri?>(null)
    var userPhotoLink by mutableStateOf<Uri?>(null)
    var vehiclePhotoFront by mutableStateOf<Uri?>(null)
    var vehiclePhotoBack by mutableStateOf<Uri?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var userSteps by mutableIntStateOf(6)
    var currentStep by mutableIntStateOf(0)

//IMAGE UPLOADING SYSTEM
    var uploadedPhotoLink by mutableStateOf<String?>(null)
    var uploadedCNICLink by mutableStateOf<String?>(null)
    var uploadedLicenseLink by mutableStateOf<String?>(null)
    var uploadedVehicleFrontLink by mutableStateOf<String?>(null)
    var uploadedVehicleBackLink by mutableStateOf<String?>(null)

    private suspend fun uploadFile(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }

            val requestBody = tempFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            val response = RetrofitClient.instance.uploadFile(part)
            if (response.isSuccessful) response.body()?.link else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun uploadAllCustomerFiles(context: Context): Boolean {
        val photoLink = userPhotoLink?.let { uploadFile(context, it, "photo.jpg") }

        if (photoLink == null) {
            errorMessage = "Failed to upload profile photo"
            return false
        }

        uploadedPhotoLink = photoLink
        return true
    }

    private suspend fun uploadAllDriverFiles(context: Context): Boolean {
        val photoLink = userPhotoLink?.let { uploadFile(context, it, "photo.jpg") }
        val cnicLink = userCNICLink?.let { uploadFile(context, it, "cnic.jpg") }
        val licenseLink = userLicenseLink?.let { uploadFile(context, it, "license.jpg") }
        val frontLink = vehiclePhotoFront?.let { uploadFile(context, it, "vehicle_front.jpg") }
        val backLink = vehiclePhotoBack?.let { uploadFile(context, it, "vehicle_back.jpg") }

        if (photoLink == null || cnicLink == null || licenseLink == null ||
            frontLink == null || backLink == null) {
            errorMessage = "Failed to upload one or more files. Please try again."
            return false
        }

        uploadedPhotoLink = photoLink
        uploadedCNICLink = cnicLink
        uploadedLicenseLink = licenseLink
        uploadedVehicleFrontLink = frontLink
        uploadedVehicleBackLink = backLink
        return true
    }

    private suspend fun syncSession(id: Int, roleBasedId: Int, roleName: String?): Boolean {
        val roleEnum = mapRoleNameToEnum(roleName)
        val profileResponse = RetrofitClient.instance.getUserData(UserDataRequest(userId = id))
        
        return if (profileResponse.isSuccessful && profileResponse.body() != null) {
            sessionViewModel.updateSession(
                id = id,
                roleBasedId = roleBasedId,
                email = userEmail,
                role = roleEnum,
                userData = profileResponse.body()!!
            )
            true
        } else {
            errorMessage = "Account verified, but profile sync failed. Please try again."
            sessionViewModel.clearSession()
            false
        }
    }

    fun login(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val loginResponse = RetrofitClient.instance.loginUser(
                    LoginRequest(userEmail, hashPassword(userPassword))
                )

                if (loginResponse.isSuccessful) {
                    val loginBody = loginResponse.body()
                    if (loginBody == null) {
                        errorMessage = "Server response error"
                        return@launch
                    }

                    val id = loginBody.userId ?: -1
                    val roleBasedId = loginBody.roleBasedId ?: -1
                    val roleName = loginBody.roleName

                    if (syncSession(id, roleBasedId, roleName)) {
                        onSuccess(roleName ?: "")
                    }
                } else {
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                errorMessage = "Connection Error: Please check your internet."
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun registerUser(context: Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val uploadSuccess = uploadAllCustomerFiles(context)
            if (!uploadSuccess) {
                isLoading = false
                return@launch
            }

            val request = RegisterRequest(
                email = userEmail,
                password = hashPassword(userPassword),
                role = userRole.name,
                firstName = userFirstName,
                lastName = userLastName,
                cnic = userCNIC,
                contactNo = userContact,
                streetNo = userStreetAddress,
                city = userCity,
                photoLink = uploadedPhotoLink ?: ""
            )

            try {
                val response = RetrofitClient.instance.registerUser(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val id = body.userId ?: -1
                    val roleBasedId = body.roleBasedId ?: -1
                    val roleName = body.roleName

                    if (syncSession(id, roleBasedId, roleName)) {
                        onSuccess(roleName ?: "")
                    }
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: Please check your connection"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun registerDriver(context: Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val uploadSuccess = uploadAllDriverFiles(context)
            if (!uploadSuccess) {
                isLoading = false
                return@launch
            }

            val vehicle = VehicleData(
                regNo = vehicleRegNo,
                model = vehicleModel,
                type = vehicleType,
                weightCapacity = vehicleWeightCapacity,
                length = vehicleLength,
                width = vehicleWidth,
                height = vehicleHeight
            )

            val documents = DocumentsData(
                cnicLink = uploadedCNICLink ?: "",
                licenseLink = uploadedLicenseLink ?: "",
                frontLink = uploadedVehicleFrontLink ?: "",
                backLink = uploadedVehicleBackLink ?: ""
            )

            val request = RegisterRequest(
                email = userEmail,
                password = hashPassword(userPassword),
                role = userRole.name,
                firstName = userFirstName,
                lastName = userLastName,
                cnic = userCNIC,
                contactNo = userContact,
                streetNo = userStreetAddress,
                city = userCity,
                photoLink = uploadedPhotoLink ?: "",
                licenseNo = userCNIC,
                vehicle = vehicle,
                documents = documents
            )

            try {
                val response = RetrofitClient.instance.registerUser(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val id = body.userId ?: -1
                    val roleBasedId = body.roleBasedId ?: -1
                    val roleName = body.roleName

                    if (syncSession(id, roleBasedId, roleName)) {
                        onSuccess(roleName ?: "")
                    }
                } else {
                    errorMessage = response.errorBody()?.string() ?: "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: Please check your connection"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

// --- Validation ---

    fun validateAccountInfo(): Boolean {
        return userEmail.contains("@") &&
                userPassword.length >= 6 &&
                userPassword == userConfirmPassword
    }

    fun validatePersonalDetails(): Boolean {
        return userFirstName.isNotBlank() &&
                userLastName.isNotBlank() &&
                userCNIC.isNotBlank() &&
                userContact.isNotBlank() &&
                userCity.isNotBlank()
    }

    fun validateVehicleDetails(): Boolean {
        return vehicleRegNo.isNotBlank() &&
                vehicleModel.isNotBlank() &&
                vehicleType.isNotBlank() &&
                vehicleWeightCapacity.isNotBlank()
    }

// --- Utility ---

    fun logout() {
        sessionViewModel.clearSession()
        userId = -1
        userEmail = ""
        userPassword = ""
        userConfirmPassword = ""
        userRole = UserRoles.NotSpecified
        userFirstName = ""
        userLastName = ""
        userCNIC = ""
        userContact = ""
        userCity = ""
        userStreetAddress = ""
        vehicleModel = ""
        vehicleRegNo = ""
        vehicleWeightCapacity = ""
        vehicleType = ""
        vehicleLength = ""
        vehicleWidth = ""
        vehicleHeight = ""
        userCNICLink = null
        userLicenseLink = null
        userPhotoLink = null
        vehiclePhotoFront = null
        vehiclePhotoBack = null
        errorMessage = null
        currentStep = 0
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun mapRoleNameToEnum(roleName: String?): UserRoles {
        val lower = roleName?.lowercase()
        return when (lower) {
            "driver" -> UserRoles.Driver
            "customer" -> UserRoles.Customer
            "admin" -> UserRoles.Admin
            else -> UserRoles.NotSpecified
        }
    }

}
