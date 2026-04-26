package com.example.cargoconnectapp.data

import com.example.cargoconnectapp.ui.models.datamodels.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>
    @Multipart
    @POST("api/auth/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<UploadResponse>
    @POST("api/users/getdata")
    suspend fun getUserData(@Body request: UserDataRequest): Response<UserDataResponse>

    // ── Routes ────────────────────────────────────────────────────────────────
    @GET("api/driver/get-active-route/{driverId}")
    suspend fun getActiveRoute(@Path("driverId") driverId: Int): Response<RouteData>

    @GET("api/driver/get-next-route/{driverId}")
    suspend fun getNextRoute(@Path("driverId") driverId: Int): Response<RouteData>

    @GET("api/driver/get-routes/{driverId}")
    suspend fun getRoutes(@Path("driverId") driverId: Int): Response<List<RouteData>>

    @POST("api/driver/save-route")
    suspend fun saveRoute(@Body request: CreateRouteRequest): Response<SaveRouteResponse>

    @POST("api/driver/activate-route/{routeId}")
    suspend fun activateRoute(@Path("routeId") routeId: Int): Response<String>

    @POST("api/driver/schedule-next-route/{routeId}")
    suspend fun scheduleNextRoute(@Path("routeId") routeId: Int): Response<String>

    @DELETE("api/driver/delete-route/{routeId}")
    suspend fun deleteRoute(@Path("routeId") routeId: Int): Response<String>

    //Driver Requests
    @GET("api/drivers/{id}/requests/pending")
    suspend fun getDriverRequests(
        @Path("id") driverId: Int
    ): Response<DriverRequestsResponse>

    // ── Shipments ─────────────────────────────────────────────────────────────
    @POST("api/shipments/draft/{customerId}")
    suspend fun createOrGetDraft(@Path("customerId") customerId: Int): Response<CreateShipmentResponse>

    @POST("api/shipments/add/package")
    suspend fun addPackage(@Body request: PackageWithAttributesRequest): Response<Unit>

    @POST("api/shipments/complete")
    suspend fun completeShipment(@Body request: CompleteShipmentRequest): Response<Unit>

    @GET("api/shipments/pending/customer/{customerId}")
    suspend fun getPendingShipments(
        @Path("customerId") customerId: Int
    ): Response<List<ShipmentDto>>

    @GET("api/customers/{customerId}/bookings")
    suspend fun getCustomerBookings(
        @Path("customerId") customerId: Int
    ): Response<List<BookingDto>>

    @POST("api/trucks/available")
    suspend fun getAvailableTrucks(
        @Query("shipmentId") shipmentId: Int
    ): Response<List<AvailabilityDto>>

    //---------- REQUESTS AAAAA
    @POST("api/request/send")
    suspend fun sendRequest(
    @Query("shipmentId") shipmentId: Int,
    @Query("driverId") driverId: Int,
    @Query("routeId") routeId: Int
): Response<String>

    @POST("api/requests/decline")
    suspend fun declineRequest(
        @Query("requestId") requestId: Int
    ): Response<DeclineResponseDto>

    @POST("api/drivers/accept-request")
    suspend fun acceptRequest(
        @Query("requestId") requestId: Int
    ): Response<AcceptResponseDto>

    //----------- DRIVER BOOKINGS -------------//
    @GET("api/drivers/{driverId}/bookings/assigned")
    suspend fun getConfirmedBookings(@Path("driverId") driverId: Int): Response<List<BookingData>>

    @GET("api/drivers/{driverId}/bookings/in-transit")
    suspend fun getInTransitBookings(@Path("driverId") driverId: Int): Response<List<BookingData>>

    @GET("api/drivers/{driverId}/bookings/completed")
    suspend fun getCompletedBookings(@Path("driverId") driverId: Int): Response<List<BookingData>>

    @GET("api/drivers/{driverId}/bookings/canceled")
    suspend fun getCanceledBookings(@Path("driverId") driverId: Int): Response<List<BookingData>>


    //---------- REACHED FUNC -----------------//
    @GET("api/checkpoints/{checkpointId}/shipments")
    suspend fun getShipmentsAtCheckpoint(
        @Path("checkpointId") checkpointId: Int,
        @Query("driverId") driverId: Int
    ): Response<CheckpointShipmentsResponse>

    @POST("api/checkpoints/{checkpointId}/confirm")
    suspend fun confirmCheckpointReached(
        @Path("checkpointId") checkpointId: Int,
        @Query("driverId") driverId: Int
    ): Response<CheckpointConfirmResponse>

    @POST("api/trips/start/{checkpointId}")
    suspend fun startTrip(
        @Path("checkpointId") checkpointId: Int,
        @Query("driverId") driverId: Int
    ): Response<StartTripResponse>



}
