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
  List<Map<String, dynamic>> asesores = [];

  @override
  void initState() {
    super.initState();
    _loadAsesores();
  }

  Future<void> _loadAsesores() async {
    try {
      final supabase = Supabase.instance.client;
      final response = await supabase.from('asesores').select();
      setState(() {
        asesores = List<Map<String, dynamic>>.from(response);
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
            'Gestión de Asesores',
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
                      DataColumn(label: Text('Código', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Nombres', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Apellidos', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Perfil', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Email', style: TextStyle(fontWeight: FontWeight.bold))),
                      DataColumn(label: Text('Celular', style: TextStyle(fontWeight: FontWeight.bold))),
                    ],
                    rows: asesores.map((a) {
                      return DataRow(cells: [
                        DataCell(Text(a['codigo_empleado']?.toString() ?? '-')),
                        DataCell(Text(a['nombres']?.toString() ?? '-')),
                        DataCell(Text(a['apellidos']?.toString() ?? '-')),
                        DataCell(Text(a['perfil']?.toString() ?? '-')),
                        DataCell(Text(a['email']?.toString() ?? '-')),
                        DataCell(Text(a['celular']?.toString() ?? '-')),
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
