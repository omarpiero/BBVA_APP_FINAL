package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Comercio
import com.example.appbanco_s8.data.model.PlinTransaccion
import com.example.appbanco_s8.data.remote.RetrofitClient

class PlinRepository {
    private val api = RetrofitClient.api

    suspend fun getComercios(token: String): Result<List<Comercio>> = try {
        val r = api.getComercios("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getPlinTransacciones(token: String): Result<List<PlinTransaccion>> = try {
        val r = api.getPlinTransacciones("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}
