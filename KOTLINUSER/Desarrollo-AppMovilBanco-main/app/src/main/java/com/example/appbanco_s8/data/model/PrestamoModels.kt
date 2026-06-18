
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

data class CreditoCore(
    val id: String = "",
    val cod_cuenta_credito: String = "",
    val producto: String? = null,
    val monto_desembolsado: Double = 0.0,
    val saldo_capital: Double = 0.0,
    val saldo_total: Double = 0.0,
    val estado: String = "",
    val fecha_desembolso: String? = null,
    val tea: Double? = null,
    val cuotas_total: Int? = null,
    val cuotas_pagadas: Int? = null,
    val cuotas: List<CronogramaCuota> = emptyList()
)

data class CronogramaCuota(
    val id: String = "",
    val cod_cuenta_credito: String = "",
    val nro_cuota: Int = 0,
    val fecha_vencimiento: String = "",
    val monto_cuota: Double = 0.0,
    val monto_capital: Double = 0.0,
    val monto_interes: Double = 0.0,
    val saldo: Double = 0.0,
    val estado_cuota: String = ""
)

data class SolicitudCreditoRequest(
    val asesor_id: String,
    val cliente_id: String,
    val canal: String = "cliente",
    val tipo_negocio: String? = null,
    val nombre_negocio: String? = null,
    val antiguedad_negocio_meses: Int? = null,
    val ingresos_estimados: Double? = null,
    val gastos_mensuales: Double? = null,
    val monto_solicitado: Double,
    val plazo_meses: Int,
    val garantia: String? = null,
    val destino_credito: String? = null,
    val cuota_estimada: Double? = null,
    val tea_referencial: Double = 43.92,
    val lat_captura: Double? = null,
    val lng_captura: Double? = null,
    val estado: String = "enviado"
)

data class Asesor(
    val id: String,
    val nombres: String,
    val apellidos: String
)

data class ClienteCore(
    val id: String,
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val numero_documento: String = "",
    val tipo_negocio: String? = null,
    val nombre_negocio: String? = null,
    val antiguedad_negocio_meses: Int? = null,
    val ingresos_estimados: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class SolicitudCreditoEstado(
    val id: String = "",
    val numero_expediente: String? = null,
    val estado: String = "enviado",
    val monto_solicitado: Double = 0.0,
    val monto_aprobado: Double? = null,
    val plazo_meses: Int? = null,
    val cuota_estimada: Double? = null,
    val created_at: String? = null
)

data class SyncOutboxRequest(
    val entidad: String = "solicitudes_credito",
    val entidad_id: String,
    val operacion: String = "create",
    val payload: Map<String, Any?>
)
