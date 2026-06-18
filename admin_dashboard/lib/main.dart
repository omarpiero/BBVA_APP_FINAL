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
import 'ui/views/login_view.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  usePathUrlStrategy();

  await Supabase.initialize(url: supabaseUrl, publishableKey: supabaseAnonKey);

  runApp(const AdminDashboardApp());
}

final _router = GoRouter(
  initialLocation: '/login',
  routes: [
    GoRoute(path: '/login', builder: (context, state) => const LoginView()),
    ShellRoute(
      builder: (context, state, child) {
        return MainLayout(child: child);
      },
      routes: [
        GoRoute(path: '/', builder: (context, state) => const DashboardView()),
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
