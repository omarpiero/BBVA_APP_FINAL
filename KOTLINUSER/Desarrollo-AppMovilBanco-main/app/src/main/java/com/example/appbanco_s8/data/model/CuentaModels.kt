
package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Cuenta(
    val id:           String = "",
    @SerializedName("user_id")
    val userId:       String = "",
    val tipo:         String = "",
    @SerializedName("numero_cuenta")
    val numeroCuenta: String = "",
    val saldo:        Double = 0.0
)

data class Transaccion(
    val id:          String = "",
    @SerializedName("cuenta_id")
    val cuentaId:    String = "",
    val tipo:        String = "",
    val descripcion: String = "",
    val monto:       Double = 0.0,
    val fecha:       String = ""
) {
    fun esDebito()        = tipo == "debito"
    fun montoFormateado() = "S/ %,.2f".format(monto)
}

data class CuentaAhorro(
    val id:            String = "",
    @SerializedName("user_id")
    val userId:        String = "",
    val saldo:         Double = 0.0,
    @SerializedName("meta_ahorro")
    val metaAhorro:    Double = 0.0,
    @SerializedName("tasa_interes")
    val tasaInteres:   Double = 0.0,
    @SerializedName("fecha_apertura")
    val fechaApertura: String = ""
) {
    fun porcentaje() = (saldo / metaAhorro).coerceIn(0.0, 1.0).toFloat()
}