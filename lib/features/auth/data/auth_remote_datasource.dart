import 'package:supabase_flutter/supabase_flutter.dart';
import '../domain/asesor_model.dart';

/// Resultado del login: token + asesor.
class LoginResult {
  final String token;
  final AsesorModel asesor;
  const LoginResult(this.token, this.asesor);
}

/// Fuente remota de autenticacion usando Supabase Auth.
class AuthRemoteDataSource {
  final SupabaseClient _supabase;
  AuthRemoteDataSource(this._supabase);

  Future<LoginResult> login({
    required String email,
    required String password,
  }) async {
    final response = await _supabase.auth.signInWithPassword(
      email: email,
      password: password,
    );

    if (response.user == null) {
      throw Exception('Credenciales inválidas');
    }

    // Control de acceso por rol (RBAC). El rol viaja firmado en el JWT
    // (app_metadata.role) emitido por el backend. Esta app es exclusiva de
    // la fuerza de ventas: solo asesor / supervisor pueden ingresar. Un
    // administrador o un cliente, aunque tengan credenciales válidas, son
    // rechazados y se cierra la sesión.
    const rolesPermitidos = {'asesor', 'supervisor', 'operador'};
    final rol = (response.user!.appMetadata['role'] ??
            response.user!.userMetadata?['role'] ??
            '')
        .toString();
    if (!rolesPermitidos.contains(rol)) {
      await _supabase.auth.signOut();
      throw Exception(
        'Esta cuenta no corresponde a la app de asesores. '
        'Usa la app del rol que te corresponde.',
      );
    }

    // Fetch extra user details from the asesores table synced by trigger
    final data = await _supabase
        .from('asesores')
        .select()
        .eq('id', response.user!.id)
        .single();

    final asesor = AsesorModel.fromJson(data);
    return LoginResult(response.session?.accessToken ?? '', asesor);
  }
}
