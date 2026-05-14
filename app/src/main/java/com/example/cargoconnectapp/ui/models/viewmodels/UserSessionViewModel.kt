package com.example.cargoconnectapp.ui.models.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.UserDataResponse
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboard
import com.example.cargoconnectapp.ui.models.destinations.CustomerDestination
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.destinations.DriverDestination
import com.example.cargoconnectapp.ui.models.helpermodels.UserRoles

class UserSessionViewModel : ViewModel() {
    var userId by mutableIntStateOf(-1)
        private set
    var roleBasedId by mutableIntStateOf(-1)
        private set
    var userEmail by mutableStateOf("Not Specified")
        private set
    var userRole by mutableStateOf(UserRoles.NotSpecified)
        private set
    var userFullName by mutableStateOf("Not Specified")
        private set
    var userContact by mutableStateOf("")
        private set
    var userLicenseNo by mutableStateOf("")
        private set
    var userStreetAddress by mutableStateOf("")
        private set
    var userCity by mutableStateOf("")
        private set

    var userCNIC by mutableStateOf("")
        private set
    var userProfileImageUrl by mutableStateOf<String?>(null)
        private set
    var activeRouteId by mutableIntStateOf(-1)
        private set

    var currentDriverTab by mutableStateOf<DriverDestination>(DriverDashboard())
        private set

    var currentCustomerTab by mutableStateOf<CustomerDestination>(CustomerDashboard())
        private set

    fun setCustomerTab(destination: CustomerDestination) {
        currentCustomerTab = destination
    }
    fun setDriverTab(destination: DriverDestination) {
        currentDriverTab = destination
    }

    fun updateSession(
        id: Int,
        roleBasedId: Int,
        email: String,
        profileImage: String,
        role: UserRoles,
        userData: UserDataResponse
    ) {
        this.userId = id
        this.roleBasedId = roleBasedId
        this.userEmail = email
        this.userRole = role
        this.userFullName = userData.fullName
        this.userContact = userData.contactNo
        this.userLicenseNo = userData.licenseNo ?: ""
        this.userStreetAddress = userData.streetNo ?: ""
        this.userCity = userData.city ?: ""
        this.userCNIC = userData.cnic ?: ""
        
        // Centralized URL reconstruction: IP + relative path
        this.userProfileImageUrl = if (profileImage != "n/a" && profileImage.isNotBlank()) {
            when {
                profileImage.startsWith("http") -> profileImage
                profileImage.startsWith("api/") -> RetrofitClient.BASE_URL + profileImage
                profileImage.startsWith("/api/") -> RetrofitClient.BASE_URL + profileImage.substring(1)
                else -> RetrofitClient.BASE_URL + profileImage
            }
        } else null
    }

    fun clearSession() {
        userId = -1
        roleBasedId = -1
        userEmail = "Not Specified"
        userRole = UserRoles.NotSpecified
        userProfileImageUrl = null
    }
}
