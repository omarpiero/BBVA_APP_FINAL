package com.example.appbanco_s8.data.repository

import com.example.appbanco_s8.data.model.ClienteCore
import com.example.appbanco_s8.data.model.CreditoCore
import com.example.appbanco_s8.data.model.SolicitudCreditoEstado
import com.example.appbanco_s8.data.model.SolicitudCreditoRequest
import com.example.appbanco_s8.data.model.SyncOutboxRequest
import com.example.appbanco_s8.data.remote.RetrofitClient
import kotlin.math.pow

class PrestamoRepository {
    private val api = RetrofitClient.api
    private val asesorDemoId = "1ad6c5af-d359-43a0-b317-8a4069fc412e"

    suspend fun getPrestamos(token: String, email: String): Result<List<CreditoCore>> = try {
        val cliente = resolverCliente(token, email)
        val r = api.getCreditosCore(
            token = "Bearer $token",
            clienteIdEq = "eq.${cliente.id}"
        )
        if (!r.isSuccessful) {
            Result.failure(Exception("Error ${r.code()}: ${r.errorBody()?.string()}"))
        } else {
            val creditos = (r.body() ?: emptyList()).map { credito ->
                val cuotas = api.getCronogramaCredito(
                    token = "Bearer $token",
                    codCuentaEq = "eq.${credito.cod_cuenta_credito}"
                )
                credito.copy(cuotas = if (cuotas.isSuccessful) cuotas.body() ?: emptyList() else emptyList())
            }
            Result.success(creditos)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSolicitudes(
        token: String,
        email: String
    ): Result<List<SolicitudCreditoEstado>> = try {
        val cliente = resolverCliente(token, email)
        val r = api.getSolicitudesCredito(
            token = "Bearer $token",
            clienteIdEq = "eq.${cliente.id}"
        )
        if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception("Error ${r.code()}: ${r.errorBody()?.string()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun solicitarPrestamo(
        token: String,
        email: String,
        monto: Double,
        plazo: Int,
        destino: String,
        garantia: String,
        tea: Double,
        gastosMensuales: Double
    ): Result<Unit> = try {
        val cliente = resolverCliente(token, email)
        val asesoresRes = api.getAsesores(
            token = "Bearer $token",
            asesorIdEq = "eq.$asesorDemoId"
        )
        if (!asesoresRes.isSuccessful || asesoresRes.body().isNullOrEmpty()) {
            throw Exception("No se encontro asesor disponible")
        }

        val asesorId = asesoresRes.body()!!.first().id
        val cuota = calcularCuota(monto, plazo, tea)
        val request = SolicitudCreditoRequest(
            asesor_id = asesorId,
            cliente_id = cliente.id,
            tipo_negocio = cliente.tipo_negocio,
            nombre_negocio = cliente.nombre_negocio,
            antiguedad_negocio_meses = cliente.antiguedad_negocio_meses,
            ingresos_estimados = cliente.ingresos_estimados,
            gastos_mensuales = gastosMensuales,
            monto_solicitado = monto,
            plazo_meses = plazo,
            garantia = garantia,
            destino_credito = destino,
            cuota_estimada = cuota,
            tea_referencial = tea,
            lat_captura = cliente.lat,
            lng_captura = cliente.lng
        )

        val res = api.postSolicitudCredito("Bearer $token", solicitud = request)
        if (res.isSuccessful) {
            val creada = res.body()?.firstOrNull()
            if (creada != null) {
                api.postSyncOutbox(
                    token = "Bearer $token",
                    request = SyncOutboxRequest(
                        entidad_id = creada.id,
                        payload = mapOf(
                            "canal" to "cliente",
                            "cliente_id" to cliente.id,
                            "asesor_id" to asesorId,
                            "monto_solicitado" to monto,
                            "plazo_meses" to plazo,
                            "tea_referencial" to tea,
                            "estado" to creada.estado
                        )
                    )
                )
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Error ${res.code()}: ${res.errorBody()?.string()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun resolverCliente(token: String, email: String): ClienteCore {
        val r = api.getClientePorEmail(
            token = "Bearer $token",
            emailEq = "eq.$email"
        )
        if (r.isSuccessful && !r.body().isNullOrEmpty()) {
            return r.body()!!.first()
        }
        throw Exception("No se encontro un cliente core vinculado a $email")
    }

    private fun calcularCuota(monto: Double, plazoMeses: Int, teaAnual: Double): Double {
        if (monto <= 0.0 || plazoMeses <= 0) return 0.0
        val tea = teaAnual / 100.0
        val tem = (1.0 + tea).pow(1.0 / 12.0) - 1.0
        val factor = (1.0 + tem).pow(plazoMeses.toDouble())
        return monto * ((tem * factor) / (factor - 1.0))
    }
}
