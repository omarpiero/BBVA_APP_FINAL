
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.*
import com.example.appbanco_s8.data.remote.RetrofitClient

class CuentaRepository {
    private val api = RetrofitClient.api

    suspend fun getCuentas(token: String): Result<List<Cuenta>> = try {
        val r = api.getCuentas("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getTransacciones(token: String, cuentaId: String): Result<List<Transaccion>> = try {
        val r = api.getTransacciones("Bearer $token", "eq.$cuentaId")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCuentaAhorro(token: String): Result<CuentaAhorro?> = try {
        val r = api.getCuentaAhorro("Bearer $token")
        if (r.isSuccessful) Result.success(r.body()?.firstOrNull())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}