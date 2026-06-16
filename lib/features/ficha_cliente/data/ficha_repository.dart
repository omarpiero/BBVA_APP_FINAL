import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../domain/ficha_model.dart';

/// Repositorio de la ficha del cliente (Bypass API - Supabase Directo).
class FichaRepository {
  final SupabaseClient _supabase;
  FichaRepository(this._supabase);

  Future<FichaCliente> obtenerFicha(String clienteId) async {
    // 1. Obtener datos personales del cliente
    final cliente = await _supabase
        .from('clientes')
        .select()
        .eq('id', clienteId)
        .single();

    // 2. Obtener historial crediticio
    final creditos = await _supabase
        .from('cr_creditos')
        .select()
        .eq('cliente_id', clienteId);

    // 3. Calcular posicion
    double deudaTotal = 0;
    int cuentasVigentes = 0;
    int cuentasMora = 0;
    int diasMayorMora = 0;

    for (var c in creditos) {
      deudaTotal += (c['saldo_total'] as num?)?.toDouble() ?? 0;
      if (c['estado'] == 'vigente') cuentasVigentes++;
      if (c['estado'] == 'vencido') {
        cuentasMora++;
        final mora = (c['dias_mora'] as num?)?.toInt() ?? 0;
        if (mora > diasMayorMora) diasMayorMora = mora;
      }
    }

    final posicion = {
      'deuda_total': deudaTotal,
      'cuentas_vigentes': cuentasVigentes,
      'cuentas_mora': cuentasMora,
      'dias_mayor_mora': diasMayorMora,
    };

    // Comportamiento simulado para la UI (M3)
    final comportamiento = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1];

    final payload = {
      'cliente': cliente,
      'posicion': posicion,
      'historial': creditos,
      'oferta': null,
      'comportamiento': comportamiento,
      'indicadores': {
        'pct_puntual': 100,
        'dias_prom_mora': 0,
        'monto_pagado': 5000,
      }
    };

    return FichaCliente.fromJson(payload);
  }

  /// Actualiza las coordenadas del negocio del cliente (HU-10 / RF-25/26).
  Future<bool> actualizarUbicacion({
    required String clienteId,
    required double lat,
    required double lng,
    String? direccion,
  }) async {
    try {
      await _supabase.from('clientes').update({
        'lat': lat,
        'lng': lng,
        if (direccion != null) 'direccion': direccion,
      }).eq('id', clienteId);
      return true;
    } catch (_) {
      return false;
    }
  }
}

final fichaRepositoryProvider = Provider<FichaRepository>((ref) {
  return FichaRepository(Supabase.instance.client);
});
