package com.example.appbanco_s8.data.remote

import com.example.appbanco_s8.data.model.AuthResponse
import com.example.appbanco_s8.data.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest
    ): Response<AuthResponse>
}

