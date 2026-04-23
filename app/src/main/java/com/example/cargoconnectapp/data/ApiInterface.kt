package com.example.cargoconnectapp.data

import com.example.cargoconnectapp.ui.models.datamodels.AuthResponse
import com.example.cargoconnectapp.ui.models.datamodels.CreateRouteRequest
import com.example.cargoconnectapp.ui.models.datamodels.LoginRequest
import com.example.cargoconnectapp.ui.models.datamodels.RegisterRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.datamodels.SaveRouteResponse
import com.example.cargoconnectapp.ui.models.datamodels.UploadResponse
import com.example.cargoconnectapp.ui.models.datamodels.UserDataRequest
import com.example.cargoconnectapp.ui.models.datamodels.UserDataResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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
}