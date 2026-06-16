
package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Prestamo(
    val id:                  String = "",
    @SerializedName("user_id")
    val userId:              String = "",
    val tipo:                String = "",
    @SerializedName("numero_enmascarado")
    val numeroEnmascarado:   String = "",
    @SerializedName("capital_total")
    val capitalTotal:        Double = 0.0,
    @SerializedName("capital_pendiente")
    val capitalPendiente:    Double = 0.0,
    @SerializedName("cuota_numero")
    val cuotaNumero:         Int    = 0,
    @SerializedName("cuotas_total")
    val cuotasTotal:         Int    = 0,
    @SerializedName("fecha_limite")
    val fechaLimite:         String = "",
    @SerializedName("capital_cuota")
    val capitalCuota:        Double = 0.0,
    @SerializedName("intereses_cuota")
    val interesesCuota:      Double = 0.0,
    @SerializedName("seguros_cuota")
    val segurosCuota:        Double = 0.0
) {
    fun totalCuota() = capitalCuota + interesesCuota + segurosCuota
    fun progreso()   = (1.0 - capitalPendiente / capitalTotal).coerceIn(0.0, 1.0).toFloat()
}

data class SolicitudCreditoRequest(
    val asesor_id: String,
    val cliente_id: String,
    val canal: String = "cliente",
    val monto_solicitado: Double,
    val plazo_meses: Int,
    val estado: String = "enviado"
)

data class Asesor(
    val id: String,
    val nombres: String,
    val apellidos: String
)