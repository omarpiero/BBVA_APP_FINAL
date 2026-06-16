
package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Tarjeta(
    val id:                  String = "",
    @SerializedName("user_id")
    val userId:              String = "",
    val tipo:                String = "",
    @SerializedName("numero_enmascarado")
    val numeroEnmascarado:   String = "",
    val estado:              String = "",
    @SerializedName("saldo_disponible")
    val saldoDisponible:     Double = 0.0,
    @SerializedName("cuenta_asociada")
    val cuentaAsociada:      String = ""
) {
    fun estaApagada() = estado == "apagada"
}

data class Pago(
    val id:               String = "",
    @SerializedName("user_id")
    val userId:           String = "",
    val servicio:         String = "",
    @SerializedName("numero_contrato")
    val numeroContrato:   String = "",
    val monto:            Double = 0.0,
    val estado:           String = "",
    val fecha:            String = ""
)