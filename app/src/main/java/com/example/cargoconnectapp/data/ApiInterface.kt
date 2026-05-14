package com.example.cargoconnectapp.data

import com.example.cargoconnectapp.ui.models.datamodels.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

    @POST("api/driver/preview-route-times")
    suspend fun previewRouteTimes(
        @Body request: CreateRouteRequest
    ): Response<List<RouteCheckpointRequest>>
    @POST("api/driver/save-route")
    suspend fun saveRoute(
        @Body request: CreateRouteRequest
    ): Response<SaveRouteResponse>

    @POST("api/driver/update-route")
    suspend fun updateRoute(@Body request: UpdateRouteRequest): Response<RouteIdResponse>

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

    @POST("api/drivers/location")
    suspend fun updateDriverLocation(@Body request: DriverLocationRequestDto
    ): Response<DriverLocationRequestDto>

    @GET("api/driver/track-route-points/{bookingId}")
    suspend fun trackDriverRoute(
        @Path("bookingId") bookingId: Int
    ): Response<List<RoutePointDto>>

    @GET("api/drivers/location/{bookingId}")
    suspend fun getDriverLocation(
        @Path("bookingId") bookingId: Int
    ): Response<DriverLocationResponseDto>

    @POST("api/shipments/update-route")
    suspend fun updateShipmentRoute(
        @Body request: UpdateShipmentRouteRequest
    ): Response<ShipmentUpdateResponse>

    @DELETE("api/shipments/delete/{shipmentId}")
    suspend fun deleteShipment(
        @Path("shipmentId") shipmentId: Int
    ): Response<String>

    @GET("api/shipments/details/{shipmentId}")
    suspend fun getShipmentDetails(
        @Path("shipmentId") shipmentId: Int
    ): Response<UpdateShipmentRouteRequest>

    // ── Shipments ─────────────────────────────────────────────────────────────
    @POST("api/shipments/draft/{customerId}")
    suspend fun createOrGetDraft(@Path("customerId") customerId: Int): Response<CreateShipmentResponse>

    @POST("api/shipments/add/package")
    suspend fun addPackage(@Body request: PackageWithAttributesRequest): Response<Unit>

    @POST("api/shipments/complete")
    suspend fun completeShipment(@Body request: CompleteShipmentRequest): Response<Unit>

    @GET("api/customers/{customerId}/recent-packages")
    suspend fun getRecentPackages(
        @Path("customerId") customerId: Int
    ): Response<List<RecentPackageDto>>

    @POST("api/bookings/{bookingId}/pickup")
    suspend fun pickupBooking(
        @Path("bookingId") bookingId: Int,
        @Query("driverId") driverId: Int
    ): Response<BookingActionResponse>

    @POST("api/bookings/{bookingId}/deliver")
    suspend fun deliverBooking(
        @Path("bookingId") bookingId: Int,
        @Query("driverId") driverId: Int
    ): Response<BookingActionResponse>

    @GET("api/shipments/pending/customer/{customerId}")
    suspend fun getPendingShipments(
        @Path("customerId") customerId: Int
    ): Response<List<ShipmentDto>>

    @GET("api/customers/{customerId}/bookings")
    suspend fun getCustomerBookings(
        @Path("customerId") customerId: Int
    ): Response<List<CustomerBookingDto>>

    @POST("api/trucks/available")
    suspend fun getAvailableTrucks(
        @Query("shipmentId") shipmentId: Int
    ): Response<List<AvailabilityDto>>

    //---------- REQUESTS AAAAA
    @POST("api/request/send")
    suspend fun sendRequest(
    @Query("shipmentId") shipmentId: Int,
    @Query("driverId") driverId: Int,
    @Query("routeId") routeId: Int,
    @Query("fare") fare: Double,
    @Query("checkpointId") checkpointId: Int,
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

    @GET("api/activity/notifications/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: Int
    ): Response<List<NotificationDto>>

    @POST("api/driver/add-delay")
    suspend fun reportDelay(
        @Query("checkpointId") checkpointId: Int,
        @Query("hours") hours: Double,
        @Query("reason") reason: String
    ): Response<ResponseBody>

    @GET("api/checkpoints/estimated-arrival/{checkpointId}")
    suspend fun getEstimatedArrivalDate(
        @Path("checkpointId") checkpointId: Int
    ): Response<String>

    @POST("api/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: Int
    ): Response<BookingActionResponse>

    @GET("api/reviews/{userId}")
    suspend fun getAllReviews(@Path("userId") userId: Int): Response<List<ReviewDto>>

    @POST("api/reviews/send")
    suspend fun sendReview(
        @Query("fromUserId") fromUserId: Int,
        @Query("toUserId") toUserId: Int,
        @Query("tripId") tripId: Int,
        @Query("rating") rating: Int,
        @Query("comments") comments: String
    ): Response<ReviewSentResponse>

    @GET("api/reviews/rating/{userId}")
    suspend fun getAverageRating(@Path("userId") userId: Int): Response<RatingResponse>

    @GET("api/reviews/booking-info/{bookingId}")
    suspend fun getReviewInfoForBooking(@Path("bookingId") bookingId: Int): Response<ReviewInfoDto>

    @GET("api/reviews/can-review")
    suspend fun canReview(
        @Query("reviewerUserId") reviewerUserId: Int,
        @Query("bookingId") bookingId: Int
    ): Response<CanReviewDto>

    //----------------------------------- CHAT SYSTEM

    @POST("api/chat/thread")
    suspend fun getOrCreateThread(
        @Query("customerUserId") customerUserId: Int,
        @Query("driverUserId") driverUserId: Int
    ): Response<ThreadResponse>

    @GET("api/chat/list/{userId}")
    suspend fun getChatList(@Path("userId") userId: Int): Response<List<ChatThreadDto>>

    @GET("api/chat/history/{threadId}")
    suspend fun getChatHistory(
        @Path("threadId") threadId: Int,
        @Query("userId") userId: Int
    ): Response<List<ChatMessageDto>>

    @POST("api/chat/send")
    suspend fun sendMessage(
        @Query("threadId") threadId: Int,
        @Query("senderUserId") senderUserId: Int,
        @Query("messageText") messageText: String
    ): Response<SendMessageResponse>

    @GET("api/drivers/{driverId}/truck-info")
    suspend fun getTruckInfo(@Path("driverId") driverId: Int): Response<TruckInfoResponse>

    @GET("api/customers/{customerId}/requests")
    suspend fun getCustomerRequests(
        @Path("customerId") customerId: Int
    ): Response<List<CustomerRequestDto>>
}
