package com.example.cargoconnectapp.ui.models.destinations

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination

@Serializable
sealed interface AuthDestination : Destination
@Serializable
sealed interface DriverDestination : Destination
@Serializable
sealed interface CustomerDestination : Destination


//----------------------- AUTH DESTINATIONS -------------------------------------//
@Serializable
data object Splash : AuthDestination
@Serializable
data object Login : AuthDestination
@Serializable
data class Register(
    val step: RegisterStep = RegisterStep.Step1
) : AuthDestination

//----------------------- DRIVER DESTINATIONS -------------------------------------//
@Serializable
data class DriverDashboard(
    val page: DriverDashboardPage = DriverDashboardPage.TripInfo
) : DriverDestination
@Serializable
data class DriverShipmentRequest(
    val page: ShipmentRequestPage = ShipmentRequestPage.Main
) : DriverDestination
@Serializable
data class DriverDeliveries(
    val page: DeliveryPage = DeliveryPage.Main
) : DriverDestination
@Serializable
data class DriverChat(
    val page: ChatPage = ChatPage.Main
) : DriverDestination
@Serializable
data class DriverProfileScreen(
    val page: DriverProfilePage = DriverProfilePage.Main
) : DriverDestination

//----------------------- CUSTOMER DESTINATIONS -------------------------------------//
@Serializable
data class CustomerDashboard(
    val page: CustomerDashboardPage = CustomerDashboardPage.Summary
) : CustomerDestination
@Serializable
data class CustomerNewShipment(
    val page: CustomerNewShipmentPage = CustomerNewShipmentPage.AddPackages
) : CustomerDestination
@Serializable
data class CustomerBookings(
    val page: CustomerBookingPage = CustomerBookingPage.Active
) : CustomerDestination
@Serializable
data class CustomerChat(
    val page: CustomerChatPage = CustomerChatPage.Main
) : CustomerDestination
@Serializable
data class CustomerProfile(
    val page: CustomerProfilePage = CustomerProfilePage.Main
) : CustomerDestination
//---------------------- E N D AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA -----------------------//