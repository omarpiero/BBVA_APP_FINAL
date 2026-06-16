package com.example.appbanco_s8.data.remote

import com.example.appbanco_s8.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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
        @Query("select") select: String = "*",
        @Query("order")  order:  String = "tipo.asc"
    ): Response<List<Cuenta>>

    @GET("rest/v1/transacciones")
    suspend fun getTransacciones(
        @Header("Authorization") token:    String,
        @Query("cuenta_id")      cuentaId: String,
        @Query("select")         select:   String = "*",
        @Query("order")          order:    String = "fecha.desc",
        @Query("limit")          limit:    Int    = 10
    ): Response<List<Transaccion>>

    @GET("rest/v1/cuentas_ahorro")
    suspend fun getCuentaAhorro(
        @Header("Authorization") token:  String,
        @Query("select")         select: String = "*",
        @Query("limit")          limit:  Int    = 1
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
        @Query("select") select: String = "id,nombres,apellidos",
        @Query("limit") limit: Int = 1
    ): Response<List<Asesor>>

    @POST("rest/v1/solicitudes_credito")
    suspend fun postSolicitudCredito(
        @Header("Authorization") token: String,
        @Body solicitud: SolicitudCreditoRequest
    ): Response<Unit>
}