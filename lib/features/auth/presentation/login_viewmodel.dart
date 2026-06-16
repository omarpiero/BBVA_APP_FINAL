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
  static const Duration _bloqueo = Duration(seconds: 10);

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
      final asesor = await _repo.login(
        email: email,
        password: password,
      );
      RealtimeNotificacionesService.iniciar(asesor.id);
      state = state.copyWith(
        status: AuthStatus.authenticated,
        asesor: asesor,
        intentosFallidos: 0,
        limpiarBloqueo: true,
      );
      await _repo.guardarEstadoBloqueo(intentos: 0, hasta: null);
    } on supabase.AuthException catch (e) {
      final intentos = state.intentosFallidos + 1;
      final bloquear = intentos >= _maxIntentos;
      final hasta =
          bloquear ? DateTime.now().add(_bloqueo) : state.bloqueadoHasta;
      state = state.copyWith(
        status: AuthStatus.error,
        error: bloquear
            ? null
            : '${e.message} (intento $intentos de $_maxIntentos)',
        intentosFallidos: intentos,
        bloqueadoHasta: hasta,
      );
      await _repo.guardarEstadoBloqueo(intentos: intentos, hasta: hasta);
    } catch (e) {
      final intentos = state.intentosFallidos + 1;
      final bloquear = intentos >= _maxIntentos;
      final hasta =
          bloquear ? DateTime.now().add(_bloqueo) : state.bloqueadoHasta;
      state = state.copyWith(
        status: AuthStatus.error,
        error: bloquear
            ? null
            : '$e',
        intentosFallidos: intentos,
        bloqueadoHasta: hasta,
      );
      await _repo.guardarEstadoBloqueo(intentos: intentos, hasta: hasta);
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
