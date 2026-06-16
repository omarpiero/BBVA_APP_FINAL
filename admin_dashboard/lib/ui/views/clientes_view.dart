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
  List<Map<String, dynamic>> clientes = [];

  @override
  void initState() {
    super.initState();
    _loadClientes();
  }

  Future<void> _loadClientes() async {
    try {
      final supabase = Supabase.instance.client;
      final response = await supabase.from('clientes').select();
      setState(() {
        clientes = List<Map<String, dynamic>>.from(response);
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    return Padding(
      padding: const EdgeInsets.all(32.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Gestión de Clientes',
            style: TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 24),
          Expanded(
            child: Card(
              color: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: Colors.grey.shade200),
              ),
              child: SingleChildScrollView(
                scrollDirection: Axis.vertical,
                child: SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: DataTable(
                    headingRowColor: WidgetStateProperty.all(AppColors.background),
                    columns: const [
                      DataColumn(label: Text('Doc.', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Número', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Nombres', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Apellidos', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Email', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Celular', style: TextStyle(fontWeight: FontWeight.bold))),
                    ],
                    rows: clientes.map((c) {
                      return DataRow(cells: [
                        DataCell(Text(c['tipo_documento']?.toString() ?? '-')),
                        DataCell(Text(c['numero_documento']?.toString() ?? '-')),
                        DataCell(Text(c['nombres']?.toString() ?? '-')),
                        DataCell(Text(c['apellidos']?.toString() ?? '-')),
                        DataCell(Text(c['email']?.toString() ?? '-')),
                        DataCell(Text(c['celular']?.toString() ?? '-')),
                      ]);
                    }).toList(),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
