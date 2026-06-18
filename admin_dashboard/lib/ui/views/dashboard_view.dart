import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/constants.dart';

class DashboardView extends StatefulWidget {
  const DashboardView({super.key});

  @override
  State<DashboardView> createState() => _DashboardViewState();
}

class _DashboardViewState extends State<DashboardView> {
  bool isLoading = true;
  String? error;
  List<Map<String, dynamic>> solicitudes = [];
  int totalClientes = 0;
  int totalAsesores = 0;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      isLoading = true;
      error = null;
    });

    try {
      final supabase = Supabase.instance.client;

      final solResp = await supabase
          .from('solicitudes_credito')
          .select('''
            id, numero_expediente, estado, monto_solicitado, monto_aprobado,
            plazo_meses, cuota_estimada, created_at,
            clientes(nombres, apellidos, numero_documento, nombre_negocio),
            asesores(nombres, apellidos, codigo_empleado)
          ''')
          .order('created_at', ascending: false);
      final cliResp = await supabase.from('clientes').select('id');
      final aseResp = await supabase.from('asesores').select('id');

      if (!mounted) return;
      setState(() {
        solicitudes = List<Map<String, dynamic>>.from(solResp);
        totalClientes = cliResp.length;
        totalAsesores = aseResp.length;
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (error != null) {
      return _ErrorState(message: error!, onRetry: _loadData);
    }

    final totalSolicitudes = solicitudes.length;
    final aprobadas =
        solicitudes
            .where(
              (s) => s['estado'] == 'aprobado' || s['estado'] == 'desembolsado',
            )
            .length;
    final pendientes =
        solicitudes.where((s) {
          final estado = s['estado']?.toString() ?? '';
          return estado == 'enviado' ||
              estado == 'recibido_comite' ||
              estado == 'en_evaluacion';
        }).length;
    final montoSolicitado = solicitudes.fold<double>(
      0,
      (sum, s) => sum + ((s['monto_solicitado'] as num?)?.toDouble() ?? 0),
    );

    return RefreshIndicator(
      onRefresh: _loadData,
      child: ListView(
        padding: const EdgeInsets.all(32),
        children: [
          Row(
            children: [
              Expanded(
                child: _StatCard(
                  title: 'Solicitudes',
                  value: '$totalSolicitudes',
                  icon: Icons.description_rounded,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'En proceso',
                  value: '$pendientes',
                  icon: Icons.pending_actions_rounded,
                  color: AppColors.warning,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Aprobadas',
                  value: '$aprobadas',
                  icon: Icons.verified_rounded,
                  color: AppColors.success,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Monto solicitado',
                  value: _money(montoSolicitado),
                  icon: Icons.payments_rounded,
                  color: AppColors.secondary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: _StatCard(
                  title: 'Clientes core',
                  value: '$totalClientes',
                  icon: Icons.people_alt_rounded,
                  color: AppColors.secondary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Asesores activos',
                  value: '$totalAsesores',
                  icon: Icons.badge_rounded,
                  color: AppColors.primaryDark,
                ),
              ),
            ],
          ),
          const SizedBox(height: 28),
          Row(
            children: [
              const Text(
                'Solicitudes recientes',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimary,
                ),
              ),
              const Spacer(),
              FilledButton.icon(
                onPressed: () => context.go('/creditos'),
                icon: const Icon(Icons.table_view_rounded),
                label: const Text('Ver creditos'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Card(
            child:
                solicitudes.isEmpty
                    ? const Padding(
                      padding: EdgeInsets.all(28),
                      child: Text('Sin solicitudes registradas.'),
                    )
                    : SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: DataTable(
                        headingRowColor: WidgetStateProperty.all(
                          AppColors.background,
                        ),
                        columns: const [
                          DataColumn(label: Text('Expediente')),
                          DataColumn(label: Text('Cliente')),
                          DataColumn(label: Text('Estado')),
                          DataColumn(label: Text('Monto')),
                          DataColumn(label: Text('Asesor')),
                        ],
                        rows:
                            solicitudes.take(8).map((sol) {
                              return DataRow(
                                cells: [
                                  DataCell(
                                    Text(
                                      sol['numero_expediente']?.toString() ??
                                          sol['id'].toString().substring(0, 8),
                                    ),
                                  ),
                                  DataCell(Text(_cliente(sol))),
                                  DataCell(
                                    _StatusPill(
                                      estado: sol['estado']?.toString() ?? '-',
                                    ),
                                  ),
                                  DataCell(
                                    Text(
                                      _money(
                                        (sol['monto_solicitado'] as num?)
                                                ?.toDouble() ??
                                            0,
                                      ),
                                    ),
                                  ),
                                  DataCell(Text(_asesor(sol))),
                                ],
                              );
                            }).toList(),
                      ),
                    ),
          ),
        ],
      ),
    );
  }

  String _cliente(Map<String, dynamic> sol) {
    final c = sol['clientes'];
    if (c is Map) {
      return '${c['nombres'] ?? ''} ${c['apellidos'] ?? ''}'.trim();
    }
    return '-';
  }

  String _asesor(Map<String, dynamic> sol) {
    final a = sol['asesores'];
    if (a is Map) {
      final nombre = '${a['nombres'] ?? ''} ${a['apellidos'] ?? ''}'.trim();
      return nombre.isEmpty
          ? (a['codigo_empleado']?.toString() ?? '-')
          : nombre;
    }
    return '-';
  }
}

class _StatCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;
  final Color color;

  const _StatCard({
    required this.title,
    required this.value,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: color.withAlpha(26),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: color, size: 24),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 13,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    value,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  final String estado;

  const _StatusPill({required this.estado});

  @override
  Widget build(BuildContext context) {
    final color = switch (estado) {
      'aprobado' || 'desembolsado' => AppColors.success,
      'rechazado' => AppColors.error,
      'condicionado' => AppColors.warning,
      _ => AppColors.secondary,
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withAlpha(24),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        estado.replaceAll('_', ' ').toUpperCase(),
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}

class _ErrorState extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;

  const _ErrorState({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 560),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(
                  Icons.lock_outline_rounded,
                  color: AppColors.error,
                  size: 34,
                ),
                const SizedBox(height: 12),
                const Text(
                  'No se pudieron cargar los datos',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                Text(
                  message,
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
                const SizedBox(height: 18),
                FilledButton.icon(
                  onPressed: onRetry,
                  icon: const Icon(Icons.refresh_rounded),
                  label: const Text('Reintentar'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

String _money(double value) => 'S/ ${value.toStringAsFixed(2)}';
