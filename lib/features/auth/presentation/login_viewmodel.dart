import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/auth_repository.dart';
import '../domain/asesor_model.dart';
import '../../../core/notificaciones/realtime_notificaciones_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart' as supabase;

enum AuthStatus { idle, loading, authenticated, error }

/// Estado inmutable de la pantalla de login.
class AuthState {
  final AuthStatus status;
  final AsesorModel? asesor;
  final String? error;
  final int intentosFallidos;
  final DateTime? bloqueadoHasta;

  const AuthState({
    this.status = AuthStatus.idle,
    this.asesor,
    this.error,
    this.intentosFallidos = 0,
    this.bloqueadoHasta,
  });

  bool get estaBloqueado =>
      bloqueadoHasta != null && DateTime.now().isBefore(bloqueadoHasta!);

  Duration get tiempoRestante => bloqueadoHasta == null
      ? Duration.zero
      : bloqueadoHasta!.difference(DateTime.now());

  AuthState copyWith({
    AuthStatus? status,
    AsesorModel? asesor,
    String? error,
    int? intentosFallidos,
    DateTime? bloqueadoHasta,
    bool limpiarError = false,
    bool limpiarBloqueo = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      asesor: asesor ?? this.asesor,
      error: limpiarError ? null : (error ?? this.error),
      intentosFallidos: intentosFallidos ?? this.intentosFallidos,
      bloqueadoHasta:
          limpiarBloqueo ? null : (bloqueadoHasta ?? this.bloqueadoHasta),
    );
  }
}

/// ViewModel de autenticacion (M0).
class LoginViewModel extends StateNotifier<AuthState> {
  final AuthRepository _repo;
  StreamSubscription<supabase.AuthState>? _authSubscription;

  LoginViewModel(this._repo) : super(const AuthState()) {
    _authSubscription = supabase.Supabase.instance.client.auth.onAuthStateChange.listen((data) {
      final session = data.session;
      if (session == null && state.status == AuthStatus.authenticated) {
        // Token expiro o revocado
        logout();
      }
    });
  }

  @override
  void dispose() {
    _authSubscription?.cancel();
    super.dispose();
  }

  static const int _maxIntentos = 5;
  static const Duration _bloqueo = Duration(minutes: 5);

  /// Restaura sesion vigente al iniciar la app (RF-03).
  Future<void> restaurarSesion() async {
    final asesor = await _repo.sesionActual();
    if (asesor != null) {
      RealtimeNotificacionesService.iniciar(asesor.id);
      state = state.copyWith(
          status: AuthStatus.authenticated, asesor: asesor);
    }
  }

  /// Carga el estado de bloqueo persistido (RF-04). El bloqueo sigue vigente
  /// aunque se haya cerrado y reabierto la app.
  Future<void> cargarEstadoBloqueo() async {
    final (intentos, hasta) = await _repo.leerEstadoBloqueo();
    final ahora = DateTime.now();
    final vigente = hasta != null && ahora.isBefore(hasta);
    var hastaFinal = vigente ? hasta : null;
    // Si el bloqueo guardado supera el maximo actual, se recorta (util al
    // reducir _bloqueo para pruebas: un bloqueo viejo de 30 min se acorta).
    if (hastaFinal != null && hastaFinal.difference(ahora) > _bloqueo) {
      hastaFinal = ahora.add(_bloqueo);
      await _repo.guardarEstadoBloqueo(intentos: intentos, hasta: hastaFinal);
    }
    state = state.copyWith(
      intentosFallidos: vigente ? intentos : 0,
      bloqueadoHasta: hastaFinal,
      limpiarBloqueo: !vigente,
    );
    if (!vigente && (intentos != 0 || hasta != null)) {
      await _repo.guardarEstadoBloqueo(intentos: 0, hasta: null);
    }
  }

  Future<void> login(String email, String password) async {
    if (state.estaBloqueado) return;

    state = state.copyWith(status: AuthStatus.loading, limpiarError: true);
    try {
      final client = supabase.Supabase.instance.client;
      final checkBlockRes = await client.rpc('bbva_obtener_estado_bloqueo', params: {
        'p_username': email,
        'p_tipo_usuario': 'asesor',
      });

      final bool dbBloqueado = checkBlockRes['bloqueado'] as bool? ?? false;
      if (dbBloqueado) {
        final hastaStr = checkBlockRes['bloqueado_hasta'] as String?;
        final hasta = hastaStr != null ? DateTime.tryParse(hastaStr) : null;
        final actualHasta = hasta ?? DateTime.now().add(const Duration(hours: 24));
        state = state.copyWith(
          status: AuthStatus.error,
          error: 'Usuario bloqueado por seguridad debido a múltiples intentos fallidos.',
          bloqueadoHasta: actualHasta,
        );
        await _repo.guardarEstadoBloqueo(intentos: 5, hasta: actualHasta);
        return;
      }

      final asesor = await _repo.login(
        email: email,
        password: password,
      );

      await client.rpc('bbva_resetear_intentos_fallidos', params: {
        'p_username': email,
        'p_tipo_usuario': 'asesor',
      });

      RealtimeNotificacionesService.iniciar(asesor.id);
      state = state.copyWith(
        status: AuthStatus.authenticated,
        asesor: asesor,
        intentosFallidos: 0,
        limpiarBloqueo: true,
      );
      await _repo.guardarEstadoBloqueo(intentos: 0, hasta: null);
    } on supabase.AuthException catch (e) {
      final client = supabase.Supabase.instance.client;
      final failRes = await client.rpc('bbva_registrar_intento_fallido', params: {
        'p_username': email,
        'p_tipo_usuario': 'asesor',
      });

      final bool dbBloqueado = failRes['bloqueado'] as bool? ?? false;
      final hastaStr = failRes['bloqueado_hasta'] as String?;
      final hasta = hastaStr != null ? DateTime.tryParse(hastaStr) : null;
      final intentos = failRes['intentos_fallidos'] as int? ?? (state.intentosFallidos + 1);
      final actualHasta = dbBloqueado ? (hasta ?? DateTime.now().add(const Duration(hours: 24))) : state.bloqueadoHasta;

      state = state.copyWith(
        status: AuthStatus.error,
        error: dbBloqueado
            ? 'Usuario bloqueado por múltiples intentos fallidos.'
            : '${e.message} (intento $intentos de $_maxIntentos)',
        intentosFallidos: intentos,
        bloqueadoHasta: actualHasta,
      );
      await _repo.guardarEstadoBloqueo(intentos: intentos, hasta: actualHasta);
    } catch (e) {
      final client = supabase.Supabase.instance.client;
      final failRes = await client.rpc('bbva_registrar_intento_fallido', params: {
        'p_username': email,
        'p_tipo_usuario': 'asesor',
      });

      final bool dbBloqueado = failRes['bloqueado'] as bool? ?? false;
      final hastaStr = failRes['bloqueado_hasta'] as String?;
      final hasta = hastaStr != null ? DateTime.tryParse(hastaStr) : null;
      final intentos = failRes['intentos_fallidos'] as int? ?? (state.intentosFallidos + 1);
      final actualHasta = dbBloqueado ? (hasta ?? DateTime.now().add(const Duration(hours: 24))) : state.bloqueadoHasta;

      state = state.copyWith(
        status: AuthStatus.error,
        error: dbBloqueado
            ? 'Usuario bloqueado por múltiples intentos fallidos.'
            : '$e',
        intentosFallidos: intentos,
        bloqueadoHasta: actualHasta,
      );
      await _repo.guardarEstadoBloqueo(intentos: intentos, hasta: actualHasta);
    }
  }

  Future<void> logout() async {
    RealtimeNotificacionesService.detener();
    await _repo.logout();
    state = const AuthState();
  }
}

final loginViewModelProvider =
    StateNotifierProvider<LoginViewModel, AuthState>((ref) {
  return LoginViewModel(ref.watch(authRepositoryProvider));
});
