package com.example.cargoconnectapp.data

import com.example.cargoconnectapp.ui.models.datamodels.AuthResponse
import com.example.cargoconnectapp.ui.models.datamodels.LoginRequest
import com.example.cargoconnectapp.ui.models.datamodels.RegisterRequest
import com.example.cargoconnectapp.ui.models.datamodels.UploadResponse
import com.example.cargoconnectapp.ui.models.datamodels.UserDataResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
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

    @GET("api/users/getdata/{userId}")
    suspend fun getUserData(@Path("userId") userId: Int): Response<UserDataResponse>
}