import 'dart:convert';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:workmanager/workmanager.dart';

import '../notificaciones/notificacion_service.dart';
import '../../features/cartera/data/cartera_local_datasource.dart';
import '../../features/cartera/data/cartera_remote_datasource.dart';

/// Nombre unico de la tarea periodica (RF-13).
const _kTarea = 'sync_cartera_nocturna';
const _kUltimaSync = 'ultima_sync_cartera';

/// Punto de entrada del isolate de background de WorkManager (RF-13).
/// Debe ser una funcion top-level con la anotacion vm:entry-point.
@pragma('vm:entry-point')
void callbackDispatcher() {
  Workmanager().executeTask((task, inputData) => SyncNocturna.ejecutarSync());
}

/// HU-05 — Descarga automatica nocturna de la cartera.
/// Programa una tarea diaria (~22:00) que descarga la cartera del dia
/// siguiente, la cachea en SQLite y emite una notificacion local (RF-13/14).
class SyncNocturna {
  SyncNocturna._();
  static const _storage = FlutterSecureStorage();

  /// Enlaza el isolate de background. Llamar una vez en main().
  static Future<void> init() async {
    await Workmanager().initialize(callbackDispatcher);
  }

  /// Programa la sincronizacion diaria a las 22:00 con reintento exponencial.
  static Future<void> programar() async {
    final ahora = DateTime.now();
    var prox = DateTime(ahora.year, ahora.month, ahora.day, 22);
    if (!prox.isAfter(ahora)) prox = prox.add(const Duration(days: 1));
    await Workmanager().registerPeriodicTask(
      _kTarea,
      _kTarea,
      frequency: const Duration(days: 1),
      initialDelay: prox.difference(ahora),
      constraints: Constraints(networkType: NetworkType.connected),
      existingWorkPolicy: ExistingWorkPolicy.replace,
      backoffPolicy: BackoffPolicy.exponential,
      backoffPolicyDelay: const Duration(minutes: 30),
    );
  }

  /// Ejecuta la descarga de la cartera de manana y notifica (RF-13/14).
  /// Devuelve false ante error para que WorkManager reintente con backoff.
  static Future<bool> ejecutarSync() async {
    WidgetsFlutterBinding.ensureInitialized();
    try {
      final token = await _storage.read(key: 'auth_token');
      final asesorJson = await _storage.read(key: 'auth_asesor');
      if (token == null || asesorJson == null) return true; // sin sesion
      final asesorId =
          (jsonDecode(asesorJson) as Map<String, dynamic>)['id'] as String? ??
              '';

      // Inicializar Supabase en el isolate background
      try {
        await Supabase.initialize(
          url: 'https://srxoisgexbcifdpwetxo.supabase.co',
          publishableKey: 'sb_publishable_lYyLWaJxbM-lCJ3eH_wrgg_t-UnR_lC',
        );
      } catch (_) {
        // Ya inicializado
      }

      final manana = DateTime.now().add(const Duration(days: 1));
      final items = await CarteraRemoteDataSource(Supabase.instance.client)
          .obtenerCartera(asesorId: asesorId, fecha: manana);

      await CarteraLocalDataSource().guardarCache(asesorId, items);
      await guardarUltimaSync();

      try {
        await NotificacionService.init();
        await NotificacionService.carteraLista(items.length);
      } catch (_) {/* notificacion best-effort */}
      return true;
    } catch (_) {
      return false; // reintento en el siguiente ciclo (RF-13)
    }
  }

  /// Marca de tiempo de la ultima sincronizacion exitosa (encabezado HU-05).
  static Future<void> guardarUltimaSync() async {
    await _storage.write(
        key: _kUltimaSync, value: DateTime.now().toIso8601String());
  }

  static Future<DateTime?> ultimaSync() async {
    final v = await _storage.read(key: _kUltimaSync);
    return v == null ? null : DateTime.tryParse(v);
  }
}

/// Provee la marca de "Ultima actualizacion" para el encabezado de cartera.
final ultimaSyncProvider =
    FutureProvider.autoDispose<DateTime?>((ref) => SyncNocturna.ultimaSync());
