// Modelos de dominio del modulo Solicitud (M5).

/// Resultado de crear una solicitud (numero de expediente).
class SolicitudCreada {
  final String id;
  final String numeroExpediente;
  final String estado;
  const SolicitudCreada(this.id, this.numeroExpediente, this.estado);

  factory SolicitudCreada.fromJson(Map<String, dynamic> j) => SolicitudCreada(
        j['id'] as String? ?? '',
        j['numero_expediente'] as String? ?? '',
        j['estado'] as String? ?? 'enviado',
      );
}

/// Resumen de una solicitud para historial y tablero de estado (HU-20 / M9).
class SolicitudResumen {
  final String id;
  final String numeroExpediente;
  final String clienteNombre;
  final double montoSolicitado;
  final double montoAprobado;
  final int plazoMeses;
  final double cuotaEstimada;
  final double teaReferencial;
  final String estado;
  final String? condicionAdicional;
  final String? motivoRechazo;
  final String? createdAt;

  const SolicitudResumen({
    required this.id,
    required this.numeroExpediente,
    required this.clienteNombre,
    required this.montoSolicitado,
    required this.montoAprobado,
    required this.plazoMeses,
    required this.cuotaEstimada,
    required this.teaReferencial,
    required this.estado,
    this.condicionAdicional,
    this.motivoRechazo,
    this.createdAt,
  });

  factory SolicitudResumen.fromJson(Map<String, dynamic> j) => SolicitudResumen(
        id: j['id'] as String? ?? '',
        numeroExpediente: j['numero_expediente'] as String? ?? '',
        clienteNombre: j['cliente_nombre'] as String? ?? '',
        montoSolicitado: (j['monto_solicitado'] as num?)?.toDouble() ?? 0,
        montoAprobado: (j['monto_aprobado'] as num?)?.toDouble() ?? 0,
        plazoMeses: (j['plazo_meses'] as num?)?.toInt() ?? 0,
        cuotaEstimada: (j['cuota_estimada'] as num?)?.toDouble() ?? 0,
        teaReferencial: (j['tea_referencial'] as num?)?.toDouble() ?? 0,
        estado: j['estado'] as String? ?? 'enviado',
        condicionAdicional: j['condicion_adicional'] as String?,
        motivoRechazo: j['motivo_rechazo'] as String?,
        createdAt: j['created_at'] as String?,
      );
}
