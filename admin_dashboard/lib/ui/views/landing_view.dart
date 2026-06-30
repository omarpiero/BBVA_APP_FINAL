import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/constants.dart';

/// Landing pública tematizada BBVA para el panel administrativo.
///
/// Sirve como portada/publicidad del ecosistema (Fuerza de Ventas, App Clientes
/// y Panel Admin) e incluye el acceso. El login aplica control por rol:
/// solo el rol `administrador` (claim `role` del JWT) entra al panel; cualquier
/// otra credencial (asesor / cliente) es rechazada y la sesión se cierra.
class LandingView extends StatefulWidget {
  const LandingView({super.key});

  @override
  State<LandingView> createState() => _LandingViewState();
}

class _LandingViewState extends State<LandingView> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _loginKey = GlobalKey();
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _signIn() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    final client = Supabase.instance.client;
    try {
      final res = await client.auth.signInWithPassword(
        email: _emailController.text.trim(),
        password: _passwordController.text,
      );

      // Control de acceso por rol (RBAC). El rol viaja firmado en el JWT
      // (app_metadata.role) emitido por el backend de Supabase.
      final role = (res.user?.appMetadata['role'] ??
              res.user?.userMetadata?['role'] ??
              '')
          .toString();

      if (role != 'administrador') {
        // Credencial válida pero de otro aplicativo: se rechaza el acceso.
        await client.auth.signOut();
        if (!mounted) return;
        setState(() {
          _error = _mensajeRolNoAutorizado(role);
          _loading = false;
        });
        return;
      }

      if (mounted) context.go('/panel');
    } on AuthException catch (e) {
      setState(() => _error = e.message);
    } catch (e) {
      setState(() => _error = 'No se pudo iniciar sesión. Intenta nuevamente.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  String _mensajeRolNoAutorizado(String role) {
    final destino = switch (role) {
      'asesor' || 'supervisor' || 'operador' =>
        'Tu cuenta es de asesor: ingresa desde la App Fuerza de Ventas.',
      'cliente' =>
        'Tu cuenta es de cliente: ingresa desde la App Clientes (banca móvil).',
      _ => 'Tu cuenta no tiene permisos para el panel administrativo.',
    };
    return 'Acceso denegado al panel administrativo. $destino';
  }

  void _scrollToLogin() {
    final ctx = _loginKey.currentContext;
    if (ctx != null) {
      Scrollable.ensureVisible(ctx,
          duration: const Duration(milliseconds: 450), curve: Curves.easeInOut);
    }
  }

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    final isWide = width >= 980;

    return Scaffold(
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        child: Column(
          children: [
            _TopBar(onAccess: _scrollToLogin),
            _Hero(
              isWide: isWide,
              loginCard: _LoginCard(
                key: _loginKey,
                emailController: _emailController,
                passwordController: _passwordController,
                loading: _loading,
                error: _error,
                onSubmit: _signIn,
              ),
            ),
            const _EcosystemSection(),
            const _FeaturesSection(),
            const _Footer(),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
//  Top bar
// ============================================================================
class _TopBar extends StatelessWidget {
  final VoidCallback onAccess;
  const _TopBar({required this.onAccess});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 72,
      padding: const EdgeInsets.symmetric(horizontal: 32),
      color: AppColors.primaryDark,
      child: Row(
        children: [
          const Icon(Icons.account_balance_rounded, color: Colors.white, size: 26),
          const SizedBox(width: 10),
          const Text('BBVA',
              style: TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 1.5)),
          const SizedBox(width: 12),
          Container(width: 1, height: 26, color: Colors.white24),
          const SizedBox(width: 12),
          const Text('Ecosistema de Crédito',
              style: TextStyle(color: Colors.white70, fontSize: 14)),
          const Spacer(),
          OutlinedButton.icon(
            onPressed: onAccess,
            style: OutlinedButton.styleFrom(
              foregroundColor: Colors.white,
              side: const BorderSide(color: Colors.white54),
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
            ),
            icon: const Icon(Icons.lock_outline_rounded, size: 18),
            label: const Text('Acceso administrativo'),
          ),
        ],
      ),
    );
  }
}

// ============================================================================
//  Hero
// ============================================================================
class _Hero extends StatelessWidget {
  final bool isWide;
  final Widget loginCard;
  const _Hero({required this.isWide, required this.loginCard});

  @override
  Widget build(BuildContext context) {
    final marketing = _HeroCopy(isWide: isWide);

    return Container(
      width: double.infinity,
      padding: EdgeInsets.symmetric(horizontal: 32, vertical: isWide ? 72 : 40),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF002A4D),
            AppColors.primary,
            Color(0xFF1973B8),
          ],
        ),
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1200),
          child: isWide
              ? Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Expanded(flex: 6, child: marketing),
                    const SizedBox(width: 56),
                    Expanded(flex: 5, child: loginCard),
                  ],
                )
              : Column(
                  children: [
                    marketing,
                    const SizedBox(height: 36),
                    loginCard,
                  ],
                ),
        ),
      ),
    );
  }
}

class _HeroCopy extends StatelessWidget {
  final bool isWide;
  const _HeroCopy({required this.isWide});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment:
          isWide ? CrossAxisAlignment.start : CrossAxisAlignment.center,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
          decoration: BoxDecoration(
            color: Colors.white.withAlpha(31),
            borderRadius: BorderRadius.circular(40),
          ),
          child: const Text('Banca digital · Originación de crédito',
              style: TextStyle(color: Colors.white, fontSize: 13)),
        ),
        const SizedBox(height: 22),
        Text(
          'Un ecosistema.\nUna sola base de datos.',
          textAlign: isWide ? TextAlign.start : TextAlign.center,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 44,
            height: 1.1,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 20),
        ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 520),
          child: Text(
            'Originamos créditos en campo con la Fuerza de Ventas, los '
            'procesamos en el Core financiero y los reflejamos en la banca '
            'móvil del cliente — en tiempo real y sobre una única plataforma '
            'Supabase.',
            textAlign: isWide ? TextAlign.start : TextAlign.center,
            style: const TextStyle(
                color: Colors.white70, fontSize: 16, height: 1.5),
          ),
        ),
        const SizedBox(height: 28),
        Wrap(
          spacing: 28,
          runSpacing: 16,
          children: const [
            _Stat(value: '3', label: 'Aplicativos integrados'),
            _Stat(value: '1', label: 'Core financiero compartido'),
            _Stat(value: '100%', label: 'Offline-first en campo'),
          ],
        ),
      ],
    );
  }
}

class _Stat extends StatelessWidget {
  final String value;
  final String label;
  const _Stat({required this.value, required this.label});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(value,
            style: const TextStyle(
                color: AppColors.accent,
                fontSize: 30,
                fontWeight: FontWeight.w800)),
        Text(label,
            style: const TextStyle(color: Colors.white70, fontSize: 13)),
      ],
    );
  }
}

// ============================================================================
//  Login card (con control de rol)
// ============================================================================
class _LoginCard extends StatelessWidget {
  final TextEditingController emailController;
  final TextEditingController passwordController;
  final bool loading;
  final String? error;
  final VoidCallback onSubmit;

  const _LoginCard({
    super.key,
    required this.emailController,
    required this.passwordController,
    required this.loading,
    required this.error,
    required this.onSubmit,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 12,
      borderRadius: BorderRadius.circular(16),
      shadowColor: Colors.black54,
      child: Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withAlpha(20),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.admin_panel_settings_rounded,
                      color: AppColors.primary, size: 26),
                ),
                const SizedBox(width: 14),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Panel administrativo',
                          style: TextStyle(
                              fontSize: 19,
                              fontWeight: FontWeight.w800,
                              color: AppColors.primary)),
                      Text('Acceso exclusivo para administradores',
                          style: TextStyle(
                              fontSize: 13, color: AppColors.textSecondary)),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 26),
            TextField(
              controller: emailController,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(
                labelText: 'Correo corporativo',
                hintText: 'admin.demo@bbva.pe',
                prefixIcon: Icon(Icons.mail_outline_rounded),
              ),
              onSubmitted: (_) => onSubmit(),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: passwordController,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Contraseña',
                prefixIcon: Icon(Icons.lock_outline_rounded),
              ),
              onSubmitted: (_) => onSubmit(),
            ),
            if (error != null) ...[
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.error.withAlpha(18),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: AppColors.error.withAlpha(60)),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.shield_outlined,
                        color: AppColors.error, size: 18),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(error!,
                          style: const TextStyle(
                              color: AppColors.error, fontSize: 13, height: 1.4)),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 22),
            SizedBox(
              height: 50,
              child: FilledButton.icon(
                onPressed: loading ? null : onSubmit,
                icon: loading
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(
                            strokeWidth: 2, color: Colors.white))
                    : const Icon(Icons.login_rounded),
                label: const Text('Ingresar al panel'),
              ),
            ),
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.verified_user_outlined,
                    size: 14, color: AppColors.textSecondary),
                const SizedBox(width: 6),
                Text('Protegido con JWT y control de acceso por rol',
                    style: TextStyle(
                        fontSize: 11.5,
                        color: AppColors.textSecondary.withAlpha(220))),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
//  Ecosystem section
// ============================================================================
class _EcosystemSection extends StatelessWidget {
  const _EcosystemSection();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      color: Colors.white,
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 72),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1200),
          child: Column(
            children: [
              const _SectionTitle(
                eyebrow: 'EL ECOSISTEMA',
                title: 'Tres aplicativos, un mismo Core',
                subtitle:
                    'Cada pieza atiende a un actor distinto del flujo de crédito '
                    'y todas comparten la base de datos bd_core_mobile en Supabase.',
              ),
              const SizedBox(height: 48),
              Wrap(
                spacing: 24,
                runSpacing: 24,
                alignment: WrapAlignment.center,
                children: const [
                  _AppCard(
                    icon: Icons.directions_walk_rounded,
                    color: AppColors.primary,
                    title: 'Fuerza de Ventas',
                    tech: 'Flutter · Offline-first',
                    desc:
                        'Asesores de campo: cartera diaria con GPS, ficha del '
                        'cliente, pre-evaluación, consulta de buró, solicitud y '
                        'desembolso.',
                  ),
                  _AppCard(
                    icon: Icons.phone_iphone_rounded,
                    color: AppColors.secondary,
                    title: 'App Clientes',
                    tech: 'Kotlin · Android',
                    desc:
                        'Banca móvil del cliente final: cuentas de ahorro, '
                        'créditos con cronograma, movimientos, tarjetas y '
                        'notificaciones.',
                  ),
                  _AppCard(
                    icon: Icons.dashboard_rounded,
                    color: Color(0xFF1973B8),
                    title: 'Panel Admin',
                    tech: 'Flutter Web · Vercel',
                    desc:
                        'Supervisión operativa: dashboard de cartera, gestión de '
                        'asesores, clientes y estado de créditos en tiempo real.',
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _AppCard extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String tech;
  final String desc;

  const _AppCard({
    required this.icon,
    required this.color,
    required this.title,
    required this.tech,
    required this.desc,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 350,
      padding: const EdgeInsets.all(28),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: color.withAlpha(28),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: color, size: 30),
          ),
          const SizedBox(height: 20),
          Text(title,
              style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary)),
          const SizedBox(height: 4),
          Text(tech,
              style: TextStyle(
                  fontSize: 12.5,
                  color: color,
                  fontWeight: FontWeight.w600)),
          const SizedBox(height: 14),
          Text(desc,
              style: const TextStyle(
                  fontSize: 14,
                  height: 1.5,
                  color: AppColors.textSecondary)),
        ],
      ),
    );
  }
}

// ============================================================================
//  Features section
// ============================================================================
class _FeaturesSection extends StatelessWidget {
  const _FeaturesSection();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      color: AppColors.background,
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 72),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1200),
          child: Column(
            children: [
              const _SectionTitle(
                eyebrow: 'POR QUÉ FUNCIONA',
                title: 'Integración real de extremo a extremo',
                subtitle:
                    'Del registro en campo al desembolso reflejado en la banca '
                    'del cliente, sin rupturas entre sistemas.',
              ),
              const SizedBox(height: 48),
              Wrap(
                spacing: 24,
                runSpacing: 24,
                alignment: WrapAlignment.center,
                children: const [
                  _FeatureTile(
                    icon: Icons.sync_rounded,
                    title: 'Sincronización Core',
                    desc:
                        'La solicitud se encola en sync_outbox, se promueve al '
                        'núcleo financiero y vuelve a las tablas espejo cr_*.',
                  ),
                  _FeatureTile(
                    icon: Icons.shield_rounded,
                    title: 'Seguridad por roles',
                    desc:
                        'Autenticación JWT y matriz de permisos: cada actor solo '
                        'accede a su aplicativo, validado en el backend.',
                  ),
                  _FeatureTile(
                    icon: Icons.cloud_off_rounded,
                    title: 'Offline-first',
                    desc:
                        'El asesor opera sin conexión; los cambios se sincronizan '
                        'automáticamente al recuperar la red.',
                  ),
                  _FeatureTile(
                    icon: Icons.bolt_rounded,
                    title: 'Tiempo real',
                    desc:
                        'Notificaciones y estados se propagan al instante entre '
                        'la fuerza de ventas, el cliente y el panel.',
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FeatureTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String desc;
  const _FeatureTile(
      {required this.icon, required this.title, required this.desc});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 270,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: AppColors.primary, size: 28),
          const SizedBox(height: 16),
          Text(title,
              style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary)),
          const SizedBox(height: 8),
          Text(desc,
              style: const TextStyle(
                  fontSize: 13.5,
                  height: 1.5,
                  color: AppColors.textSecondary)),
        ],
      ),
    );
  }
}

// ============================================================================
//  Shared bits
// ============================================================================
class _SectionTitle extends StatelessWidget {
  final String eyebrow;
  final String title;
  final String subtitle;
  const _SectionTitle(
      {required this.eyebrow, required this.title, required this.subtitle});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(eyebrow,
            style: const TextStyle(
                color: AppColors.secondary,
                fontSize: 13,
                fontWeight: FontWeight.w700,
                letterSpacing: 1.5)),
        const SizedBox(height: 10),
        Text(title,
            textAlign: TextAlign.center,
            style: const TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary)),
        const SizedBox(height: 14),
        ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 640),
          child: Text(subtitle,
              textAlign: TextAlign.center,
              style: const TextStyle(
                  fontSize: 16, height: 1.5, color: AppColors.textSecondary)),
        ),
      ],
    );
  }
}

class _Footer extends StatelessWidget {
  const _Footer();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      color: AppColors.primaryDark,
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 36),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1200),
          child: Row(
            children: [
              const Icon(Icons.account_balance_rounded,
                  color: Colors.white, size: 22),
              const SizedBox(width: 10),
              const Text('BBVA · Ecosistema de Crédito',
                  style: TextStyle(color: Colors.white, fontSize: 14)),
              const Spacer(),
              Text('Proyecto demo académico — Supabase + Flutter + Kotlin',
                  style: TextStyle(color: Colors.white.withAlpha(150), fontSize: 12)),
            ],
          ),
        ),
      ),
    );
  }
}
