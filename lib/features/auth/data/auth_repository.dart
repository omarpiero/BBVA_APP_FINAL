import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../../core/network/api_client.dart';
import '../../../core/storage/local_db.dart';
import '../domain/asesor_model.dart';
import 'auth_remote_datasource.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Repositorio de autenticacion. Habla con Supabase y persiste la
/// sesion (token + asesor) en almacenamiento seguro y SQLite.
class AuthRepository {
  final AuthRemoteDataSource _remote;
  final ApiClient _api;
  final FlutterSecureStorage _storage;
  final LocalDb _localDb;
  final SupabaseClient _supabase;

  AuthRepository(this._remote, this._api, this._storage, this._localDb, this._supabase);

  static const _kToken = 'auth_token';
  static const _kAsesor = 'auth_asesor';
  static const _kRemember = 'remember_codigo';
  static const _kIntentos = 'intentos_fallidos';
  static const _kBloqueoHasta = 'bloqueo_hasta';

  Future<AsesorModel> login({
    required String email,
    required String password,
  }) async {
    final res = await _remote.login(
      email: email,
      password: password,
    );
    await _storage.write(key: _kToken, value: res.token);
    await _storage.write(key: _kAsesor, value: jsonEncode(res.asesor.toJson()));
    await _localDb.guardarAsesorLocal(res.asesor);
    return res.asesor;
  }

  /// Restaura la sesion vigente al relanzar la app (RF-03).
  Future<AsesorModel?> sesionActual() async {
    // Si hay sesion en Supabase, la devolvemos
    final session = _supabase.auth.currentSession;
    final token = await _storage.read(key: _kToken);
    final asesorJson = await _storage.read(key: _kAsesor);
    
    if (asesorJson == null) return null;
    
    _api.setToken(session?.accessToken ?? token ?? '');
    return AsesorModel.fromJson(jsonDecode(asesorJson) as Map<String, dynamic>);
  }

  Future<void> logout() async {
    await _supabase.auth.signOut();
    _api.clearToken();
    await _storage.delete(key: _kToken);
    await _storage.delete(key: _kAsesor);
    await _localDb.limpiarCacheSesion();
  }

  /// "Recordarme": persiste (o borra) el codigo de empleado para prellenar.
  Future<void> recordarUsuario(String? codigo) async {
    if (codigo == null || codigo.isEmpty) {
      await _storage.delete(key: _kRemember);
    } else {
      await _storage.write(key: _kRemember, value: codigo);
    }
  }

  Future<String?> usuarioRecordado() => _storage.read(key: _kRemember);

  /// Persiste el estado de bloqueo por intentos fallidos para que sobreviva
  /// al cierre y reapertura de la app (RF-04).
  Future<void> guardarEstadoBloqueo(
      {required int intentos, DateTime? hasta}) async {
    await _storage.write(key: _kIntentos, value: '$intentos');
    if (hasta == null) {
      await _storage.delete(key: _kBloqueoHasta);
    } else {
      await _storage.write(key: _kBloqueoHasta, value: hasta.toIso8601String());
    }
  }

  /// Lee el estado de bloqueo persistido: (intentos fallidos, bloqueado hasta).
  Future<(int, DateTime?)> leerEstadoBloqueo() async {
    final i = int.tryParse(await _storage.read(key: _kIntentos) ?? '') ?? 0;
    final h = await _storage.read(key: _kBloqueoHasta);
    return (i, h == null ? null : DateTime.tryParse(h));
  }
}

final secureStorageProvider =
    Provider<FlutterSecureStorage>((ref) => const FlutterSecureStorage());

final authRemoteDataSourceProvider = Provider<AuthRemoteDataSource>((ref) {
  return AuthRemoteDataSource(Supabase.instance.client);
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(
    ref.watch(authRemoteDataSourceProvider),
    ref.watch(apiClientProvider),
    ref.watch(secureStorageProvider),
    LocalDb.instance,
    Supabase.instance.client,
  );
});
