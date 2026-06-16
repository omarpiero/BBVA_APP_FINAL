import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../domain/solicitud_model.dart';

/// Repositorio del modulo Solicitud (M5).
class SolicitudRepository {
  final SupabaseClient _supabase;
  SolicitudRepository(this._supabase);

  Future<SolicitudCreada> crear(Map<String, dynamic> datos, String asesorId) async {
    // 1. Buscamos o creamos al cliente por su DNI (numero_documento)
    final numDoc = datos['numero_documento'] as String;
    var clienteId = '';

    final clienteMatch = await _supabase
        .from('clientes')
        .select('id')
        .eq('numero_documento', numDoc)
        .maybeSingle();

    if (clienteMatch != null) {
      clienteId = clienteMatch['id'] as String;
    } else {
      // Lo insertamos como prospecto
      final res = await _supabase.from('clientes').insert({
        'numero_documento': numDoc,
        'nombres': datos['nombres'],
        'apellidos': datos['apellidos'],
        'telefono': datos['telefono'],
        'estado_civil': datos['estado_civil'],
        'tipo_negocio': datos['tipo_negocio'],
        'nombre_negocio': datos['nombre_negocio'],
        'ingresos_estimados': datos['ingresos_estimados'],
        'es_prospecto': true,
      }).select('id').single();
      clienteId = res['id'] as String;
    }

    // 2. Insertamos la solicitud_credito
    final solicitudRes = await _supabase.from('solicitudes_credito').insert({
      'asesor_id': asesorId,
      'cliente_id': clienteId,
      'tipo_negocio': datos['tipo_negocio'],
      'nombre_negocio': datos['nombre_negocio'],
      'ingresos_estimados': datos['ingresos_estimados'],
      'gastos_mensuales': datos['gastos_mensuales'],
      'patrimonio_estimado': datos['patrimonio_estimado'],
      'tiene_conyuge': datos['tiene_conyuge'],
      'conyuge_json': datos['conyuge_json'],
      'monto_solicitado': datos['monto_solicitado'],
      'plazo_meses': datos['plazo_meses'],
      'moneda': datos['moneda'],
      'tipo_cuota': datos['tipo_cuota'],
      'destino_credito': datos['destino_credito'],
      'cuota_estimada': datos['cuota_estimada'],
      'tea_referencial': datos['tea_referencial'],
      'estado': 'enviado',
      'firma_cliente_base64': datos['firma_cliente_base64'],
    }).select('id, numero_expediente').single();

    final solicitudId = solicitudRes['id'] as String;
    final numExpediente = solicitudRes['numero_expediente'] as String? ?? 'TEMP-$solicitudId';

    // 3. Cola de sincronizacion hacia el Core
    await _supabase.from('sync_outbox').insert({
      'entidad': 'solicitudes_credito',
      'entidad_id': solicitudId,
      'operacion': 'create',
      'payload': {
        'asesor_id': asesorId,
        'cliente_id': clienteId,
        ...datos,
      },
    });

    return SolicitudCreada(solicitudId, numExpediente, 'enviado');
  }

  Future<List<SolicitudResumen>> listar(String asesorId) async {
    final res = await _supabase
        .from('solicitudes_credito')
        .select('*, clientes(nombres, apellidos)')
        .eq('asesor_id', asesorId)
        .order('created_at', ascending: false);

    return (res as List).map((row) {
      final cliente = row['clientes'] as Map<String, dynamic>? ?? {};
      final nombre = '${cliente['nombres'] ?? ''} ${cliente['apellidos'] ?? ''}'.trim();
      row['cliente_nombre'] = nombre.isNotEmpty ? nombre : 'Sin nombre';
      return SolicitudResumen.fromJson(row as Map<String, dynamic>);
    }).toList();
  }

  Future<List<String>> listarNotas(String solicitudId) async {
    final res = await _supabase
        .from('solicitudes_notas_internas')
        .select('contenido')
        .eq('solicitud_id', solicitudId)
        .order('created_at', ascending: true);
    return (res as List).map((e) => e['contenido'] as String).toList();
  }

  Future<void> agregarNota(String solicitudId, String contenido, String asesorId) async {
    await _supabase.from('solicitudes_notas_internas').insert({
      'solicitud_id': solicitudId,
      'asesor_id': asesorId,
      'contenido': contenido,
    });
  }
}

final solicitudRepositoryProvider = Provider<SolicitudRepository>((ref) {
  return SolicitudRepository(Supabase.instance.client);
});
