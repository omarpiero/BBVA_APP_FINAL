package com.example.appbanco_s8.data.remote

import com.example.appbanco_s8.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // ── Comercios ────────────────────────────────────────────
    @GET("rest/v1/comercios")
    suspend fun getComercios(
        @Header("Authorization") token: String,
        @Query("activo") activo: String = "eq.true",
        @Query("select") select: String = "*",
        @Query("order") order: String = "nombre.asc"
    ): Response<List<Comercio>>

    // ── PLIN Transacciones ───────────────────────────────────
    @GET("rest/v1/plin_transacciones")
    suspend fun getPlinTransacciones(
        @Header("Authorization") token: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "fecha.desc",
        @Query("limit") limit: Int = 20
    ): Response<List<PlinTransaccion>>

    // ── Pagos en Línea ───────────────────────────────────────
    @GET("rest/v1/pagos_en_linea")
    suspend fun getPagosEnLinea(
        @Header("Authorization") token: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "fecha.desc",
        @Query("limit") limit: Int = 20
    ): Response<List<PagoEnLinea>>

    // ── RPC: Procesar pago en línea (atómico) ────────────────
    @POST("rest/v1/rpc/procesar_pago_en_linea")
    suspend fun procesarPagoEnLinea(
        @Header("Authorization") token: String,
        @Body body: PagoEnLineaRpcRequest
    ): Response<String>

    @GET("rest/v1/cuentas")
    suspend fun getCuentas(
        @Header("Authorization") token: String,
        @Query("select") select: String = "id,user_id,numero_cuenta,tipo_cuenta,saldo",
        @Query("order")  order:  String = "tipo_cuenta.asc"
    ): Response<List<Cuenta>>

    @GET("rest/v1/transacciones")
    suspend fun getTransacciones(
        @Header("Authorization") token:    String,
        @Query("cuenta_id")      cuentaId: String,
        @Query("select")         select:   String = "*",
        @Query("order")          order:    String = "fecha.desc",
        @Query("limit")          limit:    Int    = 10
    ): Response<List<Transaccion>>

    @GET("rest/v1/cr_cuentas_ahorro")
    suspend fun getCuentaAhorro(
        @Header("Authorization") token: String,
        @Query("cliente_id") clienteIdEq: String,
        @Query("select") select: String = "*"
    ): Response<List<CuentaAhorro>>

    @GET("rest/v1/tarjetas")
    suspend fun getTarjetas(
        @Header("Authorization") token:  String,
        @Query("select")         select: String = "*"
    ): Response<List<Tarjeta>>

    @GET("rest/v1/prestamos")
    suspend fun getPrestamos(
        @Header("Authorization") token:  String,
        @Query("select")         select: String = "*"
    ): Response<List<Prestamo>>

    @GET("rest/v1/cr_creditos")
    suspend fun getCreditosCore(
        @Header("Authorization") token: String,
        @Query("cliente_id") clienteIdEq: String,
        @Query("select") select: String = "id,cod_cuenta_credito,producto,monto_desembolsado,saldo_capital,saldo_total,estado,fecha_desembolso,tea,cuotas_total,cuotas_pagadas",
        @Query("order") order: String = "sync_at.desc"
    ): Response<List<CreditoCore>>

    @GET("rest/v1/cr_cronograma_pagos")
    suspend fun getCronogramaCredito(
        @Header("Authorization") token: String,
        @Query("cod_cuenta_credito") codCuentaEq: String,
        @Query("select") select: String = "id,cod_cuenta_credito,nro_cuota,fecha_vencimiento,monto_cuota,monto_capital,monto_interes,saldo,estado_cuota",
        @Query("order") order: String = "nro_cuota.asc",
        @Query("limit") limit: Int = 120
    ): Response<List<CronogramaCuota>>

    @GET("rest/v1/pagos")
    suspend fun getPagos(
        @Header("Authorization") token:  String,
        @Query("select")         select: String = "*",
        @Query("order")          order:  String = "fecha.desc",
        @Query("limit")          limit:  Int    = 20
    ): Response<List<Pago>>

    @GET("rest/v1/perfiles")
    suspend fun getPerfil(
        @Header("Authorization") token: String,
        @Query("user_id") userIdEq: String
    ): Response<List<Perfil>>

    @POST("rest/v1/perfiles")
    suspend fun createPerfil(
        @Header("Authorization") token: String,
        @Body perfil: Perfil
    ): Response<Unit>

    @PATCH("rest/v1/perfiles")
    suspend fun updatePerfil(
        @Header("Authorization") token: String,
        @Query("user_id") userIdEq: String,
        @Body perfil: Perfil
    ): Response<Unit>

    @GET("rest/v1/asesores")
    suspend fun getAsesores(
        @Header("Authorization") token: String,
        @Query("id") asesorIdEq: String? = null,
        @Query("select") select: String = "id,nombres,apellidos",
        @Query("limit") limit: Int = 1
    ): Response<List<Asesor>>

    @GET("rest/v1/clientes")
    suspend fun getClientePorEmail(
        @Header("Authorization") token: String,
        @Query("email") emailEq: String,
        @Query("select") select: String = "id,nombres,apellidos,email,numero_documento,tipo_negocio,nombre_negocio,antiguedad_negocio_meses,ingresos_estimados,lat,lng",
        @Query("limit") limit: Int = 1
    ): Response<List<ClienteCore>>

    @GET("rest/v1/solicitudes_credito")
    suspend fun getSolicitudesCredito(
        @Header("Authorization") token: String,
        @Query("cliente_id") clienteIdEq: String,
        @Query("select") select: String = "id,numero_expediente,estado,monto_solicitado,monto_aprobado,plazo_meses,cuota_estimada,created_at",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SolicitudCreditoEstado>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/solicitudes_credito")
    suspend fun postSolicitudCredito(
        @Header("Authorization") token: String,
        @Query("select") select: String = "id,numero_expediente,estado,monto_solicitado,monto_aprobado,plazo_meses,cuota_estimada,created_at",
        @Body solicitud: SolicitudCreditoRequest
    ): Response<List<SolicitudCreditoEstado>>

    @GET("rest/v1/cr_movimientos")
    suspend fun getMovimientosCore(
        @Header("Authorization") token: String,
        @Query("cliente_id") clienteIdEq: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "fecha_operacion.desc"
    ): Response<List<MovimientoCore>>

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/sync_outbox")
    suspend fun postSyncOutbox(
        @Header("Authorization") token: String,
        @Body request: SyncOutboxRequest
    ): Response<Unit>

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/operaciones_cliente")
    suspend fun postOperacionesCliente(
        @Header("Authorization") token: String,
        @Body request: OperacionClienteRequest
    ): Response<Unit>

    @POST("rest/v1/rpc/bbva_obtener_estado_bloqueo")
    suspend fun bbvaObtenerEstadoBloqueo(
        @Body body: LockoutRpcRequest
    ): Response<LockoutStatusResponse>

    @POST("rest/v1/rpc/bbva_registrar_intento_fallido")
    suspend fun bbvaRegistrarIntentoFallido(
        @Body body: LockoutRpcRequest
    ): Response<LockoutStatusResponse>

    @POST("rest/v1/rpc/bbva_resetear_intentos_fallidos")
    suspend fun bbvaResetearIntentosFallidos(
        @Body body: LockoutRpcRequest
    ): Response<Unit>
}
