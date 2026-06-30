import 'package:supabase_flutter/supabase_flutter.dart';

void main() async {
  print('Inicializando Supabase...');
  await Supabase.initialize(
    url: 'https://srxoisgexbcifdpwetxo.supabase.co',
    publishableKey: 'sb_publishable_lYyLWaJxbM-lCJ3eH_wrgg_t-UnR_lC',
  );

  final client = Supabase.instance.client;
  final email = 'asesor02@asesores.pe';

  print('Llamando a bbva_obtener_estado_bloqueo...');
  try {
    final checkBlockRes = await client.rpc('bbva_obtener_estado_bloqueo', params: {
      'p_username': email,
      'p_tipo_usuario': 'asesor',
    });
    print('Respuesta bbva_obtener_estado_bloqueo: \$checkBlockRes');
  } catch (e) {
    print('ERROR bbva_obtener_estado_bloqueo: \$e');
  }

  print('Intentando hacer login normal...');
  try {
    final authRes = await client.auth.signInWithPassword(
      email: email,
      password: 'somepassword', // Probablemente falle si no es la clave
    );
    print('Login exitoso: \${authRes.user?.id}');
  } catch (e) {
    print('ERROR Login: \$e');
  }
}
