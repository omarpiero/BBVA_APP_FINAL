
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Tarjeta
import com.example.appbanco_s8.data.remote.RetrofitClient

class TarjetaRepository {
    private val api = RetrofitClient.api

    suspend fun getTarjetas(token: String): Result<List<Tarjeta>> = try {
        val r = api.getTarjetas("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}