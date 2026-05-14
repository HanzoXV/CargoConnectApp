package com.example.cargoconnectapp.ui.models.destinations

import kotlinx.serialization.Serializable

@Serializable
enum class RegisterStep {
    Step1,
    Step2,
    Step3,
    Step4,
    Step5,
}

@Serializable
enum class DriverDashboardPage {
    TripInfo,
    TruckInfo,
    AddDelay,
    ConfirmDelivery,

    RouteDetails
}

@Serializable
enum class ShipmentRequestPage {
    Main,
    Details
}

@Serializable
enum class DeliveryPage {
    Main,
    Details
}

@Serializable
enum class ChatPage {
    Main,
    Details,
    Notifications
}

@Serializable
enum class DriverProfilePage {
    Main,
    YourInformation,
    EditInformation,
    ActivateRoute,
    SelectRoute,
    AddNewRoute,
    EstimatedDelivery,
    AllReviews
}

@Serializable
enum class CustomerDashboardPage {
    Summary,
    Details,
    Tracking
}

@Serializable
enum class CustomerNewShipmentPage {
    AddPackages,
    AddDetails,
    ChooseLocation,
    ChooseDates,
    RecipientDetails,
    FindingTruck,
    AvailableTrucks,
    TruckDetails,
}

@Serializable
enum class CustomerBookingPage {
    Active,
    Completed,
    Pending,
    Assigned,
    Details,
    CancelBooking,
    Review,
    ResearchRoute,
    Tracking
}

@Serializable
enum class CustomerChatPage {
    Main,
    Details,
    Notifications
}

@Serializable
enum class CustomerProfilePage {
    Main,
    YourInformation,
    EditInformation,
    AllReviews,
    RequestHistory
}
