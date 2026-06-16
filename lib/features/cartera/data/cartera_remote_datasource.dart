import 'package:supabase_flutter/supabase_flutter.dart';
import '../domain/cartera_model.dart';

/// Fuente remota de la cartera usando Supabase
class CarteraRemoteDataSource {
  final SupabaseClient _supabase;
  CarteraRemoteDataSource(this._supabase);

  Future<List<CarteraItem>> obtenerCartera({
    required String asesorId,
    required DateTime fecha,
  }) async {
    final fechaStr = fecha.toIso8601String().substring(0, 10);
    // Realizamos un join con clientes para obtener nombres, documento y coordenadas
    final response = await _supabase
        .from('cartera_diaria')
        .select('*, clientes(nombres, apellidos, numero_documento, lat, lng)')
        .eq('asesor_id', asesorId)
        .eq('fecha_asignacion', fechaStr);

    return (response as List)
        .map((e) => CarteraItem.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> registrarVisita({
    required String carteraId,
    required String resultado,
    required String observacion,
    double? lat,
    double? lng,
  }) async {
    // 1. Actualizamos el estado de la visita en la cartera
    await _supabase.from('cartera_diaria').update({
      'estado_visita': 'visitado',
      'resultado_visita': resultado,
      'observacion_visita': observacion,
      'timestamp_visita': DateTime.now().toUtc().toIso8601String(),
      'lat_visita': lat,
      'lng_visita': lng,
    }).eq('id', carteraId);

    // 2. Encolamos la accion hacia el core mediante sync_outbox
    // Fetch cliente_id para la accion de cobranza
    final carteraRow = await _supabase
        .from('cartera_diaria')
        .select('asesor_id, cliente_id')
        .eq('id', carteraId)
        .single();

    final payload = {
      'asesor_id': carteraRow['asesor_id'],
      'cliente_id': carteraRow['cliente_id'],
      'tipo_gestion': 'visita',
      'resultado': resultado,
      'observaciones': observacion,
      'lat': lat,
      'lng': lng,
    };

    await _supabase.from('sync_outbox').insert({
      'entidad': 'acciones_cobranza',
      'entidad_id': carteraId, // Usamos el id de cartera como ref temporal
      'operacion': 'create',
      'payload': payload,
    });
  }
}
