import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_web_plugins/url_strategy.dart';

import 'core/constants.dart';
import 'core/theme.dart';
import 'ui/layout/main_layout.dart';
import 'ui/views/dashboard_view.dart';
import 'ui/views/asesores_view.dart';
import 'ui/views/clientes_view.dart';
import 'ui/views/creditos_view.dart';
import 'ui/views/landing_view.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  usePathUrlStrategy();

  await Supabase.initialize(url: supabaseUrl, publishableKey: supabaseAnonKey);

  runApp(const AdminDashboardApp());
}

/// Rutas protegidas que exigen una sesión con rol `administrador`.
const _protectedPaths = {'/panel', '/asesores', '/clientes', '/creditos'};

/// ¿La sesión actual es de un administrador? El rol viaja firmado en el JWT
/// (claim app_metadata.role) emitido por Supabase.
bool _esAdmin() {
  final user = Supabase.instance.client.auth.currentUser;
  if (user == null) return false;
  final role =
      (user.appMetadata['role'] ?? user.userMetadata?['role'] ?? '').toString();
  return role == 'administrador';
}

final _router = GoRouter(
  initialLocation: '/',
  redirect: (context, state) {
    final loggedAdmin = _esAdmin();
    final goingProtected = _protectedPaths.contains(state.matchedLocation);

    // Sin sesión de admin no se accede al panel: de vuelta a la landing.
    if (goingProtected && !loggedAdmin) return '/';
    // Admin ya autenticado que cae en la landing: directo al panel.
    if (state.matchedLocation == '/' && loggedAdmin) return '/panel';
    return null;
  },
  routes: [
    GoRoute(path: '/', builder: (context, state) => const LandingView()),
    ShellRoute(
      builder: (context, state, child) {
        return MainLayout(child: child);
      },
      routes: [
        GoRoute(
          path: '/panel',
          builder: (context, state) => const DashboardView(),
        ),
        GoRoute(
          path: '/asesores',
          builder: (context, state) => const AsesoresView(),
        ),
        GoRoute(
          path: '/clientes',
          builder: (context, state) => const ClientesView(),
        ),
        GoRoute(
          path: '/creditos',
          builder: (context, state) => const CreditosView(),
        ),
      ],
    ),
  ],
);

class AdminDashboardApp extends StatelessWidget {
  const AdminDashboardApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'BBVA Admin Dashboard',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      routerConfig: _router,
    );
  }
}
