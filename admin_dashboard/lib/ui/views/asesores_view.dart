import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/constants.dart';

class AsesoresView extends StatefulWidget {
  const AsesoresView({super.key});

  @override
  State<AsesoresView> createState() => _AsesoresViewState();
}

class _AsesoresViewState extends State<AsesoresView> {
  bool isLoading = true;
  String? error;
  List<Map<String, dynamic>> asesores = [];

  @override
  void initState() {
    super.initState();
    _loadAsesores();
  }

  Future<void> _loadAsesores() async {
    setState(() {
      isLoading = true;
      error = null;
    });

    try {
      final response = await Supabase.instance.client
          .from('asesores')
          .select(
            'id,codigo_empleado,nombres,apellidos,perfil,activo,created_at',
          )
          .order('codigo_empleado');
      if (!mounted) return;
      setState(() {
        asesores = List<Map<String, dynamic>>.from(response);
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
      return _ErrorState(message: error!, onRetry: _loadAsesores);
    }

    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              _SummaryChip(
                label: 'Asesores',
                value: '${asesores.length}',
                icon: Icons.work_rounded,
              ),
              const SizedBox(width: 12),
              _SummaryChip(
                label: 'Activos',
                value: '${asesores.where((a) => a['activo'] != false).length}',
                icon: Icons.verified_user_rounded,
              ),
              const Spacer(),
              IconButton.filledTonal(
                tooltip: 'Actualizar',
                onPressed: _loadAsesores,
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
                    DataColumn(label: Text('Login')),
                    DataColumn(label: Text('Nombres')),
                    DataColumn(label: Text('Apellidos')),
                    DataColumn(label: Text('Perfil')),
                    DataColumn(label: Text('Activo')),
                  ],
                  rows:
                      asesores.map((a) {
                        return DataRow(
                          cells: [
                            DataCell(
                              Text(a['codigo_empleado']?.toString() ?? '-'),
                            ),
                            DataCell(Text(a['nombres']?.toString() ?? '-')),
                            DataCell(Text(a['apellidos']?.toString() ?? '-')),
                            DataCell(Text(a['perfil']?.toString() ?? '-')),
                            DataCell(
                              Icon(
                                a['activo'] == false
                                    ? Icons.block_rounded
                                    : Icons.check_circle_rounded,
                                color:
                                    a['activo'] == false
                                        ? AppColors.error
                                        : AppColors.success,
                                size: 20,
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
                  'No se pudieron cargar los asesores',
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
