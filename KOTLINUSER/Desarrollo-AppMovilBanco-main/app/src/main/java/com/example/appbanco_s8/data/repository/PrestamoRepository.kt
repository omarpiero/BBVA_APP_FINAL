
package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.Prestamo
import com.example.appbanco_s8.data.remote.RetrofitClient

class PrestamoRepository {
    private val api = RetrofitClient.api

    suspend fun getPrestamos(token: String): Result<List<Prestamo>> = try {
        val r = api.getPrestamos("Bearer $token")
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun solicitarPrestamo(token: String, clienteId: String, monto: Double, plazo: Int): Result<Unit> = try {
        // 1. Obtener asesor_id (por restricción de BD)
        val asesoresRes = api.getAsesores("Bearer $token")
        if (!asesoresRes.isSuccessful || asesoresRes.body().isNullOrEmpty()) {
            throw Exception("No se encontró asesor disponible")
        }
        val asesorId = asesoresRes.body()!![0].id

        // 2. Enviar solicitud
        val request = com.example.appbanco_s8.data.model.SolicitudCreditoRequest(
            asesor_id = asesorId,
            cliente_id = clienteId,
            monto_solicitado = monto,
            plazo_meses = plazo
        )
        val res = api.postSolicitudCredito("Bearer $token", request)
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${res.code()}: ${res.errorBody()?.string()}"))
    } catch (e: Exception) { Result.failure(e) }
}