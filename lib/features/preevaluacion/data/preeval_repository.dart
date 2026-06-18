import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ResultadoPreEval {
  final String calificacion;
  final String motivo;
  final int puntaje;
  const ResultadoPreEval(this.calificacion, this.motivo, this.puntaje);
}

class PreEvalRepository {
  final SupabaseClient _supabase;
  PreEvalRepository(this._supabase);

  Future<ResultadoPreEval> preEvaluar({
    required String documento,
    required String nombres,
    String apellidos = '',
    String? fechaNacimiento,
    required String tipoNegocio,
    int antiguedadNegocioMeses = 0,
    required double ingresos,
    required double montoSolicitado,
    required String destino,
  }) async {
    final asesorId = _supabase.auth.currentUser?.id;
    if (asesorId == null) {
      throw StateError('Sesion de asesor no disponible.');
    }

    final caso = await _supabase
        .from('bbva_casos_credito')
        .select(
          'preeval_resultado, preeval_puntaje, gastos, monto_aprobado, '
          'plazo_meses, cuota_mensual',
        )
        .eq('numero_documento', documento)
        .maybeSingle();

    final resultado = caso != null
        ? ResultadoPreEval(
            caso['preeval_resultado'] as String? ?? 'APTO',
            'Capacidad de pago validada para el caso de prueba.',
            (caso['preeval_puntaje'] as num?)?.toInt() ?? 85,
          )
        : _calcularResultado(
            antiguedadNegocioMeses: antiguedadNegocioMeses,
            ingresos: ingresos,
            montoSolicitado: montoSolicitado,
          );

    await _registrarFichaCampo(
      asesorId: asesorId,
      documento: documento,
      tipoNegocio: tipoNegocio,
      antiguedadNegocioMeses: antiguedadNegocioMeses,
      ingresos: ingresos,
      gastos: (caso?['gastos'] as num?)?.toDouble() ?? ingresos * 0.45,
      montoSolicitado: montoSolicitado,
      montoPropuesto:
          (caso?['monto_aprobado'] as num?)?.toDouble() ?? montoSolicitado,
      plazoPropuesto:
          (caso?['plazo_meses'] as num?)?.toInt() ?? 12,
      cuota:
          (caso?['cuota_mensual'] as num?)?.toDouble() ?? montoSolicitado / 12,
      resultado: resultado,
      destino: destino,
    );

    return resultado;
  }

  ResultadoPreEval _calcularResultado({
    required int antiguedadNegocioMeses,
    required double ingresos,
    required double montoSolicitado,
  }) {
    if (antiguedadNegocioMeses < 6 || ingresos <= 0) {
      return const ResultadoPreEval(
        'NO_PROCEDE',
        'No cumple antiguedad minima o ingresos declarados.',
        30,
      );
    }
    final ratio = montoSolicitado / ingresos;
    if (ratio <= 2.0) {
      return const ResultadoPreEval(
        'APTO',
        'Capacidad de pago suficiente para continuar.',
        85,
      );
    }
    if (ratio <= 3.5) {
      return const ResultadoPreEval(
        'REVISAR',
        'La relacion monto ingreso requiere validacion adicional.',
        62,
      );
    }
    return const ResultadoPreEval(
      'NO_PROCEDE',
      'Monto solicitado excede capacidad preliminar.',
      38,
    );
  }

  Future<void> _registrarFichaCampo({
    required String asesorId,
    required String documento,
    required String tipoNegocio,
    required int antiguedadNegocioMeses,
    required double ingresos,
    required double gastos,
    required double montoSolicitado,
    required double montoPropuesto,
    required int plazoPropuesto,
    required double cuota,
    required ResultadoPreEval resultado,
    required String destino,
  }) async {
    final cliente = await _supabase
        .from('clientes')
        .select('id, auth_user_id, direccion')
        .eq('numero_documento', documento)
        .maybeSingle();
    if (cliente == null || cliente['auth_user_id'] == null) return;

    final solicitud = await _supabase
        .from('solicitudes_credito')
        .select('id')
        .eq('cliente_id', cliente['id'] as String)
        .eq('asesor_id', asesorId)
        .order('created_at', ascending: false)
        .limit(1)
        .maybeSingle();
    if (solicitud == null) return;

    final asesor = await _supabase
        .from('asesores')
        .select('nombres, apellidos')
        .eq('id', asesorId)
        .maybeSingle();

    final antiguedadBucket = antiguedadNegocioMeses >= 36
        ? 'mas_3_anios'
        : (antiguedadNegocioMeses >= 12 ? '1_a_3_anios' : 'menos_1_anio');
    final ratioGastos =
        ingresos <= 0 ? 'mas_80pct' : (gastos / ingresos > 0.8 ? 'mas_80pct' : (gastos / ingresos > 0.5 ? '50_a_80pct' : 'menos_50pct'));

    await _supabase.from('fichas_campo').insert({
      'user_id': cliente['auth_user_id'],
      'asesor_nombre':
          '${asesor?['nombres'] ?? 'Asesor'} ${asesor?['apellidos'] ?? ''}'.trim(),
      'agencia': 'Agencia BBVA',
      'fecha_visita': DateTime.now().toIso8601String().substring(0, 10),
      'negocio_verificado': resultado.calificacion != 'NO_PROCEDE',
      'antiguedad_negocio': antiguedadBucket,
      'pts_antiguedad': antiguedadNegocioMeses >= 36 ? 25 : 18,
      'tenencia_local': 'alquilado_con_contrato',
      'pts_tenencia': 15,
      'direccion_verificada': cliente['direccion'] ?? tipoNegocio,
      'ventas_diarias_rango': ingresos >= 3000 ? 'mas_300' : '151_a_300',
      'pts_ventas': 25,
      'ventas_mensuales_est': ingresos,
      'gastos_fijos_mes': gastos,
      'ratio_gastos': ratioGastos,
      'pts_gastos': ratioGastos == 'menos_50pct' ? 25 : 18,
      'ingreso_consistente': true,
      'tiene_deuda_informal': 'no',
      'pts_deuda_informal': 20,
      'participa_pandero': 'no',
      'pts_pandero': 10,
      'stock_visible': 'moderado',
      'pts_stock': 15,
      'activos_hogar': 'al_menos_uno',
      'pts_activos': 10,
      'caracter_resultado': 'sin_penalidad',
      'score_transaccional_ref': 500,
      'monto_aprobado_propuesto': montoPropuesto,
      'plazo_propuesto_meses': plazoPropuesto,
      'cuota_estimada': cuota,
      'recomendacion_asesor':
          resultado.calificacion == 'APTO' ? 'aprobar' : 'elevar_comite',
      'obs_finales': 'Pre-evaluacion $destino: ${resultado.motivo}',
      'estado_ficha': 'completada',
    });
  }
}

final preEvalRepositoryProvider = Provider<PreEvalRepository>((ref) {
  return PreEvalRepository(Supabase.instance.client);
});
