
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Pago
import com.example.appbanco_s8.data.remote.RetrofitClient

class PagoRepository {
    private val api = RetrofitClient.api

    suspend fun getPagos(token: String): Result<List<Pago>> = try {
        val r = api.getPagos("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}