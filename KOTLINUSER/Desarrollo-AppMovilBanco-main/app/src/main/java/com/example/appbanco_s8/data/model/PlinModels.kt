package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Comercio(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    val activo: Boolean = true
)

data class PagoEnLinea(
    val id: String = "",
    @SerializedName("user_id")
    val userId: String = "",
    @SerializedName("comercio_id")
    val comercioId: String = "",
    @SerializedName("comercio_nombre")
    val comercioNombre: String = "",
    @SerializedName("cuenta_origen_id")
    val cuentaOrigenId: String = "",
    val monto: Double = 0.0,
    val comision: Double = 0.0,
    @SerializedName("medio_pago")
    val medioPago: String = "Plin",
    val estado: String = "completado",
    @SerializedName("numero_operacion")
    val numeroOperacion: String = "",
    val fecha: String = ""
)

data class PagoEnLineaRpcRequest(
    @SerializedName("p_user_id")
    val userId: String,
    @SerializedName("p_comercio_id")
    val comercioId: String,
    @SerializedName("p_comercio_nombre")
    val comercioNombre: String,
    @SerializedName("p_cuenta_origen_id")
    val cuentaOrigenId: String,
    @SerializedName("p_monto")
    val monto: Double,
    @SerializedName("p_numero_operacion")
    val numeroOperacion: String
)

data class PlinTransaccion(
    val id: String = "",
    @SerializedName("user_id")
    val userId: String = "",
    val tipo: String = "",
    val destinatario: String? = null,
    val comercio: String? = null,
    val monto: Double = 0.0,
    val estado: String = "completado",
    @SerializedName("medio_pago")
    val medioPago: String = "Plin",
    @SerializedName("cuenta_origen_id")
    val cuentaOrigenId: String? = null,
    @SerializedName("numero_operacion")
    val numeroOperacion: String = "",
    val fecha: String = ""
) {
    fun esFinalizado() = estado == "completado" || estado == "fallido"
    fun esPendiente() = estado == "pendiente"
    fun montoFormateado() = "S/ %,.0f".format(monto)
}
