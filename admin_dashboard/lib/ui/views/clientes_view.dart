import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/constants.dart';

class ClientesView extends StatefulWidget {
  const ClientesView({super.key});

  @override
  State<ClientesView> createState() => _ClientesViewState();
}

class _ClientesViewState extends State<ClientesView> {
  bool isLoading = true;
  String? error;
  List<Map<String, dynamic>> clientes = [];

  @override
  void initState() {
    super.initState();
    _loadClientes();
  }

  Future<void> _loadClientes() async {
    setState(() {
      isLoading = true;
      error = null;
    });

    try {
      final response = await Supabase.instance.client
          .from('clientes')
          .select('''
            id, tipo_documento, numero_documento, nombres, apellidos,
            email, telefono, tipo_negocio, nombre_negocio, ingresos_estimados
          ''')
          .order('created_at', ascending: false);
      if (!mounted) return;
      setState(() {
        clientes = List<Map<String, dynamic>>.from(response);
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
      return _ErrorState(message: error!, onRetry: _loadClientes);
    }

    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              _SummaryChip(
                label: 'Clientes',
                value: '${clientes.length}',
                icon: Icons.people_alt_rounded,
              ),
              const SizedBox(width: 12),
              _SummaryChip(
                label: 'Con negocio',
                value:
                    '${clientes.where((c) => (c['nombre_negocio']?.toString() ?? '').isNotEmpty).length}',
                icon: Icons.storefront_rounded,
              ),
              const Spacer(),
              IconButton.filledTonal(
                tooltip: 'Actualizar',
                onPressed: _loadClientes,
                icon: const Icon(Icons.refresh_rounded),
              ),
            ],
          ),
          const SizedBox(height: 18),
          Expanded(
            child: Card(
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: DataTable(
                  headingRowColor: WidgetStateProperty.all(
                    AppColors.background,
                  ),
                  columns: const [
                    DataColumn(label: Text('Documento')),
                    DataColumn(label: Text('Cliente')),
                    DataColumn(label: Text('Email')),
                    DataColumn(label: Text('Telefono')),
                    DataColumn(label: Text('Negocio')),
                    DataColumn(label: Text('Ingreso est.')),
                  ],
                  rows:
                      clientes.map((c) {
                        final nombre =
                            '${c['nombres'] ?? ''} ${c['apellidos'] ?? ''}'
                                .trim();
                        final documento =
                            '${c['tipo_documento'] ?? 'DNI'} ${c['numero_documento'] ?? '-'}';
                        return DataRow(
                          cells: [
                            DataCell(Text(documento)),
                            DataCell(Text(nombre.isEmpty ? '-' : nombre)),
                            DataCell(Text(c['email']?.toString() ?? '-')),
                            DataCell(Text(c['telefono']?.toString() ?? '-')),
                            DataCell(
                              Text(c['nombre_negocio']?.toString() ?? '-'),
                            ),
                            DataCell(
                              Text(
                                _money(
                                  (c['ingresos_estimados'] as num?)
                                          ?.toDouble() ??
                                      0,
                                ),
                              ),
                            ),
                          ],
                        );
                      }).toList(),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _SummaryChip extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;

  const _SummaryChip({
    required this.label,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Chip(
      avatar: Icon(icon, size: 18, color: AppColors.primary),
      label: Text('$label: $value'),
      backgroundColor: Colors.white,
      side: BorderSide(color: Colors.grey.shade200),
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
                  'No se pudieron cargar los clientes',
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
