import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/constants.dart';

class CreditosView extends StatefulWidget {
  const CreditosView({super.key});

  @override
  State<CreditosView> createState() => _CreditosViewState();
}

class _CreditosViewState extends State<CreditosView> {
  final _searchController = TextEditingController();
  bool isLoading = true;
  String? error;
  String estadoFiltro = 'todos';
  List<Map<String, dynamic>> solicitudes = [];

  @override
  void initState() {
    super.initState();
    _searchController.addListener(() => setState(() {}));
    _loadSolicitudes();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadSolicitudes() async {
    setState(() {
      isLoading = true;
      error = null;
    });

    try {
      final response = await Supabase.instance.client
          .from('solicitudes_credito')
          .select('''
            id, numero_expediente, estado, canal, destino_credito, garantia,
            monto_solicitado, monto_aprobado, plazo_meses, cuota_estimada,
            tea_referencial, condicion_adicional, motivo_rechazo, created_at,
            clientes(nombres, apellidos, numero_documento, email, nombre_negocio),
            asesores(nombres, apellidos, codigo_empleado, perfil)
          ''')
          .order('created_at', ascending: false);

      if (!mounted) return;
      setState(() {
        solicitudes = List<Map<String, dynamic>>.from(response);
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

  List<Map<String, dynamic>> get _filtered {
    final query = _searchController.text.trim().toLowerCase();
    return solicitudes.where((sol) {
      final estado = sol['estado']?.toString() ?? '';
      if (estadoFiltro != 'todos' && estado != estadoFiltro) return false;

      if (query.isEmpty) return true;
      final haystack =
          [
            sol['numero_expediente']?.toString() ?? '',
            sol['destino_credito']?.toString() ?? '',
            estado,
            _cliente(sol),
            _asesor(sol),
          ].join(' ').toLowerCase();
      return haystack.contains(query);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (error != null) {
      return _ErrorState(message: error!, onRetry: _loadSolicitudes);
    }

    final filtradas = _filtered;
    final montoSolicitado = filtradas.fold<double>(
      0,
      (sum, s) => sum + ((s['monto_solicitado'] as num?)?.toDouble() ?? 0),
    );
    final montoAprobado = filtradas.fold<double>(
      0,
      (sum, s) => sum + ((s['monto_aprobado'] as num?)?.toDouble() ?? 0),
    );
    final enProceso =
        filtradas.where((s) {
          final estado = s['estado']?.toString() ?? '';
          return estado == 'enviado' ||
              estado == 'recibido_comite' ||
              estado == 'en_evaluacion';
        }).length;

    return RefreshIndicator(
      onRefresh: _loadSolicitudes,
      child: ListView(
        padding: const EdgeInsets.all(32),
        children: [
          Row(
            children: [
              Expanded(
                child: _StatCard(
                  title: 'Expedientes',
                  value: '${filtradas.length}',
                  icon: Icons.folder_copy_rounded,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'En proceso',
                  value: '$enProceso',
                  icon: Icons.pending_actions_rounded,
                  color: AppColors.warning,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Monto solicitado',
                  value: _money(montoSolicitado),
                  icon: Icons.request_quote_rounded,
                  color: AppColors.secondary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _StatCard(
                  title: 'Monto aprobado',
                  value: _money(montoAprobado),
                  icon: Icons.verified_rounded,
                  color: AppColors.success,
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  decoration: const InputDecoration(
                    labelText: 'Buscar expediente, cliente o asesor',
                    prefixIcon: Icon(Icons.search_rounded),
                  ),
                ),
              ),
              const SizedBox(width: 14),
              SizedBox(
                width: 220,
                child: DropdownButtonFormField<String>(
                  value: estadoFiltro,
                  decoration: const InputDecoration(labelText: 'Estado'),
                  items: const [
                    DropdownMenuItem(value: 'todos', child: Text('Todos')),
                    DropdownMenuItem(value: 'enviado', child: Text('Enviado')),
                    DropdownMenuItem(
                      value: 'recibido_comite',
                      child: Text('Recibido comite'),
                    ),
                    DropdownMenuItem(
                      value: 'en_evaluacion',
                      child: Text('En evaluacion'),
                    ),
                    DropdownMenuItem(
                      value: 'aprobado',
                      child: Text('Aprobado'),
                    ),
                    DropdownMenuItem(
                      value: 'condicionado',
                      child: Text('Condicionado'),
                    ),
                    DropdownMenuItem(
                      value: 'rechazado',
                      child: Text('Rechazado'),
                    ),
                    DropdownMenuItem(
                      value: 'desembolsado',
                      child: Text('Desembolsado'),
                    ),
                  ],
                  onChanged:
                      (value) =>
                          setState(() => estadoFiltro = value ?? 'todos'),
                ),
              ),
              const SizedBox(width: 14),
              IconButton.filledTonal(
                tooltip: 'Actualizar',
                onPressed: _loadSolicitudes,
                icon: const Icon(Icons.refresh_rounded),
              ),
            ],
          ),
          const SizedBox(height: 18),
          Card(
            child:
                filtradas.isEmpty
                    ? const Padding(
                      padding: EdgeInsets.all(28),
                      child: Text(
                        'No hay solicitudes para el filtro seleccionado.',
                      ),
                    )
                    : SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: DataTable(
                        headingRowColor: WidgetStateProperty.all(
                          AppColors.background,
                        ),
                        showCheckboxColumn: false,
                        columns: const [
                          DataColumn(label: Text('Expediente')),
                          DataColumn(label: Text('Cliente')),
                          DataColumn(label: Text('Asesor')),
                          DataColumn(label: Text('Estado')),
                          DataColumn(label: Text('Solicitado')),
                          DataColumn(label: Text('Aprobado')),
                          DataColumn(label: Text('Plazo')),
                          DataColumn(label: Text('Canal')),
                        ],
                        rows:
                            filtradas.map((sol) {
                              final estado = sol['estado']?.toString() ?? '-';
                              return DataRow(
                                onSelectChanged: (_) => _showDetail(sol),
                                cells: [
                                  DataCell(Text(_expediente(sol))),
                                  DataCell(Text(_cliente(sol))),
                                  DataCell(Text(_asesor(sol))),
                                  DataCell(_StatusPill(estado: estado)),
                                  DataCell(
                                    Text(
                                      _money(
                                        (sol['monto_solicitado'] as num?)
                                                ?.toDouble() ??
                                            0,
                                      ),
                                    ),
                                  ),
                                  DataCell(
                                    Text(
                                      _money(
                                        (sol['monto_aprobado'] as num?)
                                                ?.toDouble() ??
                                            0,
                                      ),
                                    ),
                                  ),
                                  DataCell(
                                    Text('${sol['plazo_meses'] ?? '-'} meses'),
                                  ),
                                  DataCell(
                                    Text(sol['canal']?.toString() ?? '-'),
                                  ),
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

  void _showDetail(Map<String, dynamic> sol) {
    String currentEstado = sol['estado']?.toString() ?? '-';
    
    showDialog<void>(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            bool isUpdating = false;

            Future<void> cambiarEstado(String nuevoEstado) async {
              setDialogState(() => isUpdating = true);
              try {
                await Supabase.instance.client.rpc('bbva_actualizar_solicitud', params: {
                  'p_solicitud_id': sol['id'],
                  'p_estado': nuevoEstado,
                });
                setDialogState(() {
                  currentEstado = nuevoEstado;
                  isUpdating = false;
                });
                _loadSolicitudes();
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Estado actualizado a \$nuevoEstado')),
                  );
                }
              } catch (e) {
                setDialogState(() => isUpdating = false);
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Error al actualizar: \$e')),
                  );
                }
              }
            }

            return AlertDialog(
              title: Text(_expediente(sol)),
              content: SizedBox(
                width: 520,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        _StatusPill(estado: currentEstado),
                        _InfoChip(
                          label: 'Canal',
                          value: sol['canal']?.toString() ?? '-',
                        ),
                        _InfoChip(
                          label: 'Plazo',
                          value: '${sol['plazo_meses'] ?? '-'} meses',
                        ),
                        _InfoChip(
                          label: 'TEA',
                          value: '${sol['tea_referencial'] ?? '-'}%',
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    _DetailRow(label: 'Cliente', value: _cliente(sol)),
                    _DetailRow(label: 'Asesor', value: _asesor(sol)),
                    _DetailRow(
                      label: 'Destino',
                      value: sol['destino_credito']?.toString() ?? '-',
                    ),
                    _DetailRow(
                      label: 'Garantia',
                      value: sol['garantia']?.toString() ?? '-',
                    ),
                    _DetailRow(
                      label: 'Monto solicitado',
                      value: _money(
                        (sol['monto_solicitado'] as num?)?.toDouble() ?? 0,
                      ),
                    ),
                    _DetailRow(
                      label: 'Monto aprobado',
                      value: _money(
                        (sol['monto_aprobado'] as num?)?.toDouble() ?? 0,
                      ),
                    ),
                    _DetailRow(
                      label: 'Cuota estimada',
                      value: _money(
                        (sol['cuota_estimada'] as num?)?.toDouble() ?? 0,
                      ),
                    ),
                    if ((sol['condicion_adicional']?.toString() ?? '').isNotEmpty)
                      _DetailRow(
                        label: 'Condicion',
                        value: sol['condicion_adicional'].toString(),
                      ),
                    if ((sol['motivo_rechazo']?.toString() ?? '').isNotEmpty)
                      _DetailRow(
                        label: 'Motivo rechazo',
                        value: sol['motivo_rechazo'].toString(),
                      ),
                    const Divider(),
                    Row(
                      children: [
                        const Text('Actualizar estado: ', style: TextStyle(fontWeight: FontWeight.bold)),
                        const SizedBox(width: 8),
                        isUpdating
                            ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                            : DropdownButton<String>(
                                value: currentEstado,
                                items: const [
                                  DropdownMenuItem(value: 'enviado', child: Text('Enviado')),
                                  DropdownMenuItem(value: 'recibido_comite', child: Text('Recibido comite')),
                                  DropdownMenuItem(value: 'en_evaluacion', child: Text('En evaluacion')),
                                  DropdownMenuItem(value: 'aprobado', child: Text('Aprobado')),
                                  DropdownMenuItem(value: 'condicionado', child: Text('Condicionado')),
                                  DropdownMenuItem(value: 'rechazado', child: Text('Rechazado')),
                                  DropdownMenuItem(value: 'desembolsado', child: Text('Desembolsado')),
                                ],
                                onChanged: (val) {
                                  if (val != null && val != currentEstado) {
                                    cambiarEstado(val);
                                  }
                                },
                              ),
                      ],
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Cerrar'),
                ),
              ],
            );
          }
        );
      },
    );
  }

  String _expediente(Map<String, dynamic> sol) {
    final numero = sol['numero_expediente']?.toString();
    if (numero != null && numero.isNotEmpty) return numero;
    return sol['id']?.toString().substring(0, 8) ?? '-';
  }

  String _cliente(Map<String, dynamic> sol) {
    final c = sol['clientes'];
    if (c is Map) {
      final nombre = '${c['nombres'] ?? ''} ${c['apellidos'] ?? ''}'.trim();
      final documento = c['numero_documento']?.toString();
      if (nombre.isEmpty) return documento ?? '-';
      return documento == null ? nombre : '$nombre - $documento';
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
        padding: const EdgeInsets.all(18),
        child: Row(
          children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: color.withAlpha(26),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: color),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    value,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 22,
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

class _InfoChip extends StatelessWidget {
  final String label;
  final String value;

  const _InfoChip({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Chip(
      label: Text('$label: $value'),
      backgroundColor: AppColors.background,
      side: BorderSide(color: Colors.grey.shade200),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final String label;
  final String value;

  const _DetailRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 130,
            child: Text(
              label,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
        ],
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
                  'No se pudieron cargar los creditos',
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
