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
                    DataColumn(label: Text('Acciones')),
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
                            DataCell(
                              IconButton(
                                icon: const Icon(Icons.person_add_alt_1_rounded, color: AppColors.primary),
                                tooltip: 'Asignar Cliente',
                                onPressed: () => _mostrarAsignarCliente(a),
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

  Future<void> _mostrarAsignarCliente(Map<String, dynamic> asesor) async {
    List<Map<String, dynamic>> clientes = [];
    bool loadingClientes = true;
    String? errorClientes;
    String? selectedClienteId;
    String selectedTipo = 'NUEVA_SOLICITUD';
    String searchQuery = '';

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setStateDialog) {
            if (loadingClientes) {
              Supabase.instance.client
                  .from('clientes')
                  .select('id, nombres, apellidos, numero_documento')
                  .order('nombres')
                  .then((res) {
                setStateDialog(() {
                  clientes = List<Map<String, dynamic>>.from(res);
                  loadingClientes = false;
                });
              }).catchError((err) {
                setStateDialog(() {
                  errorClientes = err.toString();
                  loadingClientes = false;
                });
              });

              return const AlertDialog(
                title: Text('Cargando Clientes'),
                content: SizedBox(
                  height: 100,
                  child: Center(child: CircularProgressIndicator()),
                ),
              );
            }

            if (errorClientes != null) {
              return AlertDialog(
                title: const Text('Error'),
                content: Text(errorClientes!),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Cerrar'),
                  ),
                ],
              );
            }

            final filteredClientes = clientes.where((c) {
              final term = searchQuery.toLowerCase();
              final nom = '${c['nombres'] ?? ''} ${c['apellidos'] ?? ''}'.toLowerCase();
              final doc = (c['numero_documento'] ?? '').toString().toLowerCase();
              return nom.contains(term) || doc.contains(term);
            }).toList();

            return AlertDialog(
              title: Text('Asignar Cliente a ${asesor['nombres']}'),
              content: SizedBox(
                width: 500,
                height: 400,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    TextField(
                      decoration: const InputDecoration(
                        labelText: 'Buscar Cliente',
                        prefixIcon: Icon(Icons.search),
                      ),
                      onChanged: (val) {
                        setStateDialog(() {
                          searchQuery = val;
                        });
                      },
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<String>(
                      initialValue: selectedTipo,
                      decoration: const InputDecoration(labelText: 'Tipo de Gestión'),
                      items: const [
                        DropdownMenuItem(value: 'RENOVACION', child: Text('RENOVACION')),
                        DropdownMenuItem(value: 'AMPLIACION', child: Text('AMPLIACION')),
                        DropdownMenuItem(value: 'NUEVA_SOLICITUD', child: Text('NUEVA_SOLICITUD')),
                        DropdownMenuItem(value: 'SEGUIMIENTO', child: Text('SEGUIMIENTO')),
                        DropdownMenuItem(value: 'RECUPERACION_MORA', child: Text('RECUPERACION_MORA')),
                        DropdownMenuItem(value: 'DESERTOR', child: Text('DESERTOR')),
                      ],
                      onChanged: (val) {
                        setStateDialog(() {
                          selectedTipo = val!;
                        });
                      },
                    ),
                    const SizedBox(height: 12),
                    const Text('Selecciona un cliente:', style: TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 6),
                    Expanded(
                      child: Container(
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.grey.shade300),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: ListView.builder(
                          itemCount: filteredClientes.length,
                          itemBuilder: (context, index) {
                            final c = filteredClientes[index];
                            final isSelected = c['id'] == selectedClienteId;
                            return ListTile(
                              selected: isSelected,
                              selectedColor: Colors.white,
                              selectedTileColor: AppColors.primary,
                              title: Text('${c['nombres']} ${c['apellidos']}'),
                              subtitle: Text('DNI: ${c['numero_documento'] ?? '-'}'),
                              onTap: () {
                                setStateDialog(() {
                                  selectedClienteId = c['id'] as String;
                                });
                              },
                            );
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancelar'),
                ),
                FilledButton(
                  onPressed: selectedClienteId == null
                      ? null
                      : () async {
                          final messenger = ScaffoldMessenger.of(context);
                          Navigator.pop(context);
                          try {
                            await Supabase.instance.client.rpc('bbva_asignar_cartera', params: {
                              'p_asesor_id': asesor['id'],
                              'p_cliente_id': selectedClienteId,
                              'p_tipo_gestion': selectedTipo,
                            });
                            messenger.showSnackBar(
                              const SnackBar(content: Text('Cliente asignado correctamente')),
                            );
                          } on PostgrestException catch (e) {
                            messenger.showSnackBar(
                              SnackBar(content: Text(e.message)),
                            );
                          } catch (e) {
                            messenger.showSnackBar(
                              SnackBar(content: Text('Error al asignar: $e')),
                            );
                          }
                        },
                  child: const Text('Asignar'),
                ),
              ],
            );
          },
        );
      },
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
