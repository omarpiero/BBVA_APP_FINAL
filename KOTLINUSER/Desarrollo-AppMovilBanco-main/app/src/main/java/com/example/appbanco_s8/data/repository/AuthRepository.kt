
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.AuthResponse
import com.example.appbanco_s8.data.model.LoginRequest
import com.example.appbanco_s8.data.remote.RetrofitClient

class AuthRepository {
    private val api = RetrofitClient.authApi

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(body = LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.error != null)
                    Result.failure(Exception(body.errorDescription ?: "Error de autenticación"))
                else
                    Result.success(body)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        }
    }
}