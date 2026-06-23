
package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Cuenta(
    val id:           String = "",
    @SerializedName("user_id")
    val userId:       String = "",
    @SerializedName("tipo_cuenta")
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
    @SerializedName("cliente_id")
    val userId:        String = "",
    @SerializedName("saldo_capital")
    val saldo:         Double = 0.0,
    @SerializedName("tea")
    val tasaInteres:   Double = 0.0,
    @SerializedName("tipo_cuenta")
    val tipoCuenta:    String = "",
    @SerializedName("moneda")
    val moneda:        String = ""
) {
    val metaAhorro:    Double = 10000.0
    fun porcentaje() = (saldo / metaAhorro).coerceIn(0.0, 1.0).toFloat()
}

data class MovimientoCore(
    val id: String = "",
    @SerializedName("cod_operacion")
    val codOperacion: String = "",
    @SerializedName("cliente_id")
    val clienteId: String = "",
    @SerializedName("cod_cuenta")
    val codCuenta: String = "",
    val tipo: String = "",
    val concepto: String = "",
    val canal: String = "",
    val monto: Double = 0.0,
    val moneda: String = "PEN",
    @SerializedName("fecha_operacion")
    val fechaOperacion: String = ""
)
