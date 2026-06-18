import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

class ResultadoBuro {
  final String calificacionSbs;
  final int entidadesConDeuda;
  final double deudaTotal;
  final double mayorDeuda;
  final int diasMayorMora;
  final bool enListaNegra;
  final String? motivoBloqueo;
  final String interpretacion;

  const ResultadoBuro({
    required this.calificacionSbs,
    required this.entidadesConDeuda,
    required this.deudaTotal,
    required this.mayorDeuda,
    required this.diasMayorMora,
    required this.enListaNegra,
    this.motivoBloqueo,
    required this.interpretacion,
  });
}

class BuroRepository {
  final SupabaseClient _supabase;
  BuroRepository(this._supabase);

  Future<ResultadoBuro> consultar(String dni, {String? clienteId}) async {
    final asesorId = _supabase.auth.currentUser?.id;
    if (asesorId == null) {
      throw StateError('Sesion de asesor no disponible.');
    }

    final cliente = clienteId != null
        ? await _supabase
            .from('clientes')
            .select('id, numero_documento')
            .eq('id', clienteId)
            .maybeSingle()
        : await _supabase
            .from('clientes')
            .select('id, numero_documento')
            .eq('numero_documento', dni)
            .maybeSingle();

    if (cliente == null) {
      throw StateError('Cliente core no encontrado para el DNI $dni.');
    }

    final solicitud = await _supabase
        .from('solicitudes_credito')
        .select('id')
        .eq('cliente_id', cliente['id'] as String)
        .eq('asesor_id', asesorId)
        .order('created_at', ascending: false)
        .limit(1)
        .maybeSingle();

    final resultado = await _resultadoParaDni(dni);

    await _supabase.from('consultas_buro').insert({
      'asesor_id': asesorId,
      'cliente_id': cliente['id'],
      'solicitud_id': solicitud?['id'],
      'dni_consultado': dni,
      'calificacion_sbs': resultado.calificacionSbs,
      'entidades_con_deuda': resultado.entidadesConDeuda,
      'deuda_total_pen': resultado.deudaTotal,
      'mayor_deuda': resultado.mayorDeuda,
      'dias_mayor_mora': resultado.diasMayorMora,
      'en_lista_negra': resultado.enListaNegra,
      'motivo_bloqueo': resultado.motivoBloqueo,
      'resultado_json': {
        'interpretacion': resultado.interpretacion,
        'fuente': 'bbva_casos_credito',
      },
    });

    if (resultado.enListaNegra && solicitud != null) {
      await _supabase.rpc('bbva_actualizar_solicitud', params: {
        'p_solicitud_id': solicitud['id'],
        'p_estado': 'rechazado',
        'p_monto_aprobado': null,
        'p_condicion_adicional': null,
        'p_motivo_rechazo': resultado.motivoBloqueo,
      });
    }

    return resultado;
  }

  Future<ResultadoBuro> _resultadoParaDni(String dni) async {
    final caso = await _supabase
        .from('bbva_casos_credito')
        .select(
          'buro_calificacion, buro_entidades, buro_deuda_total, '
          'buro_mayor_mora',
        )
        .eq('numero_documento', dni)
        .maybeSingle();

    if (caso != null) {
      final deuda = (caso['buro_deuda_total'] as num?)?.toDouble() ?? 0;
      final entidades = (caso['buro_entidades'] as num?)?.toInt() ?? 0;
      return ResultadoBuro(
        calificacionSbs: caso['buro_calificacion'] as String? ?? 'NORMAL',
        entidadesConDeuda: entidades,
        deudaTotal: deuda,
        mayorDeuda: entidades > 0 ? deuda / entidades : deuda,
        diasMayorMora: (caso['buro_mayor_mora'] as num?)?.toInt() ?? 0,
        enListaNegra: false,
        interpretacion:
            'Perfil normal para el caso de prueba. Continua evaluacion.',
      );
    }

    final last = dni.isEmpty ? 0 : int.tryParse(dni.substring(dni.length - 1)) ?? 0;
    if (last == 9) {
      return const ResultadoBuro(
        calificacionSbs: 'PERDIDA',
        entidadesConDeuda: 4,
        deudaTotal: 28000,
        mayorDeuda: 12000,
        diasMayorMora: 120,
        enListaNegra: true,
        motivoBloqueo: 'Cliente en lista interna de inhabilitados.',
        interpretacion: 'Bloqueado por lista interna.',
      );
    }

    final calificacion = last <= 3 ? 'NORMAL' : (last <= 6 ? 'CPP' : 'DEFICIENTE');
    final mora = last <= 3 ? 0 : (last <= 6 ? 12 : 45);
    final deuda = 2500.0 + (last * 950.0);
    final entidades = (last % 3) + 1;

    return ResultadoBuro(
      calificacionSbs: calificacion,
      entidadesConDeuda: entidades,
      deudaTotal: deuda,
      mayorDeuda: deuda / entidades,
      diasMayorMora: mora,
      enListaNegra: false,
      interpretacion: calificacion == 'NORMAL'
          ? 'Riesgo bajo. Continua evaluacion.'
          : 'Requiere revision adicional por historial de pago.',
    );
  }
}

final buroRepositoryProvider = Provider<BuroRepository>((ref) {
  return BuroRepository(Supabase.instance.client);
});
