
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.AuthResponse
import com.example.appbanco_s8.data.model.LoginRequest
import com.example.appbanco_s8.data.model.LockoutRpcRequest
import com.example.appbanco_s8.data.model.LockoutStatusResponse
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

    suspend fun obtenerEstadoBloqueo(email: String): Result<LockoutStatusResponse> {
        return try {
            val response = RetrofitClient.api.bbvaObtenerEstadoBloqueo(
                LockoutRpcRequest(email, "cliente")
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estado de bloqueo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarIntentoFallido(email: String): Result<LockoutStatusResponse> {
        return try {
            val response = RetrofitClient.api.bbvaRegistrarIntentoFallido(
                LockoutRpcRequest(email, "cliente")
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar intento fallido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetearIntentosFallidos(email: String): Result<Unit> {
        return try {
            val response = RetrofitClient.api.bbvaResetearIntentosFallidos(
                LockoutRpcRequest(email, "cliente")
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al resetear intentos fallidos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}