package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.PagoEnLineaRpcRequest
import com.example.appbanco_s8.data.remote.RetrofitClient

class PagoEnLineaRepository {
    private val api = RetrofitClient.api

    suspend fun procesarPago(
        token: String,
        userId: String,
        comercioId: String,
        comercioNombre: String,
        cuentaOrigenId: String,
        monto: Double,
        numeroOperacion: String
    ): Result<String> = try {
        val request = PagoEnLineaRpcRequest(
            userId = userId,
            comercioId = comercioId,
            comercioNombre = comercioNombre,
            cuentaOrigenId = cuentaOrigenId,
            monto = monto,
            numeroOperacion = numeroOperacion
        )
        val r = api.procesarPagoEnLinea("Bearer $token", request)
        if (r.isSuccessful) {
            Result.success(r.body() ?: "")
        } else {
            val errorBody = r.errorBody()?.string() ?: "Error ${r.code()}"
            Result.failure(Exception(errorBody))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
