import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'app/app.dart';
import 'core/notificaciones/notificacion_service.dart';
import 'core/sync/sync_nocturna.dart';
import 'features/auth/presentation/login_viewmodel.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Supabase.initialize(
    url: 'https://srxoisgexbcifdpwetxo.supabase.co',
    publishableKey: 'sb_publishable_lYyLWaJxbM-lCJ3eH_wrgg_t-UnR_lC',
  );

  try {
    await NotificacionService.init();
  } catch (_) {/* notificaciones opcionales */}

  try {
    await SyncNocturna.init(); // enlaza el background de WorkManager (HU-05)
  } catch (_) {/* background opcional */}

  final container = ProviderContainer();
  // Restaura sesion persistente (token + asesor) antes de pintar (RF-03).
  await container.read(loginViewModelProvider.notifier).restaurarSesion();

  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const App(),
    ),
  );
}
