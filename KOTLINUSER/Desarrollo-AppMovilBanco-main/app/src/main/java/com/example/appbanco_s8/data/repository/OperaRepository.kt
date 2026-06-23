package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.OperacionClienteRequest
import com.example.appbanco_s8.data.model.SyncOutboxRequest
import com.example.appbanco_s8.data.remote.RetrofitClient
import java.util.UUID

class OperaRepository {
    private val api = RetrofitClient.api

    suspend fun registrarOperacion(
        token: String,
        email: String,
        codCuentaOrigen: String,
        codCuentaDestino: String,
        tipo: String,
        monto: Double
    ): Result<Unit> = try {
        // 1. Resolve client ID by email
        val clienteRes = api.getClientePorEmail("Bearer $token", "eq.$email")
        val cliente = clienteRes.body()?.firstOrNull() ?: throw Exception("Cliente no encontrado para $email")

        val opId = UUID.randomUUID().toString()

        // 2. Insert into operaciones_cliente
        val reqOp = OperacionClienteRequest(
            clienteId = cliente.id,
            codCuentaOrigen = codCuentaOrigen,
            codCuentaDestino = codCuentaDestino,
            tipo = tipo,
            monto = monto
        )
        val resOp = api.postOperacionesCliente("Bearer $token", reqOp)

        // 3. Insert into sync_outbox
        val payload = mapOf(
            "cliente_id" to cliente.id,
            "cod_cuenta_origen" to codCuentaOrigen,
            "cod_cuenta_destino" to codCuentaDestino,
            "tipo" to tipo,
            "monto" to monto,
            "moneda" to "PEN"
        )
        val reqSync = SyncOutboxRequest(
            entidad = "operaciones_cliente",
            entidad_id = opId,
            operacion = "create",
            payload = payload
        )
        val resSync = api.postSyncOutbox("Bearer $token", reqSync)

        if (resOp.isSuccessful && resSync.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Error al registrar operacion. Op: ${resOp.code()}, Sync: ${resSync.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
