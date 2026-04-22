package com.example.cargoconnectapp.ui.models.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cargoconnectapp.ui.models.datamodels.UserDataResponse
import com.example.cargoconnectapp.ui.models.helpermodels.UserRoles

class UserSessionViewModel : ViewModel() {
    var userId by mutableIntStateOf(-1)
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
    var userProfileImageUrl by mutableStateOf<String?>(null)
        private set
    var activeRouteId by mutableIntStateOf(-1)
        private set

    fun updateSession(
        id: Int,
        email: String,
        role: UserRoles,
        userData: UserDataResponse
    ) {
        userId = id
        userEmail = email
        userRole = role
        userFullName = userData.fullName
        userContact = userData.contactNo
        userLicenseNo = userData.licenseNo
        userStreetAddress = userData.streetNo
        userCity = userData.city
        userProfileImageUrl = userData.profileImageUrl
        activeRouteId = userData.activeRoute?.routeId ?: -1
    }

    fun clearSession() {
        userId = -1
        userEmail = "Not Specified"
        userRole = UserRoles.NotSpecified
        // ... clear other fields as needed
    }
}
