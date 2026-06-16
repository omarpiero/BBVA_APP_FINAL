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
